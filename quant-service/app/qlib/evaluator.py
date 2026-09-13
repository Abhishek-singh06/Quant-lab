"""Qlib Signal Evaluator and Factor Performance Analyzer.

Computes Information Coefficient (IC), Rank IC, ICIR, Long-Short Portfolio returns,
Quantile monotonicity, Turnover, Sharpe, and Drawdown metrics.
"""

from typing import Dict, Any, List, Optional, Tuple, Union
import numpy as np
import pandas as pd
from scipy.stats import spearmanr, pearsonr


class QlibSignalEvaluator:
    """Evaluates alpha signal quality, predictive power, and long-short performance."""

    def __init__(self, top_k: int = 5, n_drop: int = 0, n_quantiles: int = 5, annualization_factor: int = 252):
        self.top_k = top_k
        self.n_drop = n_drop
        self.n_quantiles = n_quantiles
        self.annualization_factor = annualization_factor

    def evaluate(self, predictions: pd.Series, realized_returns: pd.Series) -> Dict[str, Any]:
        """Evaluates predictive signals against realized future returns.
        
        Args:
            predictions: Series with MultiIndex (datetime, instrument) containing alpha scores
            realized_returns: Series with MultiIndex (datetime, instrument) containing realized forward returns
            
        Returns:
            Dict containing detailed evaluation metrics and timeseries
        """
        combined = pd.DataFrame({"score": predictions, "return": realized_returns}).dropna()

        if combined.empty:
            return {"error": "No valid overlapping data for evaluation"}

        dt_level = "datetime" if "datetime" in combined.index.names else 0

        # -------------------------------------------------------------------
        # 1. Daily IC and Rank IC
        # -------------------------------------------------------------------
        daily_ic = []
        daily_rank_ic = []
        dates = []

        for dt, group in combined.groupby(level=dt_level):
            if len(group) < 3:
                continue
            s = group["score"].values
            r = group["return"].values
            
            # Pearson IC
            if np.std(s) > 1e-12 and np.std(r) > 1e-12:
                ic_val, _ = pearsonr(s, r)
                rank_ic_val, _ = spearmanr(s, r)
            else:
                ic_val, rank_ic_val = 0.0, 0.0

            daily_ic.append(float(ic_val) if not np.isnan(ic_val) else 0.0)
            daily_rank_ic.append(float(rank_ic_val) if not np.isnan(rank_ic_val) else 0.0)
            dates.append(dt)

        ic_arr = np.array(daily_ic)
        rank_ic_arr = np.array(daily_rank_ic)

        ic_mean = float(np.mean(ic_arr)) if len(ic_arr) > 0 else 0.0
        ic_std = float(np.std(ic_arr)) if len(ic_arr) > 0 else 1.0
        icir = float(ic_mean / (ic_std + 1e-12) * np.sqrt(self.annualization_factor))

        rank_ic_mean = float(np.mean(rank_ic_arr)) if len(rank_ic_arr) > 0 else 0.0
        rank_ic_std = float(np.std(rank_ic_arr)) if len(rank_ic_arr) > 0 else 1.0
        rank_icir = float(rank_ic_mean / (rank_ic_std + 1e-12) * np.sqrt(self.annualization_factor))

        pos_ic_ratio = float(np.mean(ic_arr > 0)) if len(ic_arr) > 0 else 0.0

        # -------------------------------------------------------------------
        # 2. Top-K Long-Short Portfolio Simulation & Turnover
        # -------------------------------------------------------------------
        daily_long_ret = []
        daily_short_ret = []
        daily_ls_spread = []
        daily_benchmark_ret = []
        prev_long_holdings = set()
        turnover_list = []

        for dt in dates:
            group = combined.xs(dt, level=dt_level)
            n_assets = len(group)
            k = min(self.top_k, max(1, n_assets // 2))

            sorted_group = group.sort_values(by="score", ascending=False)
            
            # Select top-k (after optional n_drop)
            top_slice = sorted_group.iloc[self.n_drop : self.n_drop + k]
            bottom_slice = sorted_group.iloc[-k:]

            long_ret = float(top_slice["return"].mean())
            short_ret = float(bottom_slice["return"].mean())
            bench_ret = float(group["return"].mean())

            daily_long_ret.append(long_ret)
            daily_short_ret.append(short_ret)
            daily_ls_spread.append(long_ret - short_ret)
            daily_benchmark_ret.append(bench_ret)

            # Turnover
            current_long_holdings = set(top_slice.index)
            if prev_long_holdings:
                changed = len(current_long_holdings - prev_long_holdings)
                turnover = changed / float(len(current_long_holdings))
            else:
                turnover = 1.0
            turnover_list.append(turnover)
            prev_long_holdings = current_long_holdings

        ls_arr = np.array(daily_ls_spread)
        long_arr = np.array(daily_long_ret)

        # Performance calculations
        def _calc_stats(rets):
            if len(rets) == 0:
                return 0.0, 0.0, 0.0, 0.0
            mean_r = np.mean(rets)
            std_r = np.std(rets)
            ann_ret = float(mean_r * self.annualization_factor)
            ann_vol = float(std_r * np.sqrt(self.annualization_factor))
            sharpe = float(ann_ret / (ann_vol + 1e-12))
            cum_ret = np.cumprod(1.0 + np.array(rets))
            peak = np.maximum.accumulate(cum_ret)
            drawdown = (cum_ret - peak) / peak
            max_dd = float(np.min(drawdown)) if len(drawdown) > 0 else 0.0
            return ann_ret, ann_vol, sharpe, max_dd

        ls_cagr, ls_vol, ls_sharpe, ls_max_dd = _calc_stats(daily_ls_spread)
        long_cagr, long_vol, long_sharpe, long_max_dd = _calc_stats(daily_long_ret)

        # -------------------------------------------------------------------
        # 3. Quantile Monotonicity Analysis
        # -------------------------------------------------------------------
        quantile_returns: Dict[str, List[float]] = {f"Q{q+1}": [] for q in range(self.n_quantiles)}
        for dt in dates:
            group = combined.xs(dt, level=dt_level)
            if len(group) >= self.n_quantiles:
                group["q"] = pd.qcut(group["score"].rank(method="first"), self.n_quantiles, labels=False)
                for q in range(self.n_quantiles):
                    q_ret = group[group["q"] == q]["return"].mean()
                    quantile_returns[f"Q{q+1}"].append(float(q_ret))

        quantile_summary = {}
        for q_key, q_rets in quantile_returns.items():
            if q_rets:
                ann_r = float(np.mean(q_rets) * self.annualization_factor)
                quantile_summary[q_key] = ann_r

        return {
            "ic_mean": ic_mean,
            "ic_std": ic_std,
            "icir": icir,
            "rank_ic_mean": rank_ic_mean,
            "rank_ic_std": rank_ic_std,
            "rank_icir": rank_icir,
            "positive_ic_ratio": pos_ic_ratio,
            "long_short_annualized_return": ls_cagr,
            "long_short_annualized_volatility": ls_vol,
            "long_short_sharpe": ls_sharpe,
            "long_short_max_drawdown": ls_max_dd,
            "long_only_annualized_return": long_cagr,
            "long_only_sharpe": long_sharpe,
            "long_only_max_drawdown": long_max_dd,
            "mean_daily_turnover": float(np.mean(turnover_list)) if turnover_list else 0.0,
            "quantile_annualized_returns": quantile_summary,
            "n_periods": len(dates),
        }
