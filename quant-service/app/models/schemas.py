"""Pydantic schemas for the quant service.

These represent real market data structures
for Indian equity markets.
"""

from datetime import datetime
from pydantic import BaseModel, Field


class StockQuote(BaseModel):
    """Stock quote data."""
    symbol: str
    name: str
    exchange: str = Field(pattern="^(NSE|BSE)$")
    last_price: float
    open: float
    high: float
    low: float
    close: float
    volume: int
    change: float
    change_percent: float
    timestamp: datetime


class OHLCV(BaseModel):
    """OHLCV candlestick data."""
    date: str
    open: float
    high: float
    low: float
    close: float
    volume: int


class MarketIndex(BaseModel):
    """Market index data."""
    symbol: str
    name: str
    last_price: float
    change: float
    change_percent: float
    timestamp: datetime
