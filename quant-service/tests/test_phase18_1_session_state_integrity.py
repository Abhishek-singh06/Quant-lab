"""
Phase 18.1 Test Suite: Paper Session Runtime / API State Integrity Audit.
Validates:
1. CLI and API share ONE authoritative persisted session state (identical Session ID).
2. Authoritative session persistence on disk.
3. Duplicate session start idempotency (does not create second session).
4. Observation processing updates persisted state atomically.
5. API get_session_summary reflects observation updates without restart.
6. Process restart recovery restores active session and open positions.
7. Order idempotency blocks duplicate orders.
8. Paper/Live strict separation & fail-closed isolation.
9. No fake data or fabricated ticks.
10. Market closed safety behavior (zero orders outside hours).
"""

import pytest
import os
import json
from datetime import datetime, timezone, timedelta, date
from uuid import uuid4

from app.paper.schemas import (
    ExecutionMode,
    PaperTradingStatus,
    ClockType,
    OrderStatus
)
from app.paper.persistence import PaperTradingPersistenceManager
from app.paper.session_runner import PaperTradingSessionOrchestrator


# 1. CLI / API Same Session State
def test_cli_api_same_session_state(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    
    # Process A: CLI Runner starts session
    runner = PaperTradingSessionOrchestrator(
        session_name="SESSION_CLI",
        clock_type=ClockType.LIVE_CLOCK,
        persistence_dir=data_dir
    )
    ok, msg = runner.start_session()
    assert ok is True
    cli_session_id = runner.session.id

    # Process B: API Server initializes orchestrator
    api_orch = PaperTradingSessionOrchestrator(
        session_name="SESSION_API",
        clock_type=ClockType.LIVE_CLOCK,
        persistence_dir=data_dir
    )
    summary = api_orch.get_session_summary()

    assert str(cli_session_id) == summary["session_id"]
    assert summary["status"] == "RUNNING"


# 2. Persistent Session State on Disk
def test_persistent_session_state(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    persistence = PaperTradingPersistenceManager(data_dir=data_dir)

    runner = PaperTradingSessionOrchestrator(
        session_name="PERSISTENT_TEST",
        persistence_dir=data_dir
    )
    runner.start_session()

    assert persistence.has_active_session() is True
    sess_data = persistence.load_active_session()
    assert sess_data["id"] == str(runner.session.id)
    assert sess_data["status"] == "RUNNING"


# 3. Duplicate Start Protection / Idempotency
def test_duplicate_start_protection(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="IDEMPOTENCY_TEST",
        persistence_dir=data_dir
    )
    ok1, msg1 = runner.start_session()
    first_id = runner.session.id

    # Attempt second start
    ok2, msg2 = runner.start_session()
    assert ok2 is True
    assert runner.session.id == first_id
    assert "already active" in msg2.lower()


# 4. Observation Updates Persisted State & 5. API Reflects Updates
def test_observation_updates_persisted_state_and_api(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)

    # Process A: Runner processes tick
    runner = PaperTradingSessionOrchestrator(
        session_name="TICK_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.clock.advance_to(trade_time)
    runner.start_session()

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    res = runner.process_live_market_tick(
        symbol="RELIANCE", price=2950.0, open_p=2940.0, high_p=2960.0, low_p=2935.0, close_p=2950.0,
        volume=50000, quote_timestamp=trade_time, features=features
    )
    assert res["status"] == "ORDER_EXECUTED"

    # Process B: API queries session summary
    api_orch = PaperTradingSessionOrchestrator(
        session_name="API_READER",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    api_summary = api_orch.get_session_summary()

    assert api_summary["session_id"] == str(runner.session.id)
    assert api_summary["total_fills"] == 1
    assert api_summary["open_positions_count"] == 1
    assert api_summary["invested_value"] > 0


# 6. Restart Recovery
def test_restart_recovery(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)

    # Instance 1
    inst1 = PaperTradingSessionOrchestrator(
        session_name="RESTART_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    inst1.clock.advance_to(trade_time)
    inst1.start_session()

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    inst1.process_live_market_tick(
        symbol="TCS", price=3800.0, open_p=3780.0, high_p=3820.0, low_p=3770.0, close_p=3800.0,
        volume=30000, quote_timestamp=trade_time, features=features
    )
    sess_id = inst1.session.id

    # Simulated Complete Process Restart: Instance 2 loads from disk
    inst2 = PaperTradingSessionOrchestrator(
        session_name="RESTART_TEST_2",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )

    assert inst2.session.id == sess_id
    assert inst2.session.status == PaperTradingStatus.RUNNING
    assert "TCS" in inst2.portfolio_mgr.positions
    assert inst2.portfolio_mgr.positions["TCS"].quantity > 0


# 7. Duplicate Order Protection
def test_duplicate_order_protection(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)

    runner = PaperTradingSessionOrchestrator(
        session_name="DUP_ORDER_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.clock.advance_to(trade_time)
    runner.start_session()

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    res1 = runner.process_live_market_tick(
        symbol="INFY", price=1900.0, open_p=1890.0, high_p=1910.0, low_p=1880.0, close_p=1900.0,
        volume=40000, quote_timestamp=trade_time, features=features
    )
    assert res1["status"] == "ORDER_EXECUTED"

    res2 = runner.process_live_market_tick(
        symbol="INFY", price=1905.0, open_p=1890.0, high_p=1910.0, low_p=1880.0, close_p=1905.0,
        volume=40000, quote_timestamp=trade_time + timedelta(seconds=15), features=features
    )
    assert res2["status"] in ("DUPLICATE_ORDER_BLOCKED", "RISK_REJECTED")


# 8. Paper / Live Separation
def test_paper_live_separation():
    runner = PaperTradingSessionOrchestrator()
    assert runner.live_trading_enabled is False
    assert runner.automated_live_trading_enabled is False
    assert runner.paper_trading is True
    assert runner.session.execution_mode == ExecutionMode.PAPER_TRADING


# 9. No Fake Data
def test_no_fake_data(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    runner = PaperTradingSessionOrchestrator(
        session_name="NO_FAKE_DATA",
        persistence_dir=data_dir
    )
    runner.start_session()
    summary = runner.get_session_summary()

    # Zero ticks fed -> zero decisions, orders, fills, and zero P&L
    assert summary["total_decisions"] == 0
    assert summary["total_orders"] == 0
    assert summary["total_fills"] == 0
    assert summary["pnl_inr"] == 0.0


# 10. Market Closed Safety
def test_market_closed_safety(tmp_path):
    data_dir = str(tmp_path / "paper_data")
    # Saturday evening
    closed_time = datetime(2024, 10, 19, 14, 0, 0, tzinfo=timezone.utc)

    runner = PaperTradingSessionOrchestrator(
        session_name="CLOSED_TEST",
        clock_type=ClockType.REPLAY_CLOCK,
        persistence_dir=data_dir
    )
    runner.clock.advance_to(closed_time)
    runner.start_session()

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    res = runner.process_live_market_tick(
        symbol="SBIN", price=800.0, open_p=790.0, high_p=810.0, low_p=785.0, close_p=800.0,
        volume=20000, quote_timestamp=closed_time, features=features
    )
    assert res["status"] == "MARKET_CLOSED"
    assert len(runner.orders) == 0
