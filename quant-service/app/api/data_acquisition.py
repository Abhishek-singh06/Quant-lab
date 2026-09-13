"""FastAPI Router for Real Market Data Acquisition & Ingestion."""

from typing import Dict, Any, List, Optional
from fastapi import APIRouter, HTTPException, Query, Body
from pydantic import BaseModel, Field
from datetime import date, datetime, timedelta

from app.data_acquisition.providers.yahoo_adapter import YahooFinanceIndianMarketAdapter
from app.data_acquisition.providers.amfi_adapter import AMFIOpenDataAdapter
from app.data_acquisition.ingestion_pipeline import DataIngestionPipeline
from app.data_acquisition.survivorship import HistoricalUniverseProvider
from app.data_acquisition.instrument_master import InstrumentMasterRegistry
from app.data_acquisition.validator import DataQualityEngine
from app.data_acquisition.models import ExchangeEnum

router = APIRouter(prefix="/api/v1/data-acquisition", tags=["data-acquisition"])

# Shared singleton instances
_universe_provider = HistoricalUniverseProvider()
_instrument_registry = InstrumentMasterRegistry()
_quality_engine = DataQualityEngine()
_yahoo_adapter = YahooFinanceIndianMarketAdapter()
_ingestion_pipeline = DataIngestionPipeline(provider=_yahoo_adapter)


class IngestRequest(BaseModel):
    symbols: List[str] = Field(default=["RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK"], description="List of NSE equity symbols")
    start_date: str = Field(default="2024-01-01", description="Start date YYYY-MM-DD")
    end_date: str = Field(default="2024-12-31", description="End date YYYY-MM-DD")
    exchange: str = Field(default="NSE", description="Exchange: NSE or BSE")
    dataset_version_tag: Optional[str] = Field(default=None, description="Optional custom dataset version tag")


@router.get("/status")
async def get_provider_status() -> Dict[str, Any]:
    """Returns live health and connectivity status of configured data providers."""
    is_yahoo_avail = await _yahoo_adapter.is_available()
    amfi_adapter = AMFIOpenDataAdapter()
    is_amfi_avail = await amfi_adapter.is_available()

    return {
        "status": "success",
        "providers": {
            "YAHOO_FINANCE": {
                "available": is_yahoo_avail,
                "domain": "NSE/BSE Equities & Corporate Actions",
                "mode": "REAL_DATA",
            },
            "AMFI_INDIA": {
                "available": is_amfi_avail,
                "domain": "Mutual Fund Historical NAVs & Scheme Master",
                "mode": "REAL_DATA",
            },
            "NSE_OFFICIAL": {
                "configured": False,
                "reason": "Requires MARKET_DATA_API_KEY environment variable.",
                "mode": "LICENSED",
            },
        },
        "safeguards": {
            "fail_closed_mode": True,
            "zero_synthetic_fallback": True,
            "raw_adjusted_separation": True,
            "point_in_time_tracking": True,
            "survivorship_safe_universe": True,
        }
    }


@router.get("/universe")
def get_historical_universe(
    as_of: str = Query(default="2020-01-01", description="As-of date YYYY-MM-DD"),
    index_name: str = Query(default="NIFTY_50", description="Index name"),
) -> Dict[str, Any]:
    """Retrieves survivorship-bias-safe point-in-time index constituent universe."""
    try:
        as_of_date = datetime.strptime(as_of, "%Y-%m-%d").date()
        symbols = _universe_provider.universe(as_of=as_of_date, index_name=index_name)
        delisted_or_removed = _universe_provider.get_delisted_or_removed_members(as_of=as_of_date, index_name=index_name)

        return {
            "status": "success",
            "index_name": index_name,
            "as_of_date": as_of_date.isoformat(),
            "constituent_count": len(symbols),
            "constituents": symbols,
            "historical_exits_included": delisted_or_removed,
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.post("/ingest")
async def trigger_ingestion(req: IngestRequest) -> Dict[str, Any]:
    """Runs data ingestion pipeline fetching and adjusting real market data."""
    try:
        s_date = datetime.strptime(req.start_date, "%Y-%m-%d").date()
        e_date = datetime.strptime(req.end_date, "%Y-%m-%d").date()
        exch = ExchangeEnum.BSE if req.exchange.upper() == "BSE" else ExchangeEnum.NSE

        metadata, adj_df, actions = await _ingestion_pipeline.run_ingestion(
            symbols=req.symbols,
            start_date=s_date,
            end_date=e_date,
            exchange=exch,
            dataset_version_tag=req.dataset_version_tag,
        )

        return {
            "status": "success",
            "run_id": metadata.run_id,
            "provider": metadata.provider,
            "dataset_version": metadata.dataset_version,
            "records_inserted": metadata.records_inserted,
            "invalid_count": metadata.invalid_count,
            "gaps_count": metadata.gaps_count,
            "corporate_actions_count": len(actions),
            "checksum_sha256": metadata.checksum_sha256,
            "sample_records": adj_df.head(5).to_dict(orient="records") if not adj_df.empty else [],
        }
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))


@router.get("/snapshots")
def list_dataset_snapshots() -> Dict[str, Any]:
    """Lists registered immutable dataset snapshots."""
    snapshots_list = [
        {
            "dataset_id": s.dataset_id,
            "dataset_version": s.dataset_version,
            "as_of_timestamp": s.as_of_timestamp.isoformat(),
            "start_date": s.start_date.isoformat(),
            "end_date": s.end_date.isoformat(),
            "symbols_count": len(s.symbols),
            "total_records": s.total_records,
            "checksum_sha256": s.checksum_sha256,
            "provider": s.provider,
        }
        for s in _ingestion_pipeline.snapshots.values()
    ]
    return {
        "status": "success",
        "snapshots": snapshots_list,
    }
