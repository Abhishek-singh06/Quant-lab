"""
Unit and integration tests for QuantLab Paper Trading Engine (Part 17).
Tests session management, order execution, cash reconciliation, stops/targets, and expected vs realized analytics.
"""

from datetime import datetime, timezone, timedelta
import pytest
from uuid import uuid4

from app.paper.schemas import (
    ExecutionMode,
    PaperTradingSession,
    PaperTradingStatus,
    ClockType,
    DataFreshnessStatus,
    ConnectionStatus,
    OrderSide,
    OrderStatus,
    SignalOutcomeStatus
)
from app.paper.engine import PaperTradingEngine
from app.paper.health import LiveDataHealthMonitor
from app.paper.execution import PaperExecutionSimulator
from app.paper.portfolio import PaperPortfolioManager
from app.paper.outcomes import ExpectedVsRealizedEngine
from app.paper.monitoring import ModelMonitoringEngine


def test_paper_trading_startup_safety():
    engine = PaperTradingEngine(
        session_name="TEST_ST_SESSION",
        horizon="SHORT_TERM",
        initial_virtual_capital=1000000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )

    report = engine.start_session()
    assert report.overall_status == "HEALTHY"
    assert engine.session.status == PaperTradingStatus.RUNNING
    assert engine.session.execution_mode == ExecutionMode.PAPER_TRADING


def test_paper_order_execution_and_cash_reconciliation():
    engine = PaperTradingEngine(
        session_name="TEST_EXEC_RECON",
        horizon="SHORT_TERM",
        initial_virtual_capital=1000000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    engine.start_session()

    dt1 = datetime(2024, 1, 15, 10, 0, tzinfo=timezone.utc)
    sig_data = {
        "signal": "BUY",
        "signal_score": 75.0,
        "confidence": 0.80,
        "expected_return": 0.025,
        "direction": "BULLISH",
        "signal_id": uuid4(),
        "model_version": "GB_1D_v1.0",
        "supporting_evidence": [{"feature": "RSI", "score": 70.0}]
    }
    risk_data = {
        "risk_decision": "APPROVED",
        "risk_level": "MODERATE",
        "recommended_quantity": 100,
        "stop_price": 2400.0,
        "target_price": 2700.0,
        "risk_assessment_id": uuid4()
    }

    # Process buy tick: RELIANCE @ 2500.0
    dec = engine.process_tick_or_bar(
        symbol="RELIANCE",
        current_price=2500.0,
        bar_timestamp=dt1,
        current_volume=50000,
        signal_data=sig_data,
        risk_data=risk_data
    )

    assert dec is not None
    assert dec.decision == "BUY"
    assert dec.status == "RECORDED"
    assert len(engine.orders) == 1
    assert len(engine.fills) == 1

    fill = engine.fills[0]
    assert fill.symbol == "RELIANCE"
    assert fill.quantity == 100
    assert fill.fill_price > 2500.0  # Buy slippage applied
    assert fill.total_fees > 0.0     # Indian fees calculated

    # Position check
    pos = engine.portfolio_mgr.positions["RELIANCE"]
    assert pos.quantity == 100
    assert pos.stop_price == 2400.0
    assert pos.target_price == 2700.0

    # Reconciliation check
    recon, msg = engine.portfolio_mgr.verify_reconciliation()
    assert recon, msg
    assert engine.portfolio_mgr.portfolio.total_portfolio_value > 990000.0


def test_automatic_stop_loss_trigger():
    engine = PaperTradingEngine(
        session_name="TEST_STOP_TRIGGER",
        horizon="SHORT_TERM",
        initial_virtual_capital=500000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    engine.start_session()

    dt1 = datetime(2024, 1, 15, 10, 0, tzinfo=timezone.utc)
    engine.process_tick_or_bar(
        symbol="TCS",
        current_price=3500.0,
        bar_timestamp=dt1,
        signal_data={"signal": "BUY", "signal_score": 80.0, "confidence": 0.85, "expected_return": 0.03},
        risk_data={"risk_decision": "APPROVED", "recommended_quantity": 50, "stop_price": 3400.0, "target_price": 3800.0}
    )

    assert "TCS" in engine.portfolio_mgr.positions

    # Price drops to 3380.0 (below stop price 3400.0)
    dt2 = datetime(2024, 1, 16, 11, 0, tzinfo=timezone.utc)
    engine.process_tick_or_bar(
        symbol="TCS",
        current_price=3380.0,
        bar_timestamp=dt2
    )

    # Position should be closed
    assert "TCS" not in engine.portfolio_mgr.positions
    assert len(engine.fills) == 2  # Buy fill + Stop loss Sell fill
    assert engine.portfolio_mgr.portfolio.total_realized_pnl < 0.0


def test_expected_vs_realized_analytics():
    engine = PaperTradingEngine(
        session_name="TEST_OUTCOMES",
        horizon="SHORT_TERM",
        initial_virtual_capital=1000000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    engine.start_session()

    dt1 = datetime(2024, 1, 10, 10, 0, tzinfo=timezone.utc)
    engine.process_tick_or_bar(
        symbol="INFY",
        current_price=1600.0,
        bar_timestamp=dt1,
        signal_data={"signal": "BUY", "signal_score": 70.0, "confidence": 0.75, "expected_return": 0.020},
        risk_data={"risk_decision": "APPROVED", "recommended_quantity": 100}
    )

    # Price moves to 1640.0 (+2.5% return) after horizon
    dt2 = datetime(2024, 1, 15, 15, 30, tzinfo=timezone.utc)
    engine.evaluate_outcomes(current_prices={"INFY": 1640.0}, now_ts=dt2)

    assert len(engine.prediction_outcomes) == 1
    assert len(engine.signal_outcomes) == 1

    pred_out = engine.prediction_outcomes[0]
    assert pytest.approx(pred_out.expected_return, 0.001) == 0.020
    assert pytest.approx(pred_out.realized_return, 0.001) == 0.025
    assert pytest.approx(pred_out.prediction_error, 0.001) == 0.005
    assert pred_out.is_direction_correct is True

    sig_out = engine.signal_outcomes[0]
    assert sig_out.outcome_status == SignalOutcomeStatus.CORRECT_DIRECTION


def test_duplicate_event_deduplication():
    engine = PaperTradingEngine(
        session_name="TEST_DEDUP",
        horizon="SHORT_TERM",
        initial_virtual_capital=500000.0,
        clock_type=ClockType.REPLAY_CLOCK
    )
    engine.start_session()

    dt1 = datetime(2024, 1, 15, 10, 0, tzinfo=timezone.utc)
    # Process same event twice
    dec1 = engine.process_tick_or_bar(
        symbol="SBIN",
        current_price=600.0,
        bar_timestamp=dt1,
        signal_data={"signal": "BUY", "signal_score": 65.0, "confidence": 0.70},
        risk_data={"risk_decision": "APPROVED", "recommended_quantity": 100},
        event_id="SBIN_20240115_1000"
    )

    dec2 = engine.process_tick_or_bar(
        symbol="SBIN",
        current_price=600.0,
        bar_timestamp=dt1,
        signal_data={"signal": "BUY", "signal_score": 65.0, "confidence": 0.70},
        risk_data={"risk_decision": "APPROVED", "recommended_quantity": 100},
        event_id="SBIN_20240115_1000"
    )

    assert dec1 is not None
    assert dec2 is None
    assert len(engine.orders) == 1  # Only executed once
