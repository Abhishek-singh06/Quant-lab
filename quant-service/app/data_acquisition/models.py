"""Data Models for Real Indian Market Data Acquisition and Point-In-Time Warehouse."""

from typing import List, Dict, Any, Optional, Union
from dataclasses import dataclass, field
from datetime import datetime, date, timezone
from enum import Enum
import uuid
import hashlib
import json


class ExchangeEnum(str, Enum):
    NSE = "NSE"
    BSE = "BSE"
    MCX = "MCX"
    GLOBAL = "GLOBAL"


class CorporateActionTypeEnum(str, Enum):
    SPLIT = "SPLIT"
    BONUS = "BONUS"
    DIVIDEND = "DIVIDEND"
    RIGHTS = "RIGHTS"
    MERGER = "MERGER"
    DEMERGER = "DEMERGER"
    SYMBOL_CHANGE = "SYMBOL_CHANGE"
    CAPITAL_REDUCTION = "CAPITAL_REDUCTION"


class ValidationStatusEnum(str, Enum):
    VALID = "VALID"
    WARNING = "WARNING"
    INVALID = "INVALID"


class IngestionStatusEnum(str, Enum):
    STARTED = "STARTED"
    RUNNING = "RUNNING"
    COMPLETED = "COMPLETED"
    PARTIAL = "PARTIAL"
    FAILED = "FAILED"


@dataclass
class RawOHLCVRecord:
    """Immutable raw unadjusted market price record."""
    symbol: str
    exchange: ExchangeEnum
    trading_date: date
    timestamp: datetime  # Stored in UTC
    open_price: float
    high_price: float
    low_price: float
    close_price: float
    volume: int
    source: str
    ingestion_run_id: str
    source_timestamp: Optional[datetime] = None
    retrieval_timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    total_traded_value: Optional[float] = None
    open_interest: Optional[int] = None
    isin: Optional[str] = None
    raw_payload_checksum: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        return {
            "symbol": self.symbol,
            "exchange": self.exchange.value,
            "trading_date": self.trading_date.isoformat(),
            "timestamp": self.timestamp.isoformat(),
            "open": self.open_price,
            "high": self.high_price,
            "low": self.low_price,
            "close": self.close_price,
            "volume": self.volume,
            "source": self.source,
            "ingestion_run_id": self.ingestion_run_id,
            "retrieval_timestamp": self.retrieval_timestamp.isoformat(),
            "isin": self.isin,
        }


@dataclass
class AdjustedPriceRecord:
    """Corporate-action adjusted price record derived from RawOHLCVRecord."""
    raw_record: RawOHLCVRecord
    adj_open: float
    adj_high: float
    adj_low: float
    adj_close: float
    cumulative_split_factor: float = 1.0
    cumulative_dividend_factor: float = 1.0
    methodology: str = "SPLIT_AND_DIVIDEND_ADJUSTED"
    adjustment_timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    data_version: int = 1


@dataclass
class CorporateActionRecord:
    """Official corporate action record with point-in-time publication tracking."""
    symbol: str
    exchange: ExchangeEnum
    action_type: CorporateActionTypeEnum
    ex_date: date
    information_available_at: datetime  # Critical PIT boundary
    source: str
    ingestion_run_id: str
    announcement_date: Optional[date] = None
    record_date: Optional[date] = None
    effective_date: Optional[date] = None
    ratio_numerator: Optional[float] = None
    ratio_denominator: Optional[float] = None
    adjustment_factor: Optional[float] = None
    dividend_amount: Optional[float] = None
    old_symbol: Optional[str] = None
    new_symbol: Optional[str] = None
    description: Optional[str] = None
    retrieval_timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))


@dataclass
class InstrumentIdentity:
    """Security identity master record."""
    symbol: str
    exchange: ExchangeEnum
    company_name: str
    isin: Optional[str] = None
    listing_date: Optional[date] = None
    delisting_date: Optional[date] = None
    status: str = "ACTIVE"  # ACTIVE | DELISTED | SUSPENDED
    sector: Optional[str] = None
    industry: Optional[str] = None
    is_index: bool = False
    symbol_history: List[Dict[str, Any]] = field(default_factory=list)


@dataclass
class FinancialStatementRecord:
    """Point-in-Time financial statement record."""
    symbol: str
    period_start: date
    period_end: date
    published_at: datetime
    information_available_at: datetime
    revenue: float
    ebitda: float
    ebit: float
    pat: float
    eps: float
    total_assets: float
    total_liabilities: float
    total_debt: float
    total_equity: float
    operating_cash_flow: float
    free_cash_flow: float
    shares_outstanding: int
    source: str
    is_restated: bool = False
    restatement_version: int = 1


@dataclass
class InstitutionalFlowRecord:
    """FII/DII Net Daily Investment Flow Record."""
    trade_date: date
    information_available_at: datetime
    fii_gross_purchase_cr: float
    fii_gross_sales_cr: float
    fii_net_cr: float
    dii_gross_purchase_cr: float
    dii_gross_sales_cr: float
    dii_net_cr: float
    source: str
    ingestion_run_id: str


@dataclass
class DataValidationResult:
    """Output of Data Quality Engine checks."""
    symbol: str
    trading_date: date
    status: ValidationStatusEnum
    error_category: Optional[str] = None
    reason: Optional[str] = None
    details: Dict[str, Any] = field(default_factory=dict)


@dataclass
class IngestionRunMetadata:
    """Audit metadata for every batch ingestion execution."""
    run_id: str
    provider: str
    start_time: datetime
    end_time: Optional[datetime] = None
    status: IngestionStatusEnum = IngestionStatusEnum.STARTED
    symbols_requested: List[str] = field(default_factory=list)
    records_inserted: int = 0
    records_updated: int = 0
    duplicates_count: int = 0
    invalid_count: int = 0
    gaps_count: int = 0
    error_message: Optional[str] = None
    dataset_version: Optional[str] = None
    checksum_sha256: Optional[str] = None


@dataclass
class DatasetSnapshot:
    """Immutable dataset version snapshot for reproducible quantitative backtests."""
    dataset_id: str
    dataset_version: str
    as_of_timestamp: datetime
    start_date: date
    end_date: date
    symbols: List[str]
    total_records: int
    checksum_sha256: str
    provider: str
    metadata: Dict[str, Any] = field(default_factory=dict)
