"""Strategy Lifecycle Domain Models and Schemas.

Defines schemas for strategy definitions, provenance tracking, configuration drift,
promotion gates, and workflow orchestration.
"""

from enum import Enum
from typing import Dict, List, Optional, Any
from datetime import datetime, timezone
from pydantic import BaseModel, Field


class StrategyStatus(str, Enum):
    """Lifecycle promotion status of a quantitative strategy."""
    DRAFT = "DRAFT"
    RESEARCH = "RESEARCH"
    VALIDATED = "VALIDATED"
    BACKTESTED = "BACKTESTED"
    PAPER_ELIGIBLE = "PAPER_ELIGIBLE"
    PAPER_RUNNING = "PAPER_RUNNING"
    PAPER_VALIDATED = "PAPER_VALIDATED"
    MANUAL_REVIEW = "MANUAL_REVIEW"
    LIVE_ELIGIBLE = "LIVE_ELIGIBLE"
    DEPRECATED = "DEPRECATED"
    ARCHIVED = "ARCHIVED"


class GateStatus(str, Enum):
    """Status of a promotion gate evaluation."""
    PENDING = "PENDING"
    PASSED = "PASSED"
    FAILED = "FAILED"
    WAIVED = "WAIVED"
    BLOCKED = "BLOCKED"


class DriftSeverity(str, Enum):
    """Severity of configuration drift between backtest and paper/live."""
    NONE = "NONE"
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"
    CRITICAL = "CRITICAL"


class JobStatus(str, Enum):
    """Status of an orchestrated lifecycle job."""
    PENDING = "PENDING"
    RUNNING = "RUNNING"
    COMPLETED = "COMPLETED"
    FAILED = "FAILED"
    CANCELLED = "CANCELLED"


# ---------------------------------------------------------------------------
# Strategy Definition
# ---------------------------------------------------------------------------

class StrategyDefinition(BaseModel):
    """Immutable definition of a quantitative strategy version."""
    strategy_id: str = Field(..., description="Unique strategy identifier / slug")
    version: str = Field(..., description="Semantic version string, e.g. '1.0.0'")
    name: str = Field(..., description="Human-readable strategy name")
    description: str = Field(default="", description="Detailed strategy description")
    author: str = Field(default="QuantLab Researcher", description="Author or team")
    horizon: str = Field(default="MEDIUM", description="Investment horizon: SHORT, MEDIUM, LONG")
    target_instruments: List[str] = Field(default_factory=list, description="Target universe tickers")
    feature_set_id: str = Field(default="default_alpha158", description="Feature schema identifier")
    feature_names: List[str] = Field(default_factory=list, description="List of feature column names")
    model_id: Optional[str] = Field(default=None, description="Trained model identifier")
    model_type: Optional[str] = Field(default=None, description="Model type (e.g. GBDT, Ridge, Ensemble)")
    entry_rules: Dict[str, Any] = Field(default_factory=dict, description="Signal entry logic/thresholds")
    exit_rules: Dict[str, Any] = Field(default_factory=dict, description="Signal exit logic/thresholds")
    risk_parameters: Dict[str, Any] = Field(
        default_factory=lambda: {
            "max_position_size": 0.10,
            "stop_loss_pct": 0.05,
            "take_profit_pct": 0.15,
            "max_drawdown_limit": 0.20,
            "max_leverage": 1.0,
        },
        description="Strict risk limits"
    )
    execution_config: Dict[str, Any] = Field(
        default_factory=lambda: {
            "slippage_bps": 5.0,
            "commission_bps": 3.0,
            "execution_delay_bars": 1,
        },
        description="Execution assumption parameters"
    )
    timeframe: str = Field(default="1D", description="Target timeframe bar resolution: 1m, 5m, 15m, 1h, 1D")
    warmup_period_bars: int = Field(default=100, description="Minimum historical warmup bars required for indicators")
    required_lookback_bars: int = Field(default=250, description="Minimum total lookback required for strategy execution")
    data_dependencies: List[str] = Field(
        default_factory=lambda: ["OHLCV"],
        description="Declared data feeds: OHLCV, FUNDAMENTALS, NEWS, INSTITUTIONAL"
    )
    status: StrategyStatus = Field(default=StrategyStatus.DRAFT, description="Current lifecycle state")
    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Creation UTC timestamp")
    updated_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc), description="Last update UTC timestamp")
    metadata: Dict[str, Any] = Field(default_factory=dict, description="Arbitrary strategy metadata")


# ---------------------------------------------------------------------------
# Experiment Provenance
# ---------------------------------------------------------------------------

class ExperimentProvenance(BaseModel):
    """End-to-end lineage linking data, features, models, backtests, and strategy versions."""
    provenance_id: str = Field(..., description="Unique provenance record ID")
    strategy_id: str = Field(..., description="Target strategy ID")
    strategy_version: str = Field(..., description="Target strategy version")
    data_version: str = Field(default="v1.0", description="Dataset version or snapshot ID")
    data_hash: str = Field(..., description="SHA-256 hash of canonical dataset")
    pit_timestamp: datetime = Field(
        default_factory=lambda: datetime.now(timezone.utc),
        description="Point-in-time cutoff metadata"
    )
    feature_version: str = Field(default="v1.0", description="Feature generator version")
    feature_config_hash: str = Field(..., description="SHA-256 hash of feature configuration")
    model_version: str = Field(default="v1.0", description="Trained model version")
    model_checkpoint: Optional[str] = Field(default=None, description="Model weights / checkpoint URI")
    train_split: Dict[str, str] = Field(
        default_factory=lambda: {"start": "2023-01-01", "end": "2023-08-31"},
        description="Training window"
    )
    val_split: Dict[str, str] = Field(
        default_factory=lambda: {"start": "2023-09-01", "end": "2023-10-31"},
        description="Validation window"
    )
    test_split: Dict[str, str] = Field(
        default_factory=lambda: {"start": "2023-11-01", "end": "2023-12-31"},
        description="Out-of-sample test window"
    )
    backtest_id: str = Field(..., description="Associated backtest run ID")
    backtest_metrics: Dict[str, float] = Field(
        default_factory=dict,
        description="Key performance metrics (Sharpe, CAGR, MaxDD, WinRate, etc.)"
    )
    created_at: datetime = Field(
        default_factory=lambda: datetime.now(timezone.utc),
        description="Provenance record creation timestamp"
    )
    run_id: str = Field(..., description="Pipeline execution run ID")


# ---------------------------------------------------------------------------
# Configuration Drift
# ---------------------------------------------------------------------------

class DriftItem(BaseModel):
    """Specific field drift finding."""
    field: str
    backtest_value: Any
    paper_value: Any
    severity: DriftSeverity
    is_blocking: bool
    explanation: str


class ConfigDriftReport(BaseModel):
    """Drift audit comparing backtested parameters against deployment parameters."""
    report_id: str = Field(..., description="Unique drift report ID")
    strategy_id: str = Field(..., description="Strategy identifier")
    strategy_version: str = Field(..., description="Strategy semantic version")
    backtest_config: Dict[str, Any] = Field(..., description="Original backtest parameter set")
    paper_config: Dict[str, Any] = Field(..., description="Target deployment parameter set")
    drift_detected: bool = Field(default=False, description="True if any parameter differs")
    max_severity: DriftSeverity = Field(default=DriftSeverity.NONE, description="Highest drift severity found")
    drift_items: List[DriftItem] = Field(default_factory=list, description="Detailed field drift items")
    is_deployment_blocked: bool = Field(default=False, description="True if material drift blocks deployment")
    blocking_reasons: List[str] = Field(default_factory=list, description="Explanations for blocking deployment")
    checked_at: datetime = Field(
        default_factory=lambda: datetime.now(timezone.utc),
        description="Audit execution timestamp"
    )


# ---------------------------------------------------------------------------
# Promotion Gates
# ---------------------------------------------------------------------------

class PromotionGateResult(BaseModel):
    """Result for a single promotion gate evaluation."""
    gate_name: str
    status: GateStatus
    description: str
    metrics_evaluated: Dict[str, Any] = Field(default_factory=dict)
    thresholds: Dict[str, Any] = Field(default_factory=dict)
    failure_reasons: List[str] = Field(default_factory=list)
    evaluated_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))


class PromotionEvaluation(BaseModel):
    """Full promotion decision across all required gates for a target status."""
    strategy_id: str
    strategy_version: str
    current_status: StrategyStatus
    target_status: StrategyStatus
    is_promoted: bool
    gate_results: List[PromotionGateResult] = Field(default_factory=list)
    blocking_reasons: List[str] = Field(default_factory=list)
    evaluated_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    comments: Optional[str] = None


# ---------------------------------------------------------------------------
# Workflow Orchestration
# ---------------------------------------------------------------------------

class WorkflowJob(BaseModel):
    """Orchestrated lifecycle pipeline job."""
    job_id: str
    job_type: str
    correlation_id: str
    status: JobStatus = JobStatus.PENDING
    params: Dict[str, Any] = Field(default_factory=dict)
    result: Optional[Dict[str, Any]] = None
    error: Optional[str] = None
    created_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    started_at: Optional[datetime] = None
    completed_at: Optional[datetime] = None
    idempotency_key: Optional[str] = None
