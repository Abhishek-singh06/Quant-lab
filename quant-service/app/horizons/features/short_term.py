"""Short-Term Trading Feature Extractor (Intraday to 1-5 Days).

Focuses on fast technical indicators, volume dynamics, realized volatility,
India VIX, overnight global market signals, and breaking news.
"""

from typing import Dict, List, Any, Optional
import numpy as np
import pandas as pd
from datetime import datetime

from app.horizons.features.base import BaseHorizonFeatureExtractor


class ShortTermFeatureExtractor(BaseHorizonFeatureExtractor):
    """Point-in-Time feature extractor for Short-Term trading models."""

    def __init__(self, version: str = "SHORT_TERM_FEATURE_SET_V1"):
        super().__init__(feature_set_version=version)

    def get_feature_names(self) -> List[str]:
        return [
            "return_1d", "return_2d", "return_3d", "return_5d", "return_10d",
            "momentum_5d", "momentum_10d", "momentum_20d",
            "rsi_14", "macd_diff", "macd_signal",
            "atr_14_pct", "volatility_5d", "volatility_10d", "volatility_20d",
            "volume_ratio_5_20", "volume_ratio_10_20",
            "price_vs_high_20d", "price_vs_low_20d",
            "india_vix_level", "india_vix_change_5d",
            "news_sentiment_24h", "global_overnight_return"
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
        """Extract short-term features ensuring strict PIT compliance."""
        features: Dict[str, float] = {}

        # 1. Filter OHLCV strictly up to as_of
        if ohlcv_df is None or len(ohlcv_df) < 20:
            return {f: 0.0 for f in self.get_feature_names()}

        df = ohlcv_df.copy()
        if "date" in df.columns:
            df["date"] = pd.to_datetime(df["date"])
            df = df[df["date"] <= pd.to_datetime(as_of)].sort_values("date")

        if len(df) < 20:
            return {f: 0.0 for f in self.get_feature_names()}

        close = df["close"].values
        high = df["high"].values if "high" in df.columns else close
        low = df["low"].values if "low" in df.columns else close
        volume = df["volume"].values if "volume" in df.columns else np.ones_like(close)

        curr_p = close[-1]

        # Returns
        features["return_1d"] = (curr_p / close[-2] - 1.0) if len(close) >= 2 else 0.0
        features["return_2d"] = (curr_p / close[-3] - 1.0) if len(close) >= 3 else 0.0
        features["return_3d"] = (curr_p / close[-4] - 1.0) if len(close) >= 4 else 0.0
        features["return_5d"] = (curr_p / close[-6] - 1.0) if len(close) >= 6 else 0.0
        features["return_10d"] = (curr_p / close[-11] - 1.0) if len(close) >= 11 else 0.0

        # Momentum
        features["momentum_5d"] = features["return_5d"]
        features["momentum_10d"] = features["return_10d"]
        features["momentum_20d"] = (curr_p / close[-21] - 1.0) if len(close) >= 21 else 0.0

        # RSI 14
        if len(close) >= 15:
            deltas = np.diff(close[-15:])
            gains = np.where(deltas > 0, deltas, 0)
            losses = np.where(deltas < 0, -deltas, 0)
            avg_gain = np.mean(gains)
            avg_loss = np.mean(losses)
            if avg_loss == 0:
                features["rsi_14"] = 100.0
            else:
                rs = avg_gain / avg_loss
                features["rsi_14"] = 100.0 - (100.0 / (1.0 + rs))
        else:
            features["rsi_14"] = 50.0

        # MACD (12, 26, 9)
        if len(close) >= 26:
            ema12 = pd.Series(close).ewm(span=12, adjust=False).mean().values[-1]
            ema26 = pd.Series(close).ewm(span=26, adjust=False).mean().values[-1]
            macd = ema12 - ema26
            features["macd_diff"] = macd / curr_p
            features["macd_signal"] = macd / curr_p * 0.9
        else:
            features["macd_diff"] = 0.0
            features["macd_signal"] = 0.0

        # ATR 14
        if len(close) >= 15:
            tr = np.maximum(high[-14:] - low[-14:], np.abs(high[-14:] - close[-15:-1]))
            tr = np.maximum(tr, np.abs(low[-14:] - close[-15:-1]))
            atr = np.mean(tr)
            features["atr_14_pct"] = atr / curr_p
        else:
            features["atr_14_pct"] = 0.02

        # Volatilities
        returns = np.diff(close) / close[:-1]
        features["volatility_5d"] = float(np.std(returns[-5:]) * np.sqrt(252)) if len(returns) >= 5 else 0.20
        features["volatility_10d"] = float(np.std(returns[-10:]) * np.sqrt(252)) if len(returns) >= 10 else 0.20
        features["volatility_20d"] = float(np.std(returns[-20:]) * np.sqrt(252)) if len(returns) >= 20 else 0.20

        # Volume ratios
        vol_5 = np.mean(volume[-5:]) if len(volume) >= 5 else 1.0
        vol_10 = np.mean(volume[-10:]) if len(volume) >= 10 else 1.0
        vol_20 = np.mean(volume[-20:]) if len(volume) >= 20 else 1.0
        features["volume_ratio_5_20"] = float(vol_5 / max(1.0, vol_20))
        features["volume_ratio_10_20"] = float(vol_10 / max(1.0, vol_20))

        # Price vs 20d High/Low
        high_20 = np.max(high[-20:])
        low_20 = np.min(low[-20:])
        features["price_vs_high_20d"] = (curr_p / high_20 - 1.0) if high_20 > 0 else 0.0
        features["price_vs_low_20d"] = (curr_p / low_20 - 1.0) if low_20 > 0 else 0.0

        # External inputs (Regime, VIX, News, Global)
        if regime_data:
            features["india_vix_level"] = float(regime_data.get("vix_level", 14.0))
            features["india_vix_change_5d"] = float(regime_data.get("vix_change_5d", 0.0))
        else:
            features["india_vix_level"] = 14.0
            features["india_vix_change_5d"] = 0.0

        if news_data:
            features["news_sentiment_24h"] = float(news_data.get("sentiment_score", 0.0))
        else:
            features["news_sentiment_24h"] = 0.0

        if global_data:
            features["global_overnight_return"] = float(global_data.get("spx_return_1d", 0.0))
        else:
            features["global_overnight_return"] = 0.0

        return features
