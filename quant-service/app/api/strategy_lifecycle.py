"""REST API Router for Strategy Lifecycle & Workflow Orchestration.

Exposes endpoints for immutable strategy registration, provenance lineage graph,
configuration drift detection, gate promotion evaluation, and workflow jobs.
"""

from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from fastapi import APIRouter, HTTPException, Query, status

from app.strategy_lifecycle import (
    StrategyDefinition,
    StrategyStatus,
    ExperimentProvenance,
    ConfigDriftReport,
    PromotionEvaluation,
    WorkflowJob,
    JobStatus,
    global_strategy_registry,
    global_provenance_tracker,
    global_promotion_gatekeeper,
    global_workflow_orchestrator,
    ConfigDriftDetector,
)

router = APIRouter(prefix="/lifecycle", tags=["Strategy Lifecycle & Provenance"])
_drift_detector = ConfigDriftDetector()


# ---------------------------------------------------------------------------
# Request Schemas
# ---------------------------------------------------------------------------

class DriftCheckRequest(BaseModel):
    strategy_id: str
    strategy_version: str
    backtest_config: Dict[str, Any]
    paper_config: Dict[str, Any]


class PromoteRequest(BaseModel):
    strategy_id: str
    strategy_version: str
    target_status: StrategyStatus
    context: Optional[Dict[str, Any]] = None


class JobSubmitRequest(BaseModel):
    job_type: str
    params: Dict[str, Any] = Field(default_factory=dict)
    correlation_id: Optional[str] = None
    idempotency_key: Optional[str] = None


class CloneStrategyRequest(BaseModel):
    strategy_id: str
    from_version: str
    new_version: str
    overrides: Optional[Dict[str, Any]] = None


# ---------------------------------------------------------------------------
# Strategy Registry Endpoints
# ---------------------------------------------------------------------------

@router.post("/strategies", response_model=StrategyDefinition, status_code=status.HTTP_201_CREATED)
def register_strategy(strategy: StrategyDefinition):
    """Register a new immutable strategy definition."""
    try:
        return global_strategy_registry.register_strategy(strategy)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


@router.get("/strategies", response_model=List[StrategyDefinition])
def list_strategies(
    status: Optional[StrategyStatus] = Query(None, description="Filter by strategy lifecycle status"),
    horizon: Optional[str] = Query(None, description="Filter by investment horizon"),
    author: Optional[str] = Query(None, description="Filter by author name substring"),
):
    """List registered strategies matching optional criteria."""
    return global_strategy_registry.list_strategies(status=status, horizon=horizon, author=author)


@router.get("/strategies/{strategy_id}", response_model=StrategyDefinition)
def get_strategy(
    strategy_id: str,
    version: Optional[str] = Query(None, description="Optional semantic version, defaults to latest"),
):
    """Get a registered strategy definition by ID and optional version."""
    strat = global_strategy_registry.get_strategy(strategy_id, version)
    if not strat:
        raise HTTPException(
            status_code=404,
            detail=f"Strategy '{strategy_id}' (version: {version or 'latest'}) not found."
        )
    return strat


@router.get("/strategies/{strategy_id}/versions", response_model=List[str])
def get_strategy_versions(strategy_id: str):
    """List all registered semantic versions for a strategy."""
    versions = global_strategy_registry.get_versions(strategy_id)
    if not versions:
        raise HTTPException(status_code=404, detail=f"Strategy '{strategy_id}' has no registered versions.")
    return versions


@router.post("/strategies/clone", response_model=StrategyDefinition)
def clone_strategy(req: CloneStrategyRequest):
    """Clone an existing strategy version to a new version with optional overrides."""
    try:
        return global_strategy_registry.clone_strategy(
            strategy_id=req.strategy_id,
            from_version=req.from_version,
            new_version=req.new_version,
            overrides=req.overrides
        )
    except KeyError as exc:
        raise HTTPException(status_code=404, detail=str(exc))
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc))


# ---------------------------------------------------------------------------
# Provenance Endpoints
# ---------------------------------------------------------------------------

@router.post("/provenance", response_model=ExperimentProvenance, status_code=status.HTTP_201_CREATED)
def record_provenance(prov: ExperimentProvenance):
    """Record an immutable experiment provenance entry."""
    return global_provenance_tracker.record_provenance(prov)


@router.get("/provenance/{strategy_id}/{version}", response_model=List[ExperimentProvenance])
def get_provenance(strategy_id: str, version: str):
    """Retrieve all provenance records for a given strategy version."""
    records = global_provenance_tracker.get_provenance_for_strategy(strategy_id, version)
    if not records:
        raise HTTPException(
            status_code=404,
            detail=f"No provenance records found for strategy '{strategy_id}' version '{version}'."
        )
    return records


@router.get("/provenance/{strategy_id}/{version}/lineage")
def get_lineage_graph(strategy_id: str, version: str):
    """Retrieve the complete DAG lineage graph for a strategy version."""
    return global_provenance_tracker.generate_lineage_graph(strategy_id, version)


# ---------------------------------------------------------------------------
# Configuration Drift & Gates Endpoints
# ---------------------------------------------------------------------------

@router.post("/drift-check", response_model=ConfigDriftReport)
def check_configuration_drift(req: DriftCheckRequest):
    """Run configuration drift detection comparing backtested parameters against deployment config."""
    return _drift_detector.evaluate_drift(
        strategy_id=req.strategy_id,
        strategy_version=req.strategy_version,
        backtest_config=req.backtest_config,
        paper_config=req.paper_config
    )


@router.post("/promote", response_model=PromotionEvaluation)
def evaluate_and_promote(req: PromoteRequest):
    """Evaluate promotion gates and transition strategy status if all gates pass."""
    strat = global_strategy_registry.get_strategy(req.strategy_id, req.strategy_version)
    if not strat:
        raise HTTPException(
            status_code=404,
            detail=f"Strategy '{req.strategy_id}' version '{req.strategy_version}' not found."
        )

    eval_result = global_promotion_gatekeeper.evaluate_promotion(
        strategy=strat,
        target_status=req.target_status,
        context=req.context
    )

    if eval_result.is_promoted:
        global_strategy_registry.update_status(
            req.strategy_id,
            req.strategy_version,
            req.target_status
        )

    return eval_result


# ---------------------------------------------------------------------------
# Workflow Orchestration Endpoints
# ---------------------------------------------------------------------------

@router.post("/jobs", response_model=WorkflowJob, status_code=status.HTTP_202_ACCEPTED)
def submit_job(req: JobSubmitRequest):
    """Submit a new pipeline orchestration job."""
    return global_workflow_orchestrator.submit_job(
        job_type=req.job_type,
        params=req.params,
        correlation_id=req.correlation_id,
        idempotency_key=req.idempotency_key
    )


@router.get("/jobs/{job_id}", response_model=WorkflowJob)
def get_job(job_id: str):
    """Retrieve status and results for an orchestrated job."""
    job = global_workflow_orchestrator.get_job(job_id)
    if not job:
        raise HTTPException(status_code=404, detail=f"Job '{job_id}' not found.")
    return job


@router.get("/jobs", response_model=List[WorkflowJob])
def list_jobs(
    status: Optional[JobStatus] = Query(None, description="Filter by job status"),
    job_type: Optional[str] = Query(None, description="Filter by job type"),
):
    """List workflow jobs."""
    return global_workflow_orchestrator.list_jobs(status=status, job_type=job_type)


# ---------------------------------------------------------------------------
# Overview & Summary Endpoints
# ---------------------------------------------------------------------------

@router.get("/overview")
def get_lifecycle_overview():
    """Retrieve high-level overview metrics of all strategies across lifecycle stages."""
    strategies = global_strategy_registry.list_strategies()
    status_counts: Dict[str, int] = {}
    for s in StrategyStatus:
        status_counts[s.value] = 0
    for strat in strategies:
        status_counts[strat.status.value] += 1

    jobs = global_workflow_orchestrator.list_jobs()
    job_counts: Dict[str, int] = {}
    for js in JobStatus:
        job_counts[js.value] = 0
    for j in jobs:
        job_counts[j.status.value] += 1

    return {
        "total_strategies": len(strategies),
        "status_distribution": status_counts,
        "total_jobs": len(jobs),
        "job_status_distribution": job_counts,
        "active_jobs_count": job_counts.get(JobStatus.RUNNING.value, 0) + job_counts.get(JobStatus.PENDING.value, 0)
    }
