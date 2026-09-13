"""Tests for Alpha158 and Alpha360 Factor Engines and PIT Integrity."""

import pytest
import numpy as np
import pandas as pd

from app.qlib.alpha158 import Alpha158Builder, Alpha360Builder


@pytest.fixture
def multi_asset_panel():
    dates = pd.date_range("2023-01-01", periods=100, freq="D")
    instruments = ["RELIANCE", "TCS", "INFY"]
    records = []
    
    rng = np.random.RandomState(42)
    for inst in instruments:
        base = 1000.0
        for dt in dates:
            r = rng.normal(0.001, 0.02)
            base = base * (1.0 + r)
            open_p = base * (1.0 + rng.uniform(-0.01, 0.01))
            high_p = max(base, open_p) * (1.0 + rng.uniform(0.001, 0.02))
            low_p = min(base, open_p) * (1.0 - rng.uniform(0.001, 0.02))
            close_p = base
            vol = rng.uniform(10000, 50000)
            
            records.append({
                "datetime": dt,
                "instrument": inst,
                "open": open_p,
                "high": high_p,
                "low": low_p,
                "close": close_p,
                "volume": vol,
            })
            
    df = pd.DataFrame(records).set_index(["datetime", "instrument"])
    return df


def test_alpha158_builder_structure_and_coverage(multi_asset_panel):
    builder = Alpha158Builder(windows=[5, 10, 20, 30, 60])
    features = builder.build_features(multi_asset_panel)

    assert isinstance(features, pd.DataFrame)
    assert len(features) == len(multi_asset_panel)
    assert "KLEN" in features.columns
    assert "KMID" in features.columns
    assert "MA5" in features.columns
    assert "MA60" in features.columns
    assert "VMA20" in features.columns
    assert "WVMA10" in features.columns
    assert "CORR5" in features.columns
    assert "SUMD30" in features.columns

    # Verify no unhandled NaNs in computed features
    assert not features.isna().any().any()


def test_alpha360_builder(multi_asset_panel):
    builder = Alpha360Builder()
    features = builder.build_features(multi_asset_panel)

    assert isinstance(features, pd.DataFrame)
    assert len(features) == len(multi_asset_panel)
    assert features.shape[1] == 360
    assert "CLOSE_0" in features.columns
    assert "CLOSE_59" in features.columns
    assert "VOLUME_59" in features.columns


def test_alpha158_adversarial_pit_invariance(multi_asset_panel):
    """Verifies that altering future data points has ZERO effect on historical factor values."""
    builder = Alpha158Builder()
    feat_orig = builder.build_features(multi_asset_panel)

    # Modify future prices at date index 80..99
    panel_modified = multi_asset_panel.copy()
    dates = multi_asset_panel.index.get_level_values("datetime").unique()
    future_dates = dates[80:]

    panel_modified.loc[panel_modified.index.get_level_values("datetime").isin(future_dates), "close"] *= 5.0
    panel_modified.loc[panel_modified.index.get_level_values("datetime").isin(future_dates), "high"] *= 10.0

    feat_modified = builder.build_features(panel_modified)

    # Historical factor values before date 80 must be EXACTLY IDENTICAL
    hist_dates = dates[:80]
    orig_hist = feat_orig.loc[feat_orig.index.get_level_values("datetime").isin(hist_dates)]
    mod_hist = feat_modified.loc[feat_modified.index.get_level_values("datetime").isin(hist_dates)]

    pd.testing.assert_frame_equal(orig_hist, mod_hist)
