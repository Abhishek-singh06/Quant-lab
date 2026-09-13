"""Side-by-side Backtesting Comparison between Qlib Vectorized and QuantLab Canonical Engines.

Compares idealized Qlib top-k portfolio returns against QuantLab's realistic Next-Bar T+1
execution engine with Indian statutory transaction costs (STT, brokerage, exchange fees) and slippage.
"""

from datetime import date
from typing import Dict, Any, List, Optional, Tuple, Union
import numpy as np
import pandas as pd

from app.backtesting.schemas import BacktestConfig, CostModelType, OrderSide
from app.backtesting.execution.costs import TransactionCostModel


class QlibBacktestComparator:
    """Compares theoretical Qlib vectorized signals with canonical realistic trade execution."""

    def __init__(
        self,
        top_k: int = 5,
        initial_capital: float = 1_000_000.0,
        slippage_bps: float = 5.0,
        brokerage_bps: float = 3.0,
        stt_bps: float = 10.0,  # 0.1% delivery STT
        annualization_factor: int = 252,
    ):
        self.top_k = top_k
        self.initial_capital = initial_capital
        self.slippage_bps = slippage_bps
        self.brokerage_bps = brokerage_bps
        self.stt_bps = stt_bps
        self.annualization_factor = annualization_factor

        # Configure Indian transaction cost model
        self.config = BacktestConfig(
            name="Qlib_Vectorized_vs_Canonical",
            symbols=["MULTI_ASSET"],
            start_date=date(2023, 1, 1),
            end_date=date(2023, 12, 31),
            initial_capital=initial_capital,
            cost_model_type=CostModelType.REALISTIC_INDIAN,
            brokerage_bps=brokerage_bps,
            stt_delivery_bps=stt_bps,
            slippage_bps=slippage_bps,
        )
        self.cost_model = TransactionCostModel(self.config)

    def run_comparison(
        self,
        predictions: pd.Series,
        price_panel: pd.DataFrame,
    ) -> Dict[str, Any]:
        """Runs both vectorized and canonical realistic backtests on the prediction series.
        
        Args:
            predictions: Series of alpha scores with MultiIndex (datetime, instrument)
            price_panel: DataFrame with MultiIndex (datetime, instrument) containing 'open', 'close'
            
        Returns:
            Dict containing detailed comparison metrics and divergence analysis.
        """
        combined = price_panel[["open", "close"]].copy()
        combined["score"] = predictions
        combined = combined.dropna()

        dt_level = "datetime" if "datetime" in combined.index.names else 0
        timestamps = sorted(combined.index.get_level_values(dt_level).unique())

        if len(timestamps) < 5:
            return {"error": "Insufficient timestamps for backtest comparison"}

        # -------------------------------------------------------------------
        # 1. Theoretical Vectorized Simulation (Close to Close, Zero Friction)
        # -------------------------------------------------------------------
        vec_daily_returns = []
        vec_portfolio_values = [self.initial_capital]

        for i in range(len(timestamps) - 1):
            curr_dt = timestamps[i]
            next_dt = timestamps[i + 1]

            curr_slice = combined.xs(curr_dt, level=dt_level)
            next_slice = combined.xs(next_dt, level=dt_level)

            # Top-k selected at curr_dt close
            k = min(self.top_k, max(1, len(curr_slice)))
            top_insts = curr_slice.sort_values(by="score", ascending=False).head(k).index

            # Calculate return from curr_dt close to next_dt close
            rets = []
            for inst in top_insts:
                if inst in next_slice.index and inst in curr_slice.index:
                    c0 = curr_slice.loc[inst, "close"]
                    c1 = next_slice.loc[inst, "close"]
                    if c0 > 0:
                        rets.append((c1 / c0) - 1.0)

            day_ret = float(np.mean(rets)) if rets else 0.0
            vec_daily_returns.append(day_ret)
            vec_portfolio_values.append(vec_portfolio_values[-1] * (1.0 + day_ret))

        # -------------------------------------------------------------------
        # 2. Canonical Realistic Next-Bar T+1 Simulation
        # -------------------------------------------------------------------
        can_cash = self.initial_capital
        can_positions: Dict[str, int] = {}  # inst -> quantity
        can_portfolio_values = [self.initial_capital]
        can_daily_returns = []
        total_costs_paid = 0.0

        for i in range(len(timestamps) - 1):
            curr_dt = timestamps[i]
            next_dt = timestamps[i + 1]

            curr_slice = combined.xs(curr_dt, level=dt_level)
            next_slice = combined.xs(next_dt, level=dt_level)

            # Signal generated at Bar T close
            k = min(self.top_k, max(1, len(curr_slice)))
            target_insts = set(curr_slice.sort_values(by="score", ascending=False).head(k).index)

            # Execution happens at Bar T+1 Open with slippage and Indian cost model
            # Step A: Liquidate positions no longer in top-k
            for inst in list(can_positions.keys()):
                if inst not in target_insts and inst in next_slice.index:
                    qty = can_positions.pop(inst)
                    open_p = next_slice.loc[inst, "open"]
                    exec_p = open_p * (1.0 - (self.slippage_bps / 10000.0))  # Sell slippage
                    cost_info = self.cost_model.calculate_cost(OrderSide.SELL, exec_p, qty, is_delivery=True)
                    fees = cost_info["total_fees"]
                    can_cash += (exec_p * qty) - fees
                    total_costs_paid += fees

            # Step B: Rebalance into target positions at T+1 Open
            target_weight_per_asset = 1.0 / float(len(target_insts))
            current_nav = can_cash + sum(
                qty * next_slice.loc[inst, "open"]
                for inst, qty in can_positions.items()
                if inst in next_slice.index
            )

            for inst in target_insts:
                if inst in next_slice.index:
                    open_p = next_slice.loc[inst, "open"]
                    exec_p = open_p * (1.0 + (self.slippage_bps / 10000.0))  # Buy slippage
                    target_alloc = current_nav * target_weight_per_asset
                    curr_qty = can_positions.get(inst, 0)
                    curr_val = curr_qty * exec_p
                    diff_val = target_alloc - curr_val

                    if diff_val > 0 and exec_p > 0:
                        add_qty = int(diff_val / exec_p)
                        if add_qty > 0:
                            cost_info = self.cost_model.calculate_cost(OrderSide.BUY, exec_p, add_qty, is_delivery=True)
                            fees = cost_info["total_fees"]
                            total_needed = (exec_p * add_qty) + fees
                            if can_cash >= total_needed:
                                can_cash -= total_needed
                                can_positions[inst] = curr_qty + add_qty
                                total_costs_paid += fees

            # Portfolio valuation at T+1 Close
            eod_val = can_cash + sum(
                qty * next_slice.loc[inst, "close"]
                for inst, qty in can_positions.items()
                if inst in next_slice.index
            )
            prev_val = can_portfolio_values[-1]
            day_ret = float((eod_val / prev_val) - 1.0) if prev_val > 0 else 0.0
            can_daily_returns.append(day_ret)
            can_portfolio_values.append(eod_val)

        # -------------------------------------------------------------------
        # 3. Metrics Compilation & Drag Analysis
        # -------------------------------------------------------------------
        def _calc_metrics(rets, vals):
            mean_r = np.mean(rets) if rets else 0.0
            std_r = np.std(rets) if rets else 1.0
            ann_ret = float(mean_r * self.annualization_factor)
            ann_vol = float(std_r * np.sqrt(self.annualization_factor))
            sharpe = float(ann_ret / (ann_vol + 1e-12))
            cum_rets = np.array(vals) / vals[0]
            peak = np.maximum.accumulate(cum_rets)
            dd = (cum_rets - peak) / peak
            max_dd = float(np.min(dd)) if len(dd) > 0 else 0.0
            final_nav = float(vals[-1])
            total_ret = float((final_nav / vals[0]) - 1.0)
            return {
                "annualized_return": ann_ret,
                "annualized_volatility": ann_vol,
                "sharpe_ratio": sharpe,
                "max_drawdown": max_dd,
                "final_nav": final_nav,
                "total_return": total_ret,
            }

        vec_metrics = _calc_metrics(vec_daily_returns, vec_portfolio_values)
        can_metrics = _calc_metrics(can_daily_returns, can_portfolio_values)

        cagr_drag_bps = float((vec_metrics["annualized_return"] - can_metrics["annualized_return"]) * 10000.0)
        sharpe_delta = float(vec_metrics["sharpe_ratio"] - can_metrics["sharpe_ratio"])

        return {
            "vectorized_qlib": vec_metrics,
            "canonical_quantlab": can_metrics,
            "divergence": {
                "cagr_drag_bps": cagr_drag_bps,
                "sharpe_delta": sharpe_delta,
                "total_friction_costs_paid": float(total_costs_paid),
                "friction_drag_percent": float((total_costs_paid / self.initial_capital) * 100.0),
            },
            "n_days": len(timestamps) - 1,
            "top_k": self.top_k,
        }
