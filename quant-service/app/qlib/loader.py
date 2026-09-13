"""Qlib Data Loader for QuantLab.

Loads market panels, executes feature generators (Alpha158, Alpha360, custom expressions),
computes forward return targets, and maintains strict Point-in-Time metadata tracking.
"""

from typing import List, Optional, Dict, Any, Union, Tuple
import pandas as pd
import numpy as np

from app.qlib.provider import QuantLabQlibDataProvider
from app.qlib.alpha158 import Alpha158Builder, Alpha360Builder
from app.qlib.expressions import QlibExpressionEvaluator, ts_ref, LookaheadBiasError


class QuantLabDataLoader:
    """Loads and transforms canonical market data into Qlib feature/target/metadata matrices."""

    def __init__(
        self,
        provider: Optional[QuantLabQlibDataProvider] = None,
        feature_type: str = "Alpha158",  # "Alpha158", "Alpha360", "custom"
        custom_expressions: Optional[List[str]] = None,
        target_expr: str = "Ref($close, -1) / $close - 1",
        target_horizon: int = 1,
    ):
        self.provider = provider or QuantLabQlibDataProvider()
        self.feature_type = feature_type
        self.custom_expressions = custom_expressions or []
        self.target_expr = target_expr
        self.target_horizon = target_horizon
        self.expr_evaluator = QlibExpressionEvaluator(allow_future=False)
        self.target_evaluator = QlibExpressionEvaluator(allow_future=True)

    def load_data(
        self,
        instruments: List[str],
        start_time: Union[str, pd.Timestamp],
        end_time: Union[str, pd.Timestamp],
        as_of: Optional[Union[str, pd.Timestamp]] = None,
        source_df: Optional[pd.DataFrame] = None,
    ) -> Tuple[pd.DataFrame, pd.Series, pd.DataFrame]:
        """Loads canonical data, computes alpha features and target, and separates metadata.
        
        Returns:
            Tuple of:
            - features_df: DataFrame of alpha factors indexed by (datetime, instrument)
            - target_series: pd.Series of forward targets indexed by (datetime, instrument)
            - metadata_df: DataFrame of PIT metadata columns indexed by (datetime, instrument)
        """
        # 1. Fetch raw panel with metadata
        raw_panel = self.provider.get_market_data(
            instruments=instruments,
            start_time=start_time,
            end_time=end_time,
            as_of=as_of,
            source_df=source_df,
        )

        if raw_panel.empty:
            empty_idx = pd.MultiIndex.from_tuples([], names=["datetime", "instrument"])
            return pd.DataFrame(index=empty_idx), pd.Series(index=empty_idx, dtype=float), pd.DataFrame(index=empty_idx)

        # 2. Extract PIT metadata columns
        meta_cols = ["source_timestamp", "information_available_at", "published_at", "ingestion_timestamp", "run_id"]
        avail_meta_cols = [c for c in meta_cols if c in raw_panel.columns]
        metadata_df = raw_panel[avail_meta_cols].copy()

        # 3. Compute Features (STRICTLY NO FUTURE ACCESS)
        if self.feature_type == "Alpha158":
            builder = Alpha158Builder()
            features_df = builder.build_features(raw_panel)
        elif self.feature_type == "Alpha360":
            builder = Alpha360Builder()
            features_df = builder.build_features(raw_panel)
        elif self.feature_type == "custom":
            features_dict = {}
            for expr in self.custom_expressions:
                features_dict[expr] = self.expr_evaluator.evaluate(expr, raw_panel)
            features_df = pd.DataFrame(features_dict, index=raw_panel.index)
        else:
            raise ValueError(f"Unknown feature_type: '{self.feature_type}'. Supported: 'Alpha158', 'Alpha360', 'custom'")

        # 4. Compute Forward Target (PERMITTED FUTURE ACCESS FOR TRAINING LABELS ONLY)
        if self.target_expr:
            target_series = self.target_evaluator.evaluate(self.target_expr, raw_panel)
            target_series.name = "target"
        else:
            # Fallback standard forward return: Ref(close, -horizon) / close - 1
            inst_level = "instrument" if "instrument" in raw_panel.index.names else 1
            close_s = raw_panel["close"].astype(float)
            ref_close = close_s.groupby(level=inst_level, group_keys=False).apply(
                lambda s: ts_ref(s, -self.target_horizon, allow_future=True)
            )
            target_series = (ref_close / (close_s + 1e-12)) - 1.0
            target_series.name = "target"

        return features_df, target_series, metadata_df
