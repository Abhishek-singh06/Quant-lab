"""Strategy Validation Domain Models and Schemas.

Defines schemas for lookahead bias analysis, recursive indicator stability,
strategy contracts, and pre-deployment validation reports.
"""

from enum import Enum
from typing import Dict, List, Optional, Any
from datetime import datetime, timezone
from pydantic import BaseModel, Field


class ValidationSeverity(str, Enum):
    """Severity of a validation finding."""
    INFO = "INFO"
    WARNING = "WARNING"
    ERROR = "ERROR"
    CRITICAL = "CRITICAL"


class LookaheadType(str, Enum):
    """Categorization of detected lookahead bias."""
    NEGATIVE_SHIFT = "NEGATIVE_SHIFT"
    CENTERED_WINDOW = "CENTERED_WINDOW"
    FUTURE_DATA_MODIFICATION = "FUTURE_DATA_MODIFICATION"
    FUTURE_JOIN_LEAKAGE = "FUTURE_JOIN_LEAKAGE"
    GLOBAL_NORMALIZATION_LEAKAGE = "GLOBAL_NORMALIZATION_LEAKAGE"
    TARGET_LEAKAGE = "TARGET_LEAKAGE"
    SURVIVORSHIP_BIAS = "SURVIVORSHIP_BIAS"


class LookaheadFinding(BaseModel):
    """Detailed finding of a lookahead bias detection."""
    check_name: str
    lookahead_type: LookaheadType
    severity: ValidationSeverity
    column: Optional[str] = None
    bar_index: Optional[int] = None
    description: str
    expected_behavior: str
    actual_behavior: str


class LookaheadReport(BaseModel):
    """Report summarizing lookahead bias analysis."""
    report_id: str
    strategy_id: Optional[str] = None
    is_clean: bool = True
    total_checks_run: int = 0
    findings: List[LookaheadFinding] = Field(default_factory=list)
    has_critical_lookahead: bool = False
    checked_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))


class WarmupMetric(BaseModel):
    """Convergence metric for a specific warmup window length."""
    warmup_bars: int
    max_absolute_error: float
    mean_absolute_error: float
    is_converged: bool


class RecursiveStabilityReport(BaseModel):
    """Report summarizing indicator warmup sufficiency and historical invariance."""
    report_id: str
    indicator_name: str
    is_stable: bool = True
    is_future_invariant: bool = True
    recommended_min_warmup_bars: int = 50
    tolerance_epsilon: float = 1e-6
    warmup_convergence: List[WarmupMetric] = Field(default_factory=list)
    findings: List[str] = Field(default_factory=list)
    checked_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))


class StrategyValidationCheckItem(BaseModel):
    """Single item in the pre-deployment strategy validation checklist."""
    check_name: str
    category: str  # "SPECIFICATION", "LOOKAHEAD", "RECURSIVE", "RISK", "EXECUTION", "PROVENANCE"
    passed: bool
    severity: ValidationSeverity
    details: str


class StrategyValidationReport(BaseModel):
    """Comprehensive pre-deployment validation report for a quantitative strategy."""
    report_id: str
    strategy_id: str
    strategy_version: str
    is_valid: bool = True
    checks_passed: int = 0
    checks_failed: int = 0
    check_items: List[StrategyValidationCheckItem] = Field(default_factory=list)
    blocking_reasons: List[str] = Field(default_factory=list)
    validated_at: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
