"""
Volatility Adjusted and Portfolio Aware Position Sizers for QuantLab Part 14.
"""

from typing import Optional, Tuple
from app.risk.models import PortfolioState, PositionRiskMetrics, PositionSizingMethod, RiskProfile
from app.risk.sizers.base import PositionSizingStrategy


class VolatilityAdjustedPositionSizer(PositionSizingStrategy):
    """
    Inversely scales position size with asset volatility:
    Base Volatility Target (e.g. 20% annualized).
    If asset vol = 40%, size is halved (0.50x multiplier).
    If Model 3 expected volatility is available, blends realized and forecast volatility.
    """

    @property
    def strategy_name(self) -> PositionSizingMethod:
        return PositionSizingMethod.VOLATILITY_ADJUSTED

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
            return 0.0, 0.0, "Invalid pricing"

        target_vol = risk_profile.max_portfolio_volatility or 0.20
        effective_vol = security_volatility
        if expected_volatility is not None and expected_volatility > 0.01:
            # Blend 50% realized + 50% Model 3 forecast
            effective_vol = 0.5 * security_volatility + 0.5 * expected_volatility

        effective_vol = max(0.05, effective_vol)
        vol_multiplier = min(1.5, max(0.2, target_vol / effective_vol))

        base_alloc = risk_profile.max_position_allocation * 0.8  # e.g. 8%
        adjusted_alloc = min(risk_profile.max_position_allocation, base_alloc * vol_multiplier)

        pos_capital = portfolio_state.current_portfolio_value * adjusted_alloc
        quantity = int(pos_capital / entry_price)

        return (
            adjusted_alloc,
            float(quantity),
            f"Volatility-adjusted sizing (vol: {effective_vol*100:.1f}%, target: {target_vol*100:.1f}%, mult: {vol_multiplier:.2f}x)"
        )


class PortfolioAwarePositionSizer(PositionSizingStrategy):
    """
    Comprehensive portfolio-aware position sizer:
    Combines Fixed Risk baseline, Volatility scaling, and Signal Confidence weighting.
    """

    @property
    def strategy_name(self) -> PositionSizingMethod:
        return PositionSizingMethod.PORTFOLIO_AWARE

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
            return 0.0, 0.0, "Invalid inputs"

        # 1. Base fixed risk quantity
        risk_budget = portfolio_state.current_portfolio_value * risk_profile.max_position_risk
        stop_dist = max(entry_price * 0.01, risk_metrics.stop_distance)
        raw_quantity = risk_budget / stop_dist

        # 2. Confidence scaling (0.7x to 1.2x)
        conf_multiplier = max(0.5, min(1.2, 0.5 + signal_confidence * 0.7))
        
        # 3. Volatility scaling
        target_vol = risk_profile.max_portfolio_volatility or 0.20
        eff_vol = max(0.05, expected_volatility or security_volatility)
        vol_multiplier = max(0.3, min(1.2, target_vol / eff_vol))

        scaled_quantity = raw_quantity * conf_multiplier * vol_multiplier

        # 4. Cap by single security max allocation limit
        max_capital = portfolio_state.current_portfolio_value * risk_profile.max_position_allocation
        max_quantity = max_capital / entry_price
        
        final_quantity = int(min(scaled_quantity, max_quantity))
        final_alloc = (final_quantity * entry_price) / portfolio_state.current_portfolio_value

        return (
            final_alloc,
            float(final_quantity),
            f"Portfolio-aware multi-factor sizing (conf: {conf_multiplier:.2f}x, vol: {vol_multiplier:.2f}x)"
        )
