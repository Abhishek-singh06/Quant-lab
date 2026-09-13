"""Comprehensive Look-Ahead Bias & Point-in-Time Safety Tests for Technical Feature Engine.

Validates 7 critical invariants:
1. Future Price Invariance: Modifying future candles has ZERO effect on historical feature values at T.
2. Temporal Expansion Stability: Appending new future bars does not alter past feature vector at T.
3. 52-Week Range Look-Ahead Protection: Future highs/lows do not leak into past 52-week position.
4. Realized Volatility Invariance: Rolling volatility uses strictly backward-looking return windows.
5. Benchmark Alignment Leakage Protection: Future benchmark returns are never accessible at T.
6. Rolling Filter & MACD Invariance: Moving averages, EMAs, and MACD strictly depend on history <= T.
7. Graceful Degradation: Insufficient history generates NaN/None rather than fabricated values.
"""

import pytest
import numpy as np
import pandas as pd
from app.warehouse.technical_feature_engine import TechnicalFeatureEngine


@pytest.fixture
def sample_price_series():
    """Generates a 100-day deterministic OHLCV price series."""
    dates = pd.date_range(start="2025-01-01", periods=100, freq="B")
    np.random.seed(42)

    base = 1000.0
    prices = []
    for i in range(100):
        drift = 0.0005
        shock = 0.01 * np.sin(i * 0.1)
        base = base * (1.0 + drift + shock)
        h = base * 1.01
        l = base * 0.99
        c = base
        v = 1000000 + int(50000 * np.sin(i * 0.2))
        prices.append({
            'date': dates[i].strftime('%Y-%m-%d'),
            'open': round(base * 0.999, 2),
            'high': round(h, 2),
            'low': round(l, 2),
            'close': round(c, 2),
            'volume': v
        })
    return pd.DataFrame(prices)


@pytest.fixture
def sample_benchmark_series():
    """Generates a 100-day benchmark (e.g. NIFTY 50) series."""
    dates = pd.date_range(start="2025-01-01", periods=100, freq="B")
    prices = []
    base = 24000.0
    for i in range(100):
        base = base * (1.0 + 0.0003 + 0.005 * np.cos(i * 0.1))
        prices.append({
            'date': dates[i].strftime('%Y-%m-%d'),
            'open': round(base, 2),
            'high': round(base * 1.005, 2),
            'low': round(base * 0.995, 2),
            'close': round(base, 2),
            'volume': 5000000
        })
    return pd.DataFrame(prices)


def test_1_future_price_modification_invariance(sample_price_series):
    """Test 1: Modify future prices on Day 51..100 drastically. Day 50 feature values MUST remain identical."""
    df_original = sample_price_series.copy()
    cutoff_date = df_original.iloc[49]['date']

    features_orig = TechnicalFeatureEngine.get_features_as_of(df_original, as_of_date=cutoff_date)

    # Modify all future prices drastically (10x spike)
    df_modified = sample_price_series.copy()
    df_modified.loc[50:, 'close'] = df_modified.loc[50:, 'close'] * 10.0
    df_modified.loc[50:, 'high'] = df_modified.loc[50:, 'high'] * 10.0

    features_after_mod = TechnicalFeatureEngine.get_features_as_of(df_modified, as_of_date=cutoff_date)

    assert features_orig['sma_20'] == features_after_mod['sma_20']
    assert features_orig['rsi_14'] == features_after_mod['rsi_14']
    assert features_orig['volatility_20d'] == features_after_mod['volatility_20d']
    assert features_orig['macd_histogram_12_26_9'] == features_after_mod['macd_histogram_12_26_9']


def test_2_temporal_expansion_stability(sample_price_series):
    """Test 2: Appending future days does not alter past features computed as of Day 30."""
    cutoff_date = sample_price_series.iloc[29]['date']

    # Sub-dataframe with only first 30 days
    df_short = sample_price_series.iloc[:30].copy()
    features_short = TechnicalFeatureEngine.get_features_as_of(df_short, as_of_date=cutoff_date)

    # Full dataframe with 100 days
    features_full = TechnicalFeatureEngine.get_features_as_of(sample_price_series, as_of_date=cutoff_date)

    assert features_short['sma_10'] == features_full['sma_10']
    assert features_short['return_5d'] == features_full['return_5d']
    assert features_short['drawdown'] == features_full['drawdown']


def test_3_week_52_position_no_future_leakage(sample_price_series):
    """Test 3: Future all-time high on Day 80 must NOT inflate 52-week high on Day 40."""
    df = sample_price_series.copy()
    cutoff_date = df.iloc[39]['date']

    # Introduce extreme price spike on day 75
    df.loc[75, 'high'] = 99999.0
    df.loc[75, 'close'] = 99999.0

    features = TechnicalFeatureEngine.get_features_as_of(df, as_of_date=cutoff_date)

    # 52-week high on Day 40 must NOT see 99999.0
    assert features['week_52_high'] < 5000.0
    assert 0.0 <= features['week_52_position'] <= 1.0


def test_4_realized_volatility_no_lookahead(sample_price_series):
    """Test 4: High future volatility on days 60..70 must not leak into 20-day volatility on Day 50."""
    df = sample_price_series.copy()
    cutoff_date = df.iloc[49]['date']

    features_calm = TechnicalFeatureEngine.get_features_as_of(df, as_of_date=cutoff_date)

    # Add extreme volatility in future
    df.loc[55:65, 'close'] = df.loc[55:65, 'close'] * np.random.uniform(0.5, 2.0, size=11)

    features_calm_after = TechnicalFeatureEngine.get_features_as_of(df, as_of_date=cutoff_date)
    assert features_calm['volatility_20d'] == features_calm_after['volatility_20d']


def test_5_relative_strength_benchmark_no_leakage(sample_price_series, sample_benchmark_series):
    """Test 5: Future benchmark rally does not affect historical relative strength as of Day 40."""
    cutoff_date = sample_price_series.iloc[39]['date']

    rs_orig = TechnicalFeatureEngine.get_features_as_of(
        sample_price_series, as_of_date=cutoff_date, benchmark_df=sample_benchmark_series
    )

    # Rally benchmark 10x on Day 60
    bench_mod = sample_benchmark_series.copy()
    bench_mod.loc[60:, 'close'] = bench_mod.loc[60:, 'close'] * 10.0

    rs_after = TechnicalFeatureEngine.get_features_as_of(
        sample_price_series, as_of_date=cutoff_date, benchmark_df=bench_mod
    )

    assert rs_orig['rs_nifty_20'] == rs_after['rs_nifty_20']


def test_6_rolling_filters_and_macd_no_future_candles(sample_price_series):
    """Test 6: MACD and SMAs at Day 40 depend strictly on bars up to Day 40."""
    cutoff_date = sample_price_series.iloc[39]['date']

    features_1 = TechnicalFeatureEngine.get_features_as_of(sample_price_series, as_of_date=cutoff_date)

    # Invert future data entirely
    inverted = sample_price_series.copy()
    inverted.loc[40:, 'close'] = 1.0 / inverted.loc[40:, 'close']

    features_2 = TechnicalFeatureEngine.get_features_as_of(inverted, as_of_date=cutoff_date)

    assert features_1['macd_line_12_26'] == features_2['macd_line_12_26']
    assert features_1['sma_20'] == features_2['sma_20']
    assert features_1['ema_20'] == features_2['ema_20']
    assert np.isnan(features_1['sma_50']) and np.isnan(features_2['sma_50'])


def test_7_insufficient_history_graceful_handling():
    """Test 7: Providing only 5 days of history returns NaN for 50-day SMA and None for empty df."""
    short_dates = pd.date_range(start="2025-01-01", periods=5, freq="B")
    short_df = pd.DataFrame({
        'date': [d.strftime('%Y-%m-%d') for d in short_dates],
        'open': [100, 101, 102, 103, 104],
        'high': [101, 102, 103, 104, 105],
        'low': [99, 100, 101, 102, 103],
        'close': [100, 101, 102, 103, 104],
        'volume': [1000, 1000, 1000, 1000, 1000]
    })

    features = TechnicalFeatureEngine.get_features_as_of(short_df, as_of_date="2025-01-07")
    assert np.isnan(features['sma_50'])
    assert np.isnan(features['volatility_20d'])
    assert features['return_1d'] is not None

    empty_features = TechnicalFeatureEngine.get_features_as_of(pd.DataFrame(), as_of_date="2025-01-07")
    assert empty_features is None
