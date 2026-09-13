"""
Correlation Risk Calculator for QuantLab Part 14.
Computes Pearson correlations between candidate security and existing portfolio positions.
"""

from typing import Any, Dict, List, Optional, Tuple
import numpy as np
import pandas as pd
from app.risk.models import PortfolioPosition, RiskProfile


class CorrelationRiskCalculator:
    """Calculates pairwise correlations and correlation risk multipliers."""

    def calculate_correlation_risk(
        self,
        candidate_symbol: str,
        positions: List[PortfolioPosition],
        returns_df: pd.DataFrame,
        risk_profile: RiskProfile,
        correlation_window: int = 60
    ) -> Tuple[Optional[float], float, List[Dict[str, Any]]]:
        """
        Calculates maximum correlation with existing holdings and returns:
        (max_correlation, correlation_multiplier, correlation_details)
        """
        active_positions = [p for p in positions if p.is_active and p.weight > 0.01 and p.symbol != candidate_symbol]
        if not active_positions or returns_df.empty or candidate_symbol not in returns_df.columns:
            # No existing positions or single security -> no correlation penalty
            return None, 1.0, []

        recent_returns = returns_df.tail(correlation_window)
        if len(recent_returns) < 15:
            # Insufficient sample history -> neutral multiplier
            return None, 1.0, []

        max_corr: float = 0.0
        details: List[Dict[str, Any]] = []

        cand_series = recent_returns[candidate_symbol]
        for pos in active_positions:
            if pos.symbol in recent_returns.columns:
                corr = float(cand_series.corr(recent_returns[pos.symbol]))
                if not np.isnan(corr):
                    details.append({
                        "symbol": pos.symbol,
                        "weight": pos.weight,
                        "correlation": round(corr, 3)
                    })
                    if corr > max_corr:
                        max_corr = corr

        # Correlation Penalty Curve:
        # Correlation <= 0.50 -> 1.00x multiplier (no reduction)
        # Correlation 0.50 - 0.70 -> moderate reduction (0.85x)
        # Correlation 0.70 - 0.85 -> strong reduction (0.65x)
        # Correlation > 0.85 -> severe reduction (0.40x)
        if max_corr <= 0.50:
            multiplier = 1.00
        elif max_corr <= risk_profile.max_correlation_exposure:
            multiplier = 0.85
        elif max_corr <= 0.85:
            multiplier = 0.65
        else:
            multiplier = 0.40

        return max_corr, multiplier, details
