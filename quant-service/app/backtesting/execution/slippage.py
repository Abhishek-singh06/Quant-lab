"""
Slippage models for realistic trade execution simulation.
"""

from typing import Tuple
from app.backtesting.schemas import OrderSide, SlippageModelType, BacktestConfig


class SlippageModel:
    """
    Computes realistic slippage for market orders.
    Buys execute at price * (1 + slippage_bps / 10000).
    Sells execute at price * (1 - slippage_bps / 10000).
    """

    def __init__(self, config: BacktestConfig):
        self.config = config
        self.slippage_type = config.slippage_model_type

    def calculate_execution_price(
        self,
        side: OrderSide,
        market_price: float,
        quantity: int,
        bar_volume: int = 100000
    ) -> Tuple[float, float, float]:
        """
        Returns (executed_price, effective_slippage_bps, slippage_amount_in_cash).
        """
        if self.slippage_type == SlippageModelType.NONE or market_price <= 0:
            return market_price, 0.0, 0.0

        if self.slippage_type == SlippageModelType.FIXED_BPS:
            bps = self.config.slippage_bps
        elif self.slippage_type == SlippageModelType.SPREAD_AND_VOLUME:
            # Volume participation penalty: if order is > 1% of bar volume, add non-linear impact
            pct_of_volume = quantity / max(bar_volume, 1)
            impact_bps = 5.0 + 20.0 * min(pct_of_volume, 0.20)
            bps = max(self.config.slippage_bps, impact_bps)
        else:
            bps = self.config.slippage_bps

        slippage_fraction = bps / 10000.0

        if side == OrderSide.BUY:
            executed_price = market_price * (1.0 + slippage_fraction)
        else:
            executed_price = market_price * (1.0 - slippage_fraction)

        # Slippage amount is the adverse cash impact
        slippage_amount = abs(executed_price - market_price) * quantity

        return executed_price, bps, slippage_amount
