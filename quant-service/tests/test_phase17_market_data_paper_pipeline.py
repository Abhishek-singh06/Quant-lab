"""
Phase 17 Comprehensive Test Suite: Real-Time / Delayed Market Data + Live Paper-Trading Pipeline.
Validates all 22+ core architectural requirements:
1. Provider capability detection
2. Stale-data rejection & StaleDataEvent logging
3. Malformed-data rejection (OHLC violation, negative volume, negative price)
4. Duplicate-data rejection & idempotency
5. Feature warm-up requirements
6. Point-in-Time timestamp safety (feature_ts <= prediction_ts)
7. Frozen Ridge model artifact loading & SHA-256 integrity
8. Fail-closed model inference on missing/corrupted data
9. Signal generation & evidence structuring
10. Risk engine rejection on limits & cash
11. Paper order creation
12. Paper/live separation & zero broker routing
13. Order idempotency controls
14. Market hours & NSE holiday enforcement
15. Emergency stop activation and recovery
16. Daily loss protection circuit breaker
17. Provider disconnect handling
18. Safe reconnection after outage
19. Paper portfolio reconciliation
20. Session start gate (all 9 subsystems)
21. Security & secrets omission in journals
22. Replay/delayed data cannot be labelled real-time
23. End-to-end fixture integration test (FIXTURE_INTEGRATION_TEST)
"""

import pytest
from datetime import datetime, timezone, timedelta, date, time
from uuid import uuid4

from app.paper.schemas import (
    ExecutionMode,
    PaperTradingStatus,
    DataFreshnessStatus,
    ConnectionStatus,
    OrderSide,
    OrderStatus,
    OrderType,
    ClockType
)
from app.paper.clock import PaperTradingClock, IST_TZ
from app.paper.health import LiveDataHealthMonitor
from app.paper.execution import PaperExecutionSimulator
from app.paper.portfolio import PaperPortfolioManager
from app.paper.frozen_model import FrozenRidgeModelManager, FrozenModelArtifact
from app.paper.live_data_ingestion import (
    LiveMarketDataIngestionService,
    LiveMarketObservation,
    ObservationValidationResult,
    StaleDataEvent
)
from app.paper.session_runner import PaperTradingSessionOrchestrator
from app.data_acquisition.models import ExchangeEnum
from app.data_acquisition.providers.base import (
    BaseMarketDataProvider,
    ProviderCapability,
    CapabilityStatus
)
from app.data_acquisition.providers.yahoo_adapter import YahooFinanceIndianMarketAdapter
from app.data_acquisition.trading_calendar import IndianTradingCalendar


# 1. Provider capability detection
def test_provider_capability_detection():
    adapter = YahooFinanceIndianMarketAdapter()
    assert adapter.supports(ProviderCapability.HISTORICAL_OHLCV)
    assert adapter.supports(ProviderCapability.CORPORATE_ACTIONS)
    assert not adapter.supports(ProviderCapability.INSTITUTIONAL_FLOWS)
    assert not adapter.supports(ProviderCapability.STREAMING_QUOTES)

    # Capability assertion raises NotImplementedError for unsupported
    with pytest.raises(NotImplementedError):
        adapter.assert_capability(ProviderCapability.STREAMING_QUOTES)


# 2. Stale-data rejection
def test_stale_data_rejection():
    service = LiveMarketDataIngestionService(max_data_age_seconds=180)
    now_ts = datetime(2024, 10, 15, 10, 0, 0, tzinfo=timezone.utc)
    old_ts = now_ts - timedelta(seconds=250)

    res = service.ingest_and_validate(
        symbol="RELIANCE",
        price=2950.0,
        open_p=2940.0,
        high_p=2960.0,
        low_p=2930.0,
        close_p=2950.0,
        volume=50000,
        quote_timestamp=old_ts,
        received_timestamp=now_ts,
        current_clock_time=now_ts
    )

    assert not res.is_valid
    assert res.is_stale
    assert len(service.stale_events) == 1
    assert service.stale_events[0].symbol == "RELIANCE"
    assert service.stale_events[0].action_taken == "HALT_NEW_PAPER_ORDERS"


# 3. Malformed-data rejection
def test_malformed_data_rejection():
    service = LiveMarketDataIngestionService()
    now_ts = datetime(2024, 10, 15, 10, 0, 0, tzinfo=timezone.utc)

    # Negative price
    res_neg_p = service.ingest_and_validate(
        symbol="TCS", price=-10.0, open_p=3800.0, high_p=3850.0, low_p=3780.0, close_p=3800.0,
        volume=1000, quote_timestamp=now_ts, current_clock_time=now_ts
    )
    assert not res_neg_p.is_valid
    assert "INVALID_PRICE" in res_neg_p.rejection_reason

    # OHLC violation: low > high
    res_ohlc = service.ingest_and_validate(
        symbol="TCS", price=3800.0, open_p=3800.0, high_p=3750.0, low_p=3850.0, close_p=3800.0,
        volume=1000, quote_timestamp=now_ts, current_clock_time=now_ts
    )
    assert not res_ohlc.is_valid
    assert "OHLC_INCONSISTENCY" in res_ohlc.rejection_reason

    # Negative volume
    res_neg_v = service.ingest_and_validate(
        symbol="TCS", price=3800.0, open_p=3800.0, high_p=3850.0, low_p=3780.0, close_p=3800.0,
        volume=-50, quote_timestamp=now_ts, current_clock_time=now_ts
    )
    assert not res_neg_v.is_valid
    assert "INVALID_VOLUME" in res_neg_v.rejection_reason


# 4. Duplicate-data rejection
def test_duplicate_data_rejection():
    service = LiveMarketDataIngestionService()
    now_ts = datetime(2024, 10, 15, 10, 0, 0, tzinfo=timezone.utc)

    res1 = service.ingest_and_validate(
        symbol="INFY", price=1950.0, open_p=1940.0, high_p=1960.0, low_p=1930.0, close_p=1950.0,
        volume=25000, quote_timestamp=now_ts, current_clock_time=now_ts
    )
    assert res1.is_valid

    res2 = service.ingest_and_validate(
        symbol="INFY", price=1950.0, open_p=1940.0, high_p=1960.0, low_p=1930.0, close_p=1950.0,
        volume=25000, quote_timestamp=now_ts, current_clock_time=now_ts
    )
    assert not res2.is_valid
    assert res2.is_duplicate
    assert "DUPLICATE_EVENT" in res2.rejection_reason


# 5. Feature warm-up
def test_feature_warmup():
    service = LiveMarketDataIngestionService()
    base_ts = datetime(2024, 10, 1, 10, 0, 0, tzinfo=timezone.utc)

    # Ingest 10 bars
    for i in range(10):
        t = base_ts + timedelta(days=i)
        service.ingest_and_validate(
            symbol="HDFCBANK", price=1600.0 + i, open_p=1590.0, high_p=1610.0 + i, low_p=1580.0, close_p=1600.0 + i,
            volume=10000, quote_timestamp=t, current_clock_time=t
        )

    # Requires 20 bars
    assert not service.has_sufficient_warmup("HDFCBANK", required_bars=20)
    assert service.has_sufficient_warmup("HDFCBANK", required_bars=10)


# 6. PIT timestamp safety
def test_pit_timestamp_safety():
    service = LiveMarketDataIngestionService()
    now_ts = datetime(2024, 10, 15, 10, 0, 0, tzinfo=timezone.utc)
    future_ts = now_ts + timedelta(minutes=5)

    res = service.ingest_and_validate(
        symbol="ICICIBANK", price=1250.0, open_p=1240.0, high_p=1260.0, low_p=1230.0, close_p=1250.0,
        volume=15000, quote_timestamp=future_ts, current_clock_time=now_ts
    )
    assert not res.is_valid
    assert "FUTURE_TIMESTAMP" in res.rejection_reason


# 7. Frozen model loading & SHA-256 integrity
def test_frozen_model_loading_and_hash():
    artifact = FrozenModelArtifact()
    manager = FrozenRidgeModelManager(artifact=artifact)

    assert manager.is_frozen
    assert manager.verify_integrity()
    assert len(artifact.model_sha256_hash) == 64

    # Attempts to mutate artifact should cause integrity check to fail
    artifact.weights[0] = 999.0
    assert not manager.verify_integrity()

    # Attempts to fit model must raise RuntimeError
    with pytest.raises(RuntimeError) as exc_info:
        manager.fit([1, 2, 3], [1, 2, 3])
    assert "Retraining or parameter tuning is strictly prohibited" in str(exc_info.value)


# 8. Model inference failure fail-closed
def test_model_inference_fail_closed():
    manager = FrozenRidgeModelManager()

    # Missing required feature
    incomplete_features = {"ret_1d": 0.01, "ret_5d": 0.02}
    assert manager.predict_score("RELIANCE", incomplete_features) is None

    # NaN in features
    nan_features = {
        "ret_1d": 0.01, "ret_5d": 0.02, "ret_20d": float("nan"),
        "volatility_20d": 0.02, "rsi_14": 55.0, "macd_diff": 0.001,
        "atr_14_pct": 0.015, "volume_ratio_20d": 1.1
    }
    assert manager.predict_score("RELIANCE", nan_features) is None


# 9. Signal generation
def test_signal_generation():
    manager = FrozenRidgeModelManager()
    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.08,
        "volatility_20d": 0.015, "rsi_14": 60.0, "macd_diff": 0.005,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.5
    }
    score = manager.predict_score("RELIANCE", features)
    assert score is not None
    assert score > 0.0  # Positive expected return


# 10. Risk rejection
def test_risk_rejection():
    replay_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)  # 10:00 AM IST
    orch = PaperTradingSessionOrchestrator(
        session_name="TEST_RISK",
        initial_cash=100_000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    orch.clock.advance_to(replay_time)
    orch.start_session()

    # Try BUY with massive price exceeding available cash
    features = {
        "ret_1d": 0.05, "ret_5d": 0.10, "ret_20d": 0.15,
        "volatility_20d": 0.01, "rsi_14": 65.0, "macd_diff": 0.01,
        "atr_14_pct": 0.01, "volume_ratio_20d": 2.0
    }
    # Open initial position to exhaust cash
    orch.portfolio_mgr.portfolio.available_cash = 50.0  # tiny cash left

    res = orch.process_live_market_tick(
        symbol="MRF",
        price=130000.0,
        open_p=129000.0,
        high_p=131000.0,
        low_p=128000.0,
        close_p=130000.0,
        volume=500,
        quote_timestamp=replay_time,
        features=features
    )
    assert res["status"] == "RISK_REJECTED"
    assert "REJECTED_CASH" in res["reason"]


# 11. Paper order creation & execution
def test_paper_order_creation_and_fill():
    replay_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)  # 10:00 AM IST
    orch = PaperTradingSessionOrchestrator(
        session_name="TEST_ORDER",
        initial_cash=1_000_000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    orch.clock.advance_to(replay_time)
    orch.start_session()

    features = {
        "ret_1d": 0.03, "ret_5d": 0.05, "ret_20d": 0.08,
        "volatility_20d": 0.015, "rsi_14": 58.0, "macd_diff": 0.005,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }

    res = orch.process_live_market_tick(
        symbol="INFY",
        price=1900.0,
        open_p=1890.0,
        high_p=1910.0,
        low_p=1880.0,
        close_p=1900.0,
        volume=100000,
        quote_timestamp=replay_time,
        features=features
    )
    assert res["status"] == "ORDER_EXECUTED"
    assert res["order"].status == OrderStatus.FILLED
    assert res["fill"] is not None
    assert "INFY" in orch.portfolio_mgr.positions
    assert orch.portfolio_mgr.positions["INFY"].quantity > 0


# 12. Paper/live separation
def test_paper_live_separation():
    orch = PaperTradingSessionOrchestrator()
    assert orch.live_trading_enabled is False
    assert orch.automated_live_trading_enabled is False
    assert orch.paper_trading is True
    assert orch.session.execution_mode == ExecutionMode.PAPER_TRADING

    # Start gate must block if live trading is ever true
    orch.live_trading_enabled = True
    gate = orch.evaluate_start_gate()
    assert not gate.is_ready_to_start
    assert any("Safety guard invariant violated" in r for r in gate.blocked_reasons)


# 13. Order idempotency
def test_order_idempotency():
    replay_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)  # 10:00 AM IST
    orch = PaperTradingSessionOrchestrator(
        session_name="TEST_IDEMPOTENCY",
        initial_cash=1_000_000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    orch.clock.advance_to(replay_time)
    orch.start_session()

    features = {
        "ret_1d": 0.03, "ret_5d": 0.05, "ret_20d": 0.08,
        "volatility_20d": 0.015, "rsi_14": 58.0, "macd_diff": 0.005,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }

    # First execution succeeds
    res1 = orch.process_live_market_tick(
        symbol="TCS", price=3800.0, open_p=3780.0, high_p=3820.0, low_p=3770.0, close_p=3800.0,
        volume=50000, quote_timestamp=replay_time, features=features
    )
    assert res1["status"] == "ORDER_EXECUTED"

    # Immediate second tick for same symbol on same date must be blocked by idempotency
    res2 = orch.process_live_market_tick(
        symbol="TCS", price=3805.0, open_p=3780.0, high_p=3820.0, low_p=3770.0, close_p=3805.0,
        volume=50000, quote_timestamp=replay_time + timedelta(seconds=30), features=features
    )
    assert res2["status"] in ("DUPLICATE_ORDER_BLOCKED", "RISK_REJECTED")


# 14. Market hours & NSE holiday enforcement
def test_market_hours_and_holiday_enforcement():
    calendar = IndianTradingCalendar()
    clock = PaperTradingClock(clock_type=ClockType.REPLAY_CLOCK, calendar=calendar)

    # 1. Weekend (Saturday)
    sat_dt = datetime(2024, 10, 19, 5, 0, 0, tzinfo=timezone.utc)  # Saturday 10:30 AM IST
    assert not clock.is_market_open(sat_dt)

    # 2. NSE Holiday (Gandhi Jayanti: 2024-10-02)
    holiday_dt = datetime(2024, 10, 2, 5, 0, 0, tzinfo=timezone.utc)
    assert not clock.is_market_open(holiday_dt)

    # 3. Regular Trading Session (Tuesday 2024-10-15 10:30 AM IST = 05:00 UTC)
    open_dt = datetime(2024, 10, 15, 5, 0, 0, tzinfo=timezone.utc)
    assert clock.is_market_open(open_dt)

    # 4. Outside hours on valid day (08:30 AM IST = 03:00 UTC)
    pre_market_dt = datetime(2024, 10, 15, 3, 0, 0, tzinfo=timezone.utc)
    assert not clock.is_market_open(pre_market_dt)


# 15. Emergency stop
def test_emergency_stop_activation_and_recovery():
    replay_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    orch = PaperTradingSessionOrchestrator(
        session_name="TEST_EMERGENCY",
        initial_cash=1_000_000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    orch.clock.advance_to(replay_time)
    orch.start_session()

    orch.trigger_emergency_stop("TEST_SAFETY_DRILL")
    assert orch.emergency_stop_active
    assert orch.session.status == PaperTradingStatus.PAUSED

    # Attempting to trade while stopped
    features = {
        "ret_1d": 0.03, "ret_5d": 0.05, "ret_20d": 0.08,
        "volatility_20d": 0.015, "rsi_14": 58.0, "macd_diff": 0.005,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    res = orch.process_live_market_tick(
        symbol="SBIN", price=800.0, open_p=795.0, high_p=805.0, low_p=790.0, close_p=800.0,
        volume=20000, quote_timestamp=replay_time, features=features
    )
    assert res["status"] == "BLOCKED_EMERGENCY_STOP"

    # Reset emergency stop
    orch.reset_emergency_stop()
    assert not orch.emergency_stop_active
    assert orch.session.status == PaperTradingStatus.RUNNING


# 16. Daily loss protection
def test_daily_loss_protection():
    orch = PaperTradingSessionOrchestrator(
        session_name="TEST_DAILY_LOSS",
        initial_cash=1_000_000.0,
        max_daily_loss_pct=0.03
    )
    orch.day_start_portfolio_value = 1_000_000.0

    # Simulate portfolio dropping by 4% (₹960,000)
    orch.portfolio_mgr.portfolio.total_portfolio_value = 960_000.0
    breached = orch.check_daily_loss_limit()
    assert breached is True
    assert orch.emergency_stop_active is True
    assert "DAILY_LOSS_LIMIT_BREACHED" in orch.emergency_stop_reason


# 17. Provider disconnect & 18. Safe reconnection
def test_provider_disconnect_and_reconnect():
    orch = PaperTradingSessionOrchestrator()
    orch.start_session()

    orch.handle_provider_disconnect(reason="NETWORK_TIMEOUT")
    assert not orch.provider_connected
    assert orch.session.status == PaperTradingStatus.DISCONNECTED

    orch.handle_provider_reconnect()
    assert orch.provider_connected
    assert orch.session.status == PaperTradingStatus.RUNNING


# 19. Paper portfolio reconciliation
def test_portfolio_reconciliation():
    orch = PaperTradingSessionOrchestrator(initial_cash=500_000.0)
    ok, err = orch.reconcile_portfolio()
    assert ok is True
    assert err is None

    # Introduce intentional discrepancy
    orch.portfolio_mgr.portfolio.total_portfolio_value = 999_999.0
    ok2, err2 = orch.reconcile_portfolio()
    assert ok2 is False
    assert "PORTFOLIO_DISCREPANCY" in err2


# 20. Session start gate
def test_session_start_gate_all_subsystems():
    orch = PaperTradingSessionOrchestrator()
    gate = orch.evaluate_start_gate()
    assert gate.is_ready_to_start is True
    assert gate.data_provider_ready is True
    assert gate.model_ready is True
    assert gate.paper_execution_ready is True
    assert gate.safety_guards_ready is True


# 21. Secrets not logged
def test_secrets_not_logged():
    orch = PaperTradingSessionOrchestrator()
    orch.log_journal("DATA_RECEIVED", symbol="RELIANCE", details={"endpoint": "https://query1.finance.yahoo.com/v8/finance/chart", "status": 200})

    for entry in orch.journal:
        entry_str = str(entry.details).lower()
        assert "password" not in entry_str
        assert "api_key" not in entry_str
        assert "secret" not in entry_str
        assert "access_token" not in entry_str


# 22. Replay data cannot be labelled real-time
def test_replay_data_not_labelled_realtime():
    health_monitor = LiveDataHealthMonitor()
    now_ts = datetime(2024, 10, 15, 10, 0, 0, tzinfo=timezone.utc)

    # Health outside market session must never be REAL_TIME
    health = health_monitor.evaluate_health(
        last_update_ts=now_ts,
        last_market_ts=now_ts,
        current_clock_ts=now_ts,
        is_market_session_active=False
    )
    assert health.freshness_status != DataFreshnessStatus.REAL_TIME
    assert health.freshness_status == DataFreshnessStatus.DELAYED


# 23. End-to-end fixture integration test
def test_end_to_end_fixture_integration_test():
    """
    [FIXTURE_INTEGRATION_TEST]
    Runs a deterministic multi-tick end-to-end simulation:
    Data Ingestion -> Validation -> Feature Warmup -> Frozen Ridge Inference ->
    Signal Engine -> Risk Engine -> Paper Order -> Simulated Fill -> Portfolio -> Reconciliation.
    """
    replay_start = datetime(2024, 10, 15, 4, 0, 0, tzinfo=timezone.utc)  # 09:30 AM IST
    orch = PaperTradingSessionOrchestrator(
        session_name="FIXTURE_INTEGRATION_TEST_SESSION",
        initial_cash=1_000_000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    orch.clock.advance_to(replay_start)

    success, msg = orch.start_session()
    assert success is True

    # 1. Warm-up historical bars
    for i in range(25):
        t = replay_start - timedelta(days=30 - i)
        orch.ingestion_service.ingest_and_validate(
            symbol="RELIANCE",
            price=2900.0 + i,
            open_p=2890.0,
            high_p=2910.0 + i,
            low_p=2880.0,
            close_p=2900.0 + i,
            volume=50000,
            quote_timestamp=t,
            current_clock_time=t
        )

    # 2. Live tick with predictive features
    features = {
        "ret_1d": 0.025,
        "ret_5d": 0.055,
        "ret_20d": 0.085,
        "volatility_20d": 0.014,
        "rsi_14": 62.0,
        "macd_diff": 0.008,
        "atr_14_pct": 0.011,
        "volume_ratio_20d": 1.4
    }

    res = orch.process_live_market_tick(
        symbol="RELIANCE",
        price=2950.0,
        open_p=2940.0,
        high_p=2965.0,
        low_p=2935.0,
        close_p=2950.0,
        volume=120000,
        quote_timestamp=replay_start,
        features=features
    )

    assert res["status"] == "ORDER_EXECUTED"
    assert res["order"].status == OrderStatus.FILLED
    assert res["fill"].fill_price > 2950.0  # Includes slippage
    assert res["fill"].total_fees > 0.0     # Includes Indian brokerage + STT + GST

    summary = orch.get_session_summary()
    assert summary["open_positions_count"] == 1
    assert summary["total_fills"] == 1
    assert summary["live_trading_enabled"] is False
    assert summary["real_money_at_risk"] == 0.0

    # Verify reconciliation passed cleanly
    reconciled, rec_err = orch.reconcile_portfolio()
    assert reconciled is True
    assert rec_err is None
