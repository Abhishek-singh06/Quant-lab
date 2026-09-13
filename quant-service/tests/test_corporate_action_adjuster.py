"""Tests for Corporate Action Adjustment Engine."""

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
def sample_raw_series():
    # 4 consecutive trading days
    dates = [date(2024, 1, 1), date(2024, 1, 2), date(2024, 1, 3), date(2024, 1, 4)]
    prices = [1000.0, 1020.0, 520.0, 530.0]  # Note: split occurred on Jan 3 (2:1 split)
    records = []
    for d, p in zip(dates, prices):
        dt = datetime(d.year, d.month, d.day, 10, 0, tzinfo=timezone.utc)
        records.append(
            RawOHLCVRecord(
                symbol="SPLIT_TEST",
                exchange=ExchangeEnum.NSE,
                trading_date=d,
                timestamp=dt,
                open_price=p - 5.0,
                high_price=p + 10.0,
                low_price=p - 10.0,
                close_price=p,
                volume=100000,
                source="TEST_FEED",
                ingestion_run_id="RUN_ADJ_001",
            )
        )
    return records


class TestCorporateActionAdjuster:

    def test_split_adjustment_immutability_and_factors(self, sample_raw_series):
        adjuster = CorporateActionAdjuster(dividend_adjustment=False)

        # 2:1 stock split on 2024-01-03 (ratio 2:1 -> factor = 2.0)
        split_action = CorporateActionRecord(
            symbol="SPLIT_TEST",
            exchange=ExchangeEnum.NSE,
            action_type=CorporateActionTypeEnum.SPLIT,
            ex_date=date(2024, 1, 3),
            information_available_at=datetime(2024, 1, 2, 18, 0, tzinfo=timezone.utc),
            source="TEST_FEED",
            ingestion_run_id="RUN_ADJ_001",
            ratio_numerator=2.0,
            ratio_denominator=1.0,
            adjustment_factor=2.0,
        )

        adj_records = adjuster.adjust_series(sample_raw_series, [split_action])

        assert len(adj_records) == 4

        # Verify raw records were NOT mutated
        assert sample_raw_series[0].close_price == 1000.0
        assert sample_raw_series[1].close_price == 1020.0
        assert sample_raw_series[2].close_price == 520.0
        assert sample_raw_series[3].close_price == 530.0

        # Verify adjusted records:
        # Pre-split bars (Jan 1, Jan 2) must be divided by 2.0
        assert adj_records[0].adj_close == 500.0
        assert adj_records[1].adj_close == 510.0
        assert adj_records[0].cumulative_split_factor == 0.5
        assert adj_records[1].cumulative_split_factor == 0.5

        # Post-split bars (Jan 3, Jan 4) remain at raw levels
        assert adj_records[2].adj_close == 520.0
        assert adj_records[3].adj_close == 530.0
        assert adj_records[2].cumulative_split_factor == 1.0
        assert adj_records[3].cumulative_split_factor == 1.0

    def test_cash_dividend_total_return_adjustment(self, sample_raw_series):
        adjuster = CorporateActionAdjuster(dividend_adjustment=True)

        # Rs. 20 dividend on Jan 3 (previous close was Jan 2 at 1020.0)
        div_action = CorporateActionRecord(
            symbol="SPLIT_TEST",
            exchange=ExchangeEnum.NSE,
            action_type=CorporateActionTypeEnum.DIVIDEND,
            ex_date=date(2024, 1, 3),
            information_available_at=datetime(2024, 1, 2, 18, 0, tzinfo=timezone.utc),
            source="TEST_FEED",
            ingestion_run_id="RUN_ADJ_001",
            dividend_amount=20.0,
        )

        adj_records = adjuster.adjust_series(sample_raw_series, [div_action])
        assert len(adj_records) == 4

        # Div factor = (1020 - 20) / 1020 = 1000 / 1020 = 0.98039216
        expected_div_factor = 1000.0 / 1020.0
        np.testing.assert_allclose(adj_records[0].cumulative_dividend_factor, expected_div_factor, rtol=1e-5)
        np.testing.assert_allclose(adj_records[1].cumulative_dividend_factor, expected_div_factor, rtol=1e-5)
        assert adj_records[2].cumulative_dividend_factor == 1.0
        assert adj_records[3].cumulative_dividend_factor == 1.0
