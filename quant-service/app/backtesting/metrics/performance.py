"""
Comprehensive performance metrics engine for backtesting.
Computes CAGR, Sharpe, Sortino, Calmar, Win Rate, Profit Factor, Turnover, and Regime breakdowns.
"""

from typing import Dict, List, Optional, Any
import numpy as np
from app.backtesting.schemas import BacktestTrade, PerformanceMetrics, PortfolioSnapshot
from app.backtesting.metrics.drawdown import DrawdownCalculator


class PerformanceMetricsEngine:
    """
    Computes rigorous quantitative metrics from backtest equity snapshots and trade ledgers.
    """

    @staticmethod
    def calculate_metrics(
        snapshots: List[PortfolioSnapshot],
        trades: List[BacktestTrade],
        initial_capital: float,
        benchmark_returns: Optional[List[float]] = None,
        risk_free_rate: float = 0.065,  # 6.5% India RBI 91-day T-Bill baseline
        trading_days_per_year: int = 252
    ) -> PerformanceMetrics:
        if not snapshots or len(snapshots) < 2:
            return PerformanceMetrics(
                total_return_pct=0.0,
                cagr=0.0,
                annualized_volatility=0.0,
                sharpe_ratio=0.0,
                sortino_ratio=0.0,
                max_drawdown_pct=0.0,
                max_drawdown_duration_days=0,
                calmar_ratio=0.0,
                win_rate_pct=0.0,
                profit_factor=0.0,
                average_trade_return_pct=0.0,
                average_win_return_pct=0.0,
                average_loss_return_pct=0.0,
                win_loss_ratio=0.0,
                total_trades_count=len(trades),
                winning_trades_count=0,
                losing_trades_count=0,
                annualized_turnover=0.0
            )

        equities = [s.total_equity for s in snapshots]
        final_equity = equities[-1]
        total_return_pct = ((final_equity - initial_capital) / initial_capital) * 100.0

        num_days = len(snapshots)
        years = max(num_days / trading_days_per_year, 1.0 / trading_days_per_year)

        # CAGR
        if final_equity > 0 and initial_capital > 0:
            cagr = ((final_equity / initial_capital) ** (1.0 / years) - 1.0) * 100.0
        else:
            cagr = -100.0

        # Daily returns
        daily_returns = np.array([s.daily_return for s in snapshots[1:]], dtype=float)
        mean_daily_return = float(np.mean(daily_returns)) if len(daily_returns) > 0 else 0.0
        std_daily_return = float(np.std(daily_returns, ddof=1)) if len(daily_returns) > 1 else 0.0

        # Annualized Volatility
        ann_volatility = (std_daily_return * np.sqrt(trading_days_per_year)) * 100.0

        # Sharpe Ratio
        daily_rf = (1.0 + risk_free_rate) ** (1.0 / trading_days_per_year) - 1.0
        excess_daily_returns = daily_returns - daily_rf
        if std_daily_return > 1e-8:
            sharpe_ratio = float((np.mean(excess_daily_returns) / std_daily_return) * np.sqrt(trading_days_per_year))
        else:
            sharpe_ratio = 0.0

        # Sortino Ratio (Downside deviation only)
        downside_returns = daily_returns[daily_returns < daily_rf]
        if len(downside_returns) > 0:
            downside_std = float(np.sqrt(np.mean((downside_returns - daily_rf) ** 2)))
            if downside_std > 1e-8:
                sortino_ratio = float((np.mean(excess_daily_returns) / downside_std) * np.sqrt(trading_days_per_year))
            else:
                sortino_ratio = 0.0
        else:
            sortino_ratio = sharpe_ratio if sharpe_ratio > 0 else 0.0

        # Drawdowns
        max_dd_pct, max_dd_days, _ = DrawdownCalculator.calculate_drawdowns(equities)

        # Calmar Ratio
        if max_dd_pct > 0.01:
            calmar_ratio = float(cagr / max_dd_pct)
        else:
            calmar_ratio = cagr if cagr > 0 else 0.0

        # Trade Level Metrics
        total_trades = len(trades)
        winning_trades = [t for t in trades if t.net_pnl > 0]
        losing_trades = [t for t in trades if t.net_pnl <= 0]
        win_count = len(winning_trades)
        loss_count = len(losing_trades)

        win_rate_pct = (win_count / total_trades * 100.0) if total_trades > 0 else 0.0

        total_gross_gain = sum(t.net_pnl for t in winning_trades)
        total_gross_loss = abs(sum(t.net_pnl for t in losing_trades))

        if total_gross_loss > 0:
            profit_factor = float(total_gross_gain / total_gross_loss)
        elif total_gross_gain > 0:
            profit_factor = 999.0  # Perfect win factor
        else:
            profit_factor = 0.0

        avg_trade_ret = float(np.mean([t.return_pct for t in trades])) if trades else 0.0
        avg_win_ret = float(np.mean([t.return_pct for t in winning_trades])) if winning_trades else 0.0
        avg_loss_ret = float(np.mean([t.return_pct for t in losing_trades])) if losing_trades else 0.0

        win_loss_ratio = (abs(avg_win_ret / avg_loss_ret)) if abs(avg_loss_ret) > 1e-6 else (avg_win_ret if avg_win_ret > 0 else 0.0)

        # Annualized Turnover = (Sum of traded volume / average portfolio equity) / years
        total_volume_traded = sum(t.entry_price * t.quantity + t.exit_price * t.quantity for t in trades)
        avg_equity = float(np.mean(equities)) if equities else initial_capital
        annualized_turnover = (total_volume_traded / (avg_equity * years)) if (avg_equity > 0 and years > 0) else 0.0

        # Beta & Alpha to Benchmark
        beta, alpha, ir = None, None, None
        if benchmark_returns and len(benchmark_returns) == len(daily_returns) and len(daily_returns) > 5:
            b_rets = np.array(benchmark_returns)
            b_var = np.var(b_rets, ddof=1)
            if b_var > 1e-8:
                covar = np.cov(daily_returns, b_rets)[0][1]
                beta = float(covar / b_var)
                b_ann_return = float(np.mean(b_rets) * trading_days_per_year)
                s_ann_return = float(np.mean(daily_returns) * trading_days_per_year)
                alpha = float(s_ann_return - (risk_free_rate + beta * (b_ann_return - risk_free_rate))) * 100.0

                diff_rets = daily_returns - b_rets
                diff_std = float(np.std(diff_rets, ddof=1))
                if diff_std > 1e-8:
                    ir = float((np.mean(diff_rets) / diff_std) * np.sqrt(trading_days_per_year))

        # Regime breakdown metrics
        regime_breakdown: Dict[str, Any] = {}
        for r_name in ["BULL", "BEAR", "SIDEWAYS", "HIGH_VOLATILITY", "LOW_VOLATILITY"]:
            r_trades = [t for t in trades if t.regime_at_entry == r_name]
            if r_trades:
                r_wins = [t for t in r_trades if t.net_pnl > 0]
                r_pnl = sum(t.net_pnl for t in r_trades)
                regime_breakdown[r_name] = {
                    "trade_count": len(r_trades),
                    "win_rate_pct": round(len(r_wins) / len(r_trades) * 100.0, 2),
                    "total_net_pnl": round(r_pnl, 2),
                    "avg_return_pct": round(float(np.mean([t.return_pct for t in r_trades])), 2)
                }

        # Subperiod breakdown (by Year)
        subperiod_metrics: Dict[str, Any] = {}
        by_year: Dict[int, List[PortfolioSnapshot]] = {}
        for s in snapshots:
            by_year.setdefault(s.snapshot_date.year, []).append(s)

        for yr, yr_snaps in by_year.items():
            if len(yr_snaps) > 1:
                start_eq = yr_snaps[0].total_equity
                end_eq = yr_snaps[-1].total_equity
                yr_ret = ((end_eq - start_eq) / start_eq * 100.0) if start_eq > 0 else 0.0
                yr_eqs = [s.total_equity for s in yr_snaps]
                yr_max_dd, _, _ = DrawdownCalculator.calculate_drawdowns(yr_eqs)
                subperiod_metrics[str(yr)] = {
                    "return_pct": round(yr_ret, 2),
                    "max_drawdown_pct": round(yr_max_dd, 2),
                    "bars_count": len(yr_snaps)
                }

        return PerformanceMetrics(
            total_return_pct=round(total_return_pct, 2),
            cagr=round(cagr, 2),
            annualized_volatility=round(ann_volatility, 2),
            sharpe_ratio=round(sharpe_ratio, 3),
            sortino_ratio=round(sortino_ratio, 3),
            max_drawdown_pct=round(max_dd_pct, 2),
            max_drawdown_duration_days=max_dd_days,
            calmar_ratio=round(calmar_ratio, 3),
            win_rate_pct=round(win_rate_pct, 2),
            profit_factor=round(profit_factor, 2),
            average_trade_return_pct=round(avg_trade_ret, 2),
            average_win_return_pct=round(avg_win_ret, 2),
            average_loss_return_pct=round(avg_loss_ret, 2),
            win_loss_ratio=round(win_loss_ratio, 2),
            total_trades_count=total_trades,
            winning_trades_count=win_count,
            losing_trades_count=loss_count,
            annualized_turnover=round(annualized_turnover, 2),
            beta_to_benchmark=round(beta, 3) if beta is not None else None,
            alpha_to_benchmark=round(alpha, 2) if alpha is not None else None,
            information_ratio=round(ir, 3) if ir is not None else None,
            subperiod_metrics=subperiod_metrics,
            regime_breakdown_metrics=regime_breakdown,
            sector_breakdown_metrics={}
        )
