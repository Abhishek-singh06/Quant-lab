"""Base Provider Abstraction for Market Data Acquisition."""

from abc import ABC, abstractmethod
from typing import List, Dict, Any, Optional, Set, Tuple
from datetime import date, datetime
from enum import Enum
import asyncio

from app.data_acquisition.models import (
    RawOHLCVRecord,
    CorporateActionRecord,
    InstrumentIdentity,
    FinancialStatementRecord,
    InstitutionalFlowRecord,
    ExchangeEnum,
)


class ProviderCapability(str, Enum):
    HISTORICAL_OHLCV = "HISTORICAL_OHLCV"
    HISTORICAL_BARS = "HISTORICAL_BARS"
    REALTIME_QUOTES = "REALTIME_QUOTES"
    STREAMING_QUOTES = "STREAMING_QUOTES"
    DELAYED_QUOTES = "DELAYED_QUOTES"
    EOD_QUOTES = "EOD_QUOTES"
    MARKET_STATUS = "MARKET_STATUS"
    CORPORATE_ACTIONS = "CORPORATE_ACTIONS"
    INSTRUMENT_MASTER = "INSTRUMENT_MASTER"
    FINANCIAL_STATEMENTS = "FINANCIAL_STATEMENTS"
    INSTITUTIONAL_FLOWS = "INSTITUTIONAL_FLOWS"
    MUTUAL_FUND_NAV = "MUTUAL_FUND_NAV"


class CapabilityStatus(str, Enum):
    VERIFIED = "VERIFIED"
    NOT_VERIFIED = "NOT_VERIFIED"
    NOT_SUPPORTED = "NOT_SUPPORTED"
    CONFIGURATION_REQUIRED = "CONFIGURATION_REQUIRED"


class ProviderError(Exception):
    """Base exception for data provider failures."""
    def __init__(self, provider_name: str, message: str):
        super().__init__(f"[{provider_name}] {message}")
        self.provider_name = provider_name


class ProviderUnavailableError(ProviderError):
    """Raised when data provider service is unreachable or unconfigured."""
    pass


class AuthenticationError(ProviderError):
    """Raised when API credentials are missing or rejected."""
    pass


class RateLimitError(ProviderError):
    """Raised when request rate limits are exceeded."""
    pass


class MalformedResponseError(ProviderError):
    """Raised when provider response format fails parsing."""
    pass


class BaseMarketDataProvider(ABC):
    """Abstract Base Class for all external market data provider adapters."""

    def __init__(self, provider_name: str, supported_capabilities: Set[ProviderCapability]):
        self.provider_name = provider_name
        self.supported_capabilities = supported_capabilities

    def supports(self, capability: ProviderCapability) -> bool:
        """Verifies if this provider natively implements the requested capability."""
        return capability in self.supported_capabilities

    def assert_capability(self, capability: ProviderCapability) -> None:
        """Enforces capability support or raises explicit exception."""
        if not self.supports(capability):
            raise NotImplementedError(
                f"Provider '{self.provider_name}' does not support capability '{capability.value}'."
            )

    @abstractmethod
    async def is_available(self) -> bool:
        """Checks live connectivity and configuration readiness."""
        pass

    @abstractmethod
    async def fetch_historical_ohlcv(
        self,
        symbol: str,
        start_date: date,
        end_date: date,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
        ingestion_run_id: str = "DEFAULT_RUN",
    ) -> List[RawOHLCVRecord]:
        """Fetches raw unadjusted historical OHLCV records."""
        pass

    @abstractmethod
    async def fetch_corporate_actions(
        self,
        symbol: str,
        start_date: date,
        end_date: date,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
        ingestion_run_id: str = "DEFAULT_RUN",
    ) -> List[CorporateActionRecord]:
        """Fetches official corporate actions (splits, bonuses, dividends)."""
        pass

    async def fetch_instrument_master(
        self,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
    ) -> List[InstrumentIdentity]:
        """Fetches instrument identity catalog if supported."""
        self.assert_capability(ProviderCapability.INSTRUMENT_MASTER)
        return []

    async def fetch_financial_statements(
        self,
        symbol: str,
    ) -> List[FinancialStatementRecord]:
        """Fetches financial statements if supported."""
        self.assert_capability(ProviderCapability.FINANCIAL_STATEMENTS)
        return []

    async def fetch_institutional_flows(
        self,
        start_date: date,
        end_date: date,
    ) -> List[InstitutionalFlowRecord]:
        """Fetches FII/DII daily net investment flows if supported."""
        self.assert_capability(ProviderCapability.INSTITUTIONAL_FLOWS)
        return []
