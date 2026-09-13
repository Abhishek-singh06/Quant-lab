"""Horizon-Specific Risk Engine Integration."""

from typing import Dict, Any
from app.horizons.schemas import TradingHorizon, HorizonPrediction, HorizonOutlook
from app.risk.models import StopMethod, PositionSizingMethod


class HorizonRiskIntegration:
    """Translates horizon predictions into tailored risk engine inputs for Part 14."""

    @staticmethod
    def get_horizon_risk_parameters(
        horizon: TradingHorizon,
        prediction: HorizonPrediction,
        current_price: float,
        atr: float
    ) -> Dict[str, Any]:
        """Generate horizon-calibrated risk parameters."""
        if horizon == TradingHorizon.SHORT_TERM:
            # Short-Term: tight stop, fast invalidation, conservative risk budget
            stop_distance = max(0.01, 1.5 * atr)
            stop_price = current_price - stop_distance if prediction.outlook == HorizonOutlook.BULLISH else current_price + stop_distance
            return {
                "horizon": horizon.value,
                "stop_method": StopMethod.ATR_MULTIPLE,
                "sizing_method": PositionSizingMethod.FIXED_RISK,
                "entry_price": current_price,
                "stop_price": stop_price,
                "stop_distance": stop_distance,
                "max_position_risk": 0.005,  # 0.5% risk
                "max_allocation": 0.05,       # 5% max capital
                "turnover_tier": "HIGH",
                "holding_days_estimate": 3
            }

        elif horizon == TradingHorizon.MEDIUM_TERM:
            # Medium-Term: 2.5x ATR stop, swing invalidation, balanced risk
            stop_distance = max(0.01, 2.5 * atr)
            stop_price = current_price - stop_distance if prediction.outlook == HorizonOutlook.BULLISH else current_price + stop_distance
            return {
                "horizon": horizon.value,
                "stop_method": StopMethod.ATR_MULTIPLE,
                "sizing_method": PositionSizingMethod.VOLATILITY_ADJUSTED,
                "entry_price": current_price,
                "stop_price": stop_price,
                "stop_distance": stop_distance,
                "max_position_risk": 0.01,   # 1.0% risk
                "max_allocation": 0.10,      # 10% max capital
                "turnover_tier": "MEDIUM",
                "holding_days_estimate": 28
            }

        else: # LONG_TERM
            # Long-Term: wide stop (12% fixed or support/resistance), quality-based allocation
            stop_distance = max(0.01, current_price * 0.12)
            stop_price = current_price - stop_distance if prediction.outlook == HorizonOutlook.BULLISH else current_price + stop_distance
            return {
                "horizon": horizon.value,
                "stop_method": StopMethod.FIXED_PERCENT,
                "sizing_method": PositionSizingMethod.PORTFOLIO_AWARE,
                "entry_price": current_price,
                "stop_price": stop_price,
                "stop_distance": stop_distance,
                "max_position_risk": 0.02,   # 2.0% risk
                "max_allocation": 0.15,      # 15% max capital
                "turnover_tier": "LOW",
                "holding_days_estimate": 365
            }
