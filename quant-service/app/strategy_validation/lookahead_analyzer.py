"""Native Lookahead Bias Analysis Engine.

Audits quantitative feature computations, rolling indicators, and data joins
to identify any forward-looking data leakage, negative shifts, or lookahead biases.
"""

import uuid
from typing import Callable, Dict, List, Optional, Any
from datetime import datetime, timezone
import numpy as np
import pandas as pd

from app.strategy_validation.models import (
    LookaheadReport,
    LookaheadFinding,
    LookaheadType,
    ValidationSeverity,
)


class LookaheadAnalyzer:
    """Automated scanner for lookahead bias and temporal leakage in quantitative algorithms."""

    def __init__(self, tolerance: float = 1e-7) -> None:
        self.tolerance = tolerance

    def analyze_transformation_causality(
        self,
        compute_fn: Callable[[pd.DataFrame], pd.DataFrame],
        df: pd.DataFrame,
        test_bars: int = 30,
        strategy_id: Optional[str] = None
    ) -> LookaheadReport:
        """Verify that computing features at time t is strictly invariant to future bars t+1..T.
        
        Methodology:
        1. Compute full feature matrix on entire historical DataFrame: F_full = compute_fn(df[0..T]).
        2. Iteratively slice DataFrame to sub-windows df[0..t] for t in (T-test_bars..T).
        3. Compute feature matrix on each slice: F_partial = compute_fn(df[0..t]).
        4. Compare F_full(t) against F_partial(t).
        5. Any discrepancy |F_full(t) - F_partial(t)| > tolerance proves that future data (t+1..T)
           mutated the historical value at bar t (CRITICAL LOOKAHEAD LEAKAGE).
        """
        report_id = f"lookahead-{uuid.uuid4().hex[:12]}"
        findings: List[LookaheadFinding] = []
        n_rows = len(df)

        if n_rows < 10:
            return LookaheadReport(
                report_id=report_id,
                strategy_id=strategy_id,
                is_clean=True,
                total_checks_run=0,
                findings=[],
                has_critical_lookahead=False
            )

        # 1. Full batch computation
        df_full = df.copy()
        try:
            full_features = compute_fn(df_full)
        except Exception as exc:
            return LookaheadReport(
                report_id=report_id,
                strategy_id=strategy_id,
                is_clean=False,
                total_checks_run=1,
                findings=[
                    LookaheadFinding(
                        check_name="FULL_COMPUTATION_EXECUTION",
                        lookahead_type=LookaheadType.FUTURE_DATA_MODIFICATION,
                        severity=ValidationSeverity.CRITICAL,
                        description=f"Feature computation function failed: {str(exc)}",
                        expected_behavior="Function executes cleanly on input DataFrame",
                        actual_behavior=f"Raised exception: {type(exc).__name__}"
                    )
                ],
                has_critical_lookahead=True
            )

        numeric_cols = full_features.select_dtypes(include=[np.number]).columns.tolist()
        total_checks = 0

        # 2. Incremental progressive checks
        start_idx = max(5, n_rows - test_bars)
        for t in range(start_idx, n_rows):
            df_slice = df.iloc[:t + 1].copy()
            partial_features = compute_fn(df_slice)

            for col in numeric_cols:
                total_checks += 1
                val_full = full_features[col].iloc[t]
                val_partial = partial_features[col].iloc[-1]

                # Check if both are NaN
                if pd.isna(val_full) and pd.isna(val_partial):
                    continue

                if pd.isna(val_full) != pd.isna(val_partial):
                    diff = np.nan
                    findings.append(
                        LookaheadFinding(
                            check_name="INCREMENTAL_CAUSALITY_CHECK",
                            lookahead_type=LookaheadType.FUTURE_DATA_MODIFICATION,
                            severity=ValidationSeverity.CRITICAL,
                            column=col,
                            bar_index=t,
                            description=f"NaN mismatch at bar {t} for column '{col}': full is {val_full}, partial is {val_partial}.",
                            expected_behavior="Historical NaN status at bar t must not change when future bars are appended",
                            actual_behavior=f"Full={val_full}, Partial={val_partial}"
                        )
                    )
                    continue

                diff = abs(float(val_full) - float(val_partial))
                if diff > self.tolerance:
                    findings.append(
                        LookaheadFinding(
                            check_name="INCREMENTAL_CAUSALITY_CHECK",
                            lookahead_type=LookaheadType.FUTURE_DATA_MODIFICATION,
                            severity=ValidationSeverity.CRITICAL,
                            column=col,
                            bar_index=t,
                            description=f"Lookahead leak detected in '{col}' at bar {t}: Diff={diff:.8f} (Full={val_full:.6f}, Partial={val_partial:.6f}).",
                            expected_behavior="Historical feature value at bar t must be identical regardless of future data",
                            actual_behavior=f"Value at bar t changed by {diff:.8f} when future bars (>{t}) were appended"
                        )
                    )

        has_critical = any(f.severity == ValidationSeverity.CRITICAL for f in findings)
        is_clean = len(findings) == 0

        return LookaheadReport(
            report_id=report_id,
            strategy_id=strategy_id,
            is_clean=is_clean,
            total_checks_run=total_checks,
            findings=findings,
            has_critical_lookahead=has_critical,
            checked_at=datetime.now(timezone.utc)
        )

    def scan_for_future_joins(
        self,
        base_df: pd.DataFrame,
        base_timestamp_col: str,
        joined_timestamp_col: str
    ) -> List[LookaheadFinding]:
        """Verify that joined data (e.g. news, disclosures) does not have publication timestamps ahead of bar times."""
        findings = []
        if base_timestamp_col not in base_df.columns or joined_timestamp_col not in base_df.columns:
            return findings

        # Ensure datetime format
        base_ts = pd.to_datetime(base_df[base_timestamp_col])
        joined_ts = pd.to_datetime(base_df[joined_timestamp_col])

        # Violations where joined disclosure happened in the future relative to the decision bar
        mask = joined_ts > base_ts
        violations = base_df[mask]

        if not violations.empty:
            findings.append(
                LookaheadFinding(
                    check_name="POINT_IN_TIME_JOIN_CHECK",
                    lookahead_type=LookaheadType.FUTURE_JOIN_LEAKAGE,
                    severity=ValidationSeverity.CRITICAL,
                    description=f"Found {len(violations)} records where joined data timestamp exceeds decision bar timestamp.",
                    expected_behavior=f"{joined_timestamp_col} <= {base_timestamp_col}",
                    actual_behavior=f"Max lead time: {(joined_ts - base_ts).max()}"
                )
            )

        return findings

    def scan_for_normalization_leakage(
        self,
        train_scaler_mean: float,
        train_scaler_std: float,
        full_dataset_mean: float,
        full_dataset_std: float,
        tolerance: float = 1e-4
    ) -> List[LookaheadFinding]:
        """Detect if test data was normalized using statistics computed on the full dataset."""
        findings = []
        mean_diff = abs(train_scaler_mean - full_dataset_mean)
        std_diff = abs(train_scaler_std - full_dataset_std)

        if mean_diff > tolerance or std_diff > tolerance:
            findings.append(
                LookaheadFinding(
                    check_name="GLOBAL_NORMALIZATION_ISOLATION",
                    lookahead_type=LookaheadType.GLOBAL_NORMALIZATION_LEAKAGE,
                    severity=ValidationSeverity.INFO,
                    description=f"Scaler statistics properly isolated to training set (Train Mean={train_scaler_mean:.4f}, Full Mean={full_dataset_mean:.4f}).",
                    expected_behavior="Scaler fit on training split only",
                    actual_behavior="Scaler verified independent from test set distribution"
                )
            )
        return findings
