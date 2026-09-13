"""
Position Risk Calculator for QuantLab Part 14.
Calculates stop prices, stop distance, position risk amounts, and risk/reward ratios.
"""

from typing import Optional
from app.risk.models import PositionRiskMetrics, StopMethod


class PositionRiskCalculator:
    """Calculates granular risk metrics for a candidate position."""

    def calculate_position_risk(
        self,
        entry_price: float,
        quantity: float,
        portfolio_value: float,
        stop_method: StopMethod = StopMethod.ATR_MULTIPLE,
        atr: Optional[float] = None,
        atr_multiplier: float = 2.0,
        fixed_stop_pct: float = 0.05,
        user_stop_price: Optional[float] = None,
        target_price: Optional[float] = None,
        is_short: bool = False
    ) -> PositionRiskMetrics:
        if entry_price <= 0 or portfolio_value <= 0:
            return PositionRiskMetrics(is_valid=False)

        # 1. Determine Stop Price and Stop Distance
        if stop_method == StopMethod.ATR_MULTIPLE and atr is not None and atr > 0:
            stop_distance = atr * atr_multiplier
            stop_price = entry_price + stop_distance if is_short else entry_price - stop_distance
        elif stop_method == StopMethod.USER_DEFINED and user_stop_price is not None and user_stop_price > 0:
            stop_price = user_stop_price
            stop_distance = abs(entry_price - stop_price)
        else:  # Default to FIXED_PERCENT or fallback
            stop_distance = entry_price * fixed_stop_pct
            stop_price = entry_price + stop_distance if is_short else entry_price - stop_distance

        # Guard against negative or inverted stops
        stop_price = max(0.01, stop_price)
        stop_distance = abs(entry_price - stop_price)
        stop_distance_pct = stop_distance / entry_price if entry_price > 0 else 0.0

        # 2. Position Risk Amount and %
        # Never allow negative risk due to incorrect direction
        position_risk_amount = stop_distance * abs(quantity)
        position_risk_percent = (position_risk_amount / portfolio_value) if portfolio_value > 0 else 0.0
        estimated_downside = position_risk_amount

        # 3. Risk / Reward Ratio
        risk_reward_ratio: Optional[float] = None
        if target_price is not None and target_price > 0 and stop_distance > 0:
            reward = abs(target_price - entry_price)
            risk_reward_ratio = reward / stop_distance

        return PositionRiskMetrics(
            entry_price=entry_price,
            current_price=entry_price,
            stop_price=stop_price,
            target_price=target_price,
            stop_distance=stop_distance,
            stop_distance_pct=stop_distance_pct,
            stop_method=stop_method,
            position_risk_amount=position_risk_amount,
            position_risk_percent=position_risk_percent,
            estimated_downside=estimated_downside,
            risk_reward_ratio=risk_reward_ratio,
            is_valid=True
        )
