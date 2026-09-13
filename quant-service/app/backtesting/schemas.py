"""
Data models and schemas for QuantLab Backtesting Engine (Part 16).
Guarantees Point-in-Time safety, realistic transaction costs, and complete auditability.
"""

from datetime import date, datetime
from enum import Enum
from typing import Dict, List, Optional, Any
from uuid import UUID, uuid4
from pydantic import BaseModel, Field


class BacktestStatus(str, Enum):
    CREATED = "CREATED"
    RUNNING = "RUNNING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"


class OrderSide(str, Enum):
    BUY = "BUY"
    SELL = "SELL"


class PositionSide(str, Enum):
    LONG = "LONG"
    SHORT = "SHORT"


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
    FORCE_CLOSE_END = "FORCE_CLOSE_END"


class CostModelType(str, Enum):
    ZERO = "ZERO"
    FIXED_BPS = "FIXED_BPS"
    REALISTIC_INDIAN = "REALISTIC_INDIAN"


class SlippageModelType(str, Enum):
    NONE = "NONE"
    FIXED_BPS = "FIXED_BPS"
    SPREAD_AND_VOLUME = "SPREAD_AND_VOLUME"


class TransactionType(str, Enum):
    CAPITAL_INJECTION = "CAPITAL_INJECTION"
    BUY_EXECUTION = "BUY_EXECUTION"
    SELL_EXECUTION = "SELL_EXECUTION"
    DIVIDEND_CREDIT = "DIVIDEND_CREDIT"
    FEE_DEBIT = "FEE_DEBIT"
    SLIPPAGE_DEBIT = "SLIPPAGE_DEBIT"
    CORPORATE_ACTION_ADJUSTMENT = "CORPORATE_ACTION_ADJUSTMENT"


class BacktestConfig(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    name: str
    description: Optional[str] = None
    horizon: str = "SHORT_TERM"  # SHORT_TERM, MEDIUM_TERM, LONG_TERM, MULTI_HORIZON
    universe_type: str = "NIFTY_50"
    symbols: List[str]
    start_date: date
    end_date: date
    initial_capital: float = 1_000_000.0  # 10 Lakhs INR default
    cash_buffer_pct: float = 0.05  # 5% cash buffer
    rebalance_frequency: str = "DAILY"  # DAILY, WEEKLY, MONTHLY, SIGNAL_DRIVEN
    execution_timing: str = "NEXT_BAR_OPEN"  # NEXT_BAR_OPEN, NEXT_BAR_VWAP
    cost_model_type: CostModelType = CostModelType.REALISTIC_INDIAN
    slippage_model_type: SlippageModelType = SlippageModelType.FIXED_BPS
    brokerage_bps: float = 3.0  # 3 bps
    stt_delivery_bps: float = 10.0  # 10 bps delivery STT
    stt_intraday_bps: float = 2.5
    exchange_charges_bps: float = 0.345
    gst_rate: float = 0.18  # 18% GST
    stamp_duty_bps: float = 1.5  # 1.5 bps on buy
    slippage_bps: float = 5.0  # 5 bps
    max_position_weight: float = 0.20  # 20% max single stock
    max_sector_weight: float = 0.35  # 35% max sector
    max_drawdown_limit: float = 0.15  # 15% drawdown limit circuit breaker
    benchmark_symbol: str = "NIFTY_50"
    version: str = "v1.0.0"


class BacktestRun(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    config_id: UUID
    name: str
    status: BacktestStatus = BacktestStatus.CREATED
    engine_version: str = "v1.0.0"
    start_date: date
    end_date: date
    total_bars_processed: int = 0
    total_trades_count: int = 0
    initial_capital: float
    final_equity: Optional[float] = None
    total_net_pnl: Optional[float] = None
    total_fees_paid: Optional[float] = None
    total_slippage_paid: Optional[float] = None
    total_dividends_received: Optional[float] = None
    error_message: Optional[str] = None
    execution_duration_ms: Optional[int] = None
    data_quality_trust_level: str = "PRODUCTION_READY"
    data_quality_report: Optional[Dict[str, Any]] = None
    created_at: datetime = Field(default_factory=lambda: datetime.now())
    completed_at: Optional[datetime] = None



class BacktestOrder(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    run_id: UUID
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
    trade_ref_id: Optional[UUID] = None
    created_at: datetime = Field(default_factory=lambda: datetime.now())


class BacktestTrade(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    run_id: UUID
    symbol: str
    side: PositionSide = PositionSide.LONG
    quantity: int
    entry_order_id: Optional[UUID] = None
    exit_order_id: Optional[UUID] = None
    entry_timestamp: datetime
    exit_timestamp: datetime
    entry_price: float
    exit_price: float
    gross_pnl: float
    net_pnl: float
    return_pct: float
    total_fees: float
    total_slippage: float
    holding_period_days: int
    exit_reason: ExitReason
    max_favorable_excursion: Optional[float] = 0.0  # MFE in %
    max_adverse_excursion: Optional[float] = 0.0   # MAE in %
    regime_at_entry: Optional[str] = None
    regime_at_exit: Optional[str] = None
    created_at: datetime = Field(default_factory=lambda: datetime.now())


class BacktestPosition(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    run_id: UUID
    symbol: str
    quantity: int
    average_entry_price: float
    current_market_price: float
    cost_basis: float
    market_value: float
    unrealized_pnl: float
    unrealized_return_pct: float
    weight_in_portfolio: float
    as_of_date: date
    highest_price_seen: float = 0.0
    lowest_price_seen: float = 0.0
    entry_timestamp: datetime
    entry_order_id: Optional[UUID] = None
    regime_at_entry: Optional[str] = None


class PortfolioSnapshot(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    run_id: UUID
    snapshot_date: date
    cash_balance: float
    positions_market_value: float
    total_equity: float
    gross_exposure: float
    net_exposure: float
    leverage: float = 1.0
    daily_pnl: float
    daily_return: float
    cumulative_return: float
    drawdown_pct: float
    open_positions_count: int
    trades_executed_today: int
    dividends_credited_today: float = 0.0


class EquityPoint(BaseModel):
    point_date: date
    strategy_equity: float
    strategy_return_pct: float
    strategy_drawdown_pct: float
    buy_and_hold_equity: float
    buy_and_hold_return_pct: float
    benchmark_equity: float
    benchmark_return_pct: float


class TransactionRecord(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    run_id: UUID
    transaction_timestamp: datetime
    transaction_type: TransactionType
    symbol: Optional[str] = None
    amount: float  # positive for credit, negative for debit
    cash_balance_before: float
    cash_balance_after: float
    description: str
    reference_id: Optional[UUID] = None


class PerformanceMetrics(BaseModel):
    total_return_pct: float
    cagr: float
    annualized_volatility: float
    sharpe_ratio: float
    sortino_ratio: float
    max_drawdown_pct: float
    max_drawdown_duration_days: int
    calmar_ratio: float
    win_rate_pct: float
    profit_factor: float
    average_trade_return_pct: float
    average_win_return_pct: float
    average_loss_return_pct: float
    win_loss_ratio: float
    total_trades_count: int
    winning_trades_count: int
    losing_trades_count: int
    annualized_turnover: float
    beta_to_benchmark: Optional[float] = None
    alpha_to_benchmark: Optional[float] = None
    information_ratio: Optional[float] = None
    subperiod_metrics: Dict[str, Any] = Field(default_factory=dict)
    regime_breakdown_metrics: Dict[str, Any] = Field(default_factory=dict)
    sector_breakdown_metrics: Dict[str, Any] = Field(default_factory=dict)


class BenchmarkComparison(BaseModel):
    benchmark_symbol: str
    strategy_total_return: float
    benchmark_total_return: float
    strategy_cagr: float
    benchmark_cagr: float
    strategy_sharpe: float
    benchmark_sharpe: float
    strategy_max_dd: float
    benchmark_max_dd: float
    alpha: float
    beta: float
    tracking_error: float
    information_ratio: float


class BacktestRejectedSignal(BaseModel):
    id: UUID = Field(default_factory=uuid4)
    run_id: UUID
    symbol: str
    signal_timestamp: datetime
    signal_type: str
    signal_strength: float
    rejection_reason: str
    details: Optional[str] = None


class BacktestDataQualityReport(BaseModel):
    missing_bars_count: int = 0
    interpolated_bars_count: int = 0
    corporate_actions_applied: int = 0
    timing_violations_prevented: int = 0
    trust_level: str = "PRODUCTION_READY"  # PRODUCTION_READY, DEGRADED, REJECTED
    notes: List[str] = Field(default_factory=list)


class BacktestResult(BaseModel):
    run_id: UUID
    config: BacktestConfig
    status: BacktestStatus
    start_date: date
    end_date: date
    initial_capital: float
    final_equity: float
    total_net_pnl: float
    total_fees_paid: float
    total_slippage_paid: float
    total_dividends_received: float
    metrics: PerformanceMetrics
    benchmark_comparison: Optional[BenchmarkComparison] = None
    equity_curve: List[EquityPoint]
    trades: List[BacktestTrade]
    portfolio_snapshots: List[PortfolioSnapshot]
    rejected_signals: List[BacktestRejectedSignal]
    data_quality_report: BacktestDataQualityReport
    execution_duration_ms: int = 0
