"""Data Quality & Anomaly Detection Engine for Financial Time Series."""

from typing import List, Dict, Any, Optional
from datetime import date
import numpy as np

from app.data_acquisition.models import (
    RawOHLCVRecord,
    DataValidationResult,
    ValidationStatusEnum,
)


class DataQualityEngine:
    """Performs mathematical and econometric integrity checks on incoming market data records."""

    def __init__(
        self,
        max_single_day_jump_pct: float = 50.0,
        allow_zero_volume: bool = False,
    ):
        self.max_single_day_jump_pct = max_single_day_jump_pct
        self.allow_zero_volume = allow_zero_volume

    def validate_record(
        self,
        record: RawOHLCVRecord,
        previous_record: Optional[RawOHLCVRecord] = None,
    ) -> DataValidationResult:
        """Validates a single OHLCV record against price consistency and range rules."""
        sym = record.symbol
        t_date = record.trading_date

        # 1. Check positive prices
        if record.open_price <= 0 or record.high_price <= 0 or record.low_price <= 0 or record.close_price <= 0:
            return DataValidationResult(
                symbol=sym,
                trading_date=t_date,
                status=ValidationStatusEnum.INVALID,
                error_category="NON_POSITIVE_PRICE",
                reason=f"Prices must be strictly positive: O={record.open_price}, H={record.high_price}, L={record.low_price}, C={record.close_price}",
            )

        # 2. Check impossible OHLC (High >= max(Open, Close) and Low <= min(Open, Close))
        # Allow tiny epsilon for floating point representation
        eps = 1e-4
        if record.high_price < record.open_price - eps or record.high_price < record.close_price - eps:
            return DataValidationResult(
                symbol=sym,
                trading_date=t_date,
                status=ValidationStatusEnum.INVALID,
                error_category="IMPOSSIBLE_HIGH",
                reason=f"High price ({record.high_price}) cannot be less than Open ({record.open_price}) or Close ({record.close_price})",
            )

        if record.low_price > record.open_price + eps or record.low_price > record.close_price + eps:
            return DataValidationResult(
                symbol=sym,
                trading_date=t_date,
                status=ValidationStatusEnum.INVALID,
                error_category="IMPOSSIBLE_LOW",
                reason=f"Low price ({record.low_price}) cannot be greater than Open ({record.open_price}) or Close ({record.close_price})",
            )

        # 3. Check negative volume
        if record.volume < 0:
            return DataValidationResult(
                symbol=sym,
                trading_date=t_date,
                status=ValidationStatusEnum.INVALID,
                error_category="NEGATIVE_VOLUME",
                reason=f"Volume cannot be negative: {record.volume}",
            )

        # 4. Check zero volume warning
        if record.volume == 0 and not self.allow_zero_volume:
            return DataValidationResult(
                symbol=sym,
                trading_date=t_date,
                status=ValidationStatusEnum.WARNING,
                error_category="ZERO_VOLUME",
                reason="Zero volume recorded on active trading session.",
            )

        # 5. Check extreme price jump vs previous day
        if previous_record is not None and previous_record.close_price > 0:
            pct_change = abs(record.close_price - previous_record.close_price) / previous_record.close_price * 100.0
            if pct_change > self.max_single_day_jump_pct:
                return DataValidationResult(
                    symbol=sym,
                    trading_date=t_date,
                    status=ValidationStatusEnum.WARNING,
                    error_category="EXTREME_PRICE_JUMP",
                    reason=f"Single-day price change of {pct_change:.1f}% exceeds threshold of {self.max_single_day_jump_pct}% (check for unadjusted corporate action).",
                    details={"pct_change": pct_change, "prev_close": previous_record.close_price, "curr_close": record.close_price},
                )

        return DataValidationResult(
            symbol=sym,
            trading_date=t_date,
            status=ValidationStatusEnum.VALID,
        )

    def validate_series(self, records: List[RawOHLCVRecord]) -> List[DataValidationResult]:
        """Validates an entire time series of OHLCV records."""
        if not records:
            return []

        results: List[DataValidationResult] = []
        seen_dates = set()
        prev = None

        for r in sorted(records, key=lambda x: x.trading_date):
            # Check duplicate timestamp
            if r.trading_date in seen_dates:
                results.append(
                    DataValidationResult(
                        symbol=r.symbol,
                        trading_date=r.trading_date,
                        status=ValidationStatusEnum.INVALID,
                        error_category="DUPLICATE_TIMESTAMP",
                        reason=f"Duplicate record detected for trading date {r.trading_date}.",
                    )
                )
                continue

            seen_dates.add(r.trading_date)
            res = self.validate_record(r, previous_record=prev)
            results.append(res)
            prev = r

        return results
