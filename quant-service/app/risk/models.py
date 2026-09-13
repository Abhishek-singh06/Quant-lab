"""
Production Risk Engine and Position Sizing Domain Models for QuantLab Part 14.
"""

from dataclasses import dataclass, field
from datetime import datetime, timezone
from enum import Enum
from typing import Any, Dict, List, Optional
import uuid


class RiskProfileType(str, Enum):
    CONSERVATIVE = "CONSERVATIVE"
    MODERATE = "MODERATE"
    AGGRESSIVE = "AGGRESSIVE"
    CUSTOM = "CUSTOM"


class StopMethod(str, Enum):
    ATR_MULTIPLE = "ATR_MULTIPLE"
    FIXED_PERCENT = "FIXED_PERCENT"
    VOLATILITY_BASED = "VOLATILITY_BASED"
    TECHNICAL_LEVEL = "TECHNICAL_LEVEL"
    USER_DEFINED = "USER_DEFINED"
    SIGNAL_DEFINED = "SIGNAL_DEFINED"


class PositionSizingMethod(str, Enum):
    FIXED_ALLOCATION = "FIXED_ALLOCATION"
    FIXED_RISK = "FIXED_RISK"
    VOLATILITY_ADJUSTED = "VOLATILITY_ADJUSTED"
    SIGNAL_CONFIDENCE_ADJUSTED = "SIGNAL_CONFIDENCE_ADJUSTED"
    PORTFOLIO_AWARE = "PORTFOLIO_AWARE"


class RiskDecision(str, Enum):
    APPROVE = "APPROVE"
    APPROVE_REDUCED = "APPROVE_REDUCED"
    REJECT = "REJECT"
    INSUFFICIENT_DATA = "INSUFFICIENT_DATA"
    BLOCKED_BY_RISK_LIMIT = "BLOCKED_BY_RISK_LIMIT"
    BLOCKED_BY_DATA_QUALITY = "BLOCKED_BY_DATA_QUALITY"
    BLOCKED_BY_LIQUIDITY = "BLOCKED_BY_LIQUIDITY"
    BLOCKED_BY_CONCENTRATION = "BLOCKED_BY_CONCENTRATION"
    BLOCKED_BY_DRAWDOWN = "BLOCKED_BY_DRAWDOWN"
    BLOCKED_BY_MODEL_QUALITY = "BLOCKED_BY_MODEL_QUALITY"


class RiskLevel(str, Enum):
    LOW = "LOW"
    MODERATE = "MODERATE"
    HIGH = "HIGH"
    EXTREME = "EXTREME"
    UNKNOWN = "UNKNOWN"


class RiskWarningSeverity(str, Enum):
    LOW = "LOW"
    MODERATE = "MODERATE"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


@dataclass
class RiskProfile:
    """Configurable User Risk Profile."""
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    name: str = "Standard Moderate Profile"
    profile_type: RiskProfileType = RiskProfileType.MODERATE
    max_portfolio_risk: float = 0.05       # 5% total portfolio risk budget
    max_position_risk: float = 0.01        # 1% risk per position
    max_position_allocation: float = 0.10  # 10% max allocation in single stock
    max_sector_allocation: float = 0.25    # 25% max sector exposure
    max_industry_allocation: float = 0.15  # 15% max industry exposure
    max_single_security_allocation: float = 0.10
    max_correlation_exposure: float = 0.70 # Max allowable correlation with portfolio
    max_drawdown_tolerance: float = 0.15   # 15% max drawdown tolerance
    max_portfolio_volatility: float = 0.20 # 20% annualized portfolio vol
    minimum_liquidity_requirement: float = 1_000_000.0 # Min ADV in INR
    max_adv_participation_rate: float = 0.05 # 5% max volume participation
    default_stop_method: StopMethod = StopMethod.ATR_MULTIPLE
    default_position_sizing_method: PositionSizingMethod = PositionSizingMethod.FIXED_RISK
    allow_short_selling: bool = False
    allow_leverage: bool = False
    max_leverage: float = 1.0
    cash_buffer: float = 0.05              # 5% minimum unencumbered cash buffer
    minimum_confidence: float = 0.50       # 50% min signal confidence
    minimum_signal_score: float = 35.0
    risk_budget_method: str = "VOLATILITY_ADJUSTED"
    is_active: bool = True
    version: str = "RP_v1.0.0"


@dataclass
class PortfolioPosition:
    """Active or historical holding in a portfolio."""
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    portfolio_id: str = ""
    instrument_id: Optional[int] = None
    symbol: str = ""
    sector: str = "UNKNOWN"
    industry: str = "UNKNOWN"
    quantity: float = 0.0
    average_entry_price: float = 0.0
    current_price: float = 0.0
    market_value: float = 0.0
    weight: float = 0.0
    current_stop_price: Optional[float] = None
    position_risk_amount: float = 0.0
    position_risk_percent: float = 0.0
    entry_timestamp: Optional[datetime] = None
    last_updated_timestamp: Optional[datetime] = None
    is_active: bool = True


@dataclass
class PortfolioState:
    """Point-in-Time snapshot of complete portfolio state."""
    portfolio_id: str = field(default_factory=lambda: str(uuid.uuid4()))
    name: str = "Default Portfolio"
    currency: str = "INR"
    as_of_timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    current_cash: float = 1_000_000.0
    current_portfolio_value: float = 1_000_000.0
    peak_portfolio_value: float = 1_000_000.0
    peak_timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    current_drawdown: float = 0.0
    max_drawdown: float = 0.0
    positions: List[PortfolioPosition] = field(default_factory=list)
    consumed_risk_budget: float = 0.0
    remaining_risk_budget: float = 0.0
    portfolio_volatility: Optional[float] = None


@dataclass
class PositionRiskMetrics:
    """Risk calculations for a single security position."""
    entry_price: float = 0.0
    current_price: float = 0.0
    stop_price: float = 0.0
    target_price: Optional[float] = None
    stop_distance: float = 0.0
    stop_distance_pct: float = 0.0
    stop_method: StopMethod = StopMethod.ATR_MULTIPLE
    position_risk_amount: float = 0.0
    position_risk_percent: float = 0.0
    estimated_downside: float = 0.0
    risk_reward_ratio: Optional[float] = None
    is_valid: bool = True


@dataclass
class RiskAdjustment:
    """Individual mathematical adjustment step in sizing."""
    adjustment_type: str
    multiplier: float
    base_allocation: float
    adjusted_allocation: float
    reason: str


@dataclass
class RiskWarning:
    """Explicit risk condition or tail-risk flag."""
    warning_code: str
    severity: RiskWarningSeverity
    message: str


@dataclass
class RiskTrace:
    """Full machine-readable audit trail of position sizing."""
    base_allocation: float = 0.0
    signal_confidence_adjustment: float = 1.0
    volatility_adjustment: float = 1.0
    correlation_adjustment: float = 1.0
    concentration_adjustment: float = 1.0
    drawdown_adjustment: float = 1.0
    liquidity_adjustment: float = 1.0
    risk_budget_cap: float = 1.0
    final_suggested_allocation: float = 0.0
    limiting_constraint: str = "NONE"


@dataclass
class RiskAssessmentResult:
    """Final Point-in-Time Risk Assessment and Sizing Output."""
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    timestamp: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    information_available_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    calculated_at: datetime = field(default_factory=lambda: datetime.now(timezone.utc))
    
    portfolio_id: str = ""
    risk_profile_id: str = ""
    signal_id: Optional[str] = None
    symbol: str = ""
    signal_type: str = "BUY"
    signal_score: float = 0.0
    signal_confidence: float = 0.0
    
    # Recommended Sizing
    suggested_allocation: float = 0.0        # e.g. 0.08 (8%)
    maximum_allocation: float = 0.0          # e.g. 0.10 (10%)
    recommended_quantity: float = 0.0        # Shares
    entry_price: float = 0.0
    stop_price: float = 0.0
    target_price: Optional[float] = None
    stop_distance: float = 0.0
    stop_distance_pct: float = 0.0
    stop_method: str = "ATR_MULTIPLE"
    
    # Financial Risk Metrics
    position_risk_amount: float = 0.0        # in INR
    position_risk_percent: float = 0.0       # % of portfolio
    estimated_downside: float = 0.0          # in INR to stop
    portfolio_value: float = 0.0
    remaining_risk_budget: float = 0.0
    portfolio_volatility: Optional[float] = None
    security_volatility: float = 0.0
    expected_volatility: Optional[float] = None
    max_correlation: Optional[float] = None
    sector_exposure_after_trade: float = 0.0
    current_drawdown: float = 0.0
    risk_reward_ratio: Optional[float] = None
    expected_return: Optional[float] = None
    
    # Decision & Auditability
    risk_decision: RiskDecision = RiskDecision.APPROVE
    risk_level: RiskLevel = RiskLevel.MODERATE
    risk_trace: RiskTrace = field(default_factory=RiskTrace)
    adjustments: List[RiskAdjustment] = field(default_factory=list)
    limiting_constraints: List[str] = field(default_factory=list)
    risk_warnings: List[RiskWarning] = field(default_factory=list)
    reasoning: str = ""
    data_quality_status: str = "HIGH_QUALITY"
    
    # Lineage & Versions
    risk_engine_version: str = "RISK_v1.0.0"
    risk_profile_version: str = "RP_v1.0.0"
    signal_version: Optional[str] = "SIGNAL_v1.0.0"
    data_version: str = "1"
