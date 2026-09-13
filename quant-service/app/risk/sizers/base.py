"""
Abstract Base Class for Position Sizing Strategies.
"""

from abc import ABC, abstractmethod
from typing import Optional, Tuple
from app.risk.models import PortfolioState, PositionRiskMetrics, PositionSizingMethod, RiskProfile


class PositionSizingStrategy(ABC):
    """Abstract position sizing strategy."""

    @property
    @abstractmethod
    def strategy_name(self) -> PositionSizingMethod:
        """Name of the position sizing strategy."""
        pass

    @abstractmethod
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
        """
        Calculate (suggested_allocation, recommended_quantity, sizing_reason).
        suggested_allocation is in fraction of portfolio (e.g. 0.08 = 8%).
        """
        pass
