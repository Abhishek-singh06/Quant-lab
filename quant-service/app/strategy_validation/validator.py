"""Pre-Deployment Strategy Validator.

Conducts comprehensive structural, statistical, lookahead, and recursive
validation on a strategy definition before paper or live deployment.
"""

import uuid
from typing import Dict, List, Optional, Any, Callable
from datetime import datetime, timezone
import pandas as pd

from app.strategy_lifecycle.models import StrategyDefinition
from app.strategy_validation.models import (
    StrategyValidationReport,
    StrategyValidationCheckItem,
    ValidationSeverity,
)
from app.strategy_validation.lookahead_analyzer import LookaheadAnalyzer
from app.strategy_validation.recursive_analyzer import RecursiveAnalyzer

VALID_TIMEFRAMES = {"1m", "3m", "5m", "15m", "30m", "1h", "2h", "4h", "1D", "1W"}


class StrategyValidator:
    """Pre-deployment verification scanner for quantitative trading strategies."""

    def __init__(self) -> None:
        self.lookahead_analyzer = LookaheadAnalyzer()
        self.recursive_analyzer = RecursiveAnalyzer()

    def validate_strategy(
        self,
        strategy: StrategyDefinition,
        feature_compute_fn: Optional[Callable[[pd.DataFrame], pd.DataFrame]] = None,
        sample_df: Optional[pd.DataFrame] = None,
        indicator_fns: Optional[Dict[str, Callable[[pd.DataFrame], pd.Series]]] = None
    ) -> StrategyValidationReport:
        """Run full pre-deployment validation suite on strategy definition and optional pipeline functions."""
        report_id = f"val-{uuid.uuid4().hex[:12]}"
        check_items: List[StrategyValidationCheckItem] = []
        blocking_reasons: List[str] = []

        # 1. Structural / Schema Checks
        self._check_specification(strategy, check_items, blocking_reasons)

        # 2. Risk Parameter Checks
        self._check_risk_parameters(strategy, check_items, blocking_reasons)

        # 3. Execution Parameter Checks
        self._check_execution_parameters(strategy, check_items, blocking_reasons)

        # 4. Optional Lookahead Bias Analysis
        if feature_compute_fn is not None and sample_df is not None:
            self._check_lookahead(strategy, feature_compute_fn, sample_df, check_items, blocking_reasons)

        # 5. Optional Recursive Indicator Stability Analysis
        if indicator_fns and sample_df is not None:
            self._check_recursive_indicators(strategy, indicator_fns, sample_df, check_items, blocking_reasons)

        passed_count = sum(1 for item in check_items if item.passed)
        failed_count = sum(1 for item in check_items if not item.passed)
        is_valid = len(blocking_reasons) == 0

        return StrategyValidationReport(
            report_id=report_id,
            strategy_id=strategy.strategy_id,
            strategy_version=strategy.version,
            is_valid=is_valid,
            checks_passed=passed_count,
            checks_failed=failed_count,
            check_items=check_items,
            blocking_reasons=blocking_reasons,
            validated_at=datetime.now(timezone.utc)
        )

    # -----------------------------------------------------------------------
    # Sub-Check Implementations
    # -----------------------------------------------------------------------

    def _check_specification(
        self,
        strategy: StrategyDefinition,
        items: List[StrategyValidationCheckItem],
        blocking: List[str]
    ) -> None:
        # Strategy ID and Version
        if not strategy.strategy_id or not strategy.version:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_ID_AND_VERSION",
                    category="SPECIFICATION",
                    passed=False,
                    severity=ValidationSeverity.CRITICAL,
                    details="strategy_id and version cannot be empty."
                )
            )
            blocking.append("Strategy ID or version is missing.")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_ID_AND_VERSION",
                    category="SPECIFICATION",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"Strategy '{strategy.strategy_id}' version '{strategy.version}' declared."
                )
            )

        # Target Universe
        if not strategy.target_instruments:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_UNIVERSE_NON_EMPTY",
                    category="SPECIFICATION",
                    passed=False,
                    severity=ValidationSeverity.CRITICAL,
                    details="target_instruments list is empty."
                )
            )
            blocking.append("Universe is empty.")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_UNIVERSE_NON_EMPTY",
                    category="SPECIFICATION",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"Universe contains {len(strategy.target_instruments)} instruments."
                )
            )

        # Timeframe
        if strategy.timeframe not in VALID_TIMEFRAMES:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_TIMEFRAME_VALID",
                    category="SPECIFICATION",
                    passed=False,
                    severity=ValidationSeverity.ERROR,
                    details=f"Invalid timeframe '{strategy.timeframe}'. Valid options: {VALID_TIMEFRAMES}."
                )
            )
            blocking.append(f"Invalid timeframe: {strategy.timeframe}")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_TIMEFRAME_VALID",
                    category="SPECIFICATION",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"Timeframe '{strategy.timeframe}' is valid."
                )
            )

        # Warmup and Lookback
        if strategy.warmup_period_bars < 20:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_WARMUP_SUFFICIENCY",
                    category="SPECIFICATION",
                    passed=False,
                    severity=ValidationSeverity.WARNING,
                    details=f"Warmup period ({strategy.warmup_period_bars} bars) is below recommended minimum 20 bars."
                )
            )
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_WARMUP_SUFFICIENCY",
                    category="SPECIFICATION",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"Warmup period ({strategy.warmup_period_bars} bars) is sufficient."
                )
            )

        if strategy.required_lookback_bars < strategy.warmup_period_bars:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_LOOKBACK_CONSISTENCY",
                    category="SPECIFICATION",
                    passed=False,
                    severity=ValidationSeverity.CRITICAL,
                    details=f"Required lookback ({strategy.required_lookback_bars}) cannot be less than warmup ({strategy.warmup_period_bars})."
                )
            )
            blocking.append("Required lookback is less than warmup period.")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="SPEC_LOOKBACK_CONSISTENCY",
                    category="SPECIFICATION",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"Required lookback ({strategy.required_lookback_bars} bars) is consistent."
                )
            )

    def _check_risk_parameters(
        self,
        strategy: StrategyDefinition,
        items: List[StrategyValidationCheckItem],
        blocking: List[str]
    ) -> None:
        risk = strategy.risk_parameters or {}
        max_pos = risk.get("max_position_size")
        stop_loss = risk.get("stop_loss_pct")
        max_lev = risk.get("max_leverage", 1.0)
        max_dd = risk.get("max_drawdown_limit")

        # Position Sizing
        if max_pos is None or not (0.0 < max_pos <= 1.0):
            items.append(
                StrategyValidationCheckItem(
                    check_name="RISK_MAX_POSITION_SIZE",
                    category="RISK",
                    passed=False,
                    severity=ValidationSeverity.CRITICAL,
                    details=f"max_position_size ({max_pos}) must be in range (0.0, 1.0]."
                )
            )
            blocking.append(f"Invalid max_position_size: {max_pos}")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="RISK_MAX_POSITION_SIZE",
                    category="RISK",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"max_position_size ({max_pos:.2%}) is within safety limits."
                )
            )

        # Stop Loss
        if stop_loss is None or not (0.0 < stop_loss <= 0.50):
            items.append(
                StrategyValidationCheckItem(
                    check_name="RISK_STOP_LOSS_BOUNDS",
                    category="RISK",
                    passed=False,
                    severity=ValidationSeverity.CRITICAL,
                    details=f"stop_loss_pct ({stop_loss}) must be in range (0.0, 0.50]."
                )
            )
            blocking.append(f"Invalid stop_loss_pct: {stop_loss}")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="RISK_STOP_LOSS_BOUNDS",
                    category="RISK",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"stop_loss_pct ({stop_loss:.2%}) is bounded."
                )
            )

        # Leverage
        if max_lev < 1.0 or max_lev > 5.0:
            items.append(
                StrategyValidationCheckItem(
                    check_name="RISK_MAX_LEVERAGE",
                    category="RISK",
                    passed=False,
                    severity=ValidationSeverity.CRITICAL,
                    details=f"max_leverage ({max_lev}x) exceeds allowed ceiling (1.0x - 5.0x)."
                )
            )
            blocking.append(f"Excessive leverage: {max_lev}x")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="RISK_MAX_LEVERAGE",
                    category="RISK",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"max_leverage ({max_lev:.1f}x) conforms to equity limits."
                )
            )

    def _check_execution_parameters(
        self,
        strategy: StrategyDefinition,
        items: List[StrategyValidationCheckItem],
        blocking: List[str]
    ) -> None:
        exec_cfg = strategy.execution_config or {}
        delay = exec_cfg.get("execution_delay_bars", 1)
        slippage = exec_cfg.get("slippage_bps", 0.0)

        # Execution Delay Bars (must be >= 1 for realistic T+1 Indian equity execution)
        if delay < 1:
            items.append(
                StrategyValidationCheckItem(
                    check_name="EXEC_DELAY_CAUSALITY",
                    category="EXECUTION",
                    passed=False,
                    severity=ValidationSeverity.CRITICAL,
                    details=f"execution_delay_bars ({delay}) must be >= 1 to prevent lookahead execution."
                )
            )
            blocking.append("execution_delay_bars < 1 violates causality.")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="EXEC_DELAY_CAUSALITY",
                    category="EXECUTION",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"execution_delay_bars ({delay}) satisfies T+1 / next-bar causality."
                )
            )

        if slippage < 0.0:
            items.append(
                StrategyValidationCheckItem(
                    check_name="EXEC_SLIPPAGE_NON_NEGATIVE",
                    category="EXECUTION",
                    passed=False,
                    severity=ValidationSeverity.CRITICAL,
                    details=f"slippage_bps ({slippage}) cannot be negative."
                )
            )
            blocking.append("Negative slippage is invalid.")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="EXEC_SLIPPAGE_NON_NEGATIVE",
                    category="EXECUTION",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"slippage_bps ({slippage} bps) is valid."
                )
            )

    def _check_lookahead(
        self,
        strategy: StrategyDefinition,
        compute_fn: Callable[[pd.DataFrame], pd.DataFrame],
        df: pd.DataFrame,
        items: List[StrategyValidationCheckItem],
        blocking: List[str]
    ) -> None:
        report = self.lookahead_analyzer.analyze_transformation_causality(
            compute_fn=compute_fn,
            df=df,
            strategy_id=strategy.strategy_id
        )

        if not report.is_clean:
            for finding in report.findings:
                items.append(
                    StrategyValidationCheckItem(
                        check_name=f"LOOKAHEAD_{finding.column or 'TRANSFORM'}",
                        category="LOOKAHEAD",
                        passed=False,
                        severity=finding.severity,
                        details=finding.description
                    )
                )
            if report.has_critical_lookahead:
                blocking.append("Lookahead bias detected in feature computation.")
        else:
            items.append(
                StrategyValidationCheckItem(
                    check_name="LOOKAHEAD_CAUSALITY_SCAN",
                    category="LOOKAHEAD",
                    passed=True,
                    severity=ValidationSeverity.INFO,
                    details=f"Lookahead scan verified clean ({report.total_checks_run} incremental checks passed)."
                )
            )

    def _check_recursive_indicators(
        self,
        strategy: StrategyDefinition,
        indicator_fns: Dict[str, Callable[[pd.DataFrame], pd.Series]],
        df: pd.DataFrame,
        items: List[StrategyValidationCheckItem],
        blocking: List[str]
    ) -> None:
        for ind_name, ind_fn in indicator_fns.items():
            rep = self.recursive_analyzer.analyze_warmup_convergence(
                indicator_fn=ind_fn,
                df=df,
                indicator_name=ind_name
            )
            if not rep.is_stable:
                items.append(
                    StrategyValidationCheckItem(
                        check_name=f"RECURSIVE_STABILITY_{ind_name}",
                        category="RECURSIVE",
                        passed=False,
                        severity=ValidationSeverity.WARNING,
                        details=f"Indicator '{ind_name}' convergence issue: {'; '.join(rep.findings)}"
                    )
                )
            else:
                items.append(
                    StrategyValidationCheckItem(
                        check_name=f"RECURSIVE_STABILITY_{ind_name}",
                        category="RECURSIVE",
                        passed=True,
                        severity=ValidationSeverity.INFO,
                        details=f"Indicator '{ind_name}' is stable with {rep.recommended_min_warmup_bars} bars warmup."
                    )
                )
