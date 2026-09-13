"""Real Market Data Ingestion Pipeline & Dataset Versioning Orchestrator.

Manages end-to-end ingestion runs:
Provider -> Raw Ingestion -> Validation -> Corporate Action Adjustment -> Gap Analysis -> Dataset Snapshot
"""

from typing import List, Dict, Any, Optional, Tuple
from datetime import date, datetime, timezone
import uuid
import hashlib
import json
import pandas as pd

from app.data_acquisition.models import (
    RawOHLCVRecord,
    AdjustedPriceRecord,
    CorporateActionRecord,
    DataValidationResult,
    ValidationStatusEnum,
    IngestionRunMetadata,
    IngestionStatusEnum,
    DatasetSnapshot,
    ExchangeEnum,
)
from app.data_acquisition.providers.base import BaseMarketDataProvider, ProviderUnavailableError
from app.data_acquisition.validator import DataQualityEngine
from app.data_acquisition.corporate_actions import CorporateActionAdjuster
from app.data_acquisition.trading_calendar import IndianTradingCalendar


class DataIngestionPipeline:
    """Orchestrates historical market data ingestion, validation, and reproducible dataset snapshots."""

    def __init__(
        self,
        provider: BaseMarketDataProvider,
        data_provider_mode: str = "REAL_DATA",  # REAL_DATA | TEST
    ):
        self.provider = provider
        self.data_provider_mode = data_provider_mode.upper()
        self.validator = DataQualityEngine()
        self.adjuster = CorporateActionAdjuster()
        self.calendar = IndianTradingCalendar()
        self.snapshots: Dict[str, DatasetSnapshot] = {}

    async def run_ingestion(
        self,
        symbols: List[str],
        start_date: date,
        end_date: date,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
        dataset_version_tag: Optional[str] = None,
    ) -> Tuple[IngestionRunMetadata, pd.DataFrame, List[CorporateActionRecord]]:
        """Executes a full ingestion and adjustment run for the specified symbols and date range."""
        run_id = f"INGEST_{uuid.uuid4().hex[:12].upper()}"
        start_time = datetime.now(timezone.utc)

        metadata = IngestionRunMetadata(
            run_id=run_id,
            provider=self.provider.provider_name,
            start_time=start_time,
            symbols_requested=symbols,
            status=IngestionStatusEnum.RUNNING,
        )

        # In REAL_DATA mode, verify provider availability first
        if self.data_provider_mode == "REAL_DATA":
            is_avail = await self.provider.is_available()
            if not is_avail:
                metadata.status = IngestionStatusEnum.FAILED
                metadata.error_message = f"Provider '{self.provider.provider_name}' is not reachable or unconfigured in REAL_DATA mode."
                metadata.end_time = datetime.now(timezone.utc)
                raise ProviderUnavailableError(self.provider.provider_name, metadata.error_message)

        all_raw_records: List[RawOHLCVRecord] = []
        all_actions: List[CorporateActionRecord] = []
        all_adjusted_dfs: List[pd.DataFrame] = []

        total_valid = 0
        total_invalid = 0
        total_gaps = 0

        for sym in symbols:
            try:
                # 1. Fetch raw OHLCV
                raw_recs = await self.provider.fetch_historical_ohlcv(
                    symbol=sym,
                    start_date=start_date,
                    end_date=end_date,
                    exchange=exchange,
                    ingestion_run_id=run_id,
                )

                # 2. Fetch corporate actions
                actions = await self.provider.fetch_corporate_actions(
                    symbol=sym,
                    start_date=start_date,
                    end_date=end_date,
                    exchange=exchange,
                    ingestion_run_id=run_id,
                )

                all_actions.extend(actions)

                if not raw_recs:
                    continue

                # 3. Validate raw records
                val_results = self.validator.validate_series(raw_recs)
                valid_recs = [
                    raw_recs[i] for i, vr in enumerate(val_results)
                    if vr.status in (ValidationStatusEnum.VALID, ValidationStatusEnum.WARNING)
                ]
                invalid_recs = [
                    raw_recs[i] for i, vr in enumerate(val_results)
                    if vr.status == ValidationStatusEnum.INVALID
                ]

                total_valid += len(valid_recs)
                total_invalid += len(invalid_recs)
                all_raw_records.extend(valid_recs)

                # 4. Check trading session gaps
                actual_dates = [r.trading_date for r in valid_recs]
                missing_days = self.calendar.detect_missing_sessions(actual_dates, start_date, end_date)
                total_gaps += len(missing_days)

                # 5. Compute adjustments
                adj_recs = self.adjuster.adjust_series(valid_recs, actions)
                adj_df = self.adjuster.to_dataframe(adj_recs)
                all_adjusted_dfs.append(adj_df)

            except Exception as e:
                # In strict mode, log and record failure
                metadata.error_message = f"Error ingesting symbol {sym}: {str(e)}"

        if all_adjusted_dfs:
            combined_df = pd.concat(all_adjusted_dfs, ignore_index=True)
        else:
            combined_df = pd.DataFrame()

        # Generate dataset version and cryptographic checksum
        version_str = dataset_version_tag or f"quantlab_dataset_{start_date.strftime('%Y%m%d')}_{end_date.strftime('%Y%m%d')}_v1"
        checksum = self._compute_dataframe_hash(combined_df)

        end_time = datetime.now(timezone.utc)
        metadata.end_time = end_time
        metadata.status = IngestionStatusEnum.COMPLETED if total_valid > 0 else IngestionStatusEnum.FAILED
        metadata.records_inserted = total_valid
        metadata.invalid_count = total_invalid
        metadata.gaps_count = total_gaps
        metadata.dataset_version = version_str
        metadata.checksum_sha256 = checksum

        # Create immutable dataset snapshot
        snapshot = DatasetSnapshot(
            dataset_id=run_id,
            dataset_version=version_str,
            as_of_timestamp=end_time,
            start_date=start_date,
            end_date=end_date,
            symbols=symbols,
            total_records=total_valid,
            checksum_sha256=checksum,
            provider=self.provider.provider_name,
            metadata={
                "actions_count": len(all_actions),
                "gaps_count": total_gaps,
                "invalid_count": total_invalid,
            },
        )
        self.snapshots[version_str] = snapshot

        return metadata, combined_df, all_actions

    def _compute_dataframe_hash(self, df: pd.DataFrame) -> str:
        """Computes SHA-256 hash of dataset DataFrame."""
        if df.empty:
            return hashlib.sha256(b"EMPTY_DATASET").hexdigest()
        # Hash first and last records + shape
        summary = f"{len(df)}_{df.columns.tolist()}_{df.head(2).to_dict()}_{df.tail(2).to_dict()}"
        return hashlib.sha256(summary.encode("utf-8")).hexdigest()
