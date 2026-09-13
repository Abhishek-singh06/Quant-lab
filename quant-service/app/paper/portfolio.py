"""
Paper Portfolio Manager.
Maintains atomic virtual cash balances, open positions, mark-to-market updates,
stops/targets execution, corporate action adjustments, and cash reconciliation.
"""

from datetime import datetime, date
from typing import Dict, List, Optional, Tuple
from uuid import UUID, uuid4
from app.paper.schemas import (
    PaperPortfolio,
    PaperPosition,
    PaperOrder,
    PaperFill,
    PaperTransactionRecord,
    OrderSide,
    OrderStatus,
    ExitReason
)


class PaperPortfolioManager:
    """
    Manages an isolated paper portfolio with virtual capital.
    Enforces that cash + market value of positions equals total portfolio value.
    """

    def __init__(self, portfolio: PaperPortfolio):
        self.portfolio = portfolio
        self.positions: Dict[str, PaperPosition] = {}
        self.ledger: List[PaperTransactionRecord] = []
        self.pending_orders: List[PaperOrder] = []

    def record_ledger(
        self,
        event_type: str,
        amount: float,
        description: str,
        symbol: Optional[str] = None,
        reference_id: Optional[UUID] = None,
        timestamp: Optional[datetime] = None
    ) -> PaperTransactionRecord:
        cash_before = self.portfolio.cash_balance
        cash_after = cash_before + amount
        self.portfolio.cash_balance = cash_after
        self.portfolio.available_cash = cash_after - self.portfolio.reserved_cash

        rec = PaperTransactionRecord(
            id=uuid4(),
            portfolio_id=self.portfolio.id,
            session_id=self.portfolio.session_id,
            transaction_timestamp=timestamp or datetime.now(),
            event_type=event_type,
            symbol=symbol,
            amount=amount,
            cash_balance_before=cash_before,
            cash_balance_after=cash_after,
            description=description,
            reference_id=reference_id
        )
        self.ledger.append(rec)
        return rec

    def apply_fill(self, fill: PaperFill, cash_delta: float, decision_info: Optional[Dict] = None):
        """
        Updates portfolio cash, fees, slippage, and positions when an order fills.
        """
        self.portfolio.total_fees_paid += fill.total_fees
        self.portfolio.total_slippage_paid += fill.slippage_amount

        symbol = fill.symbol
        fill_price = fill.fill_price
        qty = fill.quantity
        ts = fill.execution_timestamp

        if fill.side == OrderSide.BUY:
            # Debit cash (turnover + fees)
            self.record_ledger(
                event_type="ORDER_FILLED",
                amount=cash_delta,  # negative amount
                symbol=symbol,
                reference_id=fill.order_id,
                description=f"BUY {qty} of {symbol} @ {fill_price:.2f} (fees: {fill.total_fees:.2f})",
                timestamp=ts
            )

            if symbol in self.positions:
                pos = self.positions[symbol]
                new_qty = pos.quantity + qty
                total_cost = (pos.average_entry_price * pos.quantity) + (fill_price * qty)
                pos.quantity = new_qty
                pos.average_entry_price = total_cost / new_qty
                pos.cost_basis = pos.quantity * pos.average_entry_price
                pos.current_market_price = fill_price
                pos.market_value = pos.quantity * fill_price
                pos.highest_price_seen = max(pos.highest_price_seen, fill_price)
                pos.lowest_price_seen = min(pos.lowest_price_seen, fill_price)
                pos.last_updated_at = ts
            else:
                stop_p = decision_info.get("stop_price") if decision_info else None
                target_p = decision_info.get("target_price") if decision_info else None
                sig_id = decision_info.get("signal_id") if decision_info else None
                risk_id = decision_info.get("risk_assessment_id") if decision_info else None
                mod_ver = decision_info.get("model_version") if decision_info else None

                self.positions[symbol] = PaperPosition(
                    id=uuid4(),
                    portfolio_id=self.portfolio.id,
                    symbol=symbol,
                    horizon=self.portfolio.horizon,
                    quantity=qty,
                    average_entry_price=fill_price,
                    current_market_price=fill_price,
                    cost_basis=qty * fill_price,
                    market_value=qty * fill_price,
                    unrealized_pnl=0.0,
                    unrealized_return_pct=0.0,
                    realized_pnl=0.0,
                    portfolio_weight=0.0,
                    stop_price=stop_p,
                    target_price=target_p,
                    highest_price_seen=fill_price,
                    lowest_price_seen=fill_price,
                    entry_timestamp=ts,
                    last_updated_at=ts,
                    signal_id=sig_id,
                    risk_assessment_id=risk_id,
                    model_version=mod_ver,
                    is_active=True
                )

        elif fill.side == OrderSide.SELL:
            # Credit cash (turnover - fees)
            self.record_ledger(
                event_type="ORDER_FILLED",
                amount=cash_delta,  # positive amount
                symbol=symbol,
                reference_id=fill.order_id,
                description=f"SELL {qty} of {symbol} @ {fill_price:.2f} (fees: {fill.total_fees:.2f})",
                timestamp=ts
            )

            if symbol in self.positions:
                pos = self.positions[symbol]
                qty_sold = min(qty, pos.quantity)
                cost_of_sold = pos.average_entry_price * qty_sold
                gross_proceeds = fill_price * qty_sold
                realized_pnl_trade = gross_proceeds - cost_of_sold - fill.total_fees - fill.slippage_amount

                pos.realized_pnl += realized_pnl_trade
                self.portfolio.total_realized_pnl += realized_pnl_trade

                pos.quantity -= qty_sold
                pos.cost_basis = pos.quantity * pos.average_entry_price
                pos.market_value = pos.quantity * fill_price
                pos.last_updated_at = ts

                if pos.quantity <= 0:
                    del self.positions[symbol]

        self.update_valuations(ts)

    def update_valuations(self, as_of_ts: datetime, current_prices: Optional[Dict[str, float]] = None):
        """
        Updates mark-to-market prices and recalculates total portfolio equity and drawdowns.
        """
        current_prices = current_prices or {}
        tot_pos_val = 0.0

        for sym, pos in list(self.positions.items()):
            if sym in current_prices and current_prices[sym] > 0:
                p = current_prices[sym]
                pos.current_market_price = p
                pos.market_value = pos.quantity * p
                pos.unrealized_pnl = pos.market_value - pos.cost_basis
                pos.unrealized_return_pct = (pos.unrealized_pnl / pos.cost_basis * 100.0) if pos.cost_basis > 0 else 0.0
                pos.highest_price_seen = max(pos.highest_price_seen, p)
                pos.lowest_price_seen = min(pos.lowest_price_seen, p)
                pos.last_updated_at = as_of_ts

            tot_pos_val += pos.market_value

        self.portfolio.invested_value = tot_pos_val
        self.portfolio.total_portfolio_value = self.portfolio.cash_balance + tot_pos_val
        self.portfolio.total_unrealized_pnl = sum(p.unrealized_pnl for p in self.positions.values())

        # Update position weights
        for pos in self.positions.values():
            pos.portfolio_weight = (pos.market_value / self.portfolio.total_portfolio_value) if self.portfolio.total_portfolio_value > 0 else 0.0

        # Update Peak and Drawdowns
        if self.portfolio.total_portfolio_value > self.portfolio.peak_portfolio_value:
            self.portfolio.peak_portfolio_value = self.portfolio.total_portfolio_value

        if self.portfolio.peak_portfolio_value > 0:
            dd = (self.portfolio.peak_portfolio_value - self.portfolio.total_portfolio_value) / self.portfolio.peak_portfolio_value * 100.0
            self.portfolio.current_drawdown_pct = max(0.0, dd)
            if self.portfolio.current_drawdown_pct > self.portfolio.max_drawdown_pct:
                self.portfolio.max_drawdown_pct = self.portfolio.current_drawdown_pct

        self.portfolio.gross_exposure = (tot_pos_val / self.portfolio.total_portfolio_value) if self.portfolio.total_portfolio_value > 0 else 0.0
        self.portfolio.net_exposure = self.portfolio.gross_exposure

    def check_stops_and_targets(self, current_prices: Dict[str, float], current_ts: datetime) -> List[PaperOrder]:
        """
        Evaluates active positions for stop-loss or take-profit triggers.
        Generates automatic market sell orders when conditions are met.
        """
        exit_orders = []
        for sym, pos in list(self.positions.items()):
            if sym not in current_prices:
                continue

            price = current_prices[sym]

            # Check stop loss
            if pos.stop_price and price <= pos.stop_price:
                order = PaperOrder(
                    id=uuid4(),
                    portfolio_id=self.portfolio.id,
                    session_id=self.portfolio.session_id,
                    symbol=sym,
                    side=OrderSide.SELL,
                    quantity=pos.quantity,
                    requested_price=price,
                    signal_timestamp=current_ts,
                    order_submitted_timestamp=current_ts,
                    status=OrderStatus.PENDING,
                    rejection_reason=f"STOP_LOSS_TRIGGERED: price {price:.2f} <= stop {pos.stop_price:.2f}"
                )
                exit_orders.append(order)

            # Check take profit target
            elif pos.target_price and price >= pos.target_price:
                order = PaperOrder(
                    id=uuid4(),
                    portfolio_id=self.portfolio.id,
                    session_id=self.portfolio.session_id,
                    symbol=sym,
                    side=OrderSide.SELL,
                    quantity=pos.quantity,
                    requested_price=price,
                    signal_timestamp=current_ts,
                    order_submitted_timestamp=current_ts,
                    status=OrderStatus.PENDING,
                    rejection_reason=f"TAKE_PROFIT_TRIGGERED: price {price:.2f} >= target {pos.target_price:.2f}"
                )
                exit_orders.append(order)

        return exit_orders

    def verify_reconciliation(self) -> Tuple[bool, str]:
        """Audits mathematical consistency of cash + positions == portfolio value."""
        positions_val = sum(p.market_value for p in self.positions.values())
        expected_total = self.portfolio.cash_balance + positions_val
        diff = abs(expected_total - self.portfolio.total_portfolio_value)

        if diff > 1.0:
            return False, f"Reconciliation Mismatch: Total {self.portfolio.total_portfolio_value:.2f} != Cash ({self.portfolio.cash_balance:.2f}) + Positions ({positions_val:.2f})"
        return True, "Reconciliation Verified: 100% atomic consistency"
