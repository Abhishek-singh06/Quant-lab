"""Base target builder abstraction for trading and investing horizons."""

from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
import pandas as pd
from datetime import datetime


class BaseHorizonTargetBuilder(ABC):
    """Abstract base class for horizon-specific target calculation."""

    def __init__(self, target_set_version: str):
        self.target_set_version = target_set_version

    @abstractmethod
    def calculate_targets(
        self,
        symbol: str,
        as_of: datetime,
        future_ohlcv_df: pd.DataFrame,
        benchmark_ohlcv_df: Optional[pd.DataFrame] = None
    ) -> Dict[str, float]:
        """Calculate forward-looking target labels starting strictly after `as_of`.
        
        Targets must only consume future observations from `as_of + 1` forward.
        """
        pass
