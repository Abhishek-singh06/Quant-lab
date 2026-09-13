"""
Master Production Backtesting Engine for QuantLab (Part 16).
Executes realistic, Point-in-Time safe multi-asset simulations with full risk sizing and cost models.
"""

import time
from datetime import date, datetime, timedelta
from typing import Dict, List, Optional, Any, Callable
from uuid import UUID, uuid4
import pandas as pd

from app.backtesting.schemas import (
    BacktestConfig,
    BacktestDataQualityReport,
    BacktestOrder,
    BacktestPosition,
    BacktestRejectedSignal,
    BacktestResult,
    BacktestStatus,
    BacktestTrade,
    EquityPoint,
    ExitReason,
    OrderSide,
    OrderStatus,
    OrderType,
    PerformanceMetrics,
    PortfolioSnapshot,
    TransactionType
)
from app.backtesting.execution.next_bar import NextBarExecutionSimulator
from app.backtesting.corporate_actions.processor import CorporateActionProcessor
from app.backtesting.portfolio.accounting import PortfolioAccounting
from app.backtesting.metrics.performance import PerformanceMetricsEngine
from app.backtesting.baselines.buy_and_hold import BuyAndHoldBaseline
from app.backtesting.baselines.benchmark import BenchmarkComparisonEngine


class ProductionBacktestEngine:
    """
    Simulates quantitative strategies chronologically without lookahead bias.
    Flow per date/bar t:
    1. Apply corporate actions on open positions (splits, bonuses, dividends on ex-date)
    2. Execute pending orders from previous bar (t-1) at today's open price + slippage + costs
    3. Generate new signals/predictions using Point-in-Time data available up to t
    4. Run risk engine & position sizing constraints (cash buffer, max weight, circuit breakers)
    5. Submit new orders to be executed on the next bar (t+1)
    6. Mark-to-market open positions at today's close price & record daily snapshot
    """

    def __init__(self, config: BacktestConfig):
        self.config = config
        self.run_id = uuid4()
        self.accounting = PortfolioAccounting(config, self.run_id)
        self.execution_simulator = NextBarExecutionSimulator(config)
        self.pending_orders: List[BacktestOrder] = []
        self.rejected_signals: List[BacktestRejectedSignal] = []
        self.data_quality_report = BacktestDataQualityReport()

    def run(
        self,
        market_data: Dict[str, pd.DataFrame],  # symbol -> DataFrame with ['open', 'high', 'low', 'close', 'volume'] indexed by date
        signal_generator_fn: Optional[Callable[[date, Dict[str, pd.DataFrame], Dict[str, BacktestPosition], float], List[Dict]]] = None,
        corporate_actions: Optional[List[Dict]] = None,
        benchmark_series: Optional[pd.Series] = None,
        regime_series: Optional[Dict[date, str]] = None,
        sector_map: Optional[Dict[str, str]] = None
    ) -> BacktestResult:
        start_time_ms = int(time.time() * 1000)
        corporate_actions = corporate_actions or []
        sector_map = sector_map or {}
        regime_series = regime_series or {}

        # Extract unified sorted dates
        all_dates = set()
        for df in market_data.values():
            if not df.empty:
                all_dates.update(df.index)

        sorted_dates = sorted([d if isinstance(d, date) else d.date() for d in all_dates if self.config.start_date <= (d if isinstance(d, date) else d.date()) <= self.config.end_date])

        if not sorted_dates:
            return self._create_empty_result(start_time_ms, "NO_DATA_IN_DATE_RANGE")

        # Main chronological simulation loop
        for idx, current_date in enumerate(sorted_dates):
            current_regime = regime_series.get(current_date, "NEUTRAL")

            # --- STEP 1: Process Corporate Actions on Ex-Date ---
            open_positions, new_cash, divs_today, ca_ledger = CorporateActionProcessor.process_corporate_actions(
                as_of_date=current_date,
                open_positions=self.accounting.positions,
                corporate_actions=corporate_actions,
                run_id=self.run_id,
                cash_balance=self.accounting.cash
            )
            self.accounting.cash = new_cash
            self.accounting.total_dividends_received += divs_today
            self.data_quality_report.corporate_actions_applied += len(ca_ledger)

            # --- STEP 2: Execute Pending Orders from (t-1) at Today's Open ---
            today_bar_opens = {}
            today_bar_closes = {}
            today_bar_volumes = {}

            for symbol, df in market_data.items():
                if current_date in df.index:
                    row = df.loc[current_date]
                    today_bar_opens[symbol] = float(row['open'])
                    today_bar_closes[symbol] = float(row['close'])
                    today_bar_volumes[symbol] = int(row.get('volume', 100000))

            orders_to_keep = []
            for order in self.pending_orders:
                sym = order.symbol
                if sym in today_bar_opens:
                    bar_open = today_bar_opens[sym]
                    bar_vol = today_bar_volumes.get(sym, 100000)
                    bar_ts = datetime.combine(current_date, datetime.min.time()).replace(hour=9, minute=15)

                    filled_order, cash_delta, fees = self.execution_simulator.execute_order(
                        order=order,
                        bar_timestamp=bar_ts,
                        bar_open=bar_open,
                        bar_volume=bar_vol
                    )

                    if filled_order.status == OrderStatus.FILLED:
                        self.accounting.process_fill(filled_order, bar_ts, regime=current_regime)
                else:
                    # Market data missing for this symbol on this date
                    self.data_quality_report.missing_bars_count += 1
                    orders_to_keep.append(order)

            self.pending_orders = orders_to_keep

            # --- STEP 3: Generate Point-in-Time Signals for Today (Close of t) ---
            # Signals use only information up to today's close
            if signal_generator_fn:
                # Filter data available strictly up to current_date
                pit_market_data = {s: df.loc[df.index <= current_date] for s, df in market_data.items()}
                raw_signals = signal_generator_fn(
                    current_date,
                    pit_market_data,
                    self.accounting.positions,
                    self.accounting.cash
                )

                # --- STEP 4: Risk Sizing & Order Placement for (t+1) Open Execution ---
                self._process_signals_into_orders(
                    signals=raw_signals,
                    current_date=current_date,
                    today_closes=today_bar_closes,
                    sector_map=sector_map
                )

            # --- STEP 5: Mark-to-Market Valuations & Daily Snapshot ---
            snapshot = self.accounting.mark_to_market(as_of_date=current_date, market_prices=today_bar_closes)
            snapshot.dividends_credited_today = divs_today

        # Post-simulation reconciliation & performance computation
        reconciled, recon_msg = self.accounting.verify_reconciliation()
        if not reconciled:
            self.data_quality_report.trust_level = "DEGRADED"
            self.data_quality_report.notes.append(recon_msg)

        # Baseline curves
        universe_prices = {s: df['close'] for s, df in market_data.items()}
        bh_equity_series = BuyAndHoldBaseline.generate_equity_curve(
            initial_capital=self.config.initial_capital,
            dates=sorted_dates,
            universe_prices=universe_prices
        )

        b_series = benchmark_series if benchmark_series is not None else pd.Series([100.0] * len(sorted_dates), index=sorted_dates)
        b_equity_series = []
        first_b = b_series.iloc[0] if len(b_series) > 0 else 1.0
        for d in sorted_dates:
            val = b_series.get(d, b_series.iloc[-1] if len(b_series) > 0 else 1.0)
            b_equity_series.append(self.config.initial_capital * (float(val) / first_b if first_b > 0 else 1.0))

        # Build equity curve points
        equity_curve_points = []
        for i, s in enumerate(self.accounting.snapshots):
            bh_eq = bh_equity_series[i] if i < len(bh_equity_series) else self.config.initial_capital
            bh_ret = ((bh_eq - self.config.initial_capital) / self.config.initial_capital) * 100.0
            bm_eq = b_equity_series[i] if i < len(b_equity_series) else self.config.initial_capital
            bm_ret = ((bm_eq - self.config.initial_capital) / self.config.initial_capital) * 100.0

            equity_curve_points.append(
                EquityPoint(
                    point_date=s.snapshot_date,
                    strategy_equity=round(s.total_equity, 2),
                    strategy_return_pct=round(s.cumulative_return, 2),
                    strategy_drawdown_pct=round(s.drawdown_pct, 2),
                    buy_and_hold_equity=round(bh_eq, 2),
                    buy_and_hold_return_pct=round(bh_ret, 2),
                    benchmark_equity=round(bm_eq, 2),
                    benchmark_return_pct=round(bm_ret, 2)
                )
            )

        # Performance metrics
        b_daily_returns = b_series.pct_change().dropna().tolist() if len(b_series) > 1 else None
        metrics = PerformanceMetricsEngine.calculate_metrics(
            snapshots=self.accounting.snapshots,
            trades=self.accounting.closed_trades,
            initial_capital=self.config.initial_capital,
            benchmark_returns=b_daily_returns
        )

        benchmark_comparison = BenchmarkComparisonEngine.compare_to_benchmark(
            benchmark_symbol=self.config.benchmark_symbol,
            strategy_metrics=metrics,
            benchmark_prices=b_series,
            dates=sorted_dates,
            initial_capital=self.config.initial_capital
        )

        execution_duration_ms = int(time.time() * 1000) - start_time_ms
        final_equity = self.accounting.snapshots[-1].total_equity if self.accounting.snapshots else self.config.initial_capital

        return BacktestResult(
            run_id=self.run_id,
            config=self.config,
            status=BacktestStatus.COMPLETED,
            start_date=sorted_dates[0],
            end_date=sorted_dates[-1],
            initial_capital=self.config.initial_capital,
            final_equity=round(final_equity, 2),
            total_net_pnl=round(final_equity - self.config.initial_capital, 2),
            total_fees_paid=round(self.accounting.total_fees_paid, 2),
            total_slippage_paid=round(self.accounting.total_slippage_paid, 2),
            total_dividends_received=round(self.accounting.total_dividends_received, 2),
            metrics=metrics,
            benchmark_comparison=benchmark_comparison,
            equity_curve=equity_curve_points,
            trades=self.accounting.closed_trades,
            portfolio_snapshots=self.accounting.snapshots,
            rejected_signals=self.rejected_signals,
            data_quality_report=self.data_quality_report,
            execution_duration_ms=execution_duration_ms
        )

    def _process_signals_into_orders(
        self,
        signals: List[Dict],
        current_date: date,
        today_closes: Dict[str, float],
        sector_map: Dict[str, str]
    ):
        """
        Translates raw signals into validated BacktestOrders to be executed at (t+1) Open.
        Applies risk constraints: cash buffer, max single position weight, sector concentration, and drawdown limit.
        """
        curr_equity = self.accounting.snapshots[-1].total_equity if self.accounting.snapshots else self.config.initial_capital
        signal_timestamp = datetime.combine(current_date, datetime.min.time()).replace(hour=15, minute=30)
        submission_timestamp = signal_timestamp

        # Check circuit breaker drawdown limit
        curr_dd = self.accounting.snapshots[-1].drawdown_pct if self.accounting.snapshots else 0.0
        if (curr_dd / 100.0) >= self.config.max_drawdown_limit:
            for s in signals:
                if s.get("side") == OrderSide.BUY:
                    self.rejected_signals.append(
                        BacktestRejectedSignal(
                            run_id=self.run_id,
                            symbol=s.get("symbol", "UNKNOWN"),
                            signal_timestamp=signal_timestamp,
                            signal_type="BUY",
                            signal_strength=float(s.get("strength", 1.0)),
                            rejection_reason="RISK_CIRCUIT_BREAKER",
                            details=f"Current drawdown {curr_dd:.2f}% exceeds limit {self.config.max_drawdown_limit * 100.0:.2f}%"
                        )
                    )
            return

        for s in signals:
            symbol = s.get("symbol")
            side = s.get("side", OrderSide.BUY)
            strength = float(s.get("strength", 1.0))
            if isinstance(side, str):
                side = OrderSide(side.upper())

            if symbol not in today_closes or today_closes[symbol] <= 0:
                continue

            close_p = today_closes[symbol]

            if side == OrderSide.SELL:
                # Sell orders for existing positions
                if symbol in self.accounting.positions:
                    pos = self.accounting.positions[symbol]
                    sell_qty = int(s.get("quantity", pos.quantity))
                    sell_qty = min(sell_qty, pos.quantity)
                    if sell_qty > 0:
                        order = BacktestOrder(
                            run_id=self.run_id,
                            symbol=symbol,
                            side=OrderSide.SELL,
                            order_type=OrderType.MARKET,
                            quantity=sell_qty,
                            requested_price=close_p,
                            signal_timestamp=signal_timestamp,
                            order_submitted_timestamp=submission_timestamp,
                            status=OrderStatus.PENDING
                        )
                        self.pending_orders.append(order)

            elif side == OrderSide.BUY:
                # Buy orders: evaluate position sizing & cash limits
                max_alloc_inr = curr_equity * self.config.max_position_weight
                avail_cash_for_trade = self.accounting.cash - (curr_equity * self.config.cash_buffer_pct)

                if avail_cash_for_trade <= 0:
                    self.rejected_signals.append(
                        BacktestRejectedSignal(
                            run_id=self.run_id,
                            symbol=symbol,
                            signal_timestamp=signal_timestamp,
                            signal_type="BUY",
                            signal_strength=strength,
                            rejection_reason="INSUFFICIENT_CASH",
                            details=f"Available trade cash {avail_cash_for_trade:.2f} <= 0 (cash buffer {self.config.cash_buffer_pct * 100:.1f}%)"
                        )
                    )
                    continue

                # Sector check
                sec = sector_map.get(symbol, "DEFAULT")
                sec_curr_val = sum(
                    p.market_value for sym, p in self.accounting.positions.items()
                    if sector_map.get(sym, "DEFAULT") == sec
                )
                if (sec_curr_val / curr_equity) >= self.config.max_sector_weight:
                    self.rejected_signals.append(
                        BacktestRejectedSignal(
                            run_id=self.run_id,
                            symbol=symbol,
                            signal_timestamp=signal_timestamp,
                            signal_type="BUY",
                            signal_strength=strength,
                            rejection_reason="SECTOR_LIMIT_EXCEEDED",
                            details=f"Sector {sec} allocation {(sec_curr_val / curr_equity) * 100:.1f}% exceeds max {self.config.max_sector_weight * 100:.1f}%"
                        )
                    )
                    continue

                # Calculate buy quantity
                target_inr = min(max_alloc_inr, avail_cash_for_trade) * strength
                target_shares = int(target_inr / close_p)

                if target_shares <= 0:
                    self.rejected_signals.append(
                        BacktestRejectedSignal(
                            run_id=self.run_id,
                            symbol=symbol,
                            signal_timestamp=signal_timestamp,
                            signal_type="BUY",
                            signal_strength=strength,
                            rejection_reason="ALLOCATION_TOO_SMALL",
                            details=f"Target shares {target_shares} for price {close_p:.2f}"
                        )
                    )
                    continue

                order = BacktestOrder(
                    run_id=self.run_id,
                    symbol=symbol,
                    side=OrderSide.BUY,
                    order_type=OrderType.MARKET,
                    quantity=target_shares,
                    requested_price=close_p,
                    signal_timestamp=signal_timestamp,
                    order_submitted_timestamp=submission_timestamp,
                    status=OrderStatus.PENDING
                )
                self.pending_orders.append(order)

    def _create_empty_result(self, start_time_ms: int, reason: str) -> BacktestResult:
        dur = int(time.time() * 1000) - start_time_ms
        metrics = PerformanceMetricsEngine.calculate_metrics([], [], self.config.initial_capital)
        return BacktestResult(
            run_id=self.run_id,
            config=self.config,
            status=BacktestStatus.FAILED,
            start_date=self.config.start_date,
            end_date=self.config.end_date,
            initial_capital=self.config.initial_capital,
            final_equity=self.config.initial_capital,
            total_net_pnl=0.0,
            total_fees_paid=0.0,
            total_slippage_paid=0.0,
            total_dividends_received=0.0,
            metrics=metrics,
            equity_curve=[],
            trades=[],
            portfolio_snapshots=[],
            rejected_signals=[],
            data_quality_report=BacktestDataQualityReport(trust_level="REJECTED", notes=[reason]),
            execution_duration_ms=dur
        )
