"""
Fixed Allocation and Fixed Risk Position Sizers for QuantLab Part 14.
"""

from typing import Optional, Tuple
from app.risk.models import PortfolioState, PositionRiskMetrics, PositionSizingMethod, RiskProfile
from app.risk.sizers.base import PositionSizingStrategy


class FixedAllocationPositionSizer(PositionSizingStrategy):
    """Allocates a fixed fraction of the portfolio (e.g. 8%), constrained by profile maximums."""

    @property
    def strategy_name(self) -> PositionSizingMethod:
        return PositionSizingMethod.FIXED_ALLOCATION

    def calculate_allocation(
        self,
        symbol: str,
        entry_price: float,
        portfolio_state: PortfolioState,
        risk_profile: RiskProfile,
        risk_metrics: PositionRiskMetrics,
        security_volatility: float,
        expected_volatility: Optional[float] = None,
        signal_confidence: float = 1.0,
        signal_score: float = 50.0
    ) -> Tuple[float, float, str]:
        if entry_price <= 0 or portfolio_state.current_portfolio_value <= 0:
            return 0.0, 0.0, "Invalid entry price or portfolio value"

        # Base allocation default (e.g. 8%) capped by profile max
        base_alloc = min(0.08, risk_profile.max_position_allocation)
        pos_capital = portfolio_state.current_portfolio_value * base_alloc
        quantity = int(pos_capital / entry_price)

        return base_alloc, float(quantity), f"Fixed allocation of {base_alloc*100:.1f}% applied"


class FixedRiskPositionSizer(PositionSizingStrategy):
    """
    Calculates quantity from risk budget:
    Quantity = Risk Budget / Stop Distance
    where Risk Budget = Portfolio Value * Max Position Risk %
    """

    @property
    def strategy_name(self) -> PositionSizingMethod:
        return PositionSizingMethod.FIXED_RISK

    def calculate_allocation(
        self,
        symbol: str,
        entry_price: float,
        portfolio_state: PortfolioState,
        risk_profile: RiskProfile,
        risk_metrics: PositionRiskMetrics,
        security_volatility: float,
        expected_volatility: Optional[float] = None,
        signal_confidence: float = 1.0,
        signal_score: float = 50.0
    ) -> Tuple[float, float, str]:
        if entry_price <= 0 or portfolio_state.current_portfolio_value <= 0 or risk_metrics.stop_distance <= 0:
            return 0.0, 0.0, "Invalid pricing or stop distance"

        # Risk budget in monetary terms
        risk_budget = portfolio_state.current_portfolio_value * risk_profile.max_position_risk
        
        # Quantity from risk budget / stop distance
        raw_quantity = risk_budget / risk_metrics.stop_distance
        
        # Enforce max position capital allocation
        max_capital = portfolio_state.current_portfolio_value * risk_profile.max_position_allocation
        max_quantity_by_cap = max_capital / entry_price
        
        final_quantity = int(min(raw_quantity, max_quantity_by_cap))
        final_allocation = (final_quantity * entry_price) / portfolio_state.current_portfolio_value

        return (
            final_allocation,
            float(final_quantity),
            f"Fixed risk sizing targeting ₹{risk_budget:.0f} risk with stop distance ₹{risk_metrics.stop_distance:.2f}"
        )
