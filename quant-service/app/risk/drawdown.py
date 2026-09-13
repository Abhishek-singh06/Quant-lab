"""
Drawdown and Liquidity Risk Calculators for QuantLab Part 14.
"""

from typing import Optional, Tuple
from app.risk.models import PortfolioState, RiskProfile


class DrawdownRiskCalculator:
    """Calculates current portfolio drawdown and applies risk budget scaling."""

    def calculate_drawdown_adjustment(
        self,
        portfolio_state: PortfolioState,
        risk_profile: RiskProfile
    ) -> Tuple[float, float, str]:
        """
        Returns (current_drawdown_pct, drawdown_multiplier, reason).
        """
        peak = portfolio_state.peak_portfolio_value
        current = portfolio_state.current_portfolio_value
        
        if peak <= 0:
            return 0.0, 1.0, "Initial portfolio state"

        current_dd = max(0.0, (peak - current) / peak)
        max_tol = risk_profile.max_drawdown_tolerance or 0.15

        # Drawdown Scaling Policy:
        # DD < 50% of max tolerance -> 1.00x (Normal)
        # DD 50% - 80% of max tolerance -> 0.75x (Moderate reduction)
        # DD 80% - 100% of max tolerance -> 0.50x (Severe reduction)
        # DD > 100% of max tolerance -> 0.00x (Blocked)
        if current_dd < max_tol * 0.5:
            multiplier = 1.00
            reason = f"Normal drawdown ({current_dd*100:.1f}%) within tolerance"
        elif current_dd < max_tol * 0.8:
            multiplier = 0.75
            reason = f"Moderate drawdown ({current_dd*100:.1f}%) triggers 25% risk budget reduction"
        elif current_dd <= max_tol:
            multiplier = 0.50
            reason = f"Severe drawdown ({current_dd*100:.1f}%) triggers 50% risk budget reduction"
        else:
            multiplier = 0.00
            reason = f"Drawdown ({current_dd*100:.1f}%) exceeds maximum tolerance ({max_tol*100:.1f}%); new allocations blocked"

        return current_dd, multiplier, reason


class LiquidityRiskCalculator:
    """Calculates position sizing constraints based on Average Daily Volume (ADV)."""

    def calculate_liquidity_limit(
        self,
        entry_price: float,
        portfolio_value: float,
        average_daily_volume: Optional[float],
        risk_profile: RiskProfile
    ) -> Tuple[Optional[float], float, str]:
        """
        Returns (max_liquid_quantity, liquidity_multiplier, reason).
        """
        if average_daily_volume is None or average_daily_volume <= 0:
            # Missing liquidity data -> flag input
            return None, 1.0, "Average daily volume unavailable"

        max_participation = risk_profile.max_adv_participation_rate or 0.05
        max_liquid_qty = average_daily_volume * max_participation

        max_liquid_capital = max_liquid_qty * entry_price
        max_liquid_alloc = max_liquid_capital / portfolio_value if portfolio_value > 0 else 0.0

        return max_liquid_qty, max_liquid_alloc, f"Max {max_participation*100:.1f}% ADV participation: {max_liquid_qty:,.0f} shares"
