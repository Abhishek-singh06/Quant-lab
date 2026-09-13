"""Tests for Data Quality & Anomaly Detection Engine."""

import pytest
from datetime import date, datetime, timezone

from app.data_acquisition.models import RawOHLCVRecord, ExchangeEnum, ValidationStatusEnum
from app.data_acquisition.validator import DataQualityEngine


@pytest.fixture
def base_record():
    return RawOHLCVRecord(
        symbol="VALID_STOCK",
        exchange=ExchangeEnum.NSE,
        trading_date=date(2024, 6, 10),
        timestamp=datetime(2024, 6, 10, 10, 0, tzinfo=timezone.utc),
        open_price=100.0,
        high_price=105.0,
        low_price=98.0,
        close_price=102.0,
        volume=50000,
        source="TEST_FEED",
        ingestion_run_id="RUN_VAL_01",
    )


class TestDataQualityEngine:

    def test_valid_record_passes(self, base_record):
        engine = DataQualityEngine()
        res = engine.validate_record(base_record)
        assert res.status == ValidationStatusEnum.VALID

    def test_impossible_high_rejected(self, base_record):
        engine = DataQualityEngine()
        # High is lower than Open
        base_record.high_price = 95.0
        res = engine.validate_record(base_record)
        assert res.status == ValidationStatusEnum.INVALID
        assert res.error_category == "IMPOSSIBLE_HIGH"

    def test_impossible_low_rejected(self, base_record):
        engine = DataQualityEngine()
        # Low is higher than Close
        base_record.low_price = 104.0
        res = engine.validate_record(base_record)
        assert res.status == ValidationStatusEnum.INVALID
        assert res.error_category == "IMPOSSIBLE_LOW"

    def test_negative_volume_rejected(self, base_record):
        engine = DataQualityEngine()
        base_record.volume = -100
        res = engine.validate_record(base_record)
        assert res.status == ValidationStatusEnum.INVALID
        assert res.error_category == "NEGATIVE_VOLUME"

    def test_negative_price_rejected(self, base_record):
        engine = DataQualityEngine()
        base_record.close_price = -5.0
        res = engine.validate_record(base_record)
        assert res.status == ValidationStatusEnum.INVALID
        assert res.error_category == "NON_POSITIVE_PRICE"

    def test_duplicate_timestamps_rejected_in_series(self, base_record):
        engine = DataQualityEngine()
        # Create series with duplicate date
        r2 = RawOHLCVRecord(
            symbol=base_record.symbol,
            exchange=base_record.exchange,
            trading_date=base_record.trading_date,  # Duplicate date!
            timestamp=base_record.timestamp,
            open_price=101.0,
            high_price=106.0,
            low_price=99.0,
            close_price=103.0,
            volume=60000,
            source=base_record.source,
            ingestion_run_id=base_record.ingestion_run_id,
        )

        results = engine.validate_series([base_record, r2])
        assert len(results) == 2
        assert results[0].status == ValidationStatusEnum.VALID
        assert results[1].status == ValidationStatusEnum.INVALID
        assert results[1].error_category == "DUPLICATE_TIMESTAMP"
