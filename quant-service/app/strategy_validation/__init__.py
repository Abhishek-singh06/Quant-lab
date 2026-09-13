"""Strategy Validation & Verification Package.

Provides native lookahead bias analysis, recursive indicator stability testing,
warmup sufficiency verification, and pre-deployment strategy audits.
"""

from app.strategy_validation.models import (
    ValidationSeverity,
    LookaheadType,
    LookaheadFinding,
    LookaheadReport,
    WarmupMetric,
    RecursiveStabilityReport,
    StrategyValidationCheckItem,
    StrategyValidationReport,
)
from app.strategy_validation.lookahead_analyzer import (
    LookaheadAnalyzer,
)
from app.strategy_validation.recursive_analyzer import (
    RecursiveAnalyzer,
)
from app.strategy_validation.validator import (
    StrategyValidator,
    VALID_TIMEFRAMES,
)

__all__ = [
    "ValidationSeverity",
    "LookaheadType",
    "LookaheadFinding",
    "LookaheadReport",
    "WarmupMetric",
    "RecursiveStabilityReport",
    "StrategyValidationCheckItem",
    "StrategyValidationReport",
    "LookaheadAnalyzer",
    "RecursiveAnalyzer",
    "StrategyValidator",
    "VALID_TIMEFRAMES",
]
