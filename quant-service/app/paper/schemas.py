"""
Data models and schemas for QuantLab Paper Trading Engine (Part 17).
Enforces isolated paper trading mode (no real money), live data health validation,
and immutable decision logging with expected vs realized outcome tracking.
"""

from datetime import date, datetime, timezone
from enum import Enum
from typing import Dict, List, Optional, Any
from uuid import UUID, uuid4
from pydantic import BaseModel, Field


class ExecutionMode(str, Enum):
    PAPER_TRADING = "PAPER_TRADING"
    BACKTEST = "BACKTEST"


class PaperTradingStatus(str, Enum):
    CREATED = "CREATED"
    STARTING = "STARTING"
    RUNNING = "RUNNING"
    PAUSED = "PAUSED"
    DATA_DEGRADED = "DATA_DEGRADED"
    DISCONNECTED = "DISCONNECTED"
    STOPPED = "STOPPED"
    FAILED = "FAILED"


class ClockType(str, Enum):
    LIVE_CLOCK = "LIVE_CLOCK"
    REPLAY_CLOCK = "REPLAY_CLOCK"


class DataFreshnessStatus(str, Enum):
    REAL_TIME = "REAL_TIME"
    DELAYED = "DELAYED"
    STALE = "STALE"
    NOT_AVAILABLE = "NOT_AVAILABLE"
    UNKNOWN = "UNKNOWN"


class ConnectionStatus(str, Enum):
    HEALTHY = "HEALTHY"
    DEGRADED = "DEGRADED"
    STALE = "STALE"
    DISCONNECTED = "DISCONNECTED"
    UNAVAILABLE = "UNAVAILABLE"


class OrderSide(str, Enum):
    BUY = "BUY"
    SELL = "SELL"


class OrderType(str, Enum):
    MARKET = "MARKET"
    LIMIT = "LIMIT"
    STOP = "STOP"


class OrderStatus(str, Enum):
    PENDING = "PENDING"
    FILLED = "FILLED"
    REJECTED = "REJECTED"
    CANCELLED = "CANCELLED"


class ExitReason(str, Enum):
    SIGNAL = "SIGNAL"
    STOP_LOSS = "STOP_LOSS"
    TAKE_PROFIT = "TAKE_PROFIT"
    MAX_HOLDING_TIME = "MAX_HOLDING_TIME"
    REBALANCE = "REBALANCE"
    RISK_CIRCUIT = "RISK_CIRCUIT"
    SESSION_CLOSE = "SESSION_CLOSE"


class SignalOutcomeStatus(str, Enum):
    CORRECT_DIRECTION = "CORRECT_DIRECTION"
    WRONG_DIRECTION = "WRONG_DIRECTION"
    PARTIAL = "PARTIAL"
    EXPIRED = "EXPIRED"
    NO_OUTCOME_YET = "NO_OUTCOME_YET"
    INSUFFICIENT_DATA = "INSUFFICIENT_DATA"


class ModelMonitoringStatus(str, Enum):
    NORMAL = "NORMAL"
    WARNING = "WARNING"
    DRIFT = "DRIFT"
    INSUFFICIENT_DATA = "INSUFFICIENT_DATA"


class ModelStatus(str, Enum):
    ACTIVE = "ACTIVE"
    WARNING = "WARNING"
    DEGRADED = "DEGRADED"
    PAUSED = "PAUSED"
    RETIRED = "RETIRED"


class PaperTradingSession(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    name: str
    execution_mode: ExecutionMode = ExecutionMode.PAPER_TRADING
    status: PaperTradingStatus = PaperTradingStatus.CREATED
    clock_type: ClockType = ClockType.LIVE_CLOCK
    data_provider: str = "AUTHORIZED_FEED"
    data_freshness_status: DataFreshnessStatus = DataFreshnessStatus.UNKNOWN
    start_time: Optional[datetime] = None
    end_time: Optional[datetime] = None
    configuration_version: str = "v1.0.0"
    engine_version: str = "v1.0.0"
    total_decisions_count: int = 0
    total_orders_count: int = 0
    total_fills_count: int = 0
    error_message: Optional[str] = None
    created_at: datetime = Field(default_factory=lambda: datetime.now())
    updated_at: datetime = Field(default_factory=lambda: datetime.now())


class PaperPortfolio(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    session_id: Optional[UUID] = None
    name: str
    horizon: str = "SHORT_TERM"  # SHORT_TERM, MEDIUM_TERM, LONG_TERM
    risk_profile_id: Optional[UUID] = None
    currency: str = "INR"
    initial_virtual_capital: float = 1_000_000.0  # ₹10,00,000 INR default virtual capital
    cash_balance: float = 1_000_000.0
    available_cash: float = 1_000_000.0
    reserved_cash: float = 0.0
    invested_value: float = 0.0
    total_portfolio_value: float = 1_000_000.0
    peak_portfolio_value: float = 1_000_000.0
    current_drawdown_pct: float = 0.0
    max_drawdown_pct: float = 0.0
    gross_exposure: float = 0.0
    net_exposure: float = 0.0
    leverage: float = 1.0
    total_realized_pnl: float = 0.0
    total_unrealized_pnl: float = 0.0
    total_fees_paid: float = 0.0
    total_slippage_paid: float = 0.0
    total_dividends_received: float = 0.0
    status: str = "ACTIVE"
    created_at: datetime = Field(default_factory=lambda: datetime.now())
    updated_at: datetime = Field(default_factory=lambda: datetime.now())


class PaperPosition(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    portfolio_id: UUID
    symbol: str
    instrument_id: Optional[int] = None
    horizon: str = "SHORT_TERM"
    quantity: int
    average_entry_price: float
    current_market_price: float
    cost_basis: float
    market_value: float
    unrealized_pnl: float
    unrealized_return_pct: float
    realized_pnl: float = 0.0
    portfolio_weight: float = 0.0
    stop_price: Optional[float] = None
    target_price: Optional[float] = None
    stop_method: Optional[str] = None
    highest_price_seen: float
    lowest_price_seen: float
    entry_timestamp: datetime
    last_updated_at: datetime
    signal_id: Optional[UUID] = None
    risk_assessment_id: Optional[UUID] = None
    model_version: Optional[str] = None
    is_active: bool = True
    created_at: datetime = Field(default_factory=lambda: datetime.now())


class PaperTradingDecision(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    portfolio_id: UUID
    session_id: Optional[UUID] = None
    symbol: str
    timestamp: datetime
    horizon: str
    decision: str  # BUY, HOLD, SELL, NO_TRADE
    decision_reason: str
    signal_id: Optional[UUID] = None
    signal_version: Optional[str] = None
    signal_score: float
    signal_confidence: float
    expected_return: Optional[float] = None
    expected_volatility: Optional[float] = None
    predicted_direction: Optional[str] = None
    predicted_probability: Optional[float] = None
    prediction_id: Optional[UUID] = None
    model_version: Optional[str] = None
    risk_assessment_id: Optional[UUID] = None
    risk_engine_version: Optional[str] = None
    suggested_allocation: float
    maximum_allocation: float
    recommended_quantity: int
    entry_price: float
    stop_price: Optional[float] = None
    target_price: Optional[float] = None
    risk_level: str
    supporting_evidence: Optional[List[Dict[str, Any]]] = None
    opposing_evidence: Optional[List[Dict[str, Any]]] = None
    data_quality_status: str = "HIGH_QUALITY"
    data_version: str = "1"
    feature_version: str = "v1.0.0"
    information_available_at: datetime
    calculated_at: datetime
    status: str = "RECORDED"  # RECORDED, EXECUTED, REJECTED_RISK, REJECTED_CASH, REJECTED_DATA
    # Provenance fields (Phase 18.2)
    source_observation_id: Optional[str] = None
    source_provider: str = "YAHOO_FINANCE"
    source_provider_timestamp: Optional[datetime] = None
    provenance_status: str = "PROVENANCE_INCOMPLETE"
    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))


class PaperOrder(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    decision_id: Optional[UUID] = None
    portfolio_id: UUID
    session_id: Optional[UUID] = None
    symbol: str
    side: OrderSide
    order_type: OrderType = OrderType.MARKET
    quantity: int
    requested_price: float
    executed_price: Optional[float] = None
    signal_timestamp: datetime
    order_submitted_timestamp: datetime
    order_executed_timestamp: Optional[datetime] = None
    status: OrderStatus = OrderStatus.PENDING
    rejection_reason: Optional[str] = None
    slippage_bps: float = 0.0
    slippage_amount: float = 0.0
    fees_amount: float = 0.0
    # Complete Lineage Provenance Fields (Phase 18.2)
    source_observation_id: Optional[str] = None
    source_provider: str = "YAHOO_FINANCE"
    source_provider_timestamp: Optional[datetime] = None
    prediction_id: Optional[UUID] = None
    signal_id: Optional[UUID] = None
    provenance_status: str = "PROVENANCE_INCOMPLETE"  # COMPLETE, PROVENANCE_INCOMPLETE, TEST_FIXTURE
    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))


class PaperFill(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    order_id: UUID
    portfolio_id: UUID
    symbol: str
    side: OrderSide
    quantity: int
    requested_price: float
    fill_price: float
    slippage_bps: float = 0.0
    slippage_amount: float = 0.0
    brokerage: float = 0.0
    stt: float = 0.0
    exchange_charges: float = 0.0
    gst: float = 0.0
    stamp_duty: float = 0.0
    total_fees: float = 0.0
    execution_timestamp: datetime
    # Complete Lineage Provenance Fields (Phase 18.2)
    source_observation_id: Optional[str] = None
    source_provider: str = "YAHOO_FINANCE"
    source_provider_timestamp: Optional[datetime] = None
    prediction_id: Optional[UUID] = None
    signal_id: Optional[UUID] = None
    provenance_status: str = "PROVENANCE_INCOMPLETE"
    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))


class PaperTransactionRecord(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    portfolio_id: UUID
    session_id: Optional[UUID] = None
    transaction_timestamp: datetime
    event_type: str  # ORDER_CREATED, ORDER_FILLED, ORDER_REJECTED, POSITION_OPENED, POSITION_CLOSED, DIVIDEND_RECEIVED, CORPORATE_ACTION, FEE_CHARGED, SLIPPAGE, RISK_BLOCK, DATA_OUTAGE
    symbol: Optional[str] = None
    amount: float  # positive for cash credit, negative for cash debit
    cash_balance_before: float
    cash_balance_after: float
    description: str
    reference_id: Optional[UUID] = None
    created_at: datetime = Field(default_factory=lambda: datetime.now())


class PaperEquityPoint(BaseModel):
    portfolio_id: UUID
    snapshot_timestamp: datetime
    portfolio_value: float
    cash_balance: float
    invested_value: float
    daily_return_pct: float = 0.0
    cumulative_return_pct: float = 0.0
    drawdown_pct: float = 0.0


class PaperSignalOutcome(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    decision_id: UUID
    symbol: str
    horizon: str
    signal_timestamp: datetime
    evaluation_timestamp: datetime
    expected_direction: str
    realized_direction: str
    expected_return: float
    realized_return: float
    outcome_status: SignalOutcomeStatus
    attribution: Optional[Dict[str, Any]] = None


class PaperPredictionOutcome(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    decision_id: UUID
    prediction_id: Optional[UUID] = None
    model_version: str
    symbol: str
    horizon: str
    prediction_timestamp: datetime
    evaluation_timestamp: datetime
    expected_return: float
    realized_return: float
    prediction_error: float  # realized - expected
    absolute_error: float
    squared_error: float
    expected_volatility: Optional[float] = None
    realized_volatility: Optional[float] = None
    is_direction_correct: bool


class PaperModelMonitoringRecord(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    model_version: str
    horizon: str
    evaluation_window_start: date
    evaluation_window_end: date
    sample_size: int
    directional_accuracy: float
    mae: float
    rmse: float
    ic: Optional[float] = None
    rank_ic: Optional[float] = None
    drift_status: ModelMonitoringStatus = ModelMonitoringStatus.NORMAL
    model_status: ModelStatus = ModelStatus.ACTIVE
    evaluated_at: datetime = Field(default_factory=lambda: datetime.now())


class PaperRiskMonitoringRecord(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    portfolio_id: UUID
    evaluation_timestamp: datetime
    current_drawdown_pct: float
    max_position_weight: float
    max_sector_weight: float
    cash_buffer_pct: float
    limit_breached: bool = False
    breach_type: Optional[str] = None
    action_taken: str = "NONE"  # NONE, WARN, REDUCE, BLOCK_NEW_POSITION
    details: Optional[str] = None


class LiveDataHealth(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    provider_name: str
    connection_status: ConnectionStatus
    last_successful_update: Optional[datetime] = None
    last_market_timestamp: Optional[datetime] = None
    latency_ms: int = 0
    data_age_seconds: int = 0
    freshness_status: DataFreshnessStatus
    error_count: int = 0
    coverage_ratio: float = 1.0
    checked_at: datetime = Field(default_factory=lambda: datetime.now())


class PaperTradingHealthReport(BaseModel):
    market_data: str = "PASS"
    feature_engine: str = "PASS"
    model: str = "PASS"
    signal: str = "PASS"
    risk: str = "PASS"
    portfolio: str = "PASS"
    database: str = "PASS"
    execution: str = "PASS"
    overall_status: str = "HEALTHY"  # HEALTHY, DEGRADED, BLOCKED
    notes: List[str] = Field(default_factory=list)
