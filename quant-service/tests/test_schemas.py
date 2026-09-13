"""Tests for Pydantic schemas."""

from datetime import datetime, timezone

import pytest
from pydantic import ValidationError

from app.models.schemas import StockQuote, OHLCV, MarketIndex


def test_stock_quote_valid():
    """Test creating a valid stock quote."""
    quote = StockQuote(
        symbol="RELIANCE",
        name="Reliance Industries",
        exchange="NSE",
        last_price=2450.50,
        open=2440.00,
        high=2460.00,
        low=2435.00,
        close=2445.00,
        volume=5000000,
        change=5.50,
        change_percent=0.22,
        timestamp=datetime.now(timezone.utc),
    )
    assert quote.symbol == "RELIANCE"
    assert quote.exchange == "NSE"


def test_stock_quote_invalid_exchange():
    """Test that invalid exchange raises validation error."""
    with pytest.raises(ValidationError):
        StockQuote(
            symbol="RELIANCE",
            name="Reliance Industries",
            exchange="INVALID",
            last_price=2450.50,
            open=2440.00,
            high=2460.00,
            low=2435.00,
            close=2445.00,
            volume=5000000,
            change=5.50,
            change_percent=0.22,
            timestamp=datetime.now(timezone.utc),
        )


def test_ohlcv():
    """Test creating OHLCV data."""
    candle = OHLCV(
        date="2024-01-15",
        open=2440.00,
        high=2460.00,
        low=2435.00,
        close=2450.50,
        volume=5000000,
    )
    assert candle.date == "2024-01-15"
    assert candle.volume == 5000000


def test_market_index():
    """Test creating market index data."""
    index = MarketIndex(
        symbol="NIFTY 50",
        name="Nifty 50",
        last_price=24850.25,
        change=182.30,
        change_percent=0.74,
        timestamp=datetime.now(timezone.utc),
    )
    assert index.symbol == "NIFTY 50"
