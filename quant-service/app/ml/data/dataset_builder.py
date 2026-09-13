"""Production Point-in-Time Dataset Builder for Indian Equity Quant Models.
"""

from typing import Dict, Any, Optional, List, Tuple
import numpy as np
import pandas as pd
from datetime import datetime

from app.ml.data.target_builder import TargetBuilder
from app.warehouse.technical_feature_engine import TechnicalFeatureEngine
from app.warehouse.market_regime_engine import MarketRegimeEngine


class QuantDatasetBuilder:
    """Constructs reproducible, point-in-time safe ML datasets."""

    @staticmethod
    def build_dataset(
        price_df: pd.DataFrame,
        vix_df: Optional[pd.DataFrame] = None,
        fii_dii_df: Optional[pd.DataFrame] = None,
        fundamentals_df: Optional[pd.DataFrame] = None,
        start_date: Optional[str] = None,
        end_date: Optional[str] = None,
        target_name: str = "future_return_1d",
        as_of_date: Optional[str] = None
    ) -> Tuple[pd.DataFrame, pd.Series, Dict[str, Any]]:
        """Builds point-in-time training feature matrix X and target y.

        Enforces:
        1. All features available at or before observation date T.
        2. No target columns in feature matrix X.
        3. Strict temporal filtering.
        """
        if price_df.empty:
            return pd.DataFrame(), pd.Series(), {}

        # 1. Point-in-Time filter on price series
        data = price_df.copy()
        data['date'] = pd.to_datetime(data['date'])
        if as_of_date is not None:
            data = data[data['date'] <= pd.to_datetime(as_of_date)]

        data = data.sort_values('date').reset_index(drop=True)

        # 2. Compute Technical Features (Point-in-Time)
        tech_df = TechnicalFeatureEngine.calculate_features(data)

        # 3. Compute Regime Features (Point-in-Time)
        if len(data) >= 50:
            regime_df = MarketRegimeEngine.calculate_regime_timeline(data, vix_df=vix_df, fii_dii_df=fii_dii_df)
            if not regime_df.empty:
                regime_df['date'] = pd.to_datetime(regime_df['date'])
                merge_cols = ['date', 'direction_score', 'volatility_score', 'risk_score', 'prob_bull', 'prob_bear', 'confidence']
                tech_df = pd.merge(tech_df, regime_df[merge_cols], on='date', how='left')

        # 4. Merge Fundamentals with strict published date lag
        if fundamentals_df is not None and not fundamentals_df.empty:
            f_df = fundamentals_df.copy()
            f_df['date'] = pd.to_datetime(f_df['published_date']) if 'published_date' in f_df.columns else pd.to_datetime(f_df['date'])
            tech_df = pd.merge_asof(
                tech_df.sort_values('date'),
                f_df.sort_values('date'),
                on='date',
                direction='backward'
            )

        # 5. Compute Forward Targets
        full_df = TargetBuilder.calculate_targets(tech_df)

        # 6. Apply start_date and end_date filtering
        if start_date is not None:
            full_df = full_df[full_df['date'] >= pd.to_datetime(start_date)]
        if end_date is not None:
            full_df = full_df[full_df['date'] <= pd.to_datetime(end_date)]

        # 7. Drop rows where target is NaN (typically the final row of historical series)
        if target_name not in full_df.columns:
            target_name = "future_return_1d"

        clean_df = full_df.dropna(subset=[target_name]).reset_index(drop=True)

        # 8. Separate Features X from Target y and non-feature columns
        exclude_cols = {
            'date', 'symbol', 'open', 'high', 'low', 'close', 'adj_open', 'adj_high', 'adj_low', 'adj_close',
            'volume', 'future_return_1d', 'future_direction_1d', 'future_volatility_5d', 'future_volatility_20d',
            'future_return_5d', 'future_direction_5d', 'future_return_10d', 'future_direction_10d',
            'future_return_20d', 'future_direction_20d', 'future_return_63d', 'future_direction_63d'
        }

        feature_cols = [c for c in clean_df.columns if c not in exclude_cols]
        X = clean_df[feature_cols].copy().fillna(0.0)
        y = clean_df[target_name].copy()

        # 9. Build dataset snapshot metadata
        missingness = {col: float(clean_df[col].isna().mean()) for col in feature_cols}
        snapshot_metadata = {
            "dataset_id": f"DS-{clean_df['date'].min().strftime('%Y%m%d')}-{clean_df['date'].max().strftime('%Y%m%d')}",
            "start_date": clean_df['date'].min().strftime('%Y-%m-%d'),
            "end_date": clean_df['date'].max().strftime('%Y-%m-%d'),
            "row_count": len(clean_df),
            "feature_count": len(feature_cols),
            "feature_columns": feature_cols,
            "target_name": target_name,
            "missingness_stats": missingness,
            "dates": clean_df['date'].dt.strftime('%Y-%m-%d').tolist()
        }

        return X, y, snapshot_metadata
