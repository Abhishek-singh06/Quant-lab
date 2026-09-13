"""
Next-Bar Execution Simulator enforcing strict Point-in-Time order matching.
Signal at T close -> Execution at T+1 open.
"""

from datetime import datetime
from typing import Optional, Tuple
from app.backtesting.schemas import BacktestOrder, OrderStatus, OrderSide, BacktestConfig
from app.backtesting.execution.slippage import SlippageModel
from app.backtesting.execution.costs import TransactionCostModel


class NextBarExecutionSimulator:
    """
    Executes orders strictly on the NEXT available bar (at Open).
    Guarantees zero same-bar lookahead execution bias.
    """

    def __init__(self, config: BacktestConfig):
        self.config = config
        self.slippage_model = SlippageModel(config)
        self.cost_model = TransactionCostModel(config)

    def execute_order(
        self,
        order: BacktestOrder,
        bar_timestamp: datetime,
        bar_open: float,
        bar_volume: int = 100000,
        is_delivery: bool = True
    ) -> Tuple[BacktestOrder, float, float]:
        """
        Executes a pending order at bar_open with slippage and fees.
        Returns (updated_order, total_cash_required_or_received, total_fees).
        """
        # Timing validation: Bar execution timestamp MUST be > order submission timestamp
        if bar_timestamp <= order.order_submitted_timestamp:
            order.status = OrderStatus.REJECTED
            order.rejection_reason = "TIMING_VIOLATION: Cannot execute order on or before submission timestamp"
            return order, 0.0, 0.0

        if bar_open <= 0 or order.quantity <= 0:
            order.status = OrderStatus.REJECTED
            order.rejection_reason = "INVALID_PRICE_OR_QUANTITY"
            return order, 0.0, 0.0

        exec_price, slippage_bps, slippage_amount = self.slippage_model.calculate_execution_price(
            side=order.side,
            market_price=bar_open,
            quantity=order.quantity,
            bar_volume=bar_volume
        )

        cost_breakdown = self.cost_model.calculate_cost(
            side=order.side,
            price=exec_price,
            quantity=order.quantity,
            is_delivery=is_delivery
        )
        total_fees = cost_breakdown["total_fees"]

        order.executed_price = exec_price
        order.order_executed_timestamp = bar_timestamp
        order.status = OrderStatus.FILLED
        order.slippage_bps = slippage_bps
        order.slippage_amount = slippage_amount
        order.fees_amount = total_fees

        gross_value = exec_price * order.quantity

        if order.side == OrderSide.BUY:
            # Buyer pays gross price + fees
            cash_delta = -(gross_value + total_fees)
        else:
            # Seller receives gross price minus fees
            cash_delta = gross_value - total_fees

        return order, cash_delta, total_fees
