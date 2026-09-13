"""Tests for RecursiveAnalyzer.

Tests indicator warmup convergence and checks that historical values remain invariant
when new future bars are appended.
"""

import pytest
import numpy as np
import pandas as pd

from app.strategy_validation.recursive_analyzer import RecursiveAnalyzer


@pytest.fixture
def analyzer():
    return RecursiveAnalyzer(tolerance_epsilon=1e-5)


@pytest.fixture
def sample_price_series():
    np.random.seed(123)
    n = 300
    prices = 100.0 + np.cumsum(np.random.randn(n) * 0.4)
    dates = pd.date_range("2023-01-01", periods=n, freq="D")
    return pd.DataFrame({"close": prices}, index=dates)


def test_ema_warmup_convergence(analyzer, sample_price_series):
    """Verifies that an EMA indicator converges as warmup length increases."""
    def ema_20(df: pd.DataFrame) -> pd.Series:
        return df["close"].ewm(span=20, adjust=False).mean()

    report = analyzer.analyze_warmup_convergence(
        indicator_fn=ema_20,
        df=sample_price_series,
        indicator_name="EMA_20",
        test_warmup_bars=[20, 50, 100, 150, 200]
    )

    assert report.is_future_invariant is True
    assert report.recommended_min_warmup_bars >= 50
    # Error should decrease monotonically as warmup increases
    errors = [m.max_absolute_error for m in report.warmup_convergence]
    assert errors[-1] < errors[0]


def test_sma_instant_warmup(analyzer, sample_price_series):
    """A Simple Moving Average converges immediately once length reaches window period."""
    def sma_10(df: pd.DataFrame) -> pd.Series:
        return df["close"].rolling(10).mean()

    report = analyzer.analyze_warmup_convergence(
        indicator_fn=sma_10,
        df=sample_price_series,
        indicator_name="SMA_10",
        test_warmup_bars=[10, 20, 50]
    )

    assert report.is_stable is True
    assert report.is_future_invariant is True
    assert report.recommended_min_warmup_bars <= 20
