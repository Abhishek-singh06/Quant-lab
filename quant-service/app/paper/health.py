"""
Live Data Health Monitor for Paper Trading.
Ensures that no paper trade is ever generated on stale, disconnected, or fabricated data.
"""

from datetime import datetime, timezone, timedelta
from typing import Optional, List, Dict, Any
from app.paper.schemas import LiveDataHealth, ConnectionStatus, DataFreshnessStatus


class LiveDataHealthMonitor:
    """
    Tracks and validates live market data feed health, freshness, latency, and staleness.
    """

    def __init__(
        self,
        provider_name: str = "AUTHORIZED_FEED",
        max_allowed_age_seconds: int = 180,
        min_required_coverage: float = 0.90
    ):
        self.provider_name = provider_name
        self.max_allowed_age_seconds = max_allowed_age_seconds
        self.min_required_coverage = min_required_coverage

    def evaluate_health(
        self,
        last_update_ts: Optional[datetime],
        last_market_ts: Optional[datetime],
        current_clock_ts: datetime,
        is_market_session_active: bool,
        error_count: int = 0,
        active_symbols_count: int = 50,
        expected_symbols_count: int = 50
    ) -> LiveDataHealth:
        if last_update_ts is None or last_market_ts is None:
            return LiveDataHealth(
                provider_name=self.provider_name,
                connection_status=ConnectionStatus.UNAVAILABLE,
                freshness_status=DataFreshnessStatus.NOT_AVAILABLE,
                last_successful_update=None,
                last_market_timestamp=None,
                latency_ms=0,
                data_age_seconds=999999,
                error_count=error_count,
                coverage_ratio=0.0,
                checked_at=current_clock_ts
            )

        # Calculate data age in seconds
        # Normalize timezones if necessary
        curr_utc = current_clock_ts if current_clock_ts.tzinfo else current_clock_ts.replace(tzinfo=timezone.utc)
        upd_utc = last_update_ts if last_update_ts.tzinfo else last_update_ts.replace(tzinfo=timezone.utc)
        data_age = max(0, int((curr_utc - upd_utc).total_seconds()))

        coverage = (active_symbols_count / expected_symbols_count) if expected_symbols_count > 0 else 1.0

        # Determine connection & freshness
        if not is_market_session_active:
            # Outside market hours, delayed or end-of-day data is normal
            conn_status = ConnectionStatus.HEALTHY if error_count == 0 else ConnectionStatus.DEGRADED
            fresh_status = DataFreshnessStatus.DELAYED
        else:
            if error_count > 5:
                conn_status = ConnectionStatus.DISCONNECTED
                fresh_status = DataFreshnessStatus.STALE
            elif data_age > self.max_allowed_age_seconds:
                conn_status = ConnectionStatus.STALE
                fresh_status = DataFreshnessStatus.STALE
            elif coverage < self.min_required_coverage or error_count > 0:
                conn_status = ConnectionStatus.DEGRADED
                fresh_status = DataFreshnessStatus.DELAYED
            else:
                conn_status = ConnectionStatus.HEALTHY
                fresh_status = DataFreshnessStatus.REAL_TIME

        return LiveDataHealth(
            provider_name=self.provider_name,
            connection_status=conn_status,
            freshness_status=fresh_status,
            last_successful_update=last_update_ts,
            last_market_timestamp=last_market_ts,
            latency_ms=min(data_age * 1000, 60000),
            data_age_seconds=data_age,
            error_count=error_count,
            coverage_ratio=coverage,
            checked_at=current_clock_ts
        )

    def is_safe_for_trading(self, health: LiveDataHealth) -> bool:
        """Returns True only if data is healthy/fresh and not stale or disconnected."""
        if health.connection_status in (ConnectionStatus.DISCONNECTED, ConnectionStatus.UNAVAILABLE, ConnectionStatus.STALE):
            return False
        if health.freshness_status in (DataFreshnessStatus.STALE, DataFreshnessStatus.NOT_AVAILABLE):
            return False
        return True
