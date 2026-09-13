"""Dataset Builder for Short-Term Trading Models."""

from typing import List, Dict, Any, Tuple
import pandas as pd
import numpy as np
from datetime import datetime

from app.horizons.schemas import TradingHorizon
from app.horizons.features.short_term import ShortTermFeatureExtractor
from app.horizons.targets.short_term import ShortTermTargetBuilder


class ShortTermDatasetBuilder:
    """Builds Point-in-Time datasets for Short-Term trading models."""

    def __init__(
        self,
        feature_extractor: ShortTermFeatureExtractor = None,
        target_builder: ShortTermTargetBuilder = None
    ):
        self.feature_extractor = feature_extractor or ShortTermFeatureExtractor()
        self.target_builder = target_builder or ShortTermTargetBuilder()

    def build_dataset(
        self,
        symbols_ohlcv: Dict[str, pd.DataFrame],
        dates: List[datetime],
        target_name: str = "target_return_1d",
        regimes_by_date: Dict[datetime, Dict[str, Any]] = None,
        news_by_date_symbol: Dict[Tuple[datetime, str], Dict[str, Any]] = None,
        global_by_date: Dict[datetime, Dict[str, Any]] = None,
    ) -> Tuple[pd.DataFrame, pd.Series, pd.DataFrame]:
        """Build (X, y, metadata) dataset strictly respecting Point-in-Time gating."""
        feature_rows: List[Dict[str, float]] = []
        target_rows: List[float] = []
        meta_rows: List[Dict[str, Any]] = []

        for as_of in dates:
            regime = regimes_by_date.get(as_of) if regimes_by_date else None
            glob = global_by_date.get(as_of) if global_by_date else None

            for sym, ohlcv in symbols_ohlcv.items():
                news = news_by_date_symbol.get((as_of, sym)) if news_by_date_symbol else None

                feats = self.feature_extractor.extract_features(
                    symbol=sym,
                    as_of=as_of,
                    ohlcv_df=ohlcv,
                    regime_data=regime,
                    news_data=news,
                    global_data=glob
                )

                targets = self.target_builder.calculate_targets(
                    symbol=sym,
                    as_of=as_of,
                    future_ohlcv_df=ohlcv
                )

                y_val = targets.get(target_name, np.nan)
                if not np.isnan(y_val):
                    feature_rows.append(feats)
                    target_rows.append(y_val)
                    meta_rows.append({
                        "symbol": sym,
                        "as_of": as_of,
                        "horizon": TradingHorizon.SHORT_TERM.value,
                        "target_name": target_name,
                        "information_available_at": as_of.isoformat()
                    })

        X = pd.DataFrame(feature_rows)
        y = pd.Series(target_rows, name=target_name)
        meta = pd.DataFrame(meta_rows)

        return X, y, meta
