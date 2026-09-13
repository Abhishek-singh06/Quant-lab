"""
Transaction cost models for Indian equities (STT, brokerage, exchange charges, GST, stamp duty).
"""

from typing import Dict, Any
from app.backtesting.schemas import OrderSide, CostModelType, BacktestConfig


class TransactionCostModel:
    """
    Computes precise transaction costs for Indian market trades.
    Components:
    - Brokerage: percentage bps (e.g. 3 bps)
    - STT (Securities Transaction Tax):
      * Delivery: 0.1% (10 bps) on both Buy and Sell
      * Intraday: 0.025% (2.5 bps) on Sell only
    - Exchange Turnover Charges: ~0.00345% (0.345 bps) on NSE
    - GST: 18% on (Brokerage + Exchange Charges)
    - Stamp Duty: 0.015% (1.5 bps) on Buy only
    - SEBI Turnover Charges: ~0.0001% (0.01 bps)
    """

    def __init__(self, config: BacktestConfig):
        self.config = config
        self.cost_model_type = config.cost_model_type

    def calculate_cost(
        self,
        side: OrderSide,
        price: float,
        quantity: int,
        is_delivery: bool = True
    ) -> Dict[str, float]:
        if self.cost_model_type == CostModelType.ZERO:
            return {
                "brokerage": 0.0,
                "stt": 0.0,
                "exchange_charges": 0.0,
                "gst": 0.0,
                "stamp_duty": 0.0,
                "sebi_charges": 0.0,
                "total_fees": 0.0,
            }

        turnover = price * quantity

        if self.cost_model_type == CostModelType.FIXED_BPS:
            total_fees = turnover * (self.config.brokerage_bps / 10000.0)
            return {
                "brokerage": total_fees,
                "stt": 0.0,
                "exchange_charges": 0.0,
                "gst": 0.0,
                "stamp_duty": 0.0,
                "sebi_charges": 0.0,
                "total_fees": total_fees,
            }

        # REALISTIC_INDIAN model
        # 1. Brokerage
        brokerage = turnover * (self.config.brokerage_bps / 10000.0)

        # 2. STT
        stt = 0.0
        if is_delivery:
            # 10 bps on both Buy and Sell
            stt = turnover * (self.config.stt_delivery_bps / 10000.0)
        else:
            # Intraday STT only on Sell
            if side == OrderSide.SELL:
                stt = turnover * (self.config.stt_intraday_bps / 10000.0)

        # 3. Exchange charges
        exchange_charges = turnover * (self.config.exchange_charges_bps / 10000.0)

        # 4. GST on (Brokerage + Exchange Charges)
        gst = (brokerage + exchange_charges) * self.config.gst_rate

        # 5. Stamp Duty (on Buy orders only)
        stamp_duty = 0.0
        if side == OrderSide.BUY:
            stamp_duty = turnover * (self.config.stamp_duty_bps / 10000.0)

        # 6. SEBI Turnover charges (approx 10 INR per crore = 0.01 bps)
        sebi_charges = turnover * 0.000001

        total_fees = brokerage + stt + exchange_charges + gst + stamp_duty + sebi_charges

        return {
            "brokerage": brokerage,
            "stt": stt,
            "exchange_charges": exchange_charges,
            "gst": gst,
            "stamp_duty": stamp_duty,
            "sebi_charges": sebi_charges,
            "total_fees": total_fees,
        }
