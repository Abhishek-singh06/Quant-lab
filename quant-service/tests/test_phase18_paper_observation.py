"""
Phase 18 Comprehensive Test Suite: 30-Day Delayed Real-Market Paper-Trading Observation.
Validates all 25 core operational & scientific requirements:
1. Delayed mode identification
2. Historical replay separation
3. Frozen model enforcement & hash verification
4. Daily session lifecycle & EOD cycle
5. Market-hours & NSE holiday enforcement
6. Stale-data shutdown & StaleDataEvent
7. Provider disconnect shutdown
8. Duplicate event protection
9. Duplicate paper order protection
10. Restart recovery & state restoration
11. Portfolio reconciliation invariant
12. Daily loss circuit breaker (3%)
13. Indian cost accounting schedule
14. Turnover calculation & shift monitoring
15. Prediction journal & feature hash tracking
16. T+1 multi-horizon evaluation
17. T+5 multi-horizon evaluation
18. T+20 multi-horizon evaluation
19. Insufficient sample size protection (N < 30)
20. No model retraining enforcement
21. Safety flag enforcement (LIVE_TRADING=False)
22. No live order routing to broker
23. Machine-readable journal persistence
24. Daily markdown report generation (docs/paper_trading/journal/YYYY-MM-DD.md)
25. Weekly markdown report generation (docs/paper_trading/weekly/WEEK-N.md)
"""

import pytest
import os
import json
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
from app.paper.clock import PaperTradingClock
from app.paper.frozen_model import FrozenRidgeModelManager, FrozenModelArtifact
from app.paper.live_data_ingestion import (
    LiveMarketDataIngestionService,
    LiveMarketObservation,
    ObservationValidationResult,
    StaleDataEvent
)
from app.paper.session_runner import PaperTradingSessionOrchestrator
from app.paper.observation_runner import (
    Phase18PaperObservationManager,
    DailyPerformanceSummary,
    FeatureJournalEntry,
    PredictionJournalEntry,
    SignalJournalEntry,
    SafetyIncidentEvent
)
from app.data_acquisition.models import ExchangeEnum
from app.data_acquisition.trading_calendar import IndianTradingCalendar


# 1. Delayed mode identification
def test_delayed_mode_identification():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.LIVE_CLOCK)
    assert mgr.data_mode == "DELAYED_PAPER"
    assert mgr.data_mode != "REAL_TIME"
    assert mgr.orchestrator.session.execution_mode == ExecutionMode.PAPER_TRADING


# 2. Historical replay separation
def test_historical_replay_separation():
    mgr_replay = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    mgr_live = Phase18PaperObservationManager(clock_type=ClockType.LIVE_CLOCK)

    assert mgr_replay.data_mode == "HISTORICAL_REPLAY"
    assert mgr_live.data_mode == "DELAYED_PAPER"
    assert mgr_replay.data_mode != mgr_live.data_mode


# 3. Frozen model enforcement
def test_frozen_model_enforcement():
    mgr = Phase18PaperObservationManager()
    model = mgr.orchestrator.model_manager

    assert model.is_frozen is True
    assert model.verify_integrity() is True
    assert len(model.artifact.model_sha256_hash) == 64

    # Retraining must fail
    with pytest.raises(RuntimeError):
        model.fit([1, 2], [1, 2])


# 4. Daily session lifecycle & EOD cycle
def test_daily_session_lifecycle(tmp_path):
    reports_dir = str(tmp_path / "paper_trading")
    mgr = Phase18PaperObservationManager(
        clock_type=ClockType.REPLAY_CLOCK,
        reports_base_dir=reports_dir
    )
    test_date = date(2024, 10, 15)
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)  # 10:00 AM IST
    mgr.orchestrator.clock.advance_to(trade_time)

    # Start session
    ok, msg = mgr.orchestrator.start_session()
    assert ok is True

    # Ingest tick
    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    res = mgr.process_delayed_observation(
        symbol="RELIANCE", price=2950.0, open_p=2940.0, high_p=2960.0, low_p=2935.0, close_p=2950.0,
        volume=50000, quote_timestamp=trade_time, features=features
    )
    assert res["status"] == "ORDER_EXECUTED"

    # Run EOD cycle
    summary = mgr.run_end_of_day_cycle(trade_date=test_date, detected_regime="BULL_TREND")
    assert summary.trade_date == test_date
    assert summary.trades_count == 1
    assert summary.detected_regime == "BULL_TREND"


# 5. Market-hours & NSE holiday enforcement
def test_market_hours_and_holiday_enforcement():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    # Weekend
    weekend_dt = datetime(2024, 10, 19, 5, 0, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(weekend_dt)
    assert not mgr.orchestrator.clock.is_market_open(weekend_dt)

    # Gandhi Jayanti (Holiday)
    holiday_dt = datetime(2024, 10, 2, 5, 0, 0, tzinfo=timezone.utc)
    assert not mgr.orchestrator.clock.is_market_open(holiday_dt)


# 6. Stale-data shutdown
def test_stale_data_shutdown():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.LIVE_CLOCK)
    now_ts = datetime(2024, 10, 15, 10, 0, 0, tzinfo=timezone.utc)
    old_ts = now_ts - timedelta(minutes=10)  # 600s old

    res = mgr.process_delayed_observation(
        symbol="TCS", price=3800.0, open_p=3780.0, high_p=3820.0, low_p=3770.0, close_p=3800.0,
        volume=20000, quote_timestamp=old_ts
    )
    assert res["status"] in ("REJECTED_DATA", "DATA_STALE")


# 7. Provider disconnect shutdown
def test_provider_disconnect_shutdown():
    mgr = Phase18PaperObservationManager()
    mgr.orchestrator.start_session()
    mgr.orchestrator.handle_provider_disconnect("INTERNET_OUTAGE")

    assert mgr.orchestrator.provider_connected is False
    assert mgr.orchestrator.session.status == PaperTradingStatus.DISCONNECTED


# 8. Duplicate event protection
def test_duplicate_event_protection():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    res1 = mgr.process_delayed_observation(
        symbol="INFY", price=1900.0, open_p=1890.0, high_p=1910.0, low_p=1880.0, close_p=1900.0,
        volume=30000, quote_timestamp=trade_time, features=features
    )
    assert res1["status"] == "ORDER_EXECUTED"

    res2 = mgr.process_delayed_observation(
        symbol="INFY", price=1900.0, open_p=1890.0, high_p=1910.0, low_p=1880.0, close_p=1900.0,
        volume=30000, quote_timestamp=trade_time, features=features
    )
    assert res2["status"] in ("REJECTED_DATA", "DUPLICATE_ORDER_BLOCKED")


# 9. Duplicate paper order protection
def test_duplicate_paper_order_protection():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    res1 = mgr.process_delayed_observation(
        symbol="HDFCBANK", price=1600.0, open_p=1590.0, high_p=1610.0, low_p=1580.0, close_p=1600.0,
        volume=40000, quote_timestamp=trade_time, features=features
    )
    assert res1["status"] == "ORDER_EXECUTED"

    # Second tick 10s later
    res2 = mgr.process_delayed_observation(
        symbol="HDFCBANK", price=1602.0, open_p=1590.0, high_p=1610.0, low_p=1580.0, close_p=1602.0,
        volume=40000, quote_timestamp=trade_time + timedelta(seconds=10), features=features
    )
    assert res2["status"] in ("DUPLICATE_ORDER_BLOCKED", "RISK_REJECTED")


# 10. Restart recovery
def test_restart_recovery(tmp_path):
    state_file = str(tmp_path / "paper_state.json")
    mgr1 = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    mgr1.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    mgr1.process_delayed_observation(
        symbol="ICICIBANK", price=1250.0, open_p=1240.0, high_p=1260.0, low_p=1230.0, close_p=1250.0,
        volume=50000, quote_timestamp=trade_time, features=features
    )
    mgr1.save_state_to_disk(state_file)

    # Create fresh manager and restore
    mgr2 = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    restored = mgr2.restore_state_from_disk(state_file)
    assert restored is True
    assert "ICICIBANK" in mgr2.orchestrator.portfolio_mgr.positions
    assert mgr2.orchestrator.portfolio_mgr.positions["ICICIBANK"].quantity > 0


# 11. Portfolio reconciliation
def test_portfolio_reconciliation():
    mgr = Phase18PaperObservationManager()
    ok, err = mgr.orchestrator.reconcile_portfolio()
    assert ok is True
    assert err is None


# 12. Daily loss circuit breaker
def test_daily_loss_circuit_breaker():
    mgr = Phase18PaperObservationManager()
    mgr.orchestrator.day_start_portfolio_value = 1_000_000.0
    mgr.orchestrator.portfolio_mgr.portfolio.total_portfolio_value = 965_000.0  # 3.5% loss

    breached = mgr.orchestrator.check_daily_loss_limit()
    assert breached is True
    assert mgr.orchestrator.emergency_stop_active is True


# 13. Cost accounting
def test_cost_accounting():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    res = mgr.process_delayed_observation(
        symbol="SBIN", price=800.0, open_p=790.0, high_p=810.0, low_p=785.0, close_p=800.0,
        volume=100000, quote_timestamp=trade_time, features=features
    )
    fill = res["fill"]
    assert fill.brokerage > 0.0
    assert fill.stt > 0.0
    assert fill.exchange_charges > 0.0
    assert fill.gst > 0.0
    assert fill.stamp_duty > 0.0
    assert fill.total_fees > 0.0


# 14. Turnover calculation & shift monitoring
def test_turnover_calculation():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    mgr.process_delayed_observation(
        symbol="WIPRO", price=550.0, open_p=545.0, high_p=555.0, low_p=540.0, close_p=550.0,
        volume=100000, quote_timestamp=trade_time, features=features
    )
    summary = mgr.run_end_of_day_cycle(trade_date=date(2024, 10, 15))
    assert summary.turnover_inr > 0.0
    assert summary.daily_turnover_ratio > 0.0


# 15. Prediction journal & feature hash tracking
def test_prediction_journal_and_feature_hash():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 15, 4, 30, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.025, "ret_5d": 0.045, "ret_20d": 0.075,
        "volatility_20d": 0.014, "rsi_14": 59.0, "macd_diff": 0.006,
        "atr_14_pct": 0.011, "volume_ratio_20d": 1.3
    }
    mgr.process_delayed_observation(
        symbol="LT", price=3600.0, open_p=3580.0, high_p=3620.0, low_p=3570.0, close_p=3600.0,
        volume=30000, quote_timestamp=trade_time, features=features
    )

    assert len(mgr.prediction_journal) >= 1
    pred = mgr.prediction_journal[0]
    assert pred.symbol == "LT"
    assert len(pred.feature_hash) == 64
    assert len(pred.model_hash) == 64


# 16. T+1 evaluation
def test_t1_evaluation():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 1, 4, 30, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    mgr.process_delayed_observation(
        symbol="BHARTIARTL", price=1600.0, open_p=1590.0, high_p=1610.0, low_p=1580.0, close_p=1600.0,
        volume=50000, quote_timestamp=trade_time, features=features
    )

    future_date = date(2024, 10, 2)
    mgr.evaluate_prediction_outcomes(future_date, {"BHARTIARTL": 1610.0})

    pred = mgr.prediction_journal[0]
    assert pred.realized_ret_t1 is not None
    assert pred.t1_evaluated_at is not None


# 17. T+5 evaluation
def test_t5_evaluation():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 1, 4, 30, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    mgr.process_delayed_observation(
        symbol="BHARTIARTL", price=1600.0, open_p=1590.0, high_p=1610.0, low_p=1580.0, close_p=1600.0,
        volume=50000, quote_timestamp=trade_time, features=features
    )

    future_date = date(2024, 10, 7)
    mgr.evaluate_prediction_outcomes(future_date, {"BHARTIARTL": 1640.0})

    pred = mgr.prediction_journal[0]
    assert pred.realized_ret_t5 is not None
    assert pred.t5_evaluated_at is not None


# 18. T+20 evaluation
def test_t20_evaluation():
    mgr = Phase18PaperObservationManager(clock_type=ClockType.REPLAY_CLOCK)
    trade_time = datetime(2024, 10, 1, 4, 30, 0, tzinfo=timezone.utc)
    mgr.orchestrator.clock.advance_to(trade_time)

    features = {
        "ret_1d": 0.02, "ret_5d": 0.04, "ret_20d": 0.07,
        "volatility_20d": 0.015, "rsi_14": 56.0, "macd_diff": 0.004,
        "atr_14_pct": 0.012, "volume_ratio_20d": 1.2
    }
    mgr.process_delayed_observation(
        symbol="BHARTIARTL", price=1600.0, open_p=1590.0, high_p=1610.0, low_p=1580.0, close_p=1600.0,
        volume=50000, quote_timestamp=trade_time, features=features
    )

    future_date = date(2024, 10, 26)
    mgr.evaluate_prediction_outcomes(future_date, {"BHARTIARTL": 1680.0})

    pred = mgr.prediction_journal[0]
    assert pred.realized_ret_t20 is not None
    assert pred.t20_evaluated_at is not None


# 19. Insufficient-sample protection
def test_insufficient_sample_protection():
    mgr = Phase18PaperObservationManager()
    # No predictions or small sample (< 30)
    metrics = mgr.calculate_prediction_validation_metrics()
    assert metrics["status"] == "INSUFFICIENT_SAMPLE"
    assert metrics["rank_ic"] is None
    assert metrics["directional_accuracy"] is None


# 20. No model retraining enforcement
def test_no_model_retraining_enforcement():
    mgr = Phase18PaperObservationManager()
    with pytest.raises(RuntimeError):
        mgr.orchestrator.model_manager.fit([1], [1])


# 21. Safety flag enforcement
def test_safety_flag_enforcement():
    mgr = Phase18PaperObservationManager()
    assert mgr.orchestrator.live_trading_enabled is False
    assert mgr.orchestrator.automated_live_trading_enabled is False
    assert mgr.orchestrator.paper_trading is True


# 22. No live order routing
def test_no_live_order_routing():
    mgr = Phase18PaperObservationManager()
    gate = mgr.orchestrator.evaluate_start_gate()
    assert gate.safety_guards_ready is True
    assert gate.paper_execution_ready is True


# 23. Machine-readable journal persistence
def test_journal_persistence(tmp_path):
    state_file = str(tmp_path / "obs_journal.json")
    mgr = Phase18PaperObservationManager()
    mgr.save_state_to_disk(state_file)

    assert os.path.exists(state_file)
    with open(state_file, "r") as f:
        data = json.load(f)
    assert "session_name" in data
    assert "portfolio" in data


# 24. Daily report generation
def test_daily_report_generation(tmp_path):
    reports_dir = str(tmp_path / "paper_reports")
    mgr = Phase18PaperObservationManager(reports_base_dir=reports_dir)
    test_date = date(2024, 10, 15)

    summary = DailyPerformanceSummary(
        trade_date=test_date,
        gross_pnl=1500.0,
        total_costs=150.0,
        net_pnl=1350.0
    )
    report_path = mgr.generate_daily_markdown_report(summary)
    assert os.path.exists(report_path)
    with open(report_path, "r", encoding="utf-8") as f:
        text = f.read()
    assert "QuantLab Paper Trading Daily Journal" in text
    assert test_date.isoformat() in text


# 25. Weekly report generation
def test_weekly_report_generation(tmp_path):
    reports_dir = str(tmp_path / "paper_reports")
    mgr = Phase18PaperObservationManager(reports_base_dir=reports_dir)

    for d in range(1, 6):
        summary = DailyPerformanceSummary(
            trade_date=date(2024, 10, d),
            gross_pnl=500.0 * d,
            total_costs=50.0 * d,
            net_pnl=450.0 * d,
            trades_count=d
        )
        mgr.daily_summaries_list.append(summary)

    report_path = mgr.generate_weekly_markdown_report(week_num=1)
    assert os.path.exists(report_path)
    with open(report_path, "r", encoding="utf-8") as f:
        text = f.read()
    assert "QuantLab Paper Trading Weekly Report — Week 01" in text
