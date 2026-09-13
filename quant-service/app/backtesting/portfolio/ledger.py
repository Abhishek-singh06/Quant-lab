"""
Immutable transaction ledger for backtesting.
Tracks every debit, credit, fee, slippage, and dividend event.
"""

from typing import List
from uuid import UUID
from datetime import datetime
from app.backtesting.schemas import TransactionRecord, TransactionType


class TransactionLedger:
    """
    Maintains an append-only transaction ledger for complete backtest auditability.
    """

    def __init__(self, run_id: UUID):
        self.run_id = run_id
        self._entries: List[TransactionRecord] = []

    def record(
        self,
        transaction_type: TransactionType,
        amount: float,
        cash_balance_before: float,
        cash_balance_after: float,
        description: str,
        symbol: str = None,
        reference_id: UUID = None,
        timestamp: datetime = None
    ) -> TransactionRecord:
        entry = TransactionRecord(
            run_id=self.run_id,
            transaction_timestamp=timestamp or datetime.now(),
            transaction_type=transaction_type,
            symbol=symbol,
            amount=amount,
            cash_balance_before=cash_balance_before,
            cash_balance_after=cash_balance_after,
            description=description,
            reference_id=reference_id
        )
        self._entries.append(entry)
        return entry

    @property
    def entries(self) -> List[TransactionRecord]:
        return list(self._entries)

    def total_credits(self) -> float:
        return sum(e.amount for e in self._entries if e.amount > 0)

    def total_debits(self) -> float:
        return sum(abs(e.amount) for e in self._entries if e.amount < 0)
