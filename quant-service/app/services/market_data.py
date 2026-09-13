"""Market data service.

Phase 1: Service interface definition.
Will connect to real market data sources in subsequent phases.

IMPORTANT: This service will NOT generate fake predictions
or present synthetic data as real market data.
"""

from abc import ABC, abstractmethod
from typing import Optional
from datetime import date

import pandas as pd


class MarketDataProvider(ABC):
    """Abstract base class for market data providers."""

    @abstractmethod
    async def get_quote(self, symbol: str, exchange: str = "NSE") -> dict:
        """Get current quote for a symbol."""
        ...

    @abstractmethod
    async def get_historical(
        self,
        symbol: str,
        start_date: date,
        end_date: Optional[date] = None,
        exchange: str = "NSE",
    ) -> pd.DataFrame:
        """Get historical OHLCV data for a symbol."""
        ...

    @abstractmethod
    async def get_index(
        self,
        index_symbol: str,
    ) -> dict:
        """Get current index data."""
        ...
