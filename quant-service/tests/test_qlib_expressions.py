"""Unit & Adversarial Tests for Qlib Expression Engine & PIT Safeguards."""

import pytest
import numpy as np
import pandas as pd

from app.qlib.expressions import (
    QlibExpressionEvaluator,
    LookaheadBiasError,
    ts_ref,
    ts_delta,
    ts_mean,
    ts_std,
    ts_var,
    ts_max,
    ts_min,
    ts_sum,
    ts_slope,
    ts_rsquare,
    ts_resi,
    ts_corr,
    ts_rank,
    cs_rank,
    cs_zscore,
)


@pytest.fixture
def sample_panel_data():
    dates = pd.date_range("2023-01-01", periods=10, freq="D")
    instruments = ["RELIANCE", "TCS"]
    
    records = []
    for inst in instruments:
        for i, dt in enumerate(dates):
            base = 100.0 if inst == "RELIANCE" else 200.0
            records.append({
                "datetime": dt,
                "instrument": inst,
                "open": base + i * 2.0,
                "high": base + i * 2.0 + 3.0,
                "low": base + i * 2.0 - 1.0,
                "close": base + i * 2.0 + 1.0,
                "volume": 1000.0 * (i + 1),
            })
    
    df = pd.DataFrame(records).set_index(["datetime", "instrument"])
    return df


def test_ts_ref_and_lookahead_bias():
    s = pd.Series([10.0, 12.0, 15.0, 14.0])
    
    # Valid historical lag
    lag1 = ts_ref(s, 1, allow_future=False)
    assert np.isnan(lag1.iloc[0])
    assert lag1.iloc[1] == 10.0
    assert lag1.iloc[2] == 12.0

    # Negative shift without allow_future must raise LookaheadBiasError
    with pytest.raises(LookaheadBiasError):
        ts_ref(s, -1, allow_future=False)

    # Negative shift with allow_future is allowed for targets
    lead1 = ts_ref(s, -1, allow_future=True)
    assert lead1.iloc[0] == 12.0
    assert lead1.iloc[1] == 15.0
    assert np.isnan(lead1.iloc[-1])


def test_expression_evaluator_basic_math(sample_panel_data):
    evaluator = QlibExpressionEvaluator(allow_future=False)
    
    # $close - $open
    res = evaluator.evaluate("$close - $open", sample_panel_data)
    assert isinstance(res, pd.Series)
    assert np.allclose(res.values, 1.0)

    # Mean($close, 3)
    mean_res = evaluator.evaluate("Mean($close, 3)", sample_panel_data)
    assert len(mean_res) == len(sample_panel_data)


def test_expression_evaluator_adversarial_lookahead(sample_panel_data):
    evaluator = QlibExpressionEvaluator(allow_future=False)
    
    # Attempting to query future close in feature mode
    with pytest.raises(LookaheadBiasError):
        evaluator.evaluate("Ref($close, -1)", sample_panel_data)

    with pytest.raises(LookaheadBiasError):
        evaluator.evaluate("Delta($close, -2)", sample_panel_data)


def test_cross_sectional_operators(sample_panel_data):
    # Cross-sectional rank
    ranks = cs_rank(sample_panel_data["close"], level="datetime")
    # RELIANCE has lower price than TCS at all dates, so RELIANCE rank should be 0.5, TCS 1.0
    dt_first = sample_panel_data.index.get_level_values(0)[0]
    first_slice = ranks.xs(dt_first, level=0)
    assert first_slice.loc["RELIANCE"] == 0.5
    assert first_slice.loc["TCS"] == 1.0

    # Cross-sectional zscore
    zscores = cs_zscore(sample_panel_data["close"], level="datetime")
    first_z = zscores.xs(dt_first, level=0)
    assert first_z.loc["RELIANCE"] < 0
    assert first_z.loc["TCS"] > 0
