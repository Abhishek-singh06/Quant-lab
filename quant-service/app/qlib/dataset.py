"""Dataset Abstraction for Qlib Research Workflows.

Provides hierarchical multi-segment dataset representation with segment slicing,
label handling, and Point-in-Time metadata tracking.
"""

from typing import Dict, Tuple, List, Optional, Union
import pandas as pd
import numpy as np


class QuantLabDatasetH:
    """Hierarchical dataset representation conforming to Qlib dataset protocol."""

    def __init__(
        self,
        segments_data: Dict[str, Tuple[pd.DataFrame, pd.Series, pd.DataFrame]],
        feature_names: Optional[List[str]] = None,
    ):
        """
        Args:
            segments_data: Dict mapping segment name ("train", "valid", "test")
                           to (features_df, target_series, metadata_df)
            feature_names: Optional list of feature column names
        """
        self.segments_data = segments_data
        self.feature_names = feature_names or (
            list(next(iter(segments_data.values()))[0].columns) if segments_data else []
        )

    def prepare(
        self,
        segment: str = "train",
        col_set: Union[str, List[str]] = ["feature", "label"],
    ) -> Union[pd.DataFrame, pd.Series, Tuple[pd.DataFrame, pd.Series]]:
        """Prepares and retrieves specified columns for a dataset segment.
        
        Args:
            segment: "train", "valid", or "test"
            col_set: "feature", "label", or ["feature", "label"]
            
        Returns:
            DataFrame, Series, or Tuple(DataFrame, Series)
        """
        if segment not in self.segments_data:
            raise KeyError(f"Segment '{segment}' not found in dataset. Available: {list(self.segments_data.keys())}")

        features_df, target_series, _ = self.segments_data[segment]

        if col_set == "feature" or col_set == ["feature"]:
            return features_df
        elif col_set == "label" or col_set == ["label"]:
            return target_series
        elif isinstance(col_set, list) and set(col_set) == {"feature", "label"}:
            return features_df, target_series
        else:
            raise ValueError(f"Unsupported col_set: {col_set}")

    def get_meta(self, segment: str = "train") -> pd.DataFrame:
        """Retrieves Point-in-Time metadata DataFrame for a segment."""
        if segment not in self.segments_data:
            raise KeyError(f"Segment '{segment}' not found in dataset.")
        return self.segments_data[segment][2]

    def get_index(self, segment: str = "train") -> pd.Index:
        """Retrieves index for a segment."""
        if segment not in self.segments_data:
            raise KeyError(f"Segment '{segment}' not found in dataset.")
        return self.segments_data[segment][0].index

    def list_segments(self) -> List[str]:
        """Returns list of available segments."""
        return list(self.segments_data.keys())
