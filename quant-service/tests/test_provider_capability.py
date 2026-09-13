"""Tests for ProviderCapability, InstrumentIdentity, and Normalized Errors."""

import pytest
from app.core.provider_capability import (
    ProviderCapability,
    InstrumentIdentity,
    ProviderError,
    ProviderRateLimitError,
    UnsupportedCapabilityError,
)


def test_instrument_identity_defaults():
    inst = InstrumentIdentity(
        instrument_id="inst-reliance-001",
        exchange="NSE",
        symbol="RELIANCE",
        isin="INE002A01018"
    )
    assert inst.currency == "INR"
    assert inst.lot_size == 1
    assert inst.tick_size == 0.05
    assert inst.historical_symbols == []


def test_rate_limit_error():
    err = ProviderRateLimitError(provider="NSE_DATA", message="Throttled by rate limit", retry_after_ms=2500)
    assert err.provider == "NSE_DATA"
    assert err.error_category == "RATE_LIMITED"
    assert err.retry_after_ms == 2500
    assert "[NSE_DATA] RATE_LIMITED" in str(err)


def test_unsupported_capability_error():
    err = UnsupportedCapabilityError(provider="MOCK_SANDBOX", capability=ProviderCapability.MARGINS)
    assert err.capability == ProviderCapability.MARGINS
    assert err.error_category == "UNSUPPORTED_CAPABILITY"
    assert "Provider does not support: MARGINS" in str(err)
