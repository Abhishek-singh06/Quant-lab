"""Strategy Lifecycle & QuantDinger Workflow Architecture Package.

Provides clean-room implementation of strategy registry, experiment provenance,
backtest-to-paper configuration drift detection, promotion gates, and workflow orchestration.
"""

from app.strategy_lifecycle.models import (
    StrategyStatus,
    GateStatus,
    DriftSeverity,
    JobStatus,
    StrategyDefinition,
    ExperimentProvenance,
    ConfigDriftReport,
    DriftItem,
    PromotionGateResult,
    PromotionEvaluation,
    WorkflowJob,
)
from app.strategy_lifecycle.registry import (
    StrategyRegistry,
    global_strategy_registry,
)
from app.strategy_lifecycle.provenance import (
    ProvenanceTracker,
    global_provenance_tracker,
    compute_hash,
)
from app.strategy_lifecycle.drift_detector import (
    ConfigDriftDetector,
)
from app.strategy_lifecycle.gates import (
    StrategyPromotionGatekeeper,
    global_promotion_gatekeeper,
    VALID_TRANSITIONS,
)
from app.strategy_lifecycle.orchestrator import (
    WorkflowOrchestrator,
    global_workflow_orchestrator,
)

__all__ = [
    "StrategyStatus",
    "GateStatus",
    "DriftSeverity",
    "JobStatus",
    "StrategyDefinition",
    "ExperimentProvenance",
    "ConfigDriftReport",
    "DriftItem",
    "PromotionGateResult",
    "PromotionEvaluation",
    "WorkflowJob",
    "StrategyRegistry",
    "global_strategy_registry",
    "ProvenanceTracker",
    "global_provenance_tracker",
    "compute_hash",
    "ConfigDriftDetector",
    "StrategyPromotionGatekeeper",
    "global_promotion_gatekeeper",
    "VALID_TRANSITIONS",
    "WorkflowOrchestrator",
    "global_workflow_orchestrator",
]
