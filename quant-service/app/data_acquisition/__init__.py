"""QuantLab Real Market Data Acquisition & Point-In-Time Ingestion Engine."""

from app.data_acquisition.models import (
    RawOHLCVRecord,
    AdjustedPriceRecord,
    CorporateActionRecord,
    InstrumentIdentity,
    FinancialStatementRecord,
    InstitutionalFlowRecord,
    DataValidationResult,
    IngestionRunMetadata,
    DatasetSnapshot,
    ExchangeEnum,
    CorporateActionTypeEnum,
    ValidationStatusEnum,
    IngestionStatusEnum,
)
from app.data_acquisition.providers.base import (
    BaseMarketDataProvider,
    ProviderCapability,
    ProviderUnavailableError,
    AuthenticationError,
    RateLimitError,
    MalformedResponseError,
)
from app.data_acquisition.providers.yahoo_adapter import YahooFinanceIndianMarketAdapter
from app.data_acquisition.providers.amfi_adapter import AMFIOpenDataAdapter
from app.data_acquisition.corporate_actions import CorporateActionAdjuster
from app.data_acquisition.instrument_master import InstrumentMasterRegistry
from app.data_acquisition.survivorship import HistoricalUniverseProvider
from app.data_acquisition.validator import DataQualityEngine
from app.data_acquisition.trading_calendar import IndianTradingCalendar
from app.data_acquisition.ingestion_pipeline import DataIngestionPipeline
from app.data_acquisition.readiness_gate import (
    DatasetReadinessGate,
    GateStatus,
    DatasetTier,
    ReadinessEvaluationReport,
)

__all__ = [
    "RawOHLCVRecord",
    "AdjustedPriceRecord",
    "CorporateActionRecord",
    "InstrumentIdentity",
    "FinancialStatementRecord",
    "InstitutionalFlowRecord",
    "DataValidationResult",
    "IngestionRunMetadata",
    "DatasetSnapshot",
    "ExchangeEnum",
    "CorporateActionTypeEnum",
    "ValidationStatusEnum",
    "IngestionStatusEnum",
    "BaseMarketDataProvider",
    "ProviderCapability",
    "ProviderUnavailableError",
    "AuthenticationError",
    "RateLimitError",
    "MalformedResponseError",
    "YahooFinanceIndianMarketAdapter",
    "AMFIOpenDataAdapter",
    "CorporateActionAdjuster",
    "InstrumentMasterRegistry",
    "HistoricalUniverseProvider",
    "DataQualityEngine",
    "IndianTradingCalendar",
    "DataIngestionPipeline",
    "DatasetReadinessGate",
    "GateStatus",
    "DatasetTier",
    "ReadinessEvaluationReport",
]
