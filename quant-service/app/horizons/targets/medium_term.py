"""Medium-Term Target Builder (1 to 12 Weeks)."""

from typing import Dict, Optional
import numpy as np
import pandas as pd
from datetime import datetime

from app.horizons.targets.base import BaseHorizonTargetBuilder


class MediumTermTargetBuilder(BaseHorizonTargetBuilder):
    """Calculates forward labels for 1-12 week intermediate horizons."""

    def __init__(self, threshold: float = 0.0, version: str = "MEDIUM_TERM_TARGET_SET_V1"):
        super().__init__(target_set_version=version)
        self.threshold = threshold

    def calculate_targets(
        self,
        symbol: str,
        as_of: datetime,
        future_ohlcv_df: pd.DataFrame,
        benchmark_ohlcv_df: Optional[pd.DataFrame] = None
    ) -> Dict[str, float]:
        targets: Dict[str, float] = {}

        if future_ohlcv_df is None or len(future_ohlcv_df) < 5:
            return {
                "target_return_1w": 0.0,
                "target_return_2w": 0.0,
                "target_return_4w": 0.0,
                "target_return_8w": 0.0,
                "target_return_12w": 0.0,
                "target_relative_return_4w": 0.0,
                "target_class_4w": 0.0,
                "target_drawdown_4w": 0.0
            }

        df = future_ohlcv_df.copy()
        if "date" in df.columns:
            df["date"] = pd.to_datetime(df["date"])
            df = df[df["date"] >= pd.to_datetime(as_of)].sort_values("date")

        close = df["close"].values
        p0 = close[0]

        targets["target_return_1w"] = float((close[5] / p0 - 1.0) if len(close) > 5 else (close[-1] / p0 - 1.0))
        targets["target_return_2w"] = float((close[10] / p0 - 1.0) if len(close) > 10 else targets["target_return_1w"])
        targets["target_return_4w"] = float((close[20] / p0 - 1.0) if len(close) > 20 else targets["target_return_2w"])
        targets["target_return_8w"] = float((close[40] / p0 - 1.0) if len(close) > 40 else targets["target_return_4w"])
        targets["target_return_12w"] = float((close[60] / p0 - 1.0) if len(close) > 60 else targets["target_return_8w"])

        targets["target_class_4w"] = 1.0 if targets["target_return_4w"] > self.threshold else 0.0

        # Benchmark relative return (e.g. NIFTY)
        bench_ret_4w = 0.0
        if benchmark_ohlcv_df is not None and len(benchmark_ohlcv_df) > 5:
            b_df = benchmark_ohlcv_df.copy()
            if "date" in b_df.columns:
                b_df["date"] = pd.to_datetime(b_df["date"])
                b_df = b_df[b_df["date"] >= pd.to_datetime(as_of)].sort_values("date")
            b_close = b_df["close"].values
            if len(b_close) > 20:
                bench_ret_4w = float(b_close[20] / b_close[0] - 1.0)
            elif len(b_close) > 1:
                bench_ret_4w = float(b_close[-1] / b_close[0] - 1.0)

        targets["target_relative_return_4w"] = float(targets["target_return_4w"] - bench_ret_4w)

        # Max drawdown over next 20 days
        window_20 = close[:21] if len(close) >= 21 else close
        cum_max = np.maximum.accumulate(window_20)
        drawdowns = (window_20 - cum_max) / cum_max
        targets["target_drawdown_4w"] = float(np.min(drawdowns))

        return targets
