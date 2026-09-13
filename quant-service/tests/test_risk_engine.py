"""
Unit tests and mathematical validation for Production Risk Engine & Position Sizing.
"""

from datetime import datetime, timezone
import pytest
import numpy as np
import pandas as pd

from app.risk.models import (
    PortfolioPosition,
    PortfolioState,
    PositionSizingMethod,
    RiskDecision,
    RiskLevel,
    RiskProfile,
    StopMethod,
)
from app.risk.position_risk import PositionRiskCalculator
from app.risk.portfolio_risk import PortfolioRiskCalculator
from app.risk.engine import ProductionRiskEngine
from app.risk.evaluator import RiskHistoricalEvaluator


def test_position_risk_calculator_math():
    """Verify exact mathematical formulas for stop distance, risk amount, risk %, and downside."""
    calc = PositionRiskCalculator()
    
    # Portfolio: 1,000,000 INR, Entry: 1000 INR, ATR: 25 INR (Multiplier: 2.0 -> Stop Dist: 50 INR)
    # Stop Price: 950 INR, Quantity: 100
    res = calc.calculate_position_risk(
        entry_price=1000.0,
        quantity=100.0,
        portfolio_value=1_000_000.0,
        stop_method=StopMethod.ATR_MULTIPLE,
        atr=25.0,
        atr_multiplier=2.0,
        target_price=1115.0
    )

    assert res.is_valid is True
    assert res.stop_price == pytest.approx(950.0)
    assert res.stop_distance == pytest.approx(50.0)
    assert res.stop_distance_pct == pytest.approx(0.05)
    assert res.position_risk_amount == pytest.approx(5000.0)
    assert res.position_risk_percent == pytest.approx(0.005)  # 0.50%
    assert res.estimated_downside == pytest.approx(5000.0)
    assert res.risk_reward_ratio == pytest.approx(2.3)       # 115 / 50 = 2.3


def test_portfolio_volatility_covariance_matrix_math():
    """Verify matrix portfolio variance formulation: w^T * Sigma * w."""
    calc = PortfolioRiskCalculator()
    
    # Two assets, weights [0.6, 0.4]
    weights = np.array([0.6, 0.4])
    # Daily covariance matrix with daily variances [0.0004, 0.000625] (vol 2% and 2.5%), cov = 0.0002
    cov_matrix = np.array([
        [0.0004, 0.0002],
        [0.0002, 0.000625]
    ])

    # Expected variance: 0.36*0.0004 + 0.16*0.000625 + 2*0.6*0.4*0.0002 = 0.000144 + 0.000100 + 0.000096 = 0.000340
    # Expected daily vol = sqrt(0.000340) = 0.018439
    # Annualized vol = 0.018439 * sqrt(252) = 0.2927 (29.27%)
    annualized_vol = calc.calculate_portfolio_volatility(weights, cov_matrix)
    
    assert annualized_vol == pytest.approx(np.sqrt(0.000340) * np.sqrt(252.0), rel=1e-3)


def test_fixed_risk_position_sizer():
    """Verify Fixed Risk position sizer: Quantity = (Portfolio * Max Risk %) / Stop Distance."""
    engine = ProductionRiskEngine()
    
    profile = RiskProfile(
        max_position_risk=0.005,        # 0.5% max risk
        max_position_allocation=0.10,   # 10% max capital allocation
        default_position_sizing_method=PositionSizingMethod.FIXED_RISK,
        default_stop_method=StopMethod.ATR_MULTIPLE
    )
    
    port_state = PortfolioState(
        current_portfolio_value=1_000_000.0,
        current_cash=1_000_000.0,
        remaining_risk_budget=50_000.0
    )
    
    # Entry: 1000 INR, ATR: 25 INR -> Stop distance: 50 INR
    # Risk budget = 1,000,000 * 0.005 = 5000 INR
    # Quantity = 5000 / 50 = 100 shares (100,000 INR = 10% allocation)
    result = engine.assess_position_risk(
        symbol="RELIANCE",
        as_of_timestamp=datetime(2026, 8, 15, 15, 30, tzinfo=timezone.utc),
        portfolio_state=port_state,
        risk_profile=profile,
        market_data={
            "current_price": 1000.0,
            "atr_14": 25.0,
            "annualized_volatility": 0.22,
            "sector": "ENERGY"
        }
    )

    assert result.risk_decision == RiskDecision.APPROVE
    assert result.recommended_quantity == 100.0
    assert result.suggested_allocation == pytest.approx(0.10)
    assert result.position_risk_amount == pytest.approx(5000.0)
    assert result.position_risk_percent == pytest.approx(0.005)


def test_concentration_and_sector_cap_reduction():
    """Verify that exceeding sector concentration caps reduces the proposed allocation."""
    engine = ProductionRiskEngine()
    
    profile = RiskProfile(
        max_sector_allocation=0.25,     # 25% sector limit
        max_position_allocation=0.15,
        default_position_sizing_method=PositionSizingMethod.FIXED_ALLOCATION
    )
    
    # Existing portfolio has 20% in Financial Services
    port_state = PortfolioState(
        current_portfolio_value=1_000_000.0,
        current_cash=500_000.0,
        positions=[
            PortfolioPosition(symbol="HDFCBANK", sector="FINANCIAL_SERVICES", weight=0.12, market_value=120_000.0, is_active=True),
            PortfolioPosition(symbol="ICICIBANK", sector="FINANCIAL_SERVICES", weight=0.08, market_value=80_000.0, is_active=True),
        ]
    )
    
    # Candidate KOTAKBANK in FINANCIAL_SERVICES proposals 8% -> would exceed 25% (20% + 8% = 28%)
    # Sector headroom is 25% - 20% = 5%
    result = engine.assess_position_risk(
        symbol="KOTAKBANK",
        as_of_timestamp=datetime(2026, 8, 15, 15, 30, tzinfo=timezone.utc),
        portfolio_state=port_state,
        risk_profile=profile,
        market_data={
            "current_price": 1800.0,
            "atr_14": 30.0,
            "annualized_volatility": 0.20,
            "sector": "FINANCIAL_SERVICES"
        }
    )

    assert result.risk_decision == RiskDecision.APPROVE_REDUCED
    assert result.suggested_allocation <= 0.051  # Capped by ~5% sector headroom
    assert "SECTOR_CONCENTRATION_LIMIT" in str(result.limiting_constraints) or result.risk_trace.limiting_constraint == "SECTOR_CONCENTRATION_LIMIT"


def test_drawdown_risk_budget_reduction():
    """Verify that severe portfolio drawdown reduces position size or blocks allocation."""
    engine = ProductionRiskEngine()
    
    profile = RiskProfile(
        max_drawdown_tolerance=0.15,   # 15% tolerance
        default_position_sizing_method=PositionSizingMethod.FIXED_RISK
    )
    
    # Portfolio suffered 16% drawdown (Peak 1,200,000 -> Current 1,000,000)
    port_state = PortfolioState(
        current_portfolio_value=1_000_000.0,
        peak_portfolio_value=1_200_000.0,
        current_drawdown=0.1667,
        current_cash=800_000.0
    )
    
    result = engine.assess_position_risk(
        symbol="INFY",
        as_of_timestamp=datetime(2026, 8, 15, 15, 30, tzinfo=timezone.utc),
        portfolio_state=port_state,
        risk_profile=profile,
        market_data={
            "current_price": 1500.0,
            "atr_14": 35.0,
            "annualized_volatility": 0.25,
            "sector": "IT"
        }
    )

    # Exceeds max drawdown tolerance -> blocked
    assert result.risk_decision == RiskDecision.BLOCKED_BY_DRAWDOWN
    assert result.recommended_quantity == 0.0
    assert result.suggested_allocation == 0.0


def test_historical_risk_evaluator():
    """Verify Historical Risk Evaluator stop-hit frequency and risk-budget integrity metrics."""
    evaluator = RiskHistoricalEvaluator()

    mock_df = pd.DataFrame([
        {"suggested_allocation": 0.08, "stop_distance_pct": 0.05, "forward_min_return_20d": -0.02, "forward_return_20d": 0.04, "position_risk_amount": 4000.0, "realized_pnl_20d": 3200.0},
        {"suggested_allocation": 0.08, "stop_distance_pct": 0.05, "forward_min_return_20d": -0.06, "forward_return_20d": -0.05, "position_risk_amount": 4000.0, "realized_pnl_20d": -4000.0}, # Stopped out
        {"suggested_allocation": 0.06, "stop_distance_pct": 0.04, "forward_min_return_20d": -0.01, "forward_return_20d": 0.02, "position_risk_amount": 3000.0, "realized_pnl_20d": 1200.0},
    ])

    res = evaluator.evaluate_historical_risk(mock_df)

    assert res["total_assessments_evaluated"] == 3
    assert res["stopped_out_positions_count"] == 1
    assert res["stop_hit_frequency_20d"] == pytest.approx(1.0 / 3.0)
    assert res["risk_budget_integrity"] == "VALIDATED"
