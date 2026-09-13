"""Tests for Data Acquisition Models and Raw/Adjusted Separation."""

import pytest
from datetime import date, datetime, timezone

from app.data_acquisition.models import (
    RawOHLCVRecord,
    AdjustedPriceRecord,
    CorporateActionRecord,
    ExchangeEnum,
    CorporateActionTypeEnum,
)


def test_raw_ohlcv_record_immutability_and_dict():
    dt = datetime(2024, 5, 15, 10, 0, 0, tzinfo=timezone.utc)
    raw = RawOHLCVRecord(
        symbol="RELIANCE",
        exchange=ExchangeEnum.NSE,
        trading_date=date(2024, 5, 15),
        timestamp=dt,
        open_price=2800.0,
        high_price=2850.0,
        low_price=2790.0,
        close_price=2840.0,
        volume=5000000,
        source="YAHOO_FINANCE",
        ingestion_run_id="RUN_TEST_001",
    )

    d = raw.to_dict()
    assert d["symbol"] == "RELIANCE"
    assert d["exchange"] == "NSE"
    assert d["open"] == 2800.0
    assert d["close"] == 2840.0
    assert d["volume"] == 5000000
    assert d["ingestion_run_id"] == "RUN_TEST_001"


def test_corporate_action_record():
    dt = datetime(2023, 10, 10, 9, 15, 0, tzinfo=timezone.utc)
    action = CorporateActionRecord(
        symbol="TCS",
        exchange=ExchangeEnum.NSE,
        action_type=CorporateActionTypeEnum.DIVIDEND,
        ex_date=date(2023, 10, 19),
        information_available_at=dt,
        source="YAHOO_FINANCE",
        ingestion_run_id="RUN_002",
        dividend_amount=9.0,
    )

    assert action.symbol == "TCS"
    assert action.action_type == CorporateActionTypeEnum.DIVIDEND
    assert action.dividend_amount == 9.0
    assert action.information_available_at == dt
