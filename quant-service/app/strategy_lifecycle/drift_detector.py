"""Backtest-to-Paper Configuration Drift Detector.

Compares backtested strategy parameters against paper/live deployment configurations
and strictly blocks deployment when material drift, risk relaxation, or execution mismatches are detected.
"""

import uuid
from typing import Dict, List, Any
from datetime import datetime, timezone

from app.strategy_lifecycle.models import (
    ConfigDriftReport,
    DriftItem,
    DriftSeverity,
)


class ConfigDriftDetector:
    """Detects configuration drift between backtest assumptions and deployment parameters."""

    def __init__(self) -> None:
        pass

    def evaluate_drift(
        self,
        strategy_id: str,
        strategy_version: str,
        backtest_config: Dict[str, Any],
        paper_config: Dict[str, Any]
    ) -> ConfigDriftReport:
        """Perform comprehensive drift audit between backtest and deployment parameters."""
        report_id = f"drift-{uuid.uuid4().hex[:12]}"
        drift_items: List[DriftItem] = []
        blocking_reasons: List[str] = []

        # 1. Check Risk Parameters
        bt_risk = backtest_config.get("risk_parameters", {})
        paper_risk = paper_config.get("risk_parameters", {})
        self._check_risk_drift(bt_risk, paper_risk, drift_items, blocking_reasons)

        # 2. Check Execution Assumptions
        bt_exec = backtest_config.get("execution_config", {})
        paper_exec = paper_config.get("execution_config", {})
        self._check_execution_drift(bt_exec, paper_exec, drift_items, blocking_reasons)

        # 3. Check Universe / Target Instruments
        bt_universe = set(backtest_config.get("target_instruments", []))
        paper_universe = set(paper_config.get("target_instruments", []))
        self._check_universe_drift(bt_universe, paper_universe, drift_items, blocking_reasons)

        # 4. Check Model / Feature ID Alignment
        bt_feat = backtest_config.get("feature_set_id")
        paper_feat = paper_config.get("feature_set_id")
        if bt_feat and paper_feat and bt_feat != paper_feat:
            item = DriftItem(
                field="feature_set_id",
                backtest_value=bt_feat,
                paper_value=paper_feat,
                severity=DriftSeverity.CRITICAL,
                is_blocking=True,
                explanation=f"Feature schema mismatch: Backtest used '{bt_feat}' but deployment has '{paper_feat}'."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

        bt_model = backtest_config.get("model_id")
        paper_model = paper_config.get("model_id")
        if bt_model and paper_model and bt_model != paper_model:
            item = DriftItem(
                field="model_id",
                backtest_value=bt_model,
                paper_value=paper_model,
                severity=DriftSeverity.CRITICAL,
                is_blocking=True,
                explanation=f"Model checkpoint mismatch: Backtest used '{bt_model}' but deployment has '{paper_model}'."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

        # Calculate max severity and deployment blocking status
        drift_detected = len(drift_items) > 0
        max_severity = DriftSeverity.NONE
        if drift_detected:
            severities = [item.severity for item in drift_items]
            if DriftSeverity.CRITICAL in severities:
                max_severity = DriftSeverity.CRITICAL
            elif DriftSeverity.HIGH in severities:
                max_severity = DriftSeverity.HIGH
            elif DriftSeverity.MEDIUM in severities:
                max_severity = DriftSeverity.MEDIUM
            else:
                max_severity = DriftSeverity.LOW

        is_deployment_blocked = len(blocking_reasons) > 0

        return ConfigDriftReport(
            report_id=report_id,
            strategy_id=strategy_id,
            strategy_version=strategy_version,
            backtest_config=backtest_config,
            paper_config=paper_config,
            drift_detected=drift_detected,
            max_severity=max_severity,
            drift_items=drift_items,
            is_deployment_blocked=is_deployment_blocked,
            blocking_reasons=blocking_reasons,
            checked_at=datetime.now(timezone.utc)
        )

    def _check_risk_drift(
        self,
        bt_risk: Dict[str, Any],
        paper_risk: Dict[str, Any],
        drift_items: List[DriftItem],
        blocking_reasons: List[str]
    ) -> None:
        """Evaluate drift in risk parameters. Any relaxation of risk is strictly blocking."""
        # max_position_size: if paper allows larger positions than backtested
        bt_max_pos = bt_risk.get("max_position_size")
        paper_max_pos = paper_risk.get("max_position_size")
        if bt_max_pos is not None and paper_max_pos is not None and paper_max_pos > bt_max_pos:
            item = DriftItem(
                field="risk_parameters.max_position_size",
                backtest_value=bt_max_pos,
                paper_value=paper_max_pos,
                severity=DriftSeverity.CRITICAL,
                is_blocking=True,
                explanation=f"Position size relaxed: Backtest ({bt_max_pos:.2%}) < Paper ({paper_max_pos:.2%})."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

        # stop_loss_pct: if paper allows larger drawdown per trade than backtested
        bt_stop = bt_risk.get("stop_loss_pct")
        paper_stop = paper_risk.get("stop_loss_pct")
        if bt_stop is not None and paper_stop is not None and paper_stop > bt_stop:
            item = DriftItem(
                field="risk_parameters.stop_loss_pct",
                backtest_value=bt_stop,
                paper_value=paper_stop,
                severity=DriftSeverity.CRITICAL,
                is_blocking=True,
                explanation=f"Stop loss relaxed: Backtest ({bt_stop:.2%}) < Paper ({paper_stop:.2%})."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

        # max_drawdown_limit: if paper allows larger max portfolio drawdown than backtested
        bt_dd = bt_risk.get("max_drawdown_limit")
        paper_dd = paper_risk.get("max_drawdown_limit")
        if bt_dd is not None and paper_dd is not None and paper_dd > bt_dd:
            item = DriftItem(
                field="risk_parameters.max_drawdown_limit",
                backtest_value=bt_dd,
                paper_value=paper_dd,
                severity=DriftSeverity.CRITICAL,
                is_blocking=True,
                explanation=f"Max drawdown limit relaxed: Backtest ({bt_dd:.2%}) < Paper ({paper_dd:.2%})."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

        # max_leverage: if paper allows higher leverage than backtested
        bt_lev = bt_risk.get("max_leverage")
        paper_lev = paper_risk.get("max_leverage")
        if bt_lev is not None and paper_lev is not None and paper_lev > bt_lev:
            item = DriftItem(
                field="risk_parameters.max_leverage",
                backtest_value=bt_lev,
                paper_value=paper_lev,
                severity=DriftSeverity.CRITICAL,
                is_blocking=True,
                explanation=f"Leverage increased: Backtest ({bt_lev:.2f}x) < Paper ({paper_lev:.2f}x)."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

    def _check_execution_drift(
        self,
        bt_exec: Dict[str, Any],
        paper_exec: Dict[str, Any],
        drift_items: List[DriftItem],
        blocking_reasons: List[str]
    ) -> None:
        """Evaluate execution assumptions. Underestimating costs in paper/live is flagged."""
        # execution_delay_bars: if paper assumes faster execution than backtest (e.g. 0 vs 1)
        bt_delay = bt_exec.get("execution_delay_bars")
        paper_delay = paper_exec.get("execution_delay_bars")
        if bt_delay is not None and paper_delay is not None and paper_delay < bt_delay:
            item = DriftItem(
                field="execution_config.execution_delay_bars",
                backtest_value=bt_delay,
                paper_value=paper_delay,
                severity=DriftSeverity.CRITICAL,
                is_blocking=True,
                explanation=f"Execution delay discrepancy: Backtest used {bt_delay} bars delay, deployment set to {paper_delay}."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

        # slippage_bps: if deployment assumes lower slippage than tested
        bt_slip = bt_exec.get("slippage_bps")
        paper_slip = paper_exec.get("slippage_bps")
        if bt_slip is not None and paper_slip is not None and paper_slip < bt_slip:
            item = DriftItem(
                field="execution_config.slippage_bps",
                backtest_value=bt_slip,
                paper_value=paper_slip,
                severity=DriftSeverity.HIGH,
                is_blocking=True,
                explanation=f"Slippage optimism: Backtest modelled {bt_slip} bps, paper uses {paper_slip} bps."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

    def _check_universe_drift(
        self,
        bt_universe: set,
        paper_universe: set,
        drift_items: List[DriftItem],
        blocking_reasons: List[str]
    ) -> None:
        """Check for unexpected or missing instruments between backtest and deployment."""
        if not bt_universe and not paper_universe:
            return

        unexpected = paper_universe - bt_universe
        if unexpected:
            item = DriftItem(
                field="target_instruments",
                backtest_value=sorted(list(bt_universe)),
                paper_value=sorted(list(paper_universe)),
                severity=DriftSeverity.CRITICAL,
                is_blocking=True,
                explanation=f"Deployment universe contains unbacktested instruments: {sorted(list(unexpected))}."
            )
            drift_items.append(item)
            blocking_reasons.append(item.explanation)

        missing = bt_universe - paper_universe
        if missing:
            item = DriftItem(
                field="target_instruments",
                backtest_value=sorted(list(bt_universe)),
                paper_value=sorted(list(paper_universe)),
                severity=DriftSeverity.MEDIUM,
                is_blocking=False,
                explanation=f"Deployment universe is a subset of backtested universe (missing: {sorted(list(missing))})."
            )
            drift_items.append(item)
