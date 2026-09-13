"""
Risk Constraint Engine for QuantLab Part 14.
Resolves and binds position sizing to the most restrictive applicable risk limit.
"""

from typing import Dict, List, Tuple
from app.risk.models import PortfolioState, PositionRiskMetrics, RiskProfile


class RiskConstraintEngine:
    """Enforces bounding constraints and identifies the active limiting bottleneck."""

    def resolve_constraints(
        self,
        raw_suggested_allocation: float,
        entry_price: float,
        portfolio_state: PortfolioState,
        risk_profile: RiskProfile,
        risk_metrics: PositionRiskMetrics,
        max_sector_headroom: float,
        max_liquid_alloc: float,
        drawdown_multiplier: float,
        correlation_multiplier: float
    ) -> Tuple[float, float, str, List[str]]:
        """
        Returns:
        (final_suggested_alloc, max_permitted_alloc, binding_constraint_name, all_limiting_reasons)
        """
        port_val = portfolio_state.current_portfolio_value
        limiting_reasons: List[str] = []

        # 1. Profile Single Security Cap
        sec_cap = risk_profile.max_single_security_allocation
        
        # 2. Portfolio Risk Budget Cap
        # Max allocation from remaining risk budget:
        # (remaining_risk_budget / stop_distance) * entry_price / port_val
        stop_dist = max(0.01, risk_metrics.stop_distance)
        rem_risk_budget = max(0.0, portfolio_state.remaining_risk_budget)
        if rem_risk_budget <= 0:
            rem_risk_budget = port_val * risk_profile.max_portfolio_risk - portfolio_state.consumed_risk_budget
        rem_risk_budget = max(0.0, rem_risk_budget)

        if stop_dist > 0 and port_val > 0:
            risk_budget_cap = (rem_risk_budget / stop_dist) * entry_price / port_val
        else:
            risk_budget_cap = sec_cap

        # 3. Cash Buffer Cap
        available_cash_ratio = max(0.0, (portfolio_state.current_cash / port_val) - risk_profile.cash_buffer)

        # 4. Sector Headroom Cap
        sector_cap = max_sector_headroom

        # 5. Liquidity Cap
        liquidity_cap = max_liquid_alloc if max_liquid_alloc > 0 else sec_cap

        # Maximum permitted allocation is the MINIMUM of all structural boundaries
        max_permitted_allocation = min(
            sec_cap,
            risk_budget_cap,
            available_cash_ratio,
            sector_cap,
            liquidity_cap
        )
        max_permitted_allocation = max(0.0, min(1.0, max_permitted_allocation))

        # Scaled raw allocation
        scaled_allocation = raw_suggested_allocation * drawdown_multiplier * correlation_multiplier
        final_suggested_allocation = min(scaled_allocation, max_permitted_allocation)

        # Identify binding constraint
        binding_constraint = "NONE"
        if final_suggested_allocation == risk_budget_cap and risk_budget_cap < sec_cap:
            binding_constraint = "PORTFOLIO_RISK_BUDGET"
            limiting_reasons.append(f"Capped by remaining portfolio risk budget (₹{rem_risk_budget:,.0f})")
        elif final_suggested_allocation == sector_cap and sector_cap < sec_cap:
            binding_constraint = "SECTOR_CONCENTRATION_LIMIT"
            limiting_reasons.append("Capped by sector exposure ceiling")
        elif final_suggested_allocation == available_cash_ratio and available_cash_ratio < sec_cap:
            binding_constraint = "CASH_BUFFER_LIMIT"
            limiting_reasons.append(f"Capped by unencumbered cash buffer requirement ({risk_profile.cash_buffer*100:.1f}%)")
        elif final_suggested_allocation == sec_cap:
            binding_constraint = "SINGLE_SECURITY_MAX_ALLOCATION"
            limiting_reasons.append(f"Bounded by max security allocation limit ({sec_cap*100:.1f}%)")

        if drawdown_multiplier < 1.0:
            limiting_reasons.append(f"Drawdown scaling applied ({drawdown_multiplier*100:.0f}% allocation)")
        if correlation_multiplier < 1.0:
            limiting_reasons.append(f"Correlation penalty applied ({correlation_multiplier*100:.0f}% allocation)")

        return final_suggested_allocation, max_permitted_allocation, binding_constraint, limiting_reasons
