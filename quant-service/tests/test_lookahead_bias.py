"""Mandatory Look-Ahead Bias and Future Information Leakage Tests.

CRITICAL REQUIREMENT VERIFICATION:
Demonstrates that feature values calculated for any historical timestamp T
do NOT change when subsequent future market observations after T are added to the dataset.
"""

import numpy as np
import pandas as pd
import pytest

from app.warehouse.feature_store import PointInTimeFeatureStore
from app.warehouse.dataset_splitter import ChronologicalDatasetSplitter


@pytest.fixture
def sample_price_history():
    """Generates 100 days of deterministic historical OHLCV data."""
    dates = pd.date_range(start="2024-01-01", periods=100, freq="B").strftime("%Y-%m-%d").tolist()
    np.random.seed(42)

    base = 1000.0
    prices = [base]
    for _ in range(99):
        ret = np.random.normal(0.0005, 0.015)
        prices.append(prices[-1] * (1 + ret))

    prices = np.array(prices)
    highs = prices * 1.01
    lows = prices * 0.99
    opens = prices * 1.002
    closes = prices
    volumes = np.random.randint(100_000, 1_000_000, size=100)

    return pd.DataFrame({
        'date': dates,
        'open': opens,
        'high': highs,
        'low': lows,
        'close': closes,
        'volume': volumes
    })


def test_adding_future_data_does_not_change_past_features(sample_price_history):
    """MANDATORY TEST:

    Compute features up to historical cutoff date T (e.g. Day 50).
    Then append 50 additional future trading days after T.
    Verify that ALL features at timestamp T remain 100% IDENTICAL.
    """
    cutoff_date = sample_price_history.iloc[50]['date']

    # 1. Compute features using ONLY data available up to cutoff date T
    past_df = sample_price_history[sample_price_history['date'] <= cutoff_date].copy()
    features_past = PointInTimeFeatureStore.calculate_features(past_df)
    row_past = features_past[features_past['date'] == cutoff_date].iloc[0]

    # 2. Compute features on the FULL dataset (including 50 future days after T)
    features_full = PointInTimeFeatureStore.calculate_features(sample_price_history.copy())
    row_full = features_full[features_full['date'] == cutoff_date].iloc[0]

    # 3. Assert all quantitative features at T are 100% identical
    feature_cols = ['sma_5', 'sma_20', 'sma_50', 'ema_20', 'return_1d', 'return_5d',
                    'return_20d', 'volatility_20', 'momentum_10', 'rsi_14', 'volume_sma_20']

    for col in feature_cols:
        val_past = row_past[col]
        val_full = row_full[col]
        assert np.isclose(val_past, val_full, equal_nan=True), (
            f"LOOK-AHEAD BIAS DETECTED in feature '{col}' at date {cutoff_date}! "
            f"Past value = {val_past}, Full value = {val_full}"
        )


def test_get_features_as_of_point_in_time(sample_price_history):
    """Verifies that get_features_as_of returns exact historical state."""
    cutoff_date = sample_price_history.iloc[40]['date']

    features = PointInTimeFeatureStore.get_features_as_of(sample_price_history, cutoff_date)

    assert features is not None
    assert features['date'] == cutoff_date
    assert 'sma_20' in features
    assert 'rsi_14' in features


def test_scaler_fitting_isolated_to_training_partition(sample_price_history):
    """Verifies feature scalers fit ONLY on training data without test distribution leakage."""
    features_df = PointInTimeFeatureStore.calculate_features(sample_price_history).dropna()

    train, val, test = ChronologicalDatasetSplitter.train_val_test_split(features_df, 0.7, 0.15, 0.15)

    feature_cols = ['sma_5', 'sma_20', 'rsi_14', 'volatility_20']
    train_scaled, val_scaled, test_scaled, scaler = ChronologicalDatasetSplitter.fit_transform_scaler_safe(
        train, val, test, feature_cols
    )

    # Scaler mean must equal train partition mean, not entire dataset mean
    for i, col in enumerate(feature_cols):
        assert np.isclose(scaler.mean_[i], train[col].mean())
