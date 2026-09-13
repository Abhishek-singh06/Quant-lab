"""Tests for LookaheadAnalyzer.

Adversarially tests detection of negative shifts, future rolling windows, centered windows,
future join leaks, and verifies that genuine causal features pass cleanly.
"""

import pytest
import numpy as np
import pandas as pd
from datetime import datetime, timezone, timedelta

from app.strategy_validation.lookahead_analyzer import LookaheadAnalyzer
from app.strategy_validation.models import LookaheadType, ValidationSeverity


@pytest.fixture
def analyzer():
    return LookaheadAnalyzer(tolerance=1e-7)


@pytest.fixture
def sample_ohlcv_df():
    np.random.seed(42)
    n = 100
    prices = 100.0 + np.cumsum(np.random.randn(n) * 0.5)
    dates = pd.date_range("2023-01-01", periods=n, freq="D")
    return pd.DataFrame({
        "open": prices + np.random.randn(n) * 0.1,
        "high": prices + 1.0,
        "low": prices - 1.0,
        "close": prices,
        "volume": np.random.randint(1000, 5000, size=n),
    }, index=dates)


def test_causal_feature_passes_clean(analyzer, sample_ohlcv_df):
    """Causal features (past returns, rolling min/max, lag) must be clean."""
    def compute_causal(df: pd.DataFrame) -> pd.DataFrame:
        out = pd.DataFrame(index=df.index)
        out["return_1d"] = df["close"].pct_change()
        out["rolling_max_10"] = df["close"].rolling(10).max()
        out["rolling_mean_5"] = df["close"].rolling(5).mean()
        return out

    report = analyzer.analyze_transformation_causality(compute_causal, sample_ohlcv_df, test_bars=20)
    assert report.is_clean is True
    assert report.has_critical_lookahead is False
    assert len(report.findings) == 0


def test_negative_shift_lookahead_detected(analyzer, sample_ohlcv_df):
    """Features using negative shift (future close) must be flagged with CRITICAL lookahead."""
    def compute_leaky_shift(df: pd.DataFrame) -> pd.DataFrame:
        out = pd.DataFrame(index=df.index)
        out["future_return_1d"] = df["close"].shift(-1) / df["close"] - 1.0
        return out

    report = analyzer.analyze_transformation_causality(compute_leaky_shift, sample_ohlcv_df, test_bars=20)
    assert report.is_clean is False
    assert report.has_critical_lookahead is True
    assert any(f.lookahead_type == LookaheadType.FUTURE_DATA_MODIFICATION for f in report.findings)


def test_centered_rolling_window_detected(analyzer, sample_ohlcv_df):
    """Centered rolling window (center=True) uses future bars and must be caught."""
    def compute_centered(df: pd.DataFrame) -> pd.DataFrame:
        out = pd.DataFrame(index=df.index)
        out["centered_ma"] = df["close"].rolling(5, center=True).mean()
        return out

    report = analyzer.analyze_transformation_causality(compute_centered, sample_ohlcv_df, test_bars=20)
    assert report.is_clean is False
    assert report.has_critical_lookahead is True


def test_future_join_leakage_detected(analyzer):
    """Detects when an external disclosure timestamp postdates the trade bar timestamp."""
    now = datetime(2023, 6, 1, 10, 0, 0, tzinfo=timezone.utc)
    base_df = pd.DataFrame({
        "bar_time": [now, now + timedelta(days=1), now + timedelta(days=2)],
        # Leak: at day 0, news published on day 2 is joined!
        "news_published_at": [now + timedelta(days=2), now + timedelta(days=1), now + timedelta(days=2)]
    })

    findings = analyzer.scan_for_future_joins(base_df, "bar_time", "news_published_at")
    assert len(findings) > 0
    assert findings[0].lookahead_type == LookaheadType.FUTURE_JOIN_LEAKAGE
    assert findings[0].severity == ValidationSeverity.CRITICAL
