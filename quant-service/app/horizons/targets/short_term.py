"""Short-Term Target Builder (1 to 5 Days)."""

from typing import Dict, Optional
import numpy as np
import pandas as pd
from datetime import datetime

from app.horizons.targets.base import BaseHorizonTargetBuilder


class ShortTermTargetBuilder(BaseHorizonTargetBuilder):
    """Calculates forward labels for 1-5 day trading horizons."""

    def __init__(self, threshold: float = 0.0, version: str = "SHORT_TERM_TARGET_SET_V1"):
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

        if future_ohlcv_df is None or len(future_ohlcv_df) < 2:
            return {
                "target_return_1d": 0.0,
                "target_return_2d": 0.0,
                "target_return_3d": 0.0,
                "target_return_5d": 0.0,
                "target_class_1d": 0.0,
                "target_class_5d": 0.0,
                "target_realized_vol_5d": 0.20
            }

        df = future_ohlcv_df.copy()
        if "date" in df.columns:
            df["date"] = pd.to_datetime(df["date"])
            df = df[df["date"] >= pd.to_datetime(as_of)].sort_values("date")

        close = df["close"].values
        p0 = close[0] # Price at as_of

        # 1-day return
        ret1 = (close[1] / p0 - 1.0) if len(close) > 1 else 0.0
        ret2 = (close[2] / p0 - 1.0) if len(close) > 2 else ret1
        ret3 = (close[3] / p0 - 1.0) if len(close) > 3 else ret2
        ret5 = (close[5] / p0 - 1.0) if len(close) > 5 else (close[-1] / p0 - 1.0)

        targets["target_return_1d"] = float(ret1)
        targets["target_return_2d"] = float(ret2)
        targets["target_return_3d"] = float(ret3)
        targets["target_return_5d"] = float(ret5)

        targets["target_class_1d"] = 1.0 if ret1 > self.threshold else 0.0
        targets["target_class_5d"] = 1.0 if ret5 > self.threshold else 0.0

        if len(close) >= 5:
            d_rets = np.diff(close[:6]) / close[:5]
            targets["target_realized_vol_5d"] = float(np.std(d_rets) * np.sqrt(252))
        else:
            targets["target_realized_vol_5d"] = 0.20

        return targets
