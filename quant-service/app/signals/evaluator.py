"""
Historical Signal Evaluator and Baseline Comparison for QuantLab Part 13.
Computes forward outcomes (5D, 20D, 63D), hit rates, score monotonicity, and baseline comparisons.
"""

from typing import Any, Dict, List
import numpy as np
import pandas as pd
from app.signals.models import SignalResult, SignalType


class SignalHistoricalEvaluator:
    """Evaluates historical signals out-of-sample without lookahead bias."""

    def evaluate_historical_signals(
        self,
        signals_df: pd.DataFrame
    ) -> Dict[str, Any]:
        """
        signals_df must contain:
        - 'signal': BUY, HOLD, SELL
        - 'signal_score': float
        - 'confidence': float
        - 'forward_return_5d': float
        - 'forward_return_20d': float
        - 'forward_return_63d': float
        """
        if signals_df.empty:
            return {"status": "EMPTY"}

        results: Dict[str, Any] = {
            "total_signals": len(signals_df),
            "signal_counts": signals_df["signal"].value_counts().to_dict(),
        }

        # 1. Forward Return Conditioning by Signal
        for sig in [SignalType.BUY.value, SignalType.HOLD.value, SignalType.SELL.value]:
            subset = signals_df[signals_df["signal"] == sig]
            if not subset.empty:
                results[f"{sig.lower()}_metrics"] = {
                    "count": len(subset),
                    "mean_5d": float(subset["forward_return_5d"].mean()),
                    "mean_20d": float(subset["forward_return_20d"].mean()),
                    "mean_63d": float(subset.get("forward_return_63d", pd.Series([0.0])).mean()),
                    "median_20d": float(subset["forward_return_20d"].median()),
                    "hit_rate_20d": float((subset["forward_return_20d"] > 0).mean()) if sig == "BUY" else float((subset["forward_return_20d"] < 0).mean()),
                    "volatility_20d": float(subset["forward_return_20d"].std()),
                }

        # 2. Score Monotonicity (Ordered Quintiles / Buckets)
        if "signal_score" in signals_df.columns and "forward_return_20d" in signals_df.columns:
            signals_df["score_bucket"] = pd.qcut(signals_df["signal_score"], q=5, labels=["Q1_Bear", "Q2", "Q3_Neutral", "Q4", "Q5_Bull"], duplicates="drop")
            bucket_returns = signals_df.groupby("score_bucket", observed=False)["forward_return_20d"].mean().to_dict()
            results["score_monotonicity_20d"] = {str(k): float(v) for k, v in bucket_returns.items()}

        # 3. Baseline Comparison
        # Naive Baseline 1: Always HOLD (Market Average Return)
        market_mean_20d = float(signals_df["forward_return_20d"].mean())
        # Naive Baseline 2: Random Signal (50/50)
        buy_subset = signals_df[signals_df["signal"] == SignalType.BUY.value]
        buy_mean_20d = float(buy_subset["forward_return_20d"].mean()) if not buy_subset.empty else 0.0

        results["baseline_comparison"] = {
            "market_benchmark_mean_20d": market_mean_20d,
            "engine_buy_alpha_vs_market_20d": buy_mean_20d - market_mean_20d,
            "engine_adds_value": (buy_mean_20d > market_mean_20d)
        }

        return results
