"""Tests for Part 12 Walk-Forward Engine and Leakage Prevention.

Verifies:
1. Target Timing & Forward Label Alignment (Zero same-day leakage)
2. Point-in-Time Dataset Invariance with Future Append
3. Strict Preprocessing Isolation (Fit on train only)
4. Purging and Embargo Boundary Protection
5. End-to-End Walk-Forward Execution (Expanding & Rolling)
"""

import pytest
import numpy as np
import pandas as pd
from datetime import datetime, timedelta

from app.ml.data.target_builder import TargetBuilder
from app.ml.data.dataset_builder import QuantDatasetBuilder
from app.ml.validation.preprocessor import LeakageSafePreprocessor
from app.ml.validation.purging import PurgeAndEmbargo
from app.ml.walk_forward.walk_forward_engine import WalkForwardEngine, FoldConfig


def generate_sample_price_series(n_days=500):
    np.random.seed(42)
    dates = pd.date_range(start="2021-01-01", periods=n_days, freq="B")
    returns = np.random.normal(0.0005, 0.012, size=n_days)
    price = 20000.0 * np.cumprod(1 + returns)

    df = pd.DataFrame({
        'date': [d.strftime('%Y-%m-%d') for d in dates],
        'symbol': 'NIFTY 50',
        'open': price * 0.998,
        'high': price * 1.005,
        'low': price * 0.995,
        'close': price,
        'adj_close': price,
        'volume': np.random.randint(100000, 500000, size=n_days)
    })
    return df


def test_target_timing_strict_forward():
    """Verifies that future_return_1d is strictly (Price[t+1] / Price[t]) - 1."""
    df = generate_sample_price_series(50)
    target_df = TargetBuilder.calculate_targets(df)

    for i in range(len(target_df) - 1):
        p_curr = target_df.loc[i, 'adj_close']
        p_next = target_df.loc[i+1, 'adj_close']
        expected_ret = (p_next / p_curr) - 1.0
        actual_ret = target_df.loc[i, 'future_return_1d']
        assert pytest.approx(expected_ret, abs=1e-6) == actual_ret

    # Last row target must be NaN
    assert np.isnan(target_df.iloc[-1]['future_return_1d'])


def test_dataset_builder_point_in_time_safety():
    """Verifies that adding future observations does not mutate historical feature matrix."""
    full_df = generate_sample_price_series(250)
    cutoff_date = full_df.iloc[180]['date']

    # 1. Build dataset with cutoff filter
    X_cut, _, meta_cut = QuantDatasetBuilder.build_dataset(full_df, as_of_date=cutoff_date)

    # 2. Build dataset on truncated dataframe up to cutoff
    df_trunc = full_df[full_df['date'] <= cutoff_date]
    X_trunc, _, meta_trunc = QuantDatasetBuilder.build_dataset(df_trunc)

    # Row count and all features at cutoff date must be strictly identical
    assert len(X_cut) == len(X_trunc)
    np.testing.assert_allclose(X_cut.values, X_trunc.values, rtol=1e-5, atol=1e-5)


def test_preprocessor_training_isolation():
    """Verifies that scalers and winsorizers are fitted ONLY on training data."""
    X_train = pd.DataFrame({'f1': [1.0, 2.0, 3.0, 4.0, 5.0]})
    X_test = pd.DataFrame({'f1': [100.0, 200.0]})  # Extreme out-of-distribution values

    preproc = LeakageSafePreprocessor(scaler_type="STANDARD")
    preproc.fit(X_train)

    # Transform test without refitting
    X_test_trans = preproc.transform(X_test)

    # Scaler mean must be 3.0 (from train), NOT influenced by 100/200
    assert preproc.scaler.mean_[0] == 3.0


def test_purge_and_embargo_overlap_removal():
    """Verifies that training samples overlapping the test horizon are purged."""
    dates = pd.date_range(start="2024-01-01", periods=20, freq="D")
    df = pd.DataFrame({'date': [d.strftime('%Y-%m-%d') for d in dates], 'val': range(20)})

    test_start = "2024-01-15"
    # Purge 5 days before test_start (i.e. dates >= 2024-01-10 are purged)
    purged = PurgeAndEmbargo.purge_train_overlap(df, test_start, target_horizon_days=5)

    max_purged_date = pd.to_datetime(purged['date'].max())
    assert max_purged_date < pd.to_datetime(test_start) - pd.Timedelta(days=4)


def test_walk_forward_execution_expanding():
    """Verifies end-to-end walk forward execution over multiple folds."""
    df = generate_sample_price_series(300)
    X, y, meta = QuantDatasetBuilder.build_dataset(df)

    dates = meta["dates"]
    assert len(X) == len(dates)

    # Define 2 test folds
    folds = [
        FoldConfig(
            fold_number=1,
            train_start=dates[0],
            train_end=dates[120],
            val_start=dates[100],
            val_end=dates[120],
            test_start=dates[125],
            test_end=dates[180]
        ),
        FoldConfig(
            fold_number=2,
            train_start=dates[0],
            train_end=dates[180],
            val_start=dates[160],
            val_end=dates[180],
            test_start=dates[185],
            test_end=dates[240]
        )
    ]

    wf_engine = WalkForwardEngine(mode="EXPANDING", model_type="REGRESSION")
    results = wf_engine.run_walk_forward(X, y, dates, folds)

    assert results["status"] == "SUCCESS"
    assert results["total_folds"] == 2
    assert "mean_ic" in results
    assert len(results["fold_results"]) == 2
    assert "champion_algorithm" in results["fold_results"][0]
    assert "metrics" in results["fold_results"][0]
