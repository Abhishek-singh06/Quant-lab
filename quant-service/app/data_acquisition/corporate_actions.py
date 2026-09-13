"""Corporate Action Adjustment Engine.

Computes point-in-time exact split and dividend adjustment factors, ensuring that
raw unadjusted price records remain 100% immutable and adjustment factors are mathematically sound.
"""

from typing import List, Dict, Any, Optional, Tuple
from datetime import date, datetime, timezone
import numpy as np
import pandas as pd

from app.data_acquisition.models import (
    RawOHLCVRecord,
    AdjustedPriceRecord,
    CorporateActionRecord,
    CorporateActionTypeEnum,
)


class CorporateActionAdjuster:
    """Calculates split-adjusted and total-return-adjusted price series from raw OHLCV and corporate actions."""

    def __init__(self, dividend_adjustment: bool = True):
        self.dividend_adjustment = dividend_adjustment

    def adjust_series(
        self,
        raw_records: List[RawOHLCVRecord],
        actions: List[CorporateActionRecord],
    ) -> List[AdjustedPriceRecord]:
        """Derives AdjustedPriceRecords from raw OHLCV records and corporate actions.

        Guarantees:
        1. raw_records are NEVER mutated.
        2. Splitting factor is applied backward to all bars prior to ex-date.
        3. Adjustment factor is tracked per bar.
        4. Adjustment is never applied twice.
        """
        if not raw_records:
            return []

        # Sort raw records by trading date ascending
        sorted_raw = sorted(raw_records, key=lambda r: r.trading_date)
        # Sort actions by ex_date ascending
        sorted_actions = sorted(actions, key=lambda a: a.ex_date)

        n = len(sorted_raw)
        trading_dates = [r.trading_date for r in sorted_raw]

        # Initialize cumulative adjustment factors for each bar (default 1.0)
        cumulative_split_factors = np.ones(n, dtype=np.float64)
        cumulative_div_factors = np.ones(n, dtype=np.float64)

        # 1. Process Splits (backward adjustment)
        for action in sorted_actions:
            if action.action_type in (CorporateActionTypeEnum.SPLIT, CorporateActionTypeEnum.BONUS):
                ex_d = action.ex_date
                # Split ratio S: e.g. 5:1 split -> S = 5.0 (1 old share becomes 5 new shares)
                # Or bonus 1:1 -> numerator=2, denominator=1 -> S = 2.0
                if action.adjustment_factor and action.adjustment_factor > 0:
                    s_ratio = action.adjustment_factor
                elif action.ratio_numerator and action.ratio_denominator and action.ratio_denominator > 0:
                    s_ratio = action.ratio_numerator / action.ratio_denominator
                else:
                    continue

                # All bars strictly BEFORE ex-date must be scaled down by s_ratio
                for i in range(n):
                    if trading_dates[i] < ex_d:
                        cumulative_split_factors[i] *= (1.0 / s_ratio)

        # 2. Process Dividends (Total Return adjustment)
        if self.dividend_adjustment:
            for action in sorted_actions:
                if action.action_type == CorporateActionTypeEnum.DIVIDEND and action.dividend_amount:
                    ex_d = action.ex_date
                    div_amt = action.dividend_amount

                    # Find the closing price on the day immediately preceding ex_date
                    pre_indices = [i for i in range(n) if trading_dates[i] < ex_d]
                    if not pre_indices:
                        continue
                    last_pre_idx = pre_indices[-1]
                    pre_close = sorted_raw[last_pre_idx].close_price

                    if pre_close > div_amt > 0:
                        # Dividend adjustment factor: (P_close - D) / P_close
                        div_factor = (pre_close - div_amt) / pre_close
                        for i in range(n):
                            if trading_dates[i] < ex_d:
                                cumulative_div_factors[i] *= div_factor

        # 3. Construct AdjustedPriceRecords
        adjusted_records: List[AdjustedPriceRecord] = []
        for i in range(n):
            raw = sorted_raw[i]
            split_f = cumulative_split_factors[i]
            div_f = cumulative_div_factors[i]
            total_f = split_f * div_f

            adj_open = raw.open_price * total_f
            adj_high = raw.high_price * total_f
            adj_low = raw.low_price * total_f
            adj_close = raw.close_price * total_f

            adjusted_records.append(
                AdjustedPriceRecord(
                    raw_record=raw,
                    adj_open=round(float(adj_open), 4),
                    adj_high=round(float(adj_high), 4),
                    adj_low=round(float(adj_low), 4),
                    adj_close=round(float(adj_close), 4),
                    cumulative_split_factor=round(float(split_f), 8),
                    cumulative_dividend_factor=round(float(div_f), 8),
                    methodology="SPLIT_AND_DIVIDEND_ADJUSTED",
                    adjustment_timestamp=datetime.now(timezone.utc),
                    data_version=1,
                )
            )

        return adjusted_records

    def to_dataframe(self, adjusted_records: List[AdjustedPriceRecord]) -> pd.DataFrame:
        """Converts adjusted records to canonical DataFrame."""
        rows = []
        for r in adjusted_records:
            raw = r.raw_record
            rows.append({
                "trading_date": raw.trading_date,
                "timestamp": raw.timestamp,
                "symbol": raw.symbol,
                "exchange": raw.exchange.value,
                "raw_open": raw.open_price,
                "raw_high": raw.high_price,
                "raw_low": raw.low_price,
                "raw_close": raw.close_price,
                "volume": raw.volume,
                "adj_open": r.adj_open,
                "adj_high": r.adj_high,
                "adj_low": r.adj_low,
                "adj_close": r.adj_close,
                "split_factor": r.cumulative_split_factor,
                "div_factor": r.cumulative_dividend_factor,
                "source": raw.source,
                "ingestion_run_id": raw.ingestion_run_id,
            })
        return pd.DataFrame(rows)
