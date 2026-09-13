"""
Concentration Risk Calculator for QuantLab Part 14.
Monitors security, sector, and industry exposures against configured limits.
"""

from typing import Dict, List, Tuple
from app.risk.models import PortfolioPosition, RiskProfile


class ConcentrationRiskCalculator:
    """Evaluates security, sector, and industry concentration limits."""

    def evaluate_concentration(
        self,
        symbol: str,
        sector: str,
        industry: str,
        proposed_allocation: float,
        existing_positions: List[PortfolioPosition],
        risk_profile: RiskProfile
    ) -> Tuple[float, float, float, float, str]:
        """
        Returns:
        (post_trade_security_weight, post_trade_sector_weight, max_allowed_alloc_by_concentration, concentration_multiplier, reason)
        """
        # Current existing weights
        curr_sec_weight = 0.0
        curr_sector_weight = 0.0
        curr_industry_weight = 0.0

        for p in existing_positions:
            if not p.is_active:
                continue
            if p.symbol == symbol:
                curr_sec_weight += p.weight
            if p.sector == sector and sector != "UNKNOWN":
                curr_sector_weight += p.weight
            if p.industry == industry and industry != "UNKNOWN":
                curr_industry_weight += p.weight

        post_sec_weight = curr_sec_weight + proposed_allocation
        post_sector_weight = curr_sector_weight + proposed_allocation

        # Check remaining headroom
        security_headroom = max(0.0, risk_profile.max_single_security_allocation - curr_sec_weight)
        sector_headroom = max(0.0, risk_profile.max_sector_allocation - curr_sector_weight)
        
        max_allowed_by_concentration = min(
            proposed_allocation,
            security_headroom,
            sector_headroom
        )

        if proposed_allocation > 0:
            multiplier = min(1.0, max_allowed_by_concentration / proposed_allocation)
        else:
            multiplier = 0.0

        reason = ""
        if multiplier < 0.99:
            if sector_headroom < security_headroom:
                reason = f"Sector '{sector}' cap reached ({post_sector_weight*100:.1f}% vs max {risk_profile.max_sector_allocation*100:.1f}%)"
            else:
                reason = f"Security '{symbol}' cap reached ({post_sec_weight*100:.1f}% vs max {risk_profile.max_single_security_allocation*100:.1f}%)"

        return post_sec_weight, post_sector_weight, max_allowed_by_concentration, multiplier, reason
