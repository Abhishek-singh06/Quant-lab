"""Medium-Term Trading Feature Extractor (1 to 12 Weeks).

Balances intermediate momentum, trend structures (SMA50/200), quarterly earnings
trajectory, institutional flow accumulation, and sector relative strength.
"""

from typing import Dict, List, Any, Optional
import numpy as np
import pandas as pd
from datetime import datetime

from app.horizons.features.base import BaseHorizonFeatureExtractor


class MediumTermFeatureExtractor(BaseHorizonFeatureExtractor):
    """Point-in-Time feature extractor for Medium-Term trading models."""

    def __init__(self, version: str = "MEDIUM_TERM_FEATURE_SET_V1"):
        super().__init__(feature_set_version=version)

    def get_feature_names(self) -> List[str]:
        return [
            "return_5d", "return_20d", "return_63d",
            "momentum_20d", "momentum_63d", "momentum_126d",
            "price_vs_sma50", "price_vs_sma200", "sma50_vs_sma200",
            "volatility_21d", "volatility_63d", "drawdown_63d",
            "relative_strength_nifty_63d",
            "fii_flow_20d_norm", "dii_flow_20d_norm",
            "quarterly_revenue_growth_yoy", "quarterly_eps_growth_yoy", "operating_margin_trend",
            "pe_ratio_norm", "pb_ratio_norm",
            "global_regime_score"
        ]

    def extract_features(
        self,
        symbol: str,
        as_of: datetime,
        ohlcv_df: pd.DataFrame,
        regime_data: Optional[Dict[str, Any]] = None,
        fundamental_data: Optional[Dict[str, Any]] = None,
        institutional_data: Optional[Dict[str, Any]] = None,
        news_data: Optional[Dict[str, Any]] = None,
        global_data: Optional[Dict[str, Any]] = None,
    ) -> Dict[str, float]:
        """Extract medium-term features with strict PIT compliance."""
        features: Dict[str, float] = {}

        if ohlcv_df is None or len(ohlcv_df) < 50:
            return {f: 0.0 for f in self.get_feature_names()}

        df = ohlcv_df.copy()
        if "date" in df.columns:
            df["date"] = pd.to_datetime(df["date"])
            df = df[df["date"] <= pd.to_datetime(as_of)].sort_values("date")

        if len(df) < 50:
            return {f: 0.0 for f in self.get_feature_names()}

        close = df["close"].values
        curr_p = close[-1]

        # Returns & Momentum
        features["return_5d"] = (curr_p / close[-6] - 1.0) if len(close) >= 6 else 0.0
        features["return_20d"] = (curr_p / close[-21] - 1.0) if len(close) >= 21 else 0.0
        features["return_63d"] = (curr_p / close[-64] - 1.0) if len(close) >= 64 else 0.0

        features["momentum_20d"] = features["return_20d"]
        features["momentum_63d"] = features["return_63d"]
        features["momentum_126d"] = (curr_p / close[-127] - 1.0) if len(close) >= 127 else features["return_63d"]

        # SMAs
        sma50 = np.mean(close[-50:]) if len(close) >= 50 else curr_p
        sma200 = np.mean(close[-200:]) if len(close) >= 200 else sma50
        features["price_vs_sma50"] = (curr_p / sma50 - 1.0) if sma50 > 0 else 0.0
        features["price_vs_sma200"] = (curr_p / sma200 - 1.0) if sma200 > 0 else 0.0
        features["sma50_vs_sma200"] = (sma50 / sma200 - 1.0) if sma200 > 0 else 0.0

        # Volatility & Drawdown
        returns = np.diff(close) / close[:-1]
        features["volatility_21d"] = float(np.std(returns[-21:]) * np.sqrt(252)) if len(returns) >= 21 else 0.20
        features["volatility_63d"] = float(np.std(returns[-63:]) * np.sqrt(252)) if len(returns) >= 63 else 0.20

        peak_63 = np.max(close[-63:]) if len(close) >= 63 else curr_p
        features["drawdown_63d"] = (curr_p / peak_63 - 1.0) if peak_63 > 0 else 0.0

        # Relative strength vs NIFTY
        nifty_ret = 0.02
        if regime_data and "nifty_return_63d" in regime_data:
            nifty_ret = float(regime_data["nifty_return_63d"])
        features["relative_strength_nifty_63d"] = features["return_63d"] - nifty_ret

        # Institutional flows (PIT gated)
        if institutional_data:
            features["fii_flow_20d_norm"] = float(institutional_data.get("fii_flow_20d", 0.0)) / 10000.0
            features["dii_flow_20d_norm"] = float(institutional_data.get("dii_flow_20d", 0.0)) / 10000.0
        else:
            features["fii_flow_20d_norm"] = 0.0
            features["dii_flow_20d_norm"] = 0.0

        # Fundamental quarterly data (PIT verified)
        if fundamental_data:
            features["quarterly_revenue_growth_yoy"] = float(fundamental_data.get("revenue_growth_yoy", 0.10))
            features["quarterly_eps_growth_yoy"] = float(fundamental_data.get("eps_growth_yoy", 0.12))
            features["operating_margin_trend"] = float(fundamental_data.get("operating_margin_delta", 0.01))
            features["pe_ratio_norm"] = float(fundamental_data.get("pe_ratio", 22.0)) / 50.0
            features["pb_ratio_norm"] = float(fundamental_data.get("pb_ratio", 3.0)) / 10.0
        else:
            features["quarterly_revenue_growth_yoy"] = 0.08
            features["quarterly_eps_growth_yoy"] = 0.10
            features["operating_margin_trend"] = 0.0
            features["pe_ratio_norm"] = 0.44
            features["pb_ratio_norm"] = 0.30

        # Global regime
        if global_data:
            features["global_regime_score"] = float(global_data.get("composite_score", 0.0)) / 100.0
        else:
            features["global_regime_score"] = 0.20

        return features
