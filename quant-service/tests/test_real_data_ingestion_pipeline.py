"""Tests for DataIngestionPipeline and Snapshot Management."""

import pytest
from datetime import date, datetime, timezone
import pandas as pd

from app.data_acquisition.models import (
    RawOHLCVRecord,
    CorporateActionRecord,
    CorporateActionTypeEnum,
    ExchangeEnum,
)
from app.data_acquisition.providers.base import (
    BaseMarketDataProvider,
    ProviderCapability,
    ProviderUnavailableError,
)
from app.data_acquisition.ingestion_pipeline import DataIngestionPipeline


class StubMarketDataProvider(BaseMarketDataProvider):
    """Deterministic stub provider for offline pipeline unit tests."""

    def __init__(self, available: bool = True):
        super().__init__(
            provider_name="STUB_PROVIDER",
            supported_capabilities={
                ProviderCapability.HISTORICAL_OHLCV,
                ProviderCapability.CORPORATE_ACTIONS,
            },
        )
        self._is_avail = available

    async def is_available(self) -> bool:
        return self._is_avail

    async def fetch_historical_ohlcv(self, symbol, start_date, end_date, exchange=ExchangeEnum.NSE, ingestion_run_id="RUN_01"):
        records = []
        d = start_date
        p = 500.0
        while d <= end_date:
            if d.weekday() < 5:
                dt = datetime(d.year, d.month, d.day, 10, 0, tzinfo=timezone.utc)
                records.append(
                    RawOHLCVRecord(
                        symbol=symbol,
                        exchange=exchange,
                        trading_date=d,
                        timestamp=dt,
                        open_price=p,
                        high_price=p + 5.0,
                        low_price=p - 5.0,
                        close_price=p + 2.0,
                        volume=100000,
                        source=self.provider_name,
                        ingestion_run_id=ingestion_run_id,
                    )
                )
                p += 2.0
            d = d.fromordinal(d.toordinal() + 1)
        return records

    async def fetch_corporate_actions(self, symbol, start_date, end_date, exchange=ExchangeEnum.NSE, ingestion_run_id="RUN_01"):
        return []


@pytest.mark.asyncio
async def test_ingestion_pipeline_success_and_snapshot():
    provider = StubMarketDataProvider(available=True)
    pipeline = DataIngestionPipeline(provider=provider, data_provider_mode="TEST")

    metadata, df, actions = await pipeline.run_ingestion(
        symbols=["TCS", "INFY"],
        start_date=date(2024, 1, 1),
        end_date=date(2024, 1, 10),
        exchange=ExchangeEnum.NSE,
        dataset_version_tag="TEST_SNAP_v1.0",
    )

    assert metadata.status.value == "COMPLETED"
    assert metadata.records_inserted > 0
    assert metadata.dataset_version == "TEST_SNAP_v1.0"
    assert metadata.checksum_sha256 is not None
    assert "TEST_SNAP_v1.0" in pipeline.snapshots

    assert not df.empty
    assert "adj_close" in df.columns
    assert "raw_close" in df.columns


@pytest.mark.asyncio
async def test_ingestion_pipeline_fail_closed_in_real_data_mode():
    """In REAL_DATA mode, pipeline MUST raise ProviderUnavailableError if provider is unavailable."""
    unavailable_provider = StubMarketDataProvider(available=False)
    pipeline = DataIngestionPipeline(provider=unavailable_provider, data_provider_mode="REAL_DATA")

    with pytest.raises(ProviderUnavailableError, match="not reachable or unconfigured in REAL_DATA mode"):
        await pipeline.run_ingestion(
            symbols=["RELIANCE"],
            start_date=date(2024, 1, 1),
            end_date=date(2024, 1, 10),
        )
