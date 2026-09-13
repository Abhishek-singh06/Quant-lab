"""
Benchmark comparison generator for backtesting (e.g. NIFTY 50 Index).
"""

from datetime import date
from typing import List, Optional
import numpy as np
import pandas as pd
from app.backtesting.schemas import BenchmarkComparison, PerformanceMetrics


class BenchmarkComparisonEngine:
    """
    Computes comparative statistics against benchmark index (e.g. NIFTY 50).
    """

    @staticmethod
    def compare_to_benchmark(
        benchmark_symbol: str,
        strategy_metrics: PerformanceMetrics,
        benchmark_prices: pd.Series,
        dates: List[date],
        initial_capital: float,
        trading_days_per_year: int = 252
    ) -> BenchmarkComparison:
        if benchmark_prices.empty or len(benchmark_prices) < 2:
            return BenchmarkComparison(
                benchmark_symbol=benchmark_symbol,
                strategy_total_return=strategy_metrics.total_return_pct,
                benchmark_total_return=0.0,
                strategy_cagr=strategy_metrics.cagr,
                benchmark_cagr=0.0,
                strategy_sharpe=strategy_metrics.sharpe_ratio,
                benchmark_sharpe=0.0,
                strategy_max_dd=strategy_metrics.max_drawdown_pct,
                benchmark_max_dd=0.0,
                alpha=strategy_metrics.alpha_to_benchmark or 0.0,
                beta=strategy_metrics.beta_to_benchmark or 1.0,
                tracking_error=0.0,
                information_ratio=strategy_metrics.information_ratio or 0.0
            )

        start_p = benchmark_prices.iloc[0]
        end_p = benchmark_prices.iloc[-1]
        b_tot_return = ((end_p - start_p) / start_p) * 100.0 if start_p > 0 else 0.0

        years = max(len(dates) / trading_days_per_year, 1.0 / trading_days_per_year)
        b_cagr = (((end_p / start_p) ** (1.0 / years) - 1.0) * 100.0) if (start_p > 0 and end_p > 0) else 0.0

        b_daily_rets = benchmark_prices.pct_change().dropna().values
        b_std = float(np.std(b_daily_rets, ddof=1)) if len(b_daily_rets) > 1 else 0.0
        rf_daily = (1.0 + 0.065) ** (1.0 / trading_days_per_year) - 1.0
        b_sharpe = float((np.mean(b_daily_rets - rf_daily) / b_std) * np.sqrt(trading_days_per_year)) if b_std > 1e-8 else 0.0

        # Benchmark Max Drawdown
        running_max = np.maximum.accumulate(benchmark_prices.values)
        b_dds = (running_max - benchmark_prices.values) / running_max * 100.0
        b_max_dd = float(np.max(b_dds))

        return BenchmarkComparison(
            benchmark_symbol=benchmark_symbol,
            strategy_total_return=strategy_metrics.total_return_pct,
            benchmark_total_return=round(b_tot_return, 2),
            strategy_cagr=strategy_metrics.cagr,
            benchmark_cagr=round(b_cagr, 2),
            strategy_sharpe=strategy_metrics.sharpe_ratio,
            benchmark_sharpe=round(b_sharpe, 3),
            strategy_max_dd=strategy_metrics.max_drawdown_pct,
            benchmark_max_dd=round(b_max_dd, 2),
            alpha=strategy_metrics.alpha_to_benchmark or 0.0,
            beta=strategy_metrics.beta_to_benchmark or 1.0,
            tracking_error=round(b_std * np.sqrt(trading_days_per_year) * 100.0, 2),
            information_ratio=strategy_metrics.information_ratio or 0.0
        )
