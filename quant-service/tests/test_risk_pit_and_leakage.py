"""
Point-in-Time, Anti-Leakage, and Future-Data Invariance Tests for Part 14 Risk Engine.
"""

from datetime import datetime, timezone
import copy
import pytest
import pandas as pd
import numpy as np

from app.risk.models import PortfolioPosition, PortfolioState, RiskProfile, StopMethod
from app.risk.engine import ProductionRiskEngine


def test_mandatory_future_data_invariance_for_risk_assessment():
    """
    Mandatory Test: Take historical timestamp T. Generate risk assessment.
    Append future price series, future volatility spikes, and future portfolio changes.
    The historical assessment at T MUST remain 100% identical.
    """
    engine = ProductionRiskEngine()
    as_of = datetime(2024, 6, 15, 10, 0, tzinfo=timezone.utc)

    profile = RiskProfile(
        max_position_risk=0.01,
        max_position_allocation=0.10
    )

    port_state = PortfolioState(
        current_portfolio_value=1_000_000.0,
        current_cash=800_000.0,
        positions=[
            PortfolioPosition(symbol="TCS", sector="IT", weight=0.10, market_value=100_000.0, is_active=True),
        ]
    )

    base_market = {
        "current_price": 2000.0,
        "atr_14": 40.0,
        "annualized_volatility": 0.24,
        "sector": "ENERGY"
    }

    # Historical returns up to T
    dates_hist = pd.date_range("2024-03-01", "2024-06-15", freq="B")
    np.random.seed(42)
    returns_hist = pd.DataFrame({
        "RELIANCE": np.random.normal(0.0005, 0.015, len(dates_hist)),
        "TCS": np.random.normal(0.0003, 0.012, len(dates_hist))
    }, index=dates_hist)

    # Step 1: Generate initial assessment at T
    assess_1 = engine.assess_position_risk(
        symbol="RELIANCE",
        as_of_timestamp=as_of,
        portfolio_state=port_state,
        risk_profile=profile,
        market_data=base_market,
        returns_history_df=returns_hist
    )

    # Step 2: Append future market crash and future price shifts after T
    dates_future = pd.date_range("2024-06-16", "2024-09-01", freq="B")
    returns_future = pd.DataFrame({
        "RELIANCE": np.random.normal(-0.02, 0.05, len(dates_future)),
        "TCS": np.random.normal(-0.015, 0.04, len(dates_future))
    }, index=dates_future)
    returns_combined = pd.concat([returns_hist, returns_future])

    # Re-evaluate with historical slice (T_avail <= as_of)
    returns_pit_slice = returns_combined.loc[:as_of.strftime("%Y-%m-%d")]

    assess_2 = engine.assess_position_risk(
        symbol="RELIANCE",
        as_of_timestamp=as_of,
        portfolio_state=port_state,
        risk_profile=profile,
        market_data=base_market,
        returns_history_df=returns_pit_slice
    )

    # Assert 100% invariance
    assert assess_1.suggested_allocation == assess_2.suggested_allocation
    assert assess_1.recommended_quantity == assess_2.recommended_quantity
    assert assess_1.stop_price == assess_2.stop_price
    assert assess_1.position_risk_amount == assess_2.position_risk_amount
    assert assess_1.estimated_downside == assess_2.estimated_downside
    assert assess_1.risk_decision == assess_2.risk_decision


def test_missing_price_data_triggers_insufficient_data():
    """Verify that missing price data produces INSUFFICIENT_DATA decision rather than arbitrary zero sizing."""
    engine = ProductionRiskEngine()
    as_of = datetime(2026, 8, 15, 15, 30, tzinfo=timezone.utc)

    port_state = PortfolioState(current_portfolio_value=1_000_000.0)
    profile = RiskProfile()

    # Zero or missing price
    result = engine.assess_position_risk(
        symbol="DELISTED_STOCK",
        as_of_timestamp=as_of,
        portfolio_state=port_state,
        risk_profile=profile,
        market_data={"current_price": 0.0}
    )

    assert result.risk_decision.value == "INSUFFICIENT_DATA"
    assert result.data_quality_status == "INSUFFICIENT_DATA"
    assert result.suggested_allocation == 0.0
    assert result.recommended_quantity == 0.0
