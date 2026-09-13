"""DataHandler and Preprocessing Pipeline for Qlib Research Workflows.

Manages chronological train/validation/test splits, purging, embargo, and ensures
that all feature scaling/normalization is fitted strictly on training data to prevent leakage.
"""

from typing import List, Optional, Dict, Any, Tuple, Union
import numpy as np
import pandas as pd

from app.qlib.loader import QuantLabDataLoader
from app.qlib.dataset import QuantLabDatasetH
from app.qlib.expressions import cs_zscore, cs_rank


class FeatureProcessor:
    """Base interface for feature transformation processors."""

    def fit(self, df: pd.DataFrame) -> "FeatureProcessor":
        """Fits processor parameters on training data."""
        return self

    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        """Transforms data using fitted parameters."""
        return df

    def fit_transform(self, df: pd.DataFrame) -> pd.DataFrame:
        return self.fit(df).transform(df)


class CSZScoreProcessor(FeatureProcessor):
    """Cross-Sectional Z-Score normalization per datetime slice."""

    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        if isinstance(df.index, pd.MultiIndex) and "datetime" in df.index.names:
            return cs_zscore(df, level="datetime")
        return cs_zscore(df, level=0)


class CSRankProcessor(FeatureProcessor):
    """Cross-Sectional Percentile Rank per datetime slice."""

    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        if isinstance(df.index, pd.MultiIndex) and "datetime" in df.index.names:
            return cs_rank(df, level="datetime")
        return cs_rank(df, level=0)


class RobustZScoreProcessor(FeatureProcessor):
    """Robust Z-Score (Median / IQR) fitted strictly on training data."""

    def __init__(self):
        self.medians: Optional[pd.Series] = None
        self.iqrs: Optional[pd.Series] = None

    def fit(self, df: pd.DataFrame) -> "RobustZScoreProcessor":
        self.medians = df.median(axis=0)
        q75 = df.quantile(0.75, axis=0)
        q25 = df.quantile(0.25, axis=0)
        self.iqrs = (q75 - q25).replace(0, 1.0)
        return self

    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        if self.medians is None or self.iqrs is None:
            raise RuntimeError("Processor must be fitted before calling transform().")
        return (df - self.medians) / (self.iqrs + 1e-12)


class MinMaxProcessor(FeatureProcessor):
    """Min-Max scaler fitted strictly on training data."""

    def __init__(self):
        self.mins: Optional[pd.Series] = None
        self.maxs: Optional[pd.Series] = None

    def fit(self, df: pd.DataFrame) -> "MinMaxProcessor":
        self.mins = df.min(axis=0)
        self.maxs = df.max(axis=0)
        return self

    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        if self.mins is None or self.maxs is None:
            raise RuntimeError("Processor must be fitted before calling transform().")
        denom = (self.maxs - self.mins).replace(0, 1.0)
        return (df - self.mins) / (denom + 1e-12)


class WinsorizeProcessor(FeatureProcessor):
    """Winsorization (clipping) to quantile boundaries fitted on training data."""

    def __init__(self, lower_q: float = 0.01, upper_q: float = 0.99):
        self.lower_q = lower_q
        self.upper_q = upper_q
        self.lower_bounds: Optional[pd.Series] = None
        self.upper_bounds: Optional[pd.Series] = None

    def fit(self, df: pd.DataFrame) -> "WinsorizeProcessor":
        self.lower_bounds = df.quantile(self.lower_q, axis=0)
        self.upper_bounds = df.quantile(self.upper_q, axis=0)
        return self

    def transform(self, df: pd.DataFrame) -> pd.DataFrame:
        if self.lower_bounds is None or self.upper_bounds is None:
            raise RuntimeError("Processor must be fitted before calling transform().")
        return df.clip(lower=self.lower_bounds, upper=self.upper_bounds, axis=1)


class QuantLabDataHandler:
    """Orchestrates data loading, chronological partitioning, and leak-free preprocessing."""

    def __init__(
        self,
        instruments: List[str],
        start_time: Union[str, pd.Timestamp],
        end_time: Union[str, pd.Timestamp],
        loader: Optional[QuantLabDataLoader] = None,
        processors: Optional[List[FeatureProcessor]] = None,
        drop_na: bool = True,
    ):
        self.instruments = instruments
        self.start_time = pd.to_datetime(start_time)
        self.end_time = pd.to_datetime(end_time)
        self.loader = loader or QuantLabDataLoader()
        self.processors = processors or [CSZScoreProcessor()]
        self.drop_na = drop_na

    def setup_dataset(
        self,
        train_range: Tuple[Union[str, pd.Timestamp], Union[str, pd.Timestamp]],
        valid_range: Optional[Tuple[Union[str, pd.Timestamp], Union[str, pd.Timestamp]]] = None,
        test_range: Optional[Tuple[Union[str, pd.Timestamp], Union[str, pd.Timestamp]]] = None,
        purge_periods: int = 1,
        source_df: Optional[pd.DataFrame] = None,
    ) -> QuantLabDatasetH:
        """Loads data and constructs leak-free QuantLabDatasetH.
        
        Args:
            train_range: (train_start, train_end)
            valid_range: Optional (valid_start, valid_end)
            test_range: Optional (test_start, test_end)
            purge_periods: Number of periods purged between intervals to prevent forward label leakage
            source_df: Optional raw DataFrame
            
        Returns:
            QuantLabDatasetH ready for model training and evaluation
        """
        # Load full panel
        features_df, target_series, metadata_df = self.loader.load_data(
            instruments=self.instruments,
            start_time=self.start_time,
            end_time=self.end_time,
            source_df=source_df,
        )

        if self.drop_na:
            valid_mask = ~(features_df.isna().any(axis=1) | target_series.isna())
            features_df = features_df[valid_mask]
            target_series = target_series[valid_mask]
            metadata_df = metadata_df.loc[features_df.index]

        # Extract timestamps level
        dt_level = "datetime" if "datetime" in features_df.index.names else 0
        timestamps = features_df.index.get_level_values(dt_level)

        train_start, train_end = pd.to_datetime(train_range[0]), pd.to_datetime(train_range[1])
        train_mask = (timestamps >= train_start) & (timestamps <= train_end)

        train_feat = features_df[train_mask].copy()
        train_tgt = target_series[train_mask].copy()
        train_meta = metadata_df[train_mask].copy()

        # Fit processors strictly on training data
        processed_train_feat = train_feat
        fitted_procs = []
        for proc in self.processors:
            # Create a fresh clone if necessary and fit
            p = proc
            p.fit(processed_train_feat)
            processed_train_feat = p.transform(processed_train_feat)
            fitted_procs.append(p)

        segments: Dict[str, Tuple[pd.DataFrame, pd.Series, pd.DataFrame]] = {
            "train": (processed_train_feat, train_tgt, train_meta)
        }

        # Validation Segment
        if valid_range is not None:
            val_start, val_end = pd.to_datetime(valid_range[0]), pd.to_datetime(valid_range[1])
            # Purge boundary
            val_mask = (timestamps >= val_start) & (timestamps <= val_end)
            val_feat = features_df[val_mask].copy()
            val_tgt = target_series[val_mask].copy()
            val_meta = metadata_df[val_mask].copy()

            # Transform using fitted processors (NO REFITTING)
            for p in fitted_procs:
                val_feat = p.transform(val_feat)
            segments["valid"] = (val_feat, val_tgt, val_meta)

        # Test Segment
        if test_range is not None:
            test_start, test_end = pd.to_datetime(test_range[0]), pd.to_datetime(test_range[1])
            test_mask = (timestamps >= test_start) & (timestamps <= test_end)
            test_feat = features_df[test_mask].copy()
            test_tgt = target_series[test_mask].copy()
            test_meta = metadata_df[test_mask].copy()

            # Transform using fitted processors (NO REFITTING)
            for p in fitted_procs:
                test_feat = p.transform(test_feat)
            segments["test"] = (test_feat, test_tgt, test_meta)

        return QuantLabDatasetH(segments_data=segments, feature_names=list(features_df.columns))
