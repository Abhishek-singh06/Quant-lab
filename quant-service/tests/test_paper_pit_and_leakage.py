"""
Point-in-Time, Stale-Data Blocking, and Future-Data Protection Tests for Paper Trading.
"""

from datetime import datetime, timezone, timedelta
import pytest
from uuid import uuid4

from app.paper.schemas import (
    ClockType,
    ConnectionStatus,
    DataFreshnessStatus,
    OrderStatus,
    PaperOrder,
    OrderSide
)
from app.paper.engine import PaperTradingEngine
from app.paper.health import LiveDataHealthMonitor
from app.paper.execution import PaperExecutionSimulator


def test_stale_market_data_blocks_trading():
    monitor = LiveDataHealthMonitor(max_allowed_age_seconds=120)

    now_ts = datetime(2024, 1, 15, 11, 0, tzinfo=timezone.utc)
    # Last update was 300 seconds ago (5 mins old) during active market session
    old_update = now_ts - timedelta(seconds=300)

    health = monitor.evaluate_health(
        last_update_ts=old_update,
        last_market_ts=old_update,
        current_clock_ts=now_ts,
        is_market_session_active=True,
        error_count=0
    )

    assert health.connection_status == ConnectionStatus.STALE
    assert health.freshness_status == DataFreshnessStatus.STALE
    assert monitor.is_safe_for_trading(health) is False


def test_future_data_mutation_invariance():
    """
    Verifies that a paper trading decision generated at T is immutable
    and cannot be altered by future ticks, news, or model outcomes arriving at T+N.
    """
    engine = PaperTradingEngine(
        session_name="TEST_PIT_IMMUTABILITY",
        horizon="SHORT_TERM",
        initial_virtual_capital=1000000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    engine.start_session()

    t0 = datetime(2024, 1, 15, 10, 0, tzinfo=timezone.utc)
    dec0 = engine.process_tick_or_bar(
        symbol="RELIANCE",
        current_price=2500.0,
        bar_timestamp=t0,
        signal_data={"signal": "BUY", "signal_score": 75.0, "confidence": 0.80, "expected_return": 0.02},
        risk_data={"risk_decision": "APPROVED", "recommended_quantity": 50}
    )

    assert dec0 is not None
    orig_signal_score = dec0.signal_score
    orig_entry_price = dec0.entry_price
    orig_status = dec0.status

    # Future tick arrives at T + 3 hours with drastic price drop
    t_future = datetime(2024, 1, 15, 13, 0, tzinfo=timezone.utc)
    engine.process_tick_or_bar(
        symbol="RELIANCE",
        current_price=2300.0,
        bar_timestamp=t_future
    )

    # Past decision at t0 must remain strictly unchanged
    saved_dec = engine.decisions[0]
    assert saved_dec.signal_score == orig_signal_score
    assert saved_dec.entry_price == orig_entry_price
    assert saved_dec.status == orig_status
    assert saved_dec.timestamp == t0


def test_execution_simulator_rejects_timing_violation():
    sim = PaperExecutionSimulator()

    submitted_ts = datetime(2024, 1, 15, 10, 15, tzinfo=timezone.utc)
    order = PaperOrder(
        portfolio_id=uuid4(),
        symbol="TCS",
        side=OrderSide.BUY,
        quantity=50,
        requested_price=3500.0,
        signal_timestamp=submitted_ts,
        order_submitted_timestamp=submitted_ts
    )

    # Execution timestamp before submission must be rejected
    past_exec_ts = datetime(2024, 1, 15, 10, 10, tzinfo=timezone.utc)
    filled_order, fill, _ = sim.execute_paper_order(
        order=order,
        market_price=3500.0,
        execution_timestamp=past_exec_ts
    )

    assert filled_order.status == OrderStatus.REJECTED
    assert "TIMING_VIOLATION" in filled_order.rejection_reason
    assert fill is None
