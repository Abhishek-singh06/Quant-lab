"""Dataset Builder for Medium-Term Trading Models."""

from typing import List, Dict, Any, Tuple, Optional
import pandas as pd
import numpy as np
from datetime import datetime

from app.horizons.schemas import TradingHorizon
from app.horizons.features.medium_term import MediumTermFeatureExtractor
from app.horizons.targets.medium_term import MediumTermTargetBuilder


class MediumTermDatasetBuilder:
    """Builds Point-in-Time datasets for Medium-Term trading models."""

    def __init__(
        self,
        feature_extractor: MediumTermFeatureExtractor = None,
        target_builder: MediumTermTargetBuilder = None
    ):
        self.feature_extractor = feature_extractor or MediumTermFeatureExtractor()
        self.target_builder = target_builder or MediumTermTargetBuilder()

    def build_dataset(
        self,
        symbols_ohlcv: Dict[str, pd.DataFrame],
        dates: List[datetime],
        target_name: str = "target_return_4w",
        benchmark_ohlcv: Optional[pd.DataFrame] = None,
        fundamentals_by_date_symbol: Dict[Tuple[datetime, str], Dict[str, Any]] = None,
        institutional_by_date_symbol: Dict[Tuple[datetime, str], Dict[str, Any]] = None,
        regimes_by_date: Dict[datetime, Dict[str, Any]] = None,
        global_by_date: Dict[datetime, Dict[str, Any]] = None,
    ) -> Tuple[pd.DataFrame, pd.Series, pd.DataFrame]:
        """Build (X, y, metadata) dataset with medium-term features and targets."""
        feature_rows: List[Dict[str, float]] = []
        target_rows: List[float] = []
        meta_rows: List[Dict[str, Any]] = []

        for as_of in dates:
            regime = regimes_by_date.get(as_of) if regimes_by_date else None
            glob = global_by_date.get(as_of) if global_by_date else None

            for sym, ohlcv in symbols_ohlcv.items():
                fund = fundamentals_by_date_symbol.get((as_of, sym)) if fundamentals_by_date_symbol else None
                inst = institutional_by_date_symbol.get((as_of, sym)) if institutional_by_date_symbol else None

                feats = self.feature_extractor.extract_features(
                    symbol=sym,
                    as_of=as_of,
                    ohlcv_df=ohlcv,
                    regime_data=regime,
                    fundamental_data=fund,
                    institutional_data=inst,
                    global_data=glob
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
                        "horizon": TradingHorizon.MEDIUM_TERM.value,
                        "target_name": target_name,
                        "information_available_at": as_of.isoformat()
                    })

        X = pd.DataFrame(feature_rows)
        y = pd.Series(target_rows, name=target_name)
        meta = pd.DataFrame(meta_rows)

        return X, y, meta
