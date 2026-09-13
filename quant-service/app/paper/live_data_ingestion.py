"""
Live Market Data Ingestion & Validation Service for QuantLab Paper Trading (Phase 17).
Handles:
1. Source timestamp preservation (never overwriting provider timestamps).
2. Point-in-time validation (OHLC consistency, price > 0, volume >= 0, impossible jump filter).
3. Stale data detection & automatic STALE_DATA_EVENT generation.
4. Duplicate event rejection & latency tracking (p50, p95, p99, max).
"""

from datetime import datetime, timezone, timedelta
from typing import Dict, List, Optional, Any, Tuple, Set
import hashlib
import json
import numpy as np
from pydantic import BaseModel, Field

from app.data_acquisition.models import ExchangeEnum


class LiveMarketObservation(BaseModel):
    """Normalized live/delayed market observation with immutable timestamps."""
    symbol: str
    exchange: ExchangeEnum = ExchangeEnum.NSE
    price: float
    open: float
    high: float
    low: float
    close: float
    volume: int
    quote_timestamp: datetime
    received_timestamp: datetime
    provider_timestamp: Optional[datetime] = None
    ingestion_timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    provider: str = "AUTHORIZED_FEED"
    provider_request_id: Optional[str] = None
    data_latency_ms: Optional[float] = None
    market_status: str = "OPEN"
    data_quality_status: str = "VALID"
    event_id: str = ""

    def calculate_event_id(self) -> str:
        """Generates deterministic unique identifier for deduplication."""
        src_ts = self.quote_timestamp.isoformat() if self.quote_timestamp else ""
        raw = f"{self.provider}:{self.symbol}:{self.exchange.value}:{src_ts}:{self.price}:{self.volume}"
        return hashlib.sha256(raw.encode("utf-8")).hexdigest()

    def model_post_init(self, __context: Any) -> None:
        if not self.event_id:
            self.event_id = self.calculate_event_id()
        if self.data_latency_ms is None and self.quote_timestamp and self.received_timestamp:
            delta = (self.received_timestamp - self.quote_timestamp).total_seconds() * 1000.0
            self.data_latency_ms = max(0.0, delta)


class StaleDataEvent(BaseModel):
    """Audit event recorded when data staleness breaches threshold."""
    timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    symbol: str
    provider: str
    age_seconds: float
    threshold_seconds: float
    action_taken: str = "HALT_NEW_PAPER_ORDERS"


class ObservationValidationResult(BaseModel):
    """Result of live observation validation."""
    is_valid: bool
    rejection_reason: Optional[str] = None
    is_stale: bool = False
    is_duplicate: bool = False
    observation: Optional[LiveMarketObservation] = None


class LiveMarketDataIngestionService:
    """
    Ingests and validates live/delayed observations for the paper trading pipeline.
    Ensures zero tainted or stale data enters the feature or inference engine.
    """

    def __init__(
        self,
        max_data_age_seconds: int = 180,
        max_price_jump_pct: float = 0.20,
        max_latency_threshold_ms: float = 30000.0
    ):
        self.max_data_age_seconds = max_data_age_seconds
        self.max_price_jump_pct = max_price_jump_pct
        self.max_latency_threshold_ms = max_latency_threshold_ms

        self.processed_event_ids: Set[str] = set()
        self.last_prices: Dict[str, float] = {}
        self.last_observation_times: Dict[str, datetime] = {}
        self.stale_events: List[StaleDataEvent] = []
        self.latencies_ms: List[float] = []
        self.symbol_bars: Dict[str, List[LiveMarketObservation]] = {}

    def ingest_and_validate(
        self,
        symbol: str,
        price: float,
        open_p: float,
        high_p: float,
        low_p: float,
        close_p: float,
        volume: int,
        quote_timestamp: datetime,
        received_timestamp: Optional[datetime] = None,
        provider_timestamp: Optional[datetime] = None,
        provider: str = "YAHOO_FINANCE",
        provider_request_id: Optional[str] = None,
        exchange: ExchangeEnum = ExchangeEnum.NSE,
        current_clock_time: Optional[datetime] = None
    ) -> ObservationValidationResult:
        """
        Validates incoming observation against all data quality invariants:
        1. Price > 0
        2. OHLC bounds consistency (low <= open, close <= high, low <= high)
        3. Volume >= 0
        4. Deduplication
        5. Timestamp safety & ordering
        6. Staleness threshold (MAX_DATA_AGE_SECONDS)
        7. Impossible jump filter (max_price_jump_pct)
        """
        recv_ts = received_timestamp or datetime.now(timezone.utc)
        clock_ts = current_clock_time or datetime.now(timezone.utc)

        # 1. Price > 0
        if price <= 0 or open_p <= 0 or high_p <= 0 or low_p <= 0 or close_p <= 0:
            return ObservationValidationResult(
                is_valid=False,
                rejection_reason="INVALID_PRICE: All prices must be strictly positive."
            )

        # 2. OHLC consistency
        if not (low_p <= high_p and low_p <= open_p <= high_p and low_p <= close_p <= high_p):
            return ObservationValidationResult(
                is_valid=False,
                rejection_reason=f"OHLC_INCONSISTENCY: low ({low_p}) <= open({open_p}), close({close_p}) <= high({high_p}) violated."
            )

        # 3. Volume >= 0
        if volume < 0:
            return ObservationValidationResult(
                is_valid=False,
                rejection_reason="INVALID_VOLUME: Volume cannot be negative."
            )

        # Create Observation object
        obs = LiveMarketObservation(
            symbol=symbol,
            exchange=exchange,
            price=price,
            open=open_p,
            high=high_p,
            low=low_p,
            close=close_p,
            volume=volume,
            quote_timestamp=quote_timestamp,
            received_timestamp=recv_ts,
            provider_timestamp=provider_timestamp,
            provider=provider,
            provider_request_id=provider_request_id,
            market_status="OPEN",
            data_quality_status="VALID"
        )

        # 4. Duplicate check
        if obs.event_id in self.processed_event_ids:
            return ObservationValidationResult(
                is_valid=False,
                is_duplicate=True,
                rejection_reason="DUPLICATE_EVENT: Event ID already processed."
            )

        # 5. Future timestamp / look-ahead rejection
        if quote_timestamp > (clock_ts + timedelta(seconds=10)):
            return ObservationValidationResult(
                is_valid=False,
                rejection_reason="FUTURE_TIMESTAMP: Quote timestamp is in the future relative to current clock."
            )

        # Out-of-order detection
        last_ts = self.last_observation_times.get(symbol)
        if last_ts and quote_timestamp < last_ts:
            return ObservationValidationResult(
                is_valid=False,
                rejection_reason=f"OUT_OF_ORDER_TIMESTAMP: Received {quote_timestamp} after {last_ts}."
            )

        # 6. Staleness check
        # Use provider timestamp if available, else quote timestamp
        ref_ts = provider_timestamp or quote_timestamp
        ref_utc = ref_ts if ref_ts.tzinfo else ref_ts.replace(tzinfo=timezone.utc)
        curr_utc = clock_ts if clock_ts.tzinfo else clock_ts.replace(tzinfo=timezone.utc)
        age_seconds = max(0.0, (curr_utc - ref_utc).total_seconds())

        if age_seconds > self.max_data_age_seconds:
            stale_event = StaleDataEvent(
                symbol=symbol,
                provider=provider,
                age_seconds=age_seconds,
                threshold_seconds=self.max_data_age_seconds,
                action_taken="HALT_NEW_PAPER_ORDERS"
            )
            self.stale_events.append(stale_event)
            obs.data_quality_status = "STALE"
            return ObservationValidationResult(
                is_valid=False,
                is_stale=True,
                rejection_reason=f"STALE_DATA: Age {age_seconds:.1f}s exceeds threshold {self.max_data_age_seconds}s.",
                observation=obs
            )

        # 7. Impossible jump check (e.g. >20% instantaneous jump)
        if symbol in self.last_prices:
            prev_p = self.last_prices[symbol]
            jump_pct = abs(price - prev_p) / prev_p
            if jump_pct > self.max_price_jump_pct:
                return ObservationValidationResult(
                    is_valid=False,
                    rejection_reason=f"IMPOSSIBLE_PRICE_JUMP: Price jumped {jump_pct * 100:.1f}% from {prev_p} to {price}."
                )

        # Observation passed all checks
        self.processed_event_ids.add(obs.event_id)
        self.last_prices[symbol] = price
        self.last_observation_times[symbol] = quote_timestamp
        self.last_valid_observation = obs
        self.total_observations_count = getattr(self, "total_observations_count", 0) + 1
        self.last_received_timestamp = recv_ts

        if obs.data_latency_ms is not None:
            self.latencies_ms.append(obs.data_latency_ms)

        if symbol not in self.symbol_bars:
            self.symbol_bars[symbol] = []
        self.symbol_bars[symbol].append(obs)

        return ObservationValidationResult(
            is_valid=True,
            observation=obs
        )

    def get_latency_metrics(self) -> Dict[str, float]:
        """Calculates p50, p95, p99, and max latency across processed observations."""
        if not self.latencies_ms:
            return {"p50_ms": 0.0, "p95_ms": 0.0, "p99_ms": 0.0, "max_ms": 0.0, "count": 0.0}

        arr = np.array(self.latencies_ms)
        return {
            "p50_ms": float(np.percentile(arr, 50)),
            "p95_ms": float(np.percentile(arr, 95)),
            "p99_ms": float(np.percentile(arr, 99)),
            "max_ms": float(np.max(arr)),
            "count": float(len(arr))
        }

    def get_telemetry(self) -> Dict[str, Any]:
        """Returns safe telemetry metadata for the ingestion pipeline."""
        last_obs_dict = None
        if hasattr(self, "last_valid_observation") and self.last_valid_observation:
            last_obs_dict = {
                "event_id": self.last_valid_observation.event_id,
                "symbol": self.last_valid_observation.symbol,
                "price": self.last_valid_observation.price,
                "quote_timestamp": self.last_valid_observation.quote_timestamp.isoformat() if self.last_valid_observation.quote_timestamp else None,
                "provider": self.last_valid_observation.provider,
                "provider_timestamp": self.last_valid_observation.provider_timestamp.isoformat() if self.last_valid_observation.provider_timestamp else None,
                "received_timestamp": self.last_valid_observation.received_timestamp.isoformat() if self.last_valid_observation.received_timestamp else None,
                "data_latency_ms": self.last_valid_observation.data_latency_ms
            }

        return {
            "total_valid_observations": getattr(self, "total_observations_count", 0),
            "stale_events_count": len(self.stale_events),
            "last_received_timestamp": getattr(self, "last_received_timestamp", None).isoformat() if getattr(self, "last_received_timestamp", None) else None,
            "last_valid_observation": last_obs_dict,
            "latency_metrics": self.get_latency_metrics()
        }

    def has_sufficient_warmup(self, symbol: str, required_bars: int = 20) -> bool:
        """Verifies that sufficient historical observations exist before computing rolling indicators."""
        bars = self.symbol_bars.get(symbol, [])
        return len(bars) >= required_bars
