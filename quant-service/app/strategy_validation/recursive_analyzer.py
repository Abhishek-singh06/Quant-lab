"""Recursive Indicator and Historical Warmup Analyzer.

Analyzes indicator initialization sensitivity, warmup sufficiency, and historical
invariance to future bar appending (verifying that indicators like EMA, RSI, MACD
stabilize properly without altering historical values).
"""

import uuid
from typing import Callable, Dict, List, Optional
from datetime import datetime, timezone
import numpy as np
import pandas as pd

from app.strategy_validation.models import (
    RecursiveStabilityReport,
    WarmupMetric,
)


class RecursiveAnalyzer:
    """Evaluates warmup sufficiency and numerical stability for recursive/exponential indicators."""

    def __init__(self, tolerance_epsilon: float = 1e-6) -> None:
        self.tolerance_epsilon = tolerance_epsilon

    def analyze_warmup_convergence(
        self,
        indicator_fn: Callable[[pd.DataFrame], pd.Series],
        df: pd.DataFrame,
        indicator_name: str = "indicator",
        test_warmup_bars: Optional[List[int]] = None,
        eval_window_bars: int = 50
    ) -> RecursiveStabilityReport:
        """Measure convergence of recursive indicator values across increasing historical warmup lengths.
        
        Methodology:
        1. Compute benchmark indicator values using entire available history: I_bench = indicator_fn(df).
        2. Evaluate tail window: [N - eval_window_bars .. N].
        3. For each candidate warmup length W:
           - Slice sub-DataFrame: df_sub = df.iloc[-(eval_window_bars + W):]
           - Compute indicator on sub-slice: I_sub = indicator_fn(df_sub)
           - Compute max absolute error: max |I_sub[-eval_window:] - I_bench[-eval_window:]|
        4. Identify the minimum warmup length W* where max error < tolerance_epsilon.
        """
        report_id = f"recur-{uuid.uuid4().hex[:12]}"
        n_rows = len(df)
        warmup_steps = test_warmup_bars or [20, 50, 100, 200, 300, 500]
        warmup_metrics: List[WarmupMetric] = []
        findings: List[str] = []

        if n_rows < eval_window_bars + 20:
            return RecursiveStabilityReport(
                report_id=report_id,
                indicator_name=indicator_name,
                is_stable=True,
                is_future_invariant=True,
                recommended_min_warmup_bars=20,
                tolerance_epsilon=self.tolerance_epsilon,
                warmup_convergence=[],
                findings=["Insufficient history to perform multi-stage warmup convergence test."]
            )

        # 1. Benchmark on full dataset
        benchmark_series = indicator_fn(df.copy())
        bench_tail = benchmark_series.iloc[-eval_window_bars:].values

        recommended_warmup = warmup_steps[-1]
        is_converged_found = False

        # 2. Test each warmup length
        for w in warmup_steps:
            required_len = eval_window_bars + w
            if required_len > n_rows:
                continue

            sub_df = df.iloc[-required_len:].copy()
            sub_series = indicator_fn(sub_df)
            sub_tail = sub_series.iloc[-eval_window_bars:].values

            # Drop NaNs for error calculation
            valid_mask = ~np.isnan(bench_tail) & ~np.isnan(sub_tail)
            if not np.any(valid_mask):
                max_err = 0.0
                mean_err = 0.0
            else:
                diffs = np.abs(bench_tail[valid_mask] - sub_tail[valid_mask])
                max_err = float(np.max(diffs))
                mean_err = float(np.mean(diffs))

            converged = max_err <= self.tolerance_epsilon
            warmup_metrics.append(
                WarmupMetric(
                    warmup_bars=w,
                    max_absolute_error=max_err,
                    mean_absolute_error=mean_err,
                    is_converged=converged
                )
            )

            if converged and not is_converged_found:
                recommended_warmup = w
                is_converged_found = True

        if not is_converged_found:
            findings.append(
                f"Indicator did not fully converge to epsilon ({self.tolerance_epsilon}) "
                f"within tested warmup range ({warmup_steps}). Recommended warmup set to {recommended_warmup} bars."
            )

        # 3. Test future expansion invariance
        split_idx = n_rows - eval_window_bars
        sub_historical = df.iloc[:split_idx].copy()
        hist_series = indicator_fn(sub_historical)

        # Check if past values (0..split_idx-1) match between full and sliced execution
        full_slice = benchmark_series.iloc[:split_idx].values
        hist_vals = hist_series.values
        valid_hist_mask = ~np.isnan(full_slice) & ~np.isnan(hist_vals)

        if np.any(valid_hist_mask):
            hist_diff = np.abs(full_slice[valid_hist_mask] - hist_vals[valid_hist_mask])
            is_future_invariant = float(np.max(hist_diff)) <= self.tolerance_epsilon
            if not is_future_invariant:
                findings.append("Historical values changed when future bars were appended (non-causal indicator).")
        else:
            is_future_invariant = True

        is_stable = is_converged_found and is_future_invariant

        return RecursiveStabilityReport(
            report_id=report_id,
            indicator_name=indicator_name,
            is_stable=is_stable,
            is_future_invariant=is_future_invariant,
            recommended_min_warmup_bars=recommended_warmup,
            tolerance_epsilon=self.tolerance_epsilon,
            warmup_convergence=warmup_metrics,
            findings=findings,
            checked_at=datetime.now(timezone.utc)
        )
