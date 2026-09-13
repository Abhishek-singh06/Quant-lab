"""
Corporate Action Processor for Backtesting (Splits, Bonus Shares, Cash Dividends).
Guarantees Point-in-Time safety on Ex-Dates and preserves economic value continuity.
"""

from datetime import date
from typing import Dict, List, Tuple
from uuid import UUID
from app.backtesting.schemas import BacktestPosition, TransactionType, TransactionRecord


class CorporateActionProcessor:
    """
    Applies splits, bonus shares, and cash dividends to live backtest positions on Ex-Date.
    """

    @staticmethod
    def process_corporate_actions(
        as_of_date: date,
        open_positions: Dict[str, BacktestPosition],
        corporate_actions: List[Dict],  # list of action dicts {symbol, action_type, ratio, dividend_amount, ex_date}
        run_id: UUID,
        cash_balance: float
    ) -> Tuple[Dict[str, BacktestPosition], float, float, List[TransactionRecord]]:
        """
        Returns (updated_positions, new_cash_balance, total_dividends_today, ledger_records).
        """
        dividends_today = 0.0
        ledger_records = []
        current_cash = cash_balance

        for ca in corporate_actions:
            ex_date = ca.get("ex_date")
            if isinstance(ex_date, str):
                ex_date = date.fromisoformat(ex_date)

            if ex_date != as_of_date:
                continue

            symbol = ca.get("symbol")
            if symbol not in open_positions:
                continue

            pos = open_positions[symbol]
            action_type = ca.get("action_type", "").upper()

            if action_type == "SPLIT":
                # Split ratio e.g. 2.0 (2-for-1 split: shares * 2, price / 2)
                ratio = float(ca.get("ratio", 1.0))
                if ratio > 0:
                    pos.quantity = int(pos.quantity * ratio)
                    pos.average_entry_price = pos.average_entry_price / ratio
                    pos.cost_basis = pos.quantity * pos.average_entry_price
                    pos.highest_price_seen = pos.highest_price_seen / ratio
                    pos.lowest_price_seen = pos.lowest_price_seen / ratio

            elif action_type == "BONUS":
                # Bonus ratio e.g. 1.0 (1 bonus share for every 1 existing: multiplier = 1 + 1 = 2)
                bonus_ratio = float(ca.get("ratio", 1.0))
                multiplier = 1.0 + bonus_ratio
                if multiplier > 0:
                    pos.quantity = int(pos.quantity * multiplier)
                    pos.average_entry_price = pos.average_entry_price / multiplier
                    pos.cost_basis = pos.quantity * pos.average_entry_price

            elif action_type == "DIVIDEND":
                dps = float(ca.get("dividend_amount", 0.0))
                if dps > 0:
                    total_div = dps * pos.quantity
                    dividends_today += total_div
                    before = current_cash
                    current_cash += total_div

                    ledger_records.append(
                        TransactionRecord(
                            run_id=run_id,
                            transaction_timestamp=as_of_date,
                            transaction_type=TransactionType.DIVIDEND_CREDIT,
                            symbol=symbol,
                            amount=total_div,
                            cash_balance_before=before,
                            cash_balance_after=current_cash,
                            description=f"Dividend credited for {symbol}: INR {dps}/share on {pos.quantity} shares"
                        )
                    )

        return open_positions, current_cash, dividends_today, ledger_records
