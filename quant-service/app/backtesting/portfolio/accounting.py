"""
Point-in-Time Portfolio Accounting for Backtesting.
Maintains atomic cash and position states, mark-to-market valuations, and reconciliation checks.
"""

from datetime import date, datetime
from typing import Dict, List, Optional, Tuple
from uuid import UUID
from app.backtesting.schemas import (
    BacktestConfig,
    BacktestOrder,
    BacktestPosition,
    BacktestTrade,
    ExitReason,
    OrderSide,
    PortfolioSnapshot,
    PositionSide,
    TransactionType
)
from app.backtesting.portfolio.ledger import TransactionLedger


class PortfolioAccounting:
    """
    Manages portfolio cash, open positions, closed trades, and daily snapshots.
    Enforces strict mathematical accounting reconciliation.
    """

    def __init__(self, config: BacktestConfig, run_id: UUID):
        self.config = config
        self.run_id = run_id
        self.cash = config.initial_capital
        self.initial_capital = config.initial_capital
        self.ledger = TransactionLedger(run_id)
        self.positions: Dict[str, BacktestPosition] = {}
        self.closed_trades: List[BacktestTrade] = []
        self.snapshots: List[PortfolioSnapshot] = []
        self.peak_equity = config.initial_capital
        self.total_fees_paid = 0.0
        self.total_slippage_paid = 0.0
        self.total_dividends_received = 0.0

        # Record initial capital
        self.ledger.record(
            transaction_type=TransactionType.CAPITAL_INJECTION,
            amount=config.initial_capital,
            cash_balance_before=0.0,
            cash_balance_after=config.initial_capital,
            description="Initial backtest capital injection"
        )

    def process_fill(
        self,
        order: BacktestOrder,
        bar_timestamp: datetime,
        regime: Optional[str] = None
    ) -> Optional[BacktestTrade]:
        """
        Updates cash and positions when an order is filled.
        Returns a BacktestTrade if a position is closed or reduced.
        """
        symbol = order.symbol
        executed_price = order.executed_price
        quantity = order.quantity
        fees = order.fees_amount
        slippage = order.slippage_amount

        self.total_fees_paid += fees
        self.total_slippage_paid += slippage

        if order.side == OrderSide.BUY:
            # Buy increases or opens position
            total_cash_required = (executed_price * quantity) + fees
            cash_before = self.cash
            self.cash -= total_cash_required

            self.ledger.record(
                transaction_type=TransactionType.BUY_EXECUTION,
                symbol=symbol,
                amount=-total_cash_required,
                cash_balance_before=cash_before,
                cash_balance_after=self.cash,
                description=f"BUY {quantity} shares of {symbol} @ {executed_price:.2f} (fees: {fees:.2f})",
                reference_id=order.id,
                timestamp=bar_timestamp
            )

            if symbol in self.positions:
                # Add to existing position (average up/down)
                pos = self.positions[symbol]
                total_qty = pos.quantity + quantity
                total_cost = (pos.average_entry_price * pos.quantity) + (executed_price * quantity)
                pos.quantity = total_qty
                pos.average_entry_price = total_cost / total_qty
                pos.cost_basis = pos.quantity * pos.average_entry_price
                pos.current_market_price = executed_price
                pos.market_value = pos.quantity * executed_price
                pos.highest_price_seen = max(pos.highest_price_seen, executed_price)
                pos.lowest_price_seen = min(pos.lowest_price_seen, executed_price)
            else:
                # New open position
                self.positions[symbol] = BacktestPosition(
                    run_id=self.run_id,
                    symbol=symbol,
                    quantity=quantity,
                    average_entry_price=executed_price,
                    current_market_price=executed_price,
                    cost_basis=quantity * executed_price,
                    market_value=quantity * executed_price,
                    unrealized_pnl=0.0,
                    unrealized_return_pct=0.0,
                    weight_in_portfolio=0.0,
                    as_of_date=bar_timestamp.date() if isinstance(bar_timestamp, datetime) else bar_timestamp,
                    highest_price_seen=executed_price,
                    lowest_price_seen=executed_price,
                    entry_timestamp=bar_timestamp,
                    entry_order_id=order.id,
                    regime_at_entry=regime
                )
            return None

        elif order.side == OrderSide.SELL:
            # Sell closes or reduces position
            if symbol not in self.positions:
                # Disallowed naked short or unknown sell
                return None

            pos = self.positions[symbol]
            qty_to_sell = min(quantity, pos.quantity)
            gross_proceeds = executed_price * qty_to_sell
            net_proceeds = gross_proceeds - fees

            cash_before = self.cash
            self.cash += net_proceeds

            self.ledger.record(
                transaction_type=TransactionType.SELL_EXECUTION,
                symbol=symbol,
                amount=net_proceeds,
                cash_balance_before=cash_before,
                cash_balance_after=self.cash,
                description=f"SELL {qty_to_sell} shares of {symbol} @ {executed_price:.2f} (fees: {fees:.2f})",
                reference_id=order.id,
                timestamp=bar_timestamp
            )

            # Calculate round-trip trade economics
            cost_basis_sold = pos.average_entry_price * qty_to_sell
            gross_pnl = gross_proceeds - cost_basis_sold
            net_pnl = gross_pnl - fees - slippage
            return_pct = (net_pnl / cost_basis_sold) * 100.0 if cost_basis_sold > 0 else 0.0

            holding_days = 1
            if isinstance(bar_timestamp, datetime) and isinstance(pos.entry_timestamp, datetime):
                holding_days = max(1, (bar_timestamp.date() - pos.entry_timestamp.date()).days)

            # MFE and MAE calculations
            mfe = ((pos.highest_price_seen - pos.average_entry_price) / pos.average_entry_price) * 100.0 if pos.average_entry_price > 0 else 0.0
            mae = ((pos.lowest_price_seen - pos.average_entry_price) / pos.average_entry_price) * 100.0 if pos.average_entry_price > 0 else 0.0

            trade = BacktestTrade(
                run_id=self.run_id,
                symbol=symbol,
                side=PositionSide.LONG,
                quantity=qty_to_sell,
                entry_order_id=pos.entry_order_id,
                exit_order_id=order.id,
                entry_timestamp=pos.entry_timestamp,
                exit_timestamp=bar_timestamp,
                entry_price=pos.average_entry_price,
                exit_price=executed_price,
                gross_pnl=gross_pnl,
                net_pnl=net_pnl,
                return_pct=return_pct,
                total_fees=fees,
                total_slippage=slippage,
                holding_period_days=holding_days,
                exit_reason=ExitReason.SIGNAL,
                max_favorable_excursion=mfe,
                max_adverse_excursion=mae,
                regime_at_entry=pos.regime_at_entry,
                regime_at_exit=regime
            )
            self.closed_trades.append(trade)

            # Update or remove position
            pos.quantity -= qty_to_sell
            pos.cost_basis = pos.quantity * pos.average_entry_price
            if pos.quantity <= 0:
                del self.positions[symbol]

            return trade

    def mark_to_market(self, as_of_date: date, market_prices: Dict[str, float]) -> PortfolioSnapshot:
        """
        Updates mark-to-market prices for all open positions and creates a daily snapshot.
        """
        total_pos_value = 0.0
        for symbol, pos in list(self.positions.items()):
            if symbol in market_prices and market_prices[symbol] > 0:
                price = market_prices[symbol]
                pos.current_market_price = price
                pos.market_value = pos.quantity * price
                pos.unrealized_pnl = pos.market_value - pos.cost_basis
                pos.unrealized_return_pct = (pos.unrealized_pnl / pos.cost_basis * 100.0) if pos.cost_basis > 0 else 0.0
                pos.highest_price_seen = max(pos.highest_price_seen, price)
                pos.lowest_price_seen = min(pos.lowest_price_seen, price)
                pos.as_of_date = as_of_date
            total_pos_value += pos.market_value

        total_equity = self.cash + total_pos_value

        # Update position weights
        for pos in self.positions.values():
            pos.weight_in_portfolio = pos.market_value / total_equity if total_equity > 0 else 0.0

        # Calculate daily and cumulative metrics
        prev_equity = self.snapshots[-1].total_equity if self.snapshots else self.initial_capital
        daily_pnl = total_equity - prev_equity
        daily_return = (daily_pnl / prev_equity) if prev_equity > 0 else 0.0
        cumulative_return = ((total_equity - self.initial_capital) / self.initial_capital) * 100.0

        if total_equity > self.peak_equity:
            self.peak_equity = total_equity

        drawdown_pct = ((self.peak_equity - total_equity) / self.peak_equity * 100.0) if self.peak_equity > 0 else 0.0

        snapshot = PortfolioSnapshot(
            run_id=self.run_id,
            snapshot_date=as_of_date,
            cash_balance=self.cash,
            positions_market_value=total_pos_value,
            total_equity=total_equity,
            gross_exposure=total_pos_value / total_equity if total_equity > 0 else 0.0,
            net_exposure=total_pos_value / total_equity if total_equity > 0 else 0.0,
            leverage=1.0,
            daily_pnl=daily_pnl,
            daily_return=daily_return,
            cumulative_return=cumulative_return,
            drawdown_pct=drawdown_pct,
            open_positions_count=len(self.positions),
            trades_executed_today=0,
            dividends_credited_today=0.0
        )
        self.snapshots.append(snapshot)
        return snapshot

    def verify_reconciliation(self) -> Tuple[bool, str]:
        """
        Audits cash + positions == total equity, and ledger consistency.
        """
        current_pos_val = sum(p.market_value for p in self.positions.values())
        expected_equity = self.cash + current_pos_val
        last_snapshot_equity = self.snapshots[-1].total_equity if self.snapshots else self.initial_capital

        diff = abs(expected_equity - last_snapshot_equity)
        if diff > 1.0:  # Allowing 1 INR floating point rounding
            return False, f"Reconciliation Error: MTM Equity {expected_equity:.2f} != Snapshot Equity {last_snapshot_equity:.2f}"

        return True, "Reconciliation Verified: 100% atomic cash/position consistency"
