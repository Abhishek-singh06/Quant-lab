"""
Point-in-Time and Lookahead Bias Protection Tests for Backtesting Engine.
Verifies that:
1. Signal at bar T cannot peek at bar T+1 or later.
2. Orders execute on NEXT bar open and cannot execute at same bar close.
3. Adding future market data does not mutate past execution or equity trajectory.
"""

from datetime import date, datetime, timedelta
import numpy as np
import pandas as pd
import pytest

from app.backtesting.schemas import BacktestConfig, BacktestOrder, OrderSide, OrderStatus
from app.backtesting.engine import ProductionBacktestEngine
from app.backtesting.execution.next_bar import NextBarExecutionSimulator


def test_next_bar_simulator_rejects_same_bar_execution():
    config = BacktestConfig(
        name="test_pit_timing",
        symbols=["TCS"],
        start_date=date(2024, 1, 1),
        end_date=date(2024, 1, 31)
    )
    sim = NextBarExecutionSimulator(config)

    sub_ts = datetime(2024, 1, 15, 15, 30)
    order = BacktestOrder(
        run_id=config.id,
        symbol="TCS",
        side=OrderSide.BUY,
        quantity=50,
        requested_price=3500.0,
        signal_timestamp=sub_ts,
        order_submitted_timestamp=sub_ts
    )

    # Attempting to execute at same timestamp or earlier must be REJECTED
    same_bar_ts = datetime(2024, 1, 15, 15, 30)
    filled_order, _, _ = sim.execute_order(
        order=order,
        bar_timestamp=same_bar_ts,
        bar_open=3500.0
    )
    assert filled_order.status == OrderStatus.REJECTED
    assert "TIMING_VIOLATION" in filled_order.rejection_reason

    # Executing on next day morning (2024-01-16 09:15) succeeds
    next_bar_ts = datetime(2024, 1, 16, 9, 15)
    order.status = OrderStatus.PENDING
    order.rejection_reason = None
    filled_order, _, _ = sim.execute_order(
        order=order,
        bar_timestamp=next_bar_ts,
        bar_open=3520.0
    )
    assert filled_order.status == OrderStatus.FILLED
    assert filled_order.executed_price > 3520.0  # Includes buy slippage


def test_future_data_mutation_invariance():
    """
    Adding 30 days of future data after the backtest window must NOT alter the equity
    or trades of the backtest period.
    """
    dates_30 = pd.date_range(start="2024-01-01", periods=30, freq="B").date
    dates_60 = pd.date_range(start="2024-01-01", periods=60, freq="B").date

    prices_30 = np.linspace(2000, 2200, 30)
    prices_60 = np.concatenate([prices_30, np.linspace(2200, 2500, 30)])

    df_30 = pd.DataFrame({"open": prices_30, "high": prices_30*1.01, "low": prices_30*0.99, "close": prices_30, "volume": 100000}, index=dates_30)
    df_60 = pd.DataFrame({"open": prices_60, "high": prices_60*1.01, "low": prices_60*0.99, "close": prices_60, "volume": 100000}, index=dates_60)

    config = BacktestConfig(
        name="test_future_invariance",
        symbols=["INFY"],
        start_date=dates_30[0],
        end_date=dates_30[-1],  # Backtest ends at day 30
        initial_capital=1000000.0
    )

    def simple_ma_signal(current_date, pit_data, positions, cash):
        # Only uses data strictly up to current_date
        infy_df = pit_data.get("INFY")
        if infy_df is not None and len(infy_df) >= 5:
            sma = infy_df['close'].tail(5).mean()
            curr = infy_df['close'].iloc[-1]
            if curr > sma and "INFY" not in positions:
                return [{"symbol": "INFY", "side": "BUY", "strength": 0.5}]
            elif curr < sma and "INFY" in positions:
                return [{"symbol": "INFY", "side": "SELL", "strength": 1.0}]
        return []

    # Run with 30 days data
    engine1 = ProductionBacktestEngine(config)
    res1 = engine1.run(market_data={"INFY": df_30}, signal_generator_fn=simple_ma_signal)

    # Run with 60 days data (with future data attached)
    engine2 = ProductionBacktestEngine(config)
    res2 = engine2.run(market_data={"INFY": df_60}, signal_generator_fn=simple_ma_signal)

    assert res1.final_equity == res2.final_equity
    assert len(res1.trades) == len(res2.trades)
    assert len(res1.equity_curve) == len(res2.equity_curve)
