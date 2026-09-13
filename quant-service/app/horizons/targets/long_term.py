"""Long-Term Investing Target Builder (6 Months to 5+ Years)."""

from typing import Dict, Optional
import numpy as np
import pandas as pd
from datetime import datetime

from app.horizons.targets.base import BaseHorizonTargetBuilder


class LongTermTargetBuilder(BaseHorizonTargetBuilder):
    """Calculates forward labels for 6-month to 5-year investment horizons."""

    def __init__(self, threshold: float = 0.08, version: str = "LONG_TERM_TARGET_SET_V1"):
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

        if future_ohlcv_df is None or len(future_ohlcv_df) < 20:
            return {
                "target_return_6m": 0.0,
                "target_return_1y": 0.0,
                "target_return_2y": 0.0,
                "target_return_3y": 0.0,
                "target_return_5y": 0.0,
                "target_alpha_1y": 0.0,
                "target_cagr_3y": 0.0,
                "target_max_drawdown_1y": 0.0
            }

        df = future_ohlcv_df.copy()
        if "date" in df.columns:
            df["date"] = pd.to_datetime(df["date"])
            df = df[df["date"] >= pd.to_datetime(as_of)].sort_values("date")

        close = df["close"].values
        p0 = close[0]

        targets["target_return_6m"] = float((close[126] / p0 - 1.0) if len(close) > 126 else (close[-1] / p0 - 1.0))
        targets["target_return_1y"] = float((close[252] / p0 - 1.0) if len(close) > 252 else targets["target_return_6m"])
        targets["target_return_2y"] = float((close[504] / p0 - 1.0) if len(close) > 504 else targets["target_return_1y"])
        targets["target_return_3y"] = float((close[756] / p0 - 1.0) if len(close) > 756 else targets["target_return_2y"])
        targets["target_return_5y"] = float((close[1260] / p0 - 1.0) if len(close) > 1260 else targets["target_return_3y"])

        # 3Y CAGR
        ret3y = targets["target_return_3y"]
        if ret3y > -0.99:
            targets["target_cagr_3y"] = float((1.0 + ret3y) ** (1.0 / 3.0) - 1.0)
        else:
            targets["target_cagr_3y"] = -0.50

        # Benchmark Alpha 1Y
        bench_ret_1y = 0.10
        if benchmark_ohlcv_df is not None and len(benchmark_ohlcv_df) > 20:
            b_df = benchmark_ohlcv_df.copy()
            if "date" in b_df.columns:
                b_df["date"] = pd.to_datetime(b_df["date"])
                b_df = b_df[b_df["date"] >= pd.to_datetime(as_of)].sort_values("date")
            b_close = b_df["close"].values
            if len(b_close) > 252:
                bench_ret_1y = float(b_close[252] / b_close[0] - 1.0)
            elif len(b_close) > 1:
                bench_ret_1y = float(b_close[-1] / b_close[0] - 1.0)

        targets["target_alpha_1y"] = float(targets["target_return_1y"] - bench_ret_1y)

        # 1-year max drawdown
        window_1y = close[:253] if len(close) >= 253 else close
        cum_max = np.maximum.accumulate(window_1y)
        drawdowns = (window_1y - cum_max) / cum_max
        targets["target_max_drawdown_1y"] = float(np.min(drawdowns))

        return targets
