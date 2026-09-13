"""Dataset Builder for Long-Term Investing Models."""

from typing import List, Dict, Any, Tuple, Optional
import pandas as pd
import numpy as np
from datetime import datetime

from app.horizons.schemas import TradingHorizon
from app.horizons.features.long_term import LongTermFeatureExtractor
from app.horizons.targets.long_term import LongTermTargetBuilder


class LongTermDatasetBuilder:
    """Builds Point-in-Time datasets for Long-Term investing models."""

    def __init__(
        self,
        feature_extractor: LongTermFeatureExtractor = None,
        target_builder: LongTermTargetBuilder = None
    ):
        self.feature_extractor = feature_extractor or LongTermFeatureExtractor()
        self.target_builder = target_builder or LongTermTargetBuilder()

    def build_dataset(
        self,
        symbols_ohlcv: Dict[str, pd.DataFrame],
        dates: List[datetime],
        target_name: str = "target_return_1y",
        benchmark_ohlcv: Optional[pd.DataFrame] = None,
        fundamentals_by_date_symbol: Dict[Tuple[datetime, str], Dict[str, Any]] = None,
        institutional_by_date_symbol: Dict[Tuple[datetime, str], Dict[str, Any]] = None,
        regimes_by_date: Dict[datetime, Dict[str, Any]] = None,
    ) -> Tuple[pd.DataFrame, pd.Series, pd.DataFrame]:
        """Build (X, y, metadata) dataset with fundamental features and long-term targets."""
        feature_rows: List[Dict[str, float]] = []
        target_rows: List[float] = []
        meta_rows: List[Dict[str, Any]] = []

        for as_of in dates:
            regime = regimes_by_date.get(as_of) if regimes_by_date else None

            for sym, ohlcv in symbols_ohlcv.items():
                fund = fundamentals_by_date_symbol.get((as_of, sym)) if fundamentals_by_date_symbol else None
                inst = institutional_by_date_symbol.get((as_of, sym)) if institutional_by_date_symbol else None

                feats = self.feature_extractor.extract_features(
                    symbol=sym,
                    as_of=as_of,
                    ohlcv_df=ohlcv,
                    regime_data=regime,
                    fundamental_data=fund,
                    institutional_data=inst
                )

                targets = self.target_builder.calculate_targets(
                    symbol=sym,
                    as_of=as_of,
                    future_ohlcv_df=ohlcv,
                    benchmark_ohlcv_df=benchmark_ohlcv
                )

                y_val = targets.get(target_name, np.nan)
                if not np.isnan(y_val):
                    feature_rows.append(feats)
                    target_rows.append(y_val)
                    meta_rows.append({
                        "symbol": sym,
                        "as_of": as_of,
                        "horizon": TradingHorizon.LONG_TERM.value,
                        "target_name": target_name,
                        "information_available_at": as_of.isoformat()
                    })

        X = pd.DataFrame(feature_rows)
        y = pd.Series(target_rows, name=target_name)
        meta = pd.DataFrame(meta_rows)

        return X, y, meta
