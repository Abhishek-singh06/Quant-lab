"""Tests for StrategyValidator.

Tests pre-deployment strategy validation against structural, risk, execution,
lookahead, and recursive constraints.
"""

import pytest
import numpy as np
import pandas as pd
from fastapi.testclient import TestClient

from app.main import app
from app.strategy_lifecycle.models import StrategyDefinition, StrategyStatus
from app.strategy_validation.validator import StrategyValidator


@pytest.fixture
def validator():
    return StrategyValidator()


@pytest.fixture
def valid_strategy() -> StrategyDefinition:
    return StrategyDefinition(
        strategy_id="stat_arb_v1",
        version="1.0.0",
        name="Statistical Arbitrage Pair Strategy",
        horizon="SHORT",
        target_instruments=["HDFCBANK", "ICICIBANK"],
        feature_set_id="alpha158",
        feature_names=["KMID", "ROC5"],
        timeframe="1D",
        warmup_period_bars=100,
        required_lookback_bars=250,
        risk_parameters={
            "max_position_size": 0.08,
            "stop_loss_pct": 0.04,
            "take_profit_pct": 0.12,
            "max_drawdown_limit": 0.15,
            "max_leverage": 1.0
        },
        execution_config={
            "slippage_bps": 5.0,
            "commission_bps": 3.0,
            "execution_delay_bars": 1
        }
    )


def test_valid_strategy_passes(validator, valid_strategy):
    report = validator.validate_strategy(valid_strategy)
    assert report.is_valid is True
    assert len(report.blocking_reasons) == 0
    assert report.checks_passed > 0
    assert report.checks_failed == 0


def test_invalid_execution_delay_rejected(validator, valid_strategy):
    """0-delay execution models violate causality and must be blocked."""
    invalid_strat = valid_strategy.model_copy(deep=True)
    invalid_strat.execution_config["execution_delay_bars"] = 0

    report = validator.validate_strategy(invalid_strat)
    assert report.is_valid is False
    assert any("execution_delay_bars < 1" in r for r in report.blocking_reasons)


def test_excessive_leverage_rejected(validator, valid_strategy):
    """Leverage above equity ceiling (> 5.0x) must be blocked."""
    invalid_strat = valid_strategy.model_copy(deep=True)
    invalid_strat.risk_parameters["max_leverage"] = 10.0

    report = validator.validate_strategy(invalid_strat)
    assert report.is_valid is False
    assert any("Excessive leverage" in r for r in report.blocking_reasons)


def test_lookahead_feature_blocks_strategy_validation(validator, valid_strategy):
    """If feature computation introduces future leaks, the strategy validation must block."""
    np.random.seed(42)
    df = pd.DataFrame({"close": 100.0 + np.cumsum(np.random.randn(80))})

    # Future leakage function
    def leaky_compute(d: pd.DataFrame) -> pd.DataFrame:
        out = pd.DataFrame(index=d.index)
        out["lead_return"] = d["close"].shift(-1)
        return out

    report = validator.validate_strategy(
        strategy=valid_strategy,
        feature_compute_fn=leaky_compute,
        sample_df=df
    )
    assert report.is_valid is False
    assert any("Lookahead bias" in r for r in report.blocking_reasons)


def test_validation_api_endpoint(valid_strategy):
    client = TestClient(app)
    payload = valid_strategy.model_dump()
    # Convert datetime objects to iso strings
    payload["created_at"] = payload["created_at"].isoformat()
    payload["updated_at"] = payload["updated_at"].isoformat()

    res = client.post("/api/v1/validation/validate-strategy", json=payload)
    assert res.status_code == 200
    data = res.json()
    assert data["is_valid"] is True
    assert data["strategy_id"] == "stat_arb_v1"
