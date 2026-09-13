"""
Paper Execution Simulator.
Executes paper orders realistically against live quotes or bars with slippage, Indian fees, and liquidity limits.
"""

from datetime import datetime
from typing import Optional, Tuple, Dict, Any
from uuid import uuid4
from app.paper.schemas import PaperOrder, PaperFill, OrderSide, OrderStatus


class PaperExecutionSimulator:
    """
    Simulates execution of paper orders.
    Enforces that paper orders are NEVER routed to real brokers.
    """

    def __init__(
        self,
        default_slippage_bps: float = 5.0,
        brokerage_bps: float = 3.0,
        stt_delivery_bps: float = 10.0,
        exchange_charges_bps: float = 0.345,
        gst_rate: float = 0.18,
        stamp_duty_bps: float = 1.5,
        max_volume_participation_pct: float = 0.05  # Max 5% of bar/current volume
    ):
        self.default_slippage_bps = default_slippage_bps
        self.brokerage_bps = brokerage_bps
        self.stt_delivery_bps = stt_delivery_bps
        self.exchange_charges_bps = exchange_charges_bps
        self.gst_rate = gst_rate
        self.stamp_duty_bps = stamp_duty_bps
        self.max_volume_participation_pct = max_volume_participation_pct

    def execute_paper_order(
        self,
        order: PaperOrder,
        market_price: float,
        execution_timestamp: datetime,
        current_volume: int = 100000,
        is_delivery: bool = True
    ) -> Tuple[PaperOrder, Optional[PaperFill], float]:
        """
        Executes paper order.
        Returns (updated_order, fill_record, total_cash_impact).
        """
        # Timing validation: Execution timestamp MUST be >= order submission timestamp
        if execution_timestamp < order.order_submitted_timestamp:
            order.status = OrderStatus.REJECTED
            order.rejection_reason = "TIMING_VIOLATION: Execution before submission timestamp"
            return order, None, 0.0

        if market_price <= 0 or order.quantity <= 0:
            order.status = OrderStatus.REJECTED
            order.rejection_reason = "INVALID_PRICE_OR_QUANTITY"
            return order, None, 0.0

        # Liquidity constraint: Check volume participation
        max_allowed_qty = max(1, int(current_volume * self.max_volume_participation_pct))
        exec_qty = min(order.quantity, max_allowed_qty)

        if exec_qty < order.quantity:
            # Partially filled or reduced due to liquidity
            order.quantity = exec_qty

        # Slippage calculation
        slippage_fraction = self.default_slippage_bps / 10000.0
        if order.side == OrderSide.BUY:
            fill_price = market_price * (1.0 + slippage_fraction)
        else:
            fill_price = market_price * (1.0 - slippage_fraction)

        slippage_amt = abs(fill_price - market_price) * exec_qty
        turnover = fill_price * exec_qty

        # Indian transaction costs
        brokerage = turnover * (self.brokerage_bps / 10000.0)
        stt = turnover * (self.stt_delivery_bps / 10000.0) if is_delivery else (turnover * 0.00025 if order.side == OrderSide.SELL else 0.0)
        exchange_charges = turnover * (self.exchange_charges_bps / 10000.0)
        gst = (brokerage + exchange_charges) * self.gst_rate
        stamp_duty = turnover * (self.stamp_duty_bps / 10000.0) if order.side == OrderSide.BUY else 0.0
        total_fees = brokerage + stt + exchange_charges + gst + stamp_duty

        # Update order
        order.executed_price = fill_price
        order.order_executed_timestamp = execution_timestamp
        order.status = OrderStatus.FILLED
        order.slippage_bps = self.default_slippage_bps
        order.slippage_amount = slippage_amt
        order.fees_amount = total_fees

        # Create PaperFill record
        fill = PaperFill(
            id=uuid4(),
            order_id=order.id,
            portfolio_id=order.portfolio_id,
            symbol=order.symbol,
            side=order.side,
            quantity=exec_qty,
            requested_price=market_price,
            fill_price=fill_price,
            slippage_bps=self.default_slippage_bps,
            slippage_amount=slippage_amt,
            brokerage=brokerage,
            stt=stt,
            exchange_charges=exchange_charges,
            gst=gst,
            stamp_duty=stamp_duty,
            total_fees=total_fees,
            execution_timestamp=execution_timestamp,
            source_observation_id=getattr(order, "source_observation_id", None),
            source_provider=getattr(order, "source_provider", "YAHOO_FINANCE"),
            source_provider_timestamp=getattr(order, "source_provider_timestamp", None),
            prediction_id=getattr(order, "prediction_id", None),
            signal_id=getattr(order, "signal_id", None),
            provenance_status=getattr(order, "provenance_status", "PROVENANCE_INCOMPLETE")
        )

        # Cash delta: Buyer pays turnover + fees; Seller receives turnover - fees
        if order.side == OrderSide.BUY:
            cash_impact = -(turnover + total_fees)
        else:
            cash_impact = turnover - total_fees

        return order, fill, cash_impact
