"""Strategy Promotion Gatekeeper and State Machine.

Enforces strict multi-tier promotion gates from research and backtesting
through paper trading and human-in-the-loop live approval.
"""

from typing import Dict, List, Optional, Any
from datetime import datetime, timezone

from app.strategy_lifecycle.models import (
    StrategyDefinition,
    StrategyStatus,
    GateStatus,
    PromotionGateResult,
    PromotionEvaluation,
    ExperimentProvenance,
    ConfigDriftReport,
)
from app.strategy_lifecycle.drift_detector import ConfigDriftDetector


# Allowed forward lifecycle state transitions
VALID_TRANSITIONS = {
    StrategyStatus.DRAFT: [StrategyStatus.RESEARCH, StrategyStatus.ARCHIVED],
    StrategyStatus.RESEARCH: [StrategyStatus.VALIDATED, StrategyStatus.DEPRECATED, StrategyStatus.ARCHIVED],
    StrategyStatus.VALIDATED: [StrategyStatus.BACKTESTED, StrategyStatus.RESEARCH, StrategyStatus.DEPRECATED],
    StrategyStatus.BACKTESTED: [StrategyStatus.PAPER_ELIGIBLE, StrategyStatus.RESEARCH, StrategyStatus.DEPRECATED],
    StrategyStatus.PAPER_ELIGIBLE: [StrategyStatus.PAPER_RUNNING, StrategyStatus.BACKTESTED, StrategyStatus.DEPRECATED],
    StrategyStatus.PAPER_RUNNING: [StrategyStatus.PAPER_VALIDATED, StrategyStatus.PAPER_ELIGIBLE, StrategyStatus.DEPRECATED],
    StrategyStatus.PAPER_VALIDATED: [StrategyStatus.MANUAL_REVIEW, StrategyStatus.PAPER_RUNNING, StrategyStatus.DEPRECATED],
    StrategyStatus.MANUAL_REVIEW: [StrategyStatus.LIVE_ELIGIBLE, StrategyStatus.PAPER_VALIDATED, StrategyStatus.DEPRECATED],
    StrategyStatus.LIVE_ELIGIBLE: [StrategyStatus.PAPER_RUNNING, StrategyStatus.DEPRECATED, StrategyStatus.ARCHIVED],
    StrategyStatus.DEPRECATED: [StrategyStatus.ARCHIVED, StrategyStatus.DRAFT],
    StrategyStatus.ARCHIVED: [StrategyStatus.DRAFT],
}


class StrategyPromotionGatekeeper:
    """Evaluates promotion criteria and enforces state transitions."""

    def __init__(self) -> None:
        self.drift_detector = ConfigDriftDetector()

    def evaluate_promotion(
        self,
        strategy: StrategyDefinition,
        target_status: StrategyStatus,
        context: Optional[Dict[str, Any]] = None
    ) -> PromotionEvaluation:
        """Evaluate whether a strategy satisfies all gates required to move to target_status."""
        context = context or {}
        current_status = strategy.status
        gate_results: List[PromotionGateResult] = []
        blocking_reasons: List[str] = []

        # 1. Check if transition is topologically valid
        allowed_next = VALID_TRANSITIONS.get(current_status, [])
        if target_status not in allowed_next:
            return PromotionEvaluation(
                strategy_id=strategy.strategy_id,
                strategy_version=strategy.version,
                current_status=current_status,
                target_status=target_status,
                is_promoted=False,
                gate_results=[
                    PromotionGateResult(
                        gate_name="STATE_TRANSITION_VALIDITY",
                        status=GateStatus.BLOCKED,
                        description="Verify valid lifecycle progression",
                        failure_reasons=[
                            f"Illegal transition from '{current_status.value}' to '{target_status.value}'. "
                            f"Allowed transitions: {[s.value for s in allowed_next]}"
                        ]
                    )
                ],
                blocking_reasons=[
                    f"Illegal transition from '{current_status.value}' to '{target_status.value}'. "
                    f"Allowed transitions: {[s.value for s in allowed_next]}"
                ],
                evaluated_at=datetime.now(timezone.utc)
            )

        # 2. Evaluate target status specific gate criteria
        if target_status == StrategyStatus.RESEARCH:
            self._eval_research_gate(strategy, context, gate_results, blocking_reasons)
        elif target_status == StrategyStatus.VALIDATED:
            self._eval_model_validation_gate(strategy, context, gate_results, blocking_reasons)
        elif target_status == StrategyStatus.BACKTESTED:
            self._eval_backtest_gate(strategy, context, gate_results, blocking_reasons)
        elif target_status == StrategyStatus.PAPER_ELIGIBLE:
            self._eval_paper_eligible_gate(strategy, context, gate_results, blocking_reasons)
        elif target_status == StrategyStatus.PAPER_RUNNING:
            self._eval_paper_running_gate(strategy, context, gate_results, blocking_reasons)
        elif target_status == StrategyStatus.PAPER_VALIDATED:
            self._eval_paper_validated_gate(strategy, context, gate_results, blocking_reasons)
        elif target_status == StrategyStatus.MANUAL_REVIEW:
            self._eval_manual_review_gate(strategy, context, gate_results, blocking_reasons)
        elif target_status == StrategyStatus.LIVE_ELIGIBLE:
            self._eval_live_eligible_gate(strategy, context, gate_results, blocking_reasons)
        elif target_status in (StrategyStatus.DEPRECATED, StrategyStatus.ARCHIVED):
            gate_results.append(
                PromotionGateResult(
                    gate_name="DECOMMISSION_APPROVAL",
                    status=GateStatus.PASSED,
                    description="Decommissioning/archiving strategy version",
                )
            )

        is_promoted = len(blocking_reasons) == 0 and all(g.status == GateStatus.PASSED for g in gate_results)

        return PromotionEvaluation(
            strategy_id=strategy.strategy_id,
            strategy_version=strategy.version,
            current_status=current_status,
            target_status=target_status,
            is_promoted=is_promoted,
            gate_results=gate_results,
            blocking_reasons=blocking_reasons,
            evaluated_at=datetime.now(timezone.utc),
            comments=context.get("comments")
        )

    # -----------------------------------------------------------------------
    # Individual Gate Evaluators
    # -----------------------------------------------------------------------

    def _eval_research_gate(
        self,
        strategy: StrategyDefinition,
        context: Dict[str, Any],
        results: List[PromotionGateResult],
        blocking: List[str]
    ) -> None:
        failures = []
        if not strategy.target_instruments:
            failures.append("Strategy target_instruments cannot be empty.")
        if not strategy.feature_names and not strategy.feature_set_id:
            failures.append("Strategy must specify feature_names or a valid feature_set_id.")
        if not strategy.risk_parameters:
            failures.append("Strategy must specify risk_parameters.")

        status = GateStatus.PASSED if not failures else GateStatus.FAILED
        if failures:
            blocking.extend(failures)
        results.append(
            PromotionGateResult(
                gate_name="GATE_SPEC_COMPLETENESS",
                status=status,
                description="Verify strategy specification completeness and schema sanity",
                failure_reasons=failures
            )
        )

    def _eval_model_validation_gate(
        self,
        strategy: StrategyDefinition,
        context: Dict[str, Any],
        results: List[PromotionGateResult],
        blocking: List[str]
    ) -> None:
        failures = []
        metrics = context.get("validation_metrics", {})
        min_ic = context.get("min_ic", 0.02)
        min_accuracy = context.get("min_accuracy", 0.51)

        ic = metrics.get("ic", 0.0)
        acc = metrics.get("accuracy", 0.0)

        if not strategy.model_id and "model_id" not in context:
            failures.append("No trained model_id associated with strategy.")
        if not metrics:
            failures.append("Missing validation_metrics in evaluation context.")
        elif ic < min_ic and acc < min_accuracy:
            failures.append(f"Model metrics below threshold: IC={ic:.4f} (req >= {min_ic}), Acc={acc:.4f} (req >= {min_accuracy}).")

        status = GateStatus.PASSED if not failures else GateStatus.FAILED
        if failures:
            blocking.extend(failures)
        results.append(
            PromotionGateResult(
                gate_name="GATE_MODEL_VALIDATION",
                status=status,
                description="Verify out-of-sample model predictive power and stability",
                metrics_evaluated=metrics,
                thresholds={"min_ic": min_ic, "min_accuracy": min_accuracy},
                failure_reasons=failures
            )
        )

    def _eval_backtest_gate(
        self,
        strategy: StrategyDefinition,
        context: Dict[str, Any],
        results: List[PromotionGateResult],
        blocking: List[str]
    ) -> None:
        failures = []
        metrics = context.get("backtest_metrics", {})
        min_sharpe = context.get("min_sharpe", 1.0)
        max_drawdown = context.get("max_drawdown", 0.25)
        min_trades = context.get("min_trades", 20)
        min_profit_factor = context.get("min_profit_factor", 1.10)

        sharpe = metrics.get("sharpe_ratio", 0.0)
        mdd = metrics.get("max_drawdown", 1.0)
        trades = metrics.get("total_trades", 0)
        pf = metrics.get("profit_factor", 0.0)

        if not metrics:
            failures.append("No backtest_metrics provided for evaluation.")
        else:
            if sharpe < min_sharpe:
                failures.append(f"Sharpe ratio {sharpe:.2f} < required {min_sharpe:.2f}.")
            if mdd > max_drawdown:
                failures.append(f"Max drawdown {mdd:.2%} > allowed {max_drawdown:.2%}.")
            if trades < min_trades:
                failures.append(f"Total trades {trades} < minimum required {min_trades}.")
            if pf < min_profit_factor:
                failures.append(f"Profit factor {pf:.2f} < minimum required {min_profit_factor:.2f}.")

        status = GateStatus.PASSED if not failures else GateStatus.FAILED
        if failures:
            blocking.extend(failures)
        results.append(
            PromotionGateResult(
                gate_name="GATE_BACKTEST_PERFORMANCE",
                status=status,
                description="Verify backtest risk-adjusted returns, sample size, and drawdown limits",
                metrics_evaluated=metrics,
                thresholds={
                    "min_sharpe": min_sharpe,
                    "max_drawdown": max_drawdown,
                    "min_trades": min_trades,
                    "min_profit_factor": min_profit_factor
                },
                failure_reasons=failures
            )
        )

    def _eval_paper_eligible_gate(
        self,
        strategy: StrategyDefinition,
        context: Dict[str, Any],
        results: List[PromotionGateResult],
        blocking: List[str]
    ) -> None:
        failures = []
        # Check provenance
        provenance: Optional[ExperimentProvenance] = context.get("provenance")
        if not provenance:
            failures.append("No ExperimentProvenance record found for strategy.")
        
        # Check configuration drift
        paper_config = context.get("paper_config", strategy.model_dump())
        drift_report = self.drift_detector.evaluate_drift(
            strategy_id=strategy.strategy_id,
            strategy_version=strategy.version,
            backtest_config=strategy.model_dump(),
            paper_config=paper_config
        )
        if drift_report.is_deployment_blocked:
            failures.extend(drift_report.blocking_reasons)

        status = GateStatus.PASSED if not failures else GateStatus.FAILED
        if failures:
            blocking.extend(failures)
        results.append(
            PromotionGateResult(
                gate_name="GATE_DRIFT_AND_PIT_AUDIT",
                status=status,
                description="Verify PIT integrity and ensure zero blocking configuration drift",
                metrics_evaluated={"drift_detected": drift_report.drift_detected, "max_severity": drift_report.max_severity},
                failure_reasons=failures
            )
        )

    def _eval_paper_running_gate(
        self,
        strategy: StrategyDefinition,
        context: Dict[str, Any],
        results: List[PromotionGateResult],
        blocking: List[str]
    ) -> None:
        failures = []
        # Ensure paper mode is active and live trading is strictly disabled
        is_paper_mode = context.get("paper_trading", True)
        live_trading_enabled = context.get("live_trading_enabled", False)

        if not is_paper_mode:
            failures.append("Deployment target must be designated as PAPER trading.")
        if live_trading_enabled:
            failures.append("Safety violation: Live trading flag is enabled during paper deployment.")

        status = GateStatus.PASSED if not failures else GateStatus.FAILED
        if failures:
            blocking.extend(failures)
        results.append(
            PromotionGateResult(
                gate_name="GATE_PAPER_DEPLOYMENT_SAFETY",
                status=status,
                description="Verify paper broker isolation and live execution safety controls",
                failure_reasons=failures
            )
        )

    def _eval_paper_validated_gate(
        self,
        strategy: StrategyDefinition,
        context: Dict[str, Any],
        results: List[PromotionGateResult],
        blocking: List[str]
    ) -> None:
        failures = []
        paper_metrics = context.get("paper_metrics", {})
        min_paper_trades = context.get("min_paper_trades", 10)
        max_slippage_error = context.get("max_slippage_error", 0.35)

        trades = paper_metrics.get("trade_count", 0)
        slippage_err = paper_metrics.get("slippage_error_pct", 0.0)
        paper_sharpe = paper_metrics.get("sharpe_ratio", 0.0)

        if not paper_metrics:
            failures.append("No paper_metrics provided for paper validation gate.")
        else:
            if trades < min_paper_trades:
                failures.append(f"Paper trade count ({trades}) below required minimum ({min_paper_trades}).")
            if slippage_err > max_slippage_error:
                failures.append(f"Realized slippage discrepancy ({slippage_err:.2%}) exceeds tolerance ({max_slippage_error:.2%}).")
            if paper_sharpe < 0.5:
                failures.append(f"Paper Sharpe ratio ({paper_sharpe:.2f}) < 0.50.")

        status = GateStatus.PASSED if not failures else GateStatus.FAILED
        if failures:
            blocking.extend(failures)
        results.append(
            PromotionGateResult(
                gate_name="GATE_PAPER_EXECUTION_STABILITY",
                status=status,
                description="Verify paper execution stability, trade counts, and slippage calibration",
                metrics_evaluated=paper_metrics,
                thresholds={"min_paper_trades": min_paper_trades, "max_slippage_error": max_slippage_error},
                failure_reasons=failures
            )
        )

    def _eval_manual_review_gate(
        self,
        strategy: StrategyDefinition,
        context: Dict[str, Any],
        results: List[PromotionGateResult],
        blocking: List[str]
    ) -> None:
        # Pre-live readiness check before human signoff
        results.append(
            PromotionGateResult(
                gate_name="GATE_PRE_LIVE_COMPLIANCE",
                status=GateStatus.PASSED,
                description="Ready for human compliance and quantitative committee sign-off",
            )
        )

    def _eval_live_eligible_gate(
        self,
        strategy: StrategyDefinition,
        context: Dict[str, Any],
        results: List[PromotionGateResult],
        blocking: List[str]
    ) -> None:
        failures = []
        signoff_author = context.get("reviewer_id")
        signoff_approved = context.get("human_approved", False)
        confirmation_enforced = context.get("require_user_confirmation", True)

        if not signoff_author or not signoff_approved:
            failures.append("Live eligibility requires explicit human reviewer approval and sign-off.")
        if not confirmation_enforced:
            failures.append("Order placement must strictly require user confirmation.")

        status = GateStatus.PASSED if not failures else GateStatus.FAILED
        if failures:
            blocking.extend(failures)
        results.append(
            PromotionGateResult(
                gate_name="GATE_LIVE_HUMAN_SIGNOFF",
                status=status,
                description="Enforce explicit human sign-off and mandatory user confirmation constraints",
                failure_reasons=failures
            )
        )


# Global singleton instance
global_promotion_gatekeeper = StrategyPromotionGatekeeper()
