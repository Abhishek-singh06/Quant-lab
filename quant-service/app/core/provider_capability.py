"""Provider Capability Model, Instrument Identity, and Execution Error Hierarchy.

Clean-room implementation inspired by CCXT exchange capability declarations,
normalized instrument identity, and fail-closed error handling for Indian Equities.
"""

from enum import Enum
from typing import Dict, List, Optional, Any, Set
from datetime import datetime, timezone, date
from pydantic import BaseModel, Field


class ProviderCapability(str, Enum):
    """Explicit capabilities supported by market data and broker providers."""
    MARKET_DATA = "MARKET_DATA"
    HISTORICAL_DATA = "HISTORICAL_DATA"
    REALTIME_QUOTES = "REALTIME_QUOTES"
    WEBSOCKET = "WEBSOCKET"
    NEWS = "NEWS"
    CORPORATE_ACTIONS = "CORPORATE_ACTIONS"
    PLACE_ORDER = "PLACE_ORDER"
    CANCEL_ORDER = "CANCEL_ORDER"
    ORDER_STATUS = "ORDER_STATUS"
    POSITIONS = "POSITIONS"
    HOLDINGS = "HOLDINGS"
    MARGINS = "MARGINS"
    RECONCILIATION = "RECONCILIATION"


class InstrumentIdentity(BaseModel):
    """Canonical representation of an equity instrument."""
    instrument_id: str = Field(..., description="Unique internal instrument UUID / slug")
    exchange: str = Field(default="NSE", description="Exchange: NSE, BSE")
    symbol: str = Field(..., description="Current ticker symbol e.g. RELIANCE, TCS")
    isin: Optional[str] = Field(default=None, description="ISIN code e.g. INE002A01018")
    asset_class: str = Field(default="EQUITY", description="Asset class e.g. EQUITY, INDEX")
    currency: str = Field(default="INR", description="Trading currency")
    lot_size: int = Field(default=1, description="Standard lot size")
    tick_size: float = Field(default=0.05, description="Minimum price variation")
    historical_symbols: List[str] = Field(default_factory=list, description="Historical symbol aliases")
    valid_from: Optional[date] = None
    valid_to: Optional[date] = None


# ---------------------------------------------------------------------------
# Normalized Provider Error Hierarchy
# ---------------------------------------------------------------------------

class ProviderError(Exception):
    """Base exception for provider and broker interactions."""
    def __init__(self, provider: str, error_category: str, message: str) -> None:
        super().__init__(f"[{provider}] {error_category}: {message}")
        self.provider = provider
        self.error_category = error_category
        self.message = message


class ProviderAuthenticationError(ProviderError):
    def __init__(self, provider: str, message: str) -> None:
        super().__init__(provider, "AUTHENTICATION_FAILURE", message)


class ProviderRateLimitError(ProviderError):
    def __init__(self, provider: str, message: str, retry_after_ms: int = 1000) -> None:
        super().__init__(provider, "RATE_LIMITED", message)
        self.retry_after_ms = retry_after_ms


class ProviderTimeoutError(ProviderError):
    def __init__(self, provider: str, message: str) -> None:
        super().__init__(provider, "TIMEOUT", message)


class ProviderDataStaleError(ProviderError):
    def __init__(self, provider: str, message: str) -> None:
        super().__init__(provider, "DATA_STALE", message)


class UnsupportedCapabilityError(ProviderError):
    def __init__(self, provider: str, capability: ProviderCapability) -> None:
        super().__init__(provider, "UNSUPPORTED_CAPABILITY", f"Provider does not support: {capability.value}")
        self.capability = capability
