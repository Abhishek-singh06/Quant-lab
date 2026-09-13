"""Workflow Orchestrator for Strategy Lifecycle.

Provides lifecycle pipeline orchestration, asynchronous task dispatching,
job state tracking, correlation ID propagation, and idempotency guarantees.
"""

import uuid
import threading
from typing import Dict, List, Optional, Any, Callable
from datetime import datetime, timezone

from app.strategy_lifecycle.models import (
    WorkflowJob,
    JobStatus,
    StrategyDefinition,
    StrategyStatus,
    ExperimentProvenance,
)
from app.strategy_lifecycle.registry import global_strategy_registry
from app.strategy_lifecycle.provenance import global_provenance_tracker, compute_hash
from app.strategy_lifecycle.drift_detector import ConfigDriftDetector
from app.strategy_lifecycle.gates import global_promotion_gatekeeper


class WorkflowOrchestrator:
    """Orchestrates end-to-end quantitative lifecycle workflows and tracks job states."""

    def __init__(self) -> None:
        self._lock = threading.RLock()
        self._jobs: Dict[str, WorkflowJob] = {}
        self._idempotency_map: Dict[str, str] = {}  # idempotency_key -> job_id
        self.drift_detector = ConfigDriftDetector()

    def submit_job(
        self,
        job_type: str,
        params: Dict[str, Any],
        correlation_id: Optional[str] = None,
        idempotency_key: Optional[str] = None
    ) -> WorkflowJob:
        """Submit a new pipeline job with idempotency deduplication."""
        with self._lock:
            if idempotency_key and idempotency_key in self._idempotency_map:
                existing_id = self._idempotency_map[idempotency_key]
                return self._jobs[existing_id].model_copy(deep=True)

            job_id = f"job-{uuid.uuid4().hex[:12]}"
            corr_id = correlation_id or f"corr-{uuid.uuid4().hex[:8]}"

            job = WorkflowJob(
                job_id=job_id,
                job_type=job_type,
                correlation_id=corr_id,
                status=JobStatus.PENDING,
                params=params,
                created_at=datetime.now(timezone.utc),
                idempotency_key=idempotency_key
            )

            self._jobs[job_id] = job
            if idempotency_key:
                self._idempotency_map[idempotency_key] = job_id

            return job.model_copy(deep=True)

    def get_job(self, job_id: str) -> Optional[WorkflowJob]:
        """Retrieve job status by job ID."""
        with self._lock:
            job = self._jobs.get(job_id)
            return job.model_copy(deep=True) if job else None

    def list_jobs(
        self,
        status: Optional[JobStatus] = None,
        job_type: Optional[str] = None
    ) -> List[WorkflowJob]:
        """List jobs filtered by optional criteria."""
        with self._lock:
            results = []
            for j in self._jobs.values():
                if status and j.status != status:
                    continue
                if job_type and j.job_type != job_type:
                    continue
                results.append(j.model_copy(deep=True))
            return sorted(results, key=lambda x: x.created_at, reverse=True)

    def execute_lifecycle_pipeline(
        self,
        strategy: StrategyDefinition,
        validation_metrics: Optional[Dict[str, Any]] = None,
        backtest_metrics: Optional[Dict[str, Any]] = None,
        correlation_id: Optional[str] = None
    ) -> WorkflowJob:
        """Execute full synchronous lifecycle workflow: Registration -> Provenance -> Gate Progression."""
        job = self.submit_job(
            job_type="LIFECYCLE_PROGRESSION",
            params={
                "strategy_id": strategy.strategy_id,
                "version": strategy.version,
                "target_instruments": strategy.target_instruments,
            },
            correlation_id=correlation_id
        )

        with self._lock:
            active_job = self._jobs[job.job_id]
            active_job.status = JobStatus.RUNNING
            active_job.started_at = datetime.now(timezone.utc)

        try:
            # 1. Register strategy in registry if not already present
            existing = global_strategy_registry.get_strategy(strategy.strategy_id, strategy.version)
            if not existing:
                registered = global_strategy_registry.register_strategy(strategy)
            else:
                registered = existing

            current_strat = registered
            evaluations = []

            # 2. Gate 1: DRAFT -> RESEARCH
            if current_strat.status == StrategyStatus.DRAFT:
                eval_res = global_promotion_gatekeeper.evaluate_promotion(
                    current_strat, StrategyStatus.RESEARCH
                )
                evaluations.append(eval_res.model_dump())
                if eval_res.is_promoted:
                    current_strat = global_strategy_registry.update_status(
                        current_strat.strategy_id, current_strat.version, StrategyStatus.RESEARCH
                    )

            # 3. Gate 2: RESEARCH -> VALIDATED
            if current_strat.status == StrategyStatus.RESEARCH and validation_metrics:
                eval_res = global_promotion_gatekeeper.evaluate_promotion(
                    current_strat,
                    StrategyStatus.VALIDATED,
                    context={"validation_metrics": validation_metrics}
                )
                evaluations.append(eval_res.model_dump())
                if eval_res.is_promoted:
                    current_strat = global_strategy_registry.update_status(
                        current_strat.strategy_id, current_strat.version, StrategyStatus.VALIDATED
                    )

            # 4. Gate 3: VALIDATED -> BACKTESTED
            provenance_rec = None
            if current_strat.status == StrategyStatus.VALIDATED and backtest_metrics:
                eval_res = global_promotion_gatekeeper.evaluate_promotion(
                    current_strat,
                    StrategyStatus.BACKTESTED,
                    context={"backtest_metrics": backtest_metrics}
                )
                evaluations.append(eval_res.model_dump())
                if eval_res.is_promoted:
                    current_strat = global_strategy_registry.update_status(
                        current_strat.strategy_id, current_strat.version, StrategyStatus.BACKTESTED
                    )

                    # Record Provenance
                    prov = ExperimentProvenance(
                        provenance_id=f"prov-{uuid.uuid4().hex[:12]}",
                        strategy_id=current_strat.strategy_id,
                        strategy_version=current_strat.version,
                        data_version="canonical-v1.0",
                        data_hash=compute_hash({"instruments": current_strat.target_instruments}),
                        pit_timestamp=datetime.now(timezone.utc),
                        feature_version="alpha158-v1.0",
                        feature_config_hash=compute_hash(current_strat.feature_names),
                        model_version="gbdt-v1.0",
                        model_checkpoint=f"checkpoints/{current_strat.strategy_id}-{current_strat.version}.pkl",
                        backtest_id=f"bt-{uuid.uuid4().hex[:8]}",
                        backtest_metrics=backtest_metrics,
                        run_id=job.job_id
                    )
                    provenance_rec = global_provenance_tracker.record_provenance(prov)

            # 5. Gate 4: BACKTESTED -> PAPER_ELIGIBLE
            if current_strat.status == StrategyStatus.BACKTESTED and provenance_rec:
                eval_res = global_promotion_gatekeeper.evaluate_promotion(
                    current_strat,
                    StrategyStatus.PAPER_ELIGIBLE,
                    context={"provenance": provenance_rec}
                )
                evaluations.append(eval_res.model_dump())
                if eval_res.is_promoted:
                    current_strat = global_strategy_registry.update_status(
                        current_strat.strategy_id, current_strat.version, StrategyStatus.PAPER_ELIGIBLE
                    )

            with self._lock:
                active_job = self._jobs[job.job_id]
                active_job.status = JobStatus.COMPLETED
                active_job.completed_at = datetime.now(timezone.utc)
                active_job.result = {
                    "strategy_id": current_strat.strategy_id,
                    "version": current_strat.version,
                    "final_status": current_strat.status.value,
                    "evaluations": evaluations,
                    "provenance_recorded": provenance_rec is not None
                }
                return active_job.model_copy(deep=True)

        except Exception as exc:
            with self._lock:
                active_job = self._jobs[job.job_id]
                active_job.status = JobStatus.FAILED
                active_job.completed_at = datetime.now(timezone.utc)
                active_job.error = str(exc)
                return active_job.model_copy(deep=True)

    def clear(self) -> None:
        """Clear all job state (primarily for testing)."""
        with self._lock:
            self._jobs.clear()
            self._idempotency_map.clear()


# Global singleton instance
global_workflow_orchestrator = WorkflowOrchestrator()
