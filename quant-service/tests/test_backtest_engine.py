"""
Unit and integration tests for QuantLab Backtesting Engine (Part 16).
Tests execution timing, cost models, corporate action adjustments, cash reconciliation, and metrics.
"""

from datetime import date, datetime, timedelta
import numpy as np
import pandas as pd
import pytest

from app.backtesting.schemas import (
    BacktestConfig,
    CostModelType,
    ExitReason,
    OrderSide,
    OrderStatus,
    PositionSide,
    SlippageModelType
)
from app.backtesting.engine import ProductionBacktestEngine
from app.backtesting.execution.costs import TransactionCostModel
from app.backtesting.execution.slippage import SlippageModel
from app.backtesting.corporate_actions.processor import CorporateActionProcessor
from app.backtesting.portfolio.accounting import PortfolioAccounting
from app.backtesting.metrics.performance import PerformanceMetricsEngine


@pytest.fixture
def sample_market_data():
    """
    Creates 30 days of synthetic daily OHLCV data for RELIANCE and TCS.
    """
    dates = pd.date_range(start="2024-01-01", periods=30, freq="B").date
    rel_prices = np.linspace(2500, 2700, len(dates))
    tcs_prices = np.linspace(3800, 3600, len(dates))

    rel_df = pd.DataFrame({
        "open": rel_prices * 0.995,
        "high": rel_prices * 1.01,
        "low": rel_prices * 0.99,
        "close": rel_prices,
        "volume": [150000] * len(dates)
    }, index=dates)

    tcs_df = pd.DataFrame({
        "open": tcs_prices * 0.995,
        "high": tcs_prices * 1.01,
        "low": tcs_prices * 0.99,
        "close": tcs_prices,
        "volume": [80000] * len(dates)
    }, index=dates)

    return {"RELIANCE": rel_df, "TCS": tcs_df}, dates


def test_transaction_cost_model_indian():
    config = BacktestConfig(
        name="test_costs",
        symbols=["RELIANCE"],
        start_date=date(2024, 1, 1),
        end_date=date(2024, 1, 31),
        cost_model_type=CostModelType.REALISTIC_INDIAN,
        brokerage_bps=3.0,
        stt_delivery_bps=10.0,
        exchange_charges_bps=0.345,
        gst_rate=0.18,
        stamp_duty_bps=1.5
    )
    cost_model = TransactionCostModel(config)

    # Buy order: 100 shares @ INR 2500 = 250,000 turnover
    buy_costs = cost_model.calculate_cost(OrderSide.BUY, 2500.0, 100, is_delivery=True)
    turnover = 250000.0

    expected_brokerage = turnover * 0.0003  # 75.0
    expected_stt = turnover * 0.0010        # 250.0
    expected_exchange = turnover * 0.0000345 # 8.625
    expected_gst = (expected_brokerage + expected_exchange) * 0.18 # 15.0525
    expected_stamp = turnover * 0.00015     # 37.5

    assert pytest.approx(buy_costs["brokerage"], 0.01) == expected_brokerage
    assert pytest.approx(buy_costs["stt"], 0.01) == expected_stt
    assert pytest.approx(buy_costs["exchange_charges"], 0.01) == expected_exchange
    assert pytest.approx(buy_costs["gst"], 0.01) == expected_gst
    assert pytest.approx(buy_costs["stamp_duty"], 0.01) == expected_stamp
    assert buy_costs["total_fees"] > 350.0


def test_slippage_model():
    config = BacktestConfig(
        name="test_slip",
        symbols=["RELIANCE"],
        start_date=date(2024, 1, 1),
        end_date=date(2024, 1, 31),
        slippage_model_type=SlippageModelType.FIXED_BPS,
        slippage_bps=5.0
    )
    slip_model = SlippageModel(config)

    # Buy executed above market price
    buy_exec, bps_b, slip_amt_b = slip_model.calculate_execution_price(OrderSide.BUY, 2000.0, 100)
    assert buy_exec == 2000.0 * (1 + 5.0 / 10000.0)
    assert slip_amt_b == 100.0

    # Sell executed below market price
    sell_exec, bps_s, slip_amt_s = slip_model.calculate_execution_price(OrderSide.SELL, 2000.0, 100)
    assert sell_exec == 2000.0 * (1 - 5.0 / 10000.0)
    assert slip_amt_s == 100.0


def test_corporate_action_split_and_dividend():
    from uuid import uuid4
    from app.backtesting.schemas import BacktestPosition

    run_id = uuid4()
    open_positions = {
        "INFY": BacktestPosition(
            run_id=run_id,
            symbol="INFY",
            quantity=100,
            average_entry_price=1600.0,
            current_market_price=1600.0,
            cost_basis=160000.0,
            market_value=160000.0,
            unrealized_pnl=0.0,
            unrealized_return_pct=0.0,
            weight_in_portfolio=0.5,
            as_of_date=date(2024, 1, 10),
            highest_price_seen=1600.0,
            lowest_price_seen=1600.0,
            entry_timestamp=datetime(2024, 1, 5, 9, 15)
        )
    }

    # 2:1 Split on 2024-01-10 and INR 20 dividend
    cas = [
        {"symbol": "INFY", "action_type": "SPLIT", "ratio": 2.0, "ex_date": date(2024, 1, 10)},
        {"symbol": "INFY", "action_type": "DIVIDEND", "dividend_amount": 20.0, "ex_date": date(2024, 1, 10)}
    ]

    updated_pos, new_cash, divs_today, ledger = CorporateActionProcessor.process_corporate_actions(
        as_of_date=date(2024, 1, 10),
        open_positions=open_positions,
        corporate_actions=cas,
        run_id=run_id,
        cash_balance=100000.0
    )

    # After 2:1 split: 200 shares @ 800 avg price, cost basis still 160,000 (preserves economic continuity)
    pos = updated_pos["INFY"]
    assert pos.quantity == 200
    assert pos.average_entry_price == 800.0
    assert pos.cost_basis == 160000.0

    # Dividend: 200 shares * INR 20 = INR 4000
    assert divs_today == 4000.0
    assert new_cash == 104000.0
    assert len(ledger) == 1


def test_next_bar_order_execution_and_cash_reconciliation(sample_market_data):
    market_data, dates = sample_market_data

    config = BacktestConfig(
        name="test_e2e_reconciliation",
        symbols=["RELIANCE", "TCS"],
        start_date=dates[0],
        end_date=dates[-1],
        initial_capital=1000000.0,
        cost_model_type=CostModelType.REALISTIC_INDIAN,
        slippage_model_type=SlippageModelType.FIXED_BPS,
        slippage_bps=5.0
    )

    # Custom signal function:
    # Buy RELIANCE on Day 2, Sell on Day 15
    def signal_fn(current_date, pit_data, open_positions, cash):
        signals = []
        if current_date == dates[1]:  # Day 2 signal -> executes Day 3 Open
            signals.append({"symbol": "RELIANCE", "side": "BUY", "strength": 0.5})
        elif current_date == dates[14]:  # Day 15 signal -> executes Day 16 Open
            signals.append({"symbol": "RELIANCE", "side": "SELL", "strength": 1.0})
        return signals

    engine = ProductionBacktestEngine(config)
    result = engine.run(market_data=market_data, signal_generator_fn=signal_fn)

    assert result.status == "COMPLETED"
    assert len(result.trades) == 1

    trade = result.trades[0]
    assert trade.symbol == "RELIANCE"
    assert trade.holding_period_days > 5
    assert trade.net_pnl == pytest.approx(trade.gross_pnl - trade.total_fees - trade.total_slippage, 0.01)

    # Cash and accounting reconciliation audit
    reconciled, msg = engine.accounting.verify_reconciliation()
    assert reconciled, msg
    assert result.data_quality_report.trust_level == "PRODUCTION_READY"


def test_reproducibility_deterministic(sample_market_data):
    market_data, dates = sample_market_data

    config = BacktestConfig(
        name="test_deterministic",
        symbols=["RELIANCE", "TCS"],
        start_date=dates[0],
        end_date=dates[-1],
        initial_capital=1000000.0
    )

    def signal_fn(current_date, pit_data, open_positions, cash):
        if current_date == dates[2]:
            return [{"symbol": "RELIANCE", "side": "BUY", "strength": 0.3}]
        return []

    engine1 = ProductionBacktestEngine(config)
    res1 = engine1.run(market_data=market_data, signal_generator_fn=signal_fn)

    engine2 = ProductionBacktestEngine(config)
    res2 = engine2.run(market_data=market_data, signal_generator_fn=signal_fn)

    assert res1.final_equity == res2.final_equity
    assert res1.metrics.total_return_pct == res2.metrics.total_return_pct
    assert len(res1.trades) == len(res2.trades)
