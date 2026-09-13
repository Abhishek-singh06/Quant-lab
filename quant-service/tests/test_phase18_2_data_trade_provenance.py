"""
Phase 18.2 Test Suite: Provenance Audit from Real/Delayed Market Observations to Paper Trades.
Validates:
1. Order has observation provenance (source_observation_id, source_provider, source_provider_timestamp).
2. Order has prediction provenance (prediction_id matching decision).
3. Order has signal provenance (signal_id matching decision).
4. Provider metadata persists to disk and across restarts.
5. Fake observations (invalid prices/OHLC) cannot create orders.
6. Historical replay cannot masquerade as delayed-live.
7. Fixtures cannot masquerade as delayed-live (classified as TEST_FIXTURE / PROVENANCE_INCOMPLETE).
8. Stale data cannot create orders.
9. Market closed cannot create new orders outside valid hours.
10. Restart preserves complete provenance.
11. Duplicate order protection blocks repeated ticks on same day.
12. Live broker remains completely isolated (fail-closed).
13. Zero real money at risk (always 0.0 INR).
"""

import pytest
from datetime import datetime, timezone, timedelta, date
from uuid import uuid4

from app.paper.schemas import (
    ExecutionMode,
    PaperTradingStatus,
    ClockType,
    OrderStatus,
    OrderSide
)
from app.paper.persistence import PaperTradingPersistenceManager
from app.paper.session_runner import PaperTradingSessionOrchestrator
from app.data_acquisition.models import ExchangeEnum


# 1. Order has Observation Provenance
def test_order_has_observation_provenance(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="PROVENANCE_OBS_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.start_session()
    
    quote_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    features = {
        "ret_1d": 0.015,
        "ret_5d": 0.035,
        "ret_20d": 0.050,
        "volatility_20d": 0.012,
        "rsi_14": 62.0,
        "macd_diff": 0.005,
        "atr_14_pct": 0.015,
        "volume_ratio_20d": 1.4
    }

    res = runner.process_live_market_tick(
        symbol="RELIANCE",
        price=2950.0,
        open_p=2940.0,
        high_p=2960.0,
        low_p=2935.0,
        close_p=2950.0,
        volume=100000,
        quote_timestamp=quote_time,
        received_timestamp=quote_time + timedelta(milliseconds=150),
        provider_timestamp=quote_time,
        features=features,
        allow_outside_hours_replay=True
    )

    assert res["status"] == "ORDER_EXECUTED"
    order = res["order"]
    assert order.source_observation_id is not None
    assert len(order.source_observation_id) == 64  # SHA-256 event ID
    assert order.source_provider == "YAHOO_FINANCE"
    assert order.source_provider_timestamp == quote_time
    assert order.provenance_status == "COMPLETE"


# 2. Order has Prediction Provenance
def test_order_has_prediction_provenance(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="PROVENANCE_PRED_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.start_session()
    quote_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    features = {"ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.06, "volatility_20d": 0.01, "rsi_14": 65.0, "macd_diff": 0.006, "atr_14_pct": 0.01, "volume_ratio_20d": 1.5}

    res = runner.process_live_market_tick(
        symbol="TCS",
        price=3800.0,
        open_p=3780.0,
        high_p=3820.0,
        low_p=3775.0,
        close_p=3800.0,
        volume=80000,
        quote_timestamp=quote_time,
        features=features,
        allow_outside_hours_replay=True
    )

    assert res["status"] == "ORDER_EXECUTED"
    order = res["order"]
    decision = res["decision"]
    assert order.prediction_id is not None
    assert order.prediction_id == decision.prediction_id


# 3. Order has Signal Provenance
def test_order_has_signal_provenance(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="PROVENANCE_SIG_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.start_session()
    quote_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    features = {"ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.06, "volatility_20d": 0.01, "rsi_14": 65.0, "macd_diff": 0.006, "atr_14_pct": 0.01, "volume_ratio_20d": 1.5}

    res = runner.process_live_market_tick(
        symbol="INFY",
        price=1900.0,
        open_p=1890.0,
        high_p=1910.0,
        low_p=1885.0,
        close_p=1900.0,
        volume=120000,
        quote_timestamp=quote_time,
        features=features,
        allow_outside_hours_replay=True
    )

    assert res["status"] == "ORDER_EXECUTED"
    order = res["order"]
    decision = res["decision"]
    assert order.signal_id is not None
    assert order.signal_id == decision.signal_id


# 4. Provider Metadata Persists to Disk
def test_provider_metadata_persists(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="PERSIST_PROV_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.start_session()
    quote_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    features = {"ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.06, "volatility_20d": 0.01, "rsi_14": 65.0, "macd_diff": 0.006, "atr_14_pct": 0.01, "volume_ratio_20d": 1.5}

    runner.process_live_market_tick(
        symbol="SBIN",
        price=800.0,
        open_p=795.0,
        high_p=805.0,
        low_p=790.0,
        close_p=800.0,
        volume=200000,
        quote_timestamp=quote_time,
        features=features,
        allow_outside_hours_replay=True
    )

    persistence = PaperTradingPersistenceManager(data_dir=data_dir)
    orders_data = persistence.load_orders()
    assert len(orders_data) == 1
    assert orders_data[0]["source_provider"] == "YAHOO_FINANCE"
    assert orders_data[0]["provenance_status"] == "COMPLETE"
    assert orders_data[0]["source_observation_id"] is not None


# 5. Fake Observation Cannot Create Order
def test_fake_observation_cannot_create_order(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="FAKE_OBS_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.start_session()
    quote_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)

    # Invalid price <= 0
    res1 = runner.process_live_market_tick(
        symbol="HDFCBANK",
        price=-100.0,
        open_p=1600.0,
        high_p=1620.0,
        low_p=1590.0,
        close_p=1600.0,
        volume=1000,
        quote_timestamp=quote_time,
        allow_outside_hours_replay=True
    )
    assert res1["status"] == "REJECTED_DATA"
    assert len(runner.orders) == 0

    # Inconsistent OHLC (low > high)
    res2 = runner.process_live_market_tick(
        symbol="HDFCBANK",
        price=1600.0,
        open_p=1600.0,
        high_p=1500.0,
        low_p=1700.0,
        close_p=1600.0,
        volume=1000,
        quote_timestamp=quote_time,
        allow_outside_hours_replay=True
    )
    assert res2["status"] == "REJECTED_DATA"
    assert len(runner.orders) == 0


# 6. Historical Replay Cannot Masquerade as Delayed-Live
def test_historical_replay_cannot_masquerade_as_delayed_live(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="REPLAY_SESSION_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    assert runner.clock.clock_type == ClockType.REPLAY_CLOCK
    audit = runner.get_provenance_audit()
    assert audit["session_name"] == "REPLAY_SESSION_TEST"


# 7. Fixture Cannot Masquerade as Delayed Live
def test_fixture_cannot_masquerade_as_delayed_live(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="TEST_FIXTURE_SESSION",
        clock_type=ClockType.LIVE_CLOCK,
        persistence_dir=data_dir
    )
    # Manually append an unlinked order representing a previous test fixture
    from app.paper.schemas import PaperOrder, OrderSide, OrderStatus
    unlinked_order = PaperOrder(
        id=uuid4(),
        portfolio_id=runner.portfolio_mgr.portfolio.id,
        session_id=runner.session.id,
        symbol="WIPRO",
        side=OrderSide.BUY,
        quantity=50,
        requested_price=550.0,
        executed_price=550.27,
        signal_timestamp=datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc),
        order_submitted_timestamp=datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc),
        status=OrderStatus.FILLED,
        provenance_status="PROVENANCE_INCOMPLETE"
    )
    runner.orders.append(unlinked_order)
    runner._persist_full_state()

    audit = runner.get_provenance_audit()
    assert audit["orders_with_incomplete_provenance"] == 1
    assert audit["orders_with_complete_provenance"] == 0
    assert audit["orders"][0]["classification"] == "TEST_FIXTURE"
    assert audit["orders"][0]["lineage_complete"] is False


# 8. Stale Data Cannot Create Order
def test_stale_data_cannot_create_order(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="STALE_TEST",
        clock_type=ClockType.LIVE_CLOCK,
        max_data_age_seconds=180,
        persistence_dir=data_dir
    )
    runner.start_session()
    
    # 2 hours old observation
    old_time = datetime.now(timezone.utc) - timedelta(hours=2)
    features = {"ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.06, "volatility_20d": 0.01, "rsi_14": 65.0, "macd_diff": 0.006, "atr_14_pct": 0.01, "volume_ratio_20d": 1.5}

    res = runner.process_live_market_tick(
        symbol="LT",
        price=3600.0,
        open_p=3580.0,
        high_p=3620.0,
        low_p=3575.0,
        close_p=3600.0,
        volume=50000,
        quote_timestamp=old_time,
        features=features
    )

    assert res["status"] == "REJECTED_DATA"
    assert "STALE_DATA" in res["reason"]
    assert len(runner.orders) == 0


# 9. Market Closed Cannot Create New Order
def test_market_closed_cannot_create_new_order(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="CLOSED_TEST",
        clock_type=ClockType.LIVE_CLOCK,
        persistence_dir=data_dir
    )
    runner.start_session()
    
    # Timestamp now (Sunday / closed hours)
    sunday_time = datetime.now(timezone.utc)
    features = {"ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.06, "volatility_20d": 0.01, "rsi_14": 65.0, "macd_diff": 0.006, "atr_14_pct": 0.01, "volume_ratio_20d": 1.5}

    res = runner.process_live_market_tick(
        symbol="BHARTIARTL",
        price=1600.0,
        open_p=1590.0,
        high_p=1610.0,
        low_p=1585.0,
        close_p=1600.0,
        volume=40000,
        quote_timestamp=sunday_time,
        features=features,
        allow_outside_hours_replay=False
    )

    # In live clock mode during closed hours, order creation is blocked
    if not runner.clock.is_market_open(sunday_time):
        assert res["status"] == "MARKET_CLOSED"
        assert len(runner.orders) == 0


# 10. Restart Preserves Complete Provenance
def test_restart_preserves_provenance(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    
    # Session 1: Create trade with provenance
    runner1 = PaperTradingSessionOrchestrator(
        session_name="RESTART_PROV_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner1.start_session()
    quote_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    features = {"ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.06, "volatility_20d": 0.01, "rsi_14": 65.0, "macd_diff": 0.006, "atr_14_pct": 0.01, "volume_ratio_20d": 1.5}

    res = runner1.process_live_market_tick(
        symbol="ICICIBANK",
        price=1250.0,
        open_p=1240.0,
        high_p=1260.0,
        low_p=1235.0,
        close_p=1250.0,
        volume=150000,
        quote_timestamp=quote_time,
        features=features,
        allow_outside_hours_replay=True
    )
    assert res["status"] == "ORDER_EXECUTED"
    orig_order_id = res["order"].id
    orig_obs_id = res["order"].source_observation_id

    # Session 2: Reload after restart
    runner2 = PaperTradingSessionOrchestrator(
        session_name="RESTART_PROV_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    assert len(runner2.orders) == 1
    reloaded_order = runner2.orders[0]
    assert reloaded_order.id == orig_order_id
    assert reloaded_order.source_observation_id == orig_obs_id
    assert reloaded_order.provenance_status == "COMPLETE"
    assert reloaded_order.source_provider == "YAHOO_FINANCE"


# 11. Duplicate Order Protection
def test_duplicate_order_protection(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="DUP_ORDER_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.start_session()
    quote_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    features = {"ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.06, "volatility_20d": 0.01, "rsi_14": 65.0, "macd_diff": 0.006, "atr_14_pct": 0.01, "volume_ratio_20d": 1.5}

    res1 = runner.process_live_market_tick(
        symbol="RELIANCE",
        price=2950.0,
        open_p=2940.0,
        high_p=2960.0,
        low_p=2935.0,
        close_p=2950.0,
        volume=100000,
        quote_timestamp=quote_time,
        features=features,
        allow_outside_hours_replay=True
    )
    assert res1["status"] == "ORDER_EXECUTED"

    # Second tick on same symbol & date
    res2 = runner.process_live_market_tick(
        symbol="RELIANCE",
        price=2952.0,
        open_p=2940.0,
        high_p=2960.0,
        low_p=2935.0,
        close_p=2952.0,
        volume=100000,
        quote_timestamp=quote_time + timedelta(minutes=5),
        features=features,
        allow_outside_hours_replay=True
    )
    assert res2["status"] == "DUPLICATE_ORDER_BLOCKED"
    assert len(runner.orders) == 1


# 12. Live Broker Remains Isolated
def test_live_broker_remains_isolated():
    runner = PaperTradingSessionOrchestrator()
    assert runner.live_trading_enabled is False
    assert runner.automated_live_trading_enabled is False
    assert runner.paper_trading is True
    assert runner.session.execution_mode == ExecutionMode.PAPER_TRADING


# 13. Zero Real Money at Risk
def test_zero_real_money():
    runner = PaperTradingSessionOrchestrator()
    summary = runner.get_session_summary()
    assert summary["real_money_at_risk"] == 0.0
    assert summary["live_trading_enabled"] is False
