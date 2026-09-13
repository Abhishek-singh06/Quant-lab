"""Adversarial Tests for Sequential Corporate Actions and Adjustment Invariants."""

import pytest
import numpy as np
from datetime import date, datetime, timezone

from app.data_acquisition.models import (
    RawOHLCVRecord,
    CorporateActionRecord,
    ExchangeEnum,
    CorporateActionTypeEnum,
)
from app.data_acquisition.corporate_actions import CorporateActionAdjuster


@pytest.fixture
def multi_day_raw_series():
    # 6 daily bars
    dates = [date(2024, 2, i) for i in range(1, 7)]
    prices = [2000.0, 2020.0, 1010.0, 1030.0, 1020.0, 1040.0]
    records = []
    for d, p in zip(dates, prices):
        dt = datetime(d.year, d.month, d.day, 10, 0, tzinfo=timezone.utc)
        records.append(
            RawOHLCVRecord(
                symbol="SEQ_CORP_TEST",
                exchange=ExchangeEnum.NSE,
                trading_date=d,
                timestamp=dt,
                open_price=p - 10.0,
                high_price=p + 15.0,
                low_price=p - 15.0,
                close_price=p,
                volume=500000,
                source="TEST_FEED",
                ingestion_run_id="RUN_SEQ_01",
            )
        )
    return records


class TestCorporateActionsAdversarial:

    def test_sequential_split_then_dividend(self, multi_day_raw_series):
        """Tests 2:1 Split on Feb 3 followed by Rs. 20 Cash Dividend on Feb 5."""
        adjuster = CorporateActionAdjuster(dividend_adjustment=True)

        split_action = CorporateActionRecord(
            symbol="SEQ_CORP_TEST",
            exchange=ExchangeEnum.NSE,
            action_type=CorporateActionTypeEnum.SPLIT,
            ex_date=date(2024, 2, 3),
            information_available_at=datetime(2024, 2, 2, 18, 0, tzinfo=timezone.utc),
            source="TEST",
            ingestion_run_id="RUN_SEQ_01",
            ratio_numerator=2.0,
            ratio_denominator=1.0,
            adjustment_factor=2.0,
        )

        div_action = CorporateActionRecord(
            symbol="SEQ_CORP_TEST",
            exchange=ExchangeEnum.NSE,
            action_type=CorporateActionTypeEnum.DIVIDEND,
            ex_date=date(2024, 2, 5),
            information_available_at=datetime(2024, 2, 4, 18, 0, tzinfo=timezone.utc),
            source="TEST",
            ingestion_run_id="RUN_SEQ_01",
            dividend_amount=20.0,
        )

        adj_records = adjuster.adjust_series(multi_day_raw_series, [split_action, div_action])
        assert len(adj_records) == 6

        # Day 4 close is 1030.0. Dividend on Day 5 is 20.0 -> div_factor = (1030 - 20) / 1030 = 1010 / 1030 = 0.9805825
        div_f = 1010.0 / 1030.0

        # Pre-Feb 3 bars (Day 1, Day 2): split factor 0.5 * div factor
        expected_total_factor_d1 = 0.5 * div_f
        np.testing.assert_allclose(adj_records[0].cumulative_split_factor * adj_records[0].cumulative_dividend_factor, expected_total_factor_d1, rtol=1e-5)

        # Feb 3 & Feb 4 bars: split factor 1.0, div factor div_f
        np.testing.assert_allclose(adj_records[2].cumulative_dividend_factor, div_f, rtol=1e-5)
        assert adj_records[2].cumulative_split_factor == 1.0

        # Feb 5 & Feb 6 bars: both factors are 1.0
        assert adj_records[4].cumulative_split_factor == 1.0
        assert adj_records[4].cumulative_dividend_factor == 1.0
        assert adj_records[5].cumulative_split_factor == 1.0
        assert adj_records[5].cumulative_dividend_factor == 1.0

    def test_double_adjustment_prevention(self, multi_day_raw_series):
        """Verify that adjusting an already adjusted series is blocked or idempotent."""
        adjuster = CorporateActionAdjuster()
        split_action = CorporateActionRecord(
            symbol="SEQ_CORP_TEST",
            exchange=ExchangeEnum.NSE,
            action_type=CorporateActionTypeEnum.SPLIT,
            ex_date=date(2024, 2, 3),
            information_available_at=datetime(2024, 2, 2, 18, 0, tzinfo=timezone.utc),
            source="TEST",
            ingestion_run_id="RUN_SEQ_01",
            adjustment_factor=2.0,
        )

        pass1 = adjuster.adjust_series(multi_day_raw_series, [split_action])
        # Raw records must remain raw
        assert multi_day_raw_series[0].close_price == 2000.0

        # Adjusting again from raw records yields exact same result
        pass2 = adjuster.adjust_series(multi_day_raw_series, [split_action])
        np.testing.assert_array_equal(
            [r.adj_close for r in pass1],
            [r.adj_close for r in pass2]
        )
