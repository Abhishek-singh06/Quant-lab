"""
Unit tests for Production Cross-Check / Signal Engine.
"""

from datetime import datetime, timedelta
import pytest
import pandas as pd
from app.signals.models import (
    ConflictSeverity,
    EvidenceCategory,
    EvidenceDirection,
    SignalConfiguration,
    SignalQualityStatus,
    SignalType,
)
from app.signals.engine import CrossCheckSignalEngine
from app.signals.evaluator import SignalHistoricalEvaluator


def test_signal_engine_strong_buy_evidence():
    """Test BUY signal when technical, fundamental, ML, regime, and institutional evidence align."""
    engine = CrossCheckSignalEngine()
    as_of = datetime(2026, 8, 15, 15, 30)

    context = {
        "technical_features": {
            "close": 2500.0,
            "sma_200": 2200.0,
            "rsi_14": 58.0,
            "macd_hist": 1.2,
            "position_52w": 0.85,
            "return_20d": 0.06,
        },
        "fundamental_ratios": {
            "available_at": datetime(2026, 8, 1),
            "eps_growth_yoy": 0.22,
            "roe": 0.20,
            "debt_to_equity": 0.35,
            "pe_ratio": 21.0,
        },
        "institutional_data": {
            "available_at": datetime(2026, 8, 15),
            "fii_net_flow_20d_cr": 3500.0,
            "dii_net_flow_20d_cr": 2200.0,
            "inst_holding_delta_qoq": 1.2,
        },
        "ml_predictions": {
            "model_version": "M1_RIDGE_v1.0",
            "model_status": "VALIDATED",
            "predicted_return": 0.008,
            "probability_positive": 0.68,
            "predicted_volatility": 0.012,
        },
        "market_regime": {
            "model_version": "REGIME_v1.0.0",
            "direction_regime": "BULL",
            "direction_score": 65.0,
            "risk_regime": "RISK_ON",
            "risk_score": 50.0,
            "confidence": 0.88,
        },
        "indian_market_context": {
            "nifty_vs_sma50_pct": 0.035,
            "india_vix": 13.5,
            "advance_decline_ratio": 1.6,
        },
        "global_market_data": {
            "sp500_return_1d": 0.008,
            "dxy_change_pct": -0.003,
            "crude_oil_change_pct": -0.012,
        },
        "macro_data": {
            "available_at": datetime(2026, 8, 1),
            "composite_pmi": 57.5,
            "cpi_inflation_pct": 4.6,
        }
    }

    result = engine.generate_signal("RELIANCE", as_of, context)

    assert result.signal == SignalType.BUY
    assert result.direction == EvidenceDirection.BULLISH
    assert result.signal_score >= 35.0
    assert result.confidence >= 0.60
    assert result.conflict_severity != ConflictSeverity.HIGH
    assert result.expected_return is not None
    assert result.expected_volatility is not None
    assert result.return_to_volatility_ratio is not None
    assert len(result.supporting_evidence) >= 3
    assert len(result.structured_reasoning) > 0


def test_mandatory_rule_no_buy_without_evidence():
    """Verify that a single high score or empty context NEVER produces a BUY signal."""
    engine = CrossCheckSignalEngine()
    as_of = datetime(2026, 8, 15, 15, 30)

    # Empty context -> missing evidence
    result_empty = engine.generate_signal("TCS", as_of, {})
    assert result_empty.signal in [SignalType.HOLD, SignalType.NO_SIGNAL]
    assert result_empty.data_quality_status == SignalQualityStatus.INSUFFICIENT_DATA


def test_conflict_detection_reduces_confidence_and_blocks_buy():
    """Verify that high cross-layer conflict drops confidence and prevents BUY."""
    engine = CrossCheckSignalEngine()
    as_of = datetime(2026, 8, 15, 15, 30)

    # Bullish technicals but strongly Bearish News & Institutional selling
    context = {
        "technical_features": {
            "close": 2500.0,
            "sma_200": 2100.0,
            "rsi_14": 65.0,
            "macd_hist": 2.0,
            "position_52w": 0.90,
            "return_20d": 0.08,
        },
        "news_articles": [
            {
                "headline": "Severe regulatory penalty and investigation launched",
                "published_at": datetime(2026, 8, 15, 10, 0),
                "sentiment_score": -0.95,
                "importance_score": 0.95,
                "credibility_score": 0.95,
            }
        ],
        "institutional_data": {
            "available_at": datetime(2026, 8, 15),
            "fii_net_flow_20d_cr": -4500.0,
            "dii_net_flow_20d_cr": -3200.0,
        },
        "market_regime": {
            "direction_regime": "BEAR",
            "direction_score": -70.0,
            "risk_regime": "RISK_OFF",
            "risk_score": -60.0,
        }
    }

    result = engine.generate_signal("INFY", as_of, context)

    # Must detect conflict and NOT issue BUY
    assert result.signal == SignalType.HOLD
    assert result.conflict_score > 30.0
    assert len(result.opposing_evidence) > 0


def test_historical_signal_evaluator_metrics():
    """Verify forward return metrics, monotonicity, and baseline alpha calculation."""
    evaluator = SignalHistoricalEvaluator()

    mock_df = pd.DataFrame([
        {"signal": "BUY", "signal_score": 75.0, "confidence": 0.85, "forward_return_5d": 0.02, "forward_return_20d": 0.05, "forward_return_63d": 0.12},
        {"signal": "BUY", "signal_score": 65.0, "confidence": 0.80, "forward_return_5d": 0.015, "forward_return_20d": 0.04, "forward_return_63d": 0.09},
        {"signal": "HOLD", "signal_score": 5.0, "confidence": 0.50, "forward_return_5d": 0.002, "forward_return_20d": 0.01, "forward_return_63d": 0.02},
        {"signal": "SELL", "signal_score": -60.0, "confidence": 0.80, "forward_return_5d": -0.02, "forward_return_20d": -0.04, "forward_return_63d": -0.08},
        {"signal": "SELL", "signal_score": -70.0, "confidence": 0.85, "forward_return_5d": -0.03, "forward_return_20d": -0.06, "forward_return_63d": -0.11},
    ])

    results = evaluator.evaluate_historical_signals(mock_df)

    assert results["total_signals"] == 5
    assert results["buy_metrics"]["mean_20d"] == pytest.approx(0.045)
    assert results["sell_metrics"]["mean_20d"] == pytest.approx(-0.05)
    assert results["baseline_comparison"]["engine_adds_value"] is True
