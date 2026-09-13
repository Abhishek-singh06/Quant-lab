"""Base feature extractor abstraction for trading and investing horizons."""

from abc import ABC, abstractmethod
from typing import Dict, List, Any, Optional
import pandas as pd
from datetime import datetime


class BaseHorizonFeatureExtractor(ABC):
    """Abstract base class for horizon-specific feature extraction."""

    def __init__(self, feature_set_version: str):
        self.feature_set_version = feature_set_version

    @abstractmethod
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
        """Extract Point-in-Time feature map for a single symbol at timestamp `as_of`.
        
        Guarantees: No data with timestamp > as_of is consumed.
        """
        pass

    @abstractmethod
    def get_feature_names(self) -> List[str]:
        """Return list of exact feature names produced by this extractor."""
        pass
