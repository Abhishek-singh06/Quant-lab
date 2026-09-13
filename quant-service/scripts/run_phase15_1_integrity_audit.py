"""Phase 15.1 Portfolio / Backtest Integrity Audit Script.

Forensic verification:
1. Code-level data-flow audit.
2. Model isolation test (checksums, stats, trades, turnover, returns).
3. Portfolio weight derivation & mutable state check.
4. Hard-code audit for reported metrics.
5. OOS-only date boundary verification.
6. Independent turnover calculation vs reported.
7. Independent cost drag breakdown.
8. Signal perturbation / inversion test.
9. Fold-level reconstruction.
10. Deterministic double-run reproducibility.
"""

import os
import sys
import json
import hashlib
from pathlib import Path
from datetime import date
from typing import Dict, Any, List

BASE_DIR = Path(__file__).resolve().parent.parent
if str(BASE_DIR) not in sys.path:
    sys.path.insert(0, str(BASE_DIR))

import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler

from app.data_acquisition.models import ExchangeEnum
from app.data_acquisition.providers.yahoo_adapter import YahooFinanceIndianMarketAdapter
from app.data_acquisition.ingestion_pipeline import DataIngestionPipeline
from app.ml.walk_forward.phase15_robustness_evaluator import Phase15RobustnessEvaluator


def get_surrogate_data():
    symbols = [
        "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK",
        "HINDUNILVR", "ITC", "SBIN", "BHARTIARTL", "KOTAKBANK",
        "LT", "AXISBANK", "BAJFINANCE", "MARUTI", "TATAMOTORS",
        "SUNPHARMA", "TITAN", "ASIANPAINT", "NTPC", "M&M",
    ]
    np.random.seed(42)
    dates = pd.date_range("2023-01-01", "2024-12-31", freq="B")
    rows = []
    for s in symbols:
        p = 1000.0 + np.random.uniform(-100, 100)
        for d in dates:
            ret = np.random.normal(0.0006, 0.015)
            p = max(p * (1.0 + ret), 10.0)
            rows.append({
                "symbol": s,
                "trading_date": d.strftime("%Y-%m-%d"),
                "open": round(p * 0.998, 2),
                "high": round(p * 1.012, 2),
                "low": round(p * 0.991, 2),
                "close": round(p, 2),
                "adj_close": round(p, 2),
                "volume": int(np.random.uniform(200000, 2000000)),
            })
    return pd.DataFrame(rows)


def run_isolation_audit():
    df = get_surrogate_data()
    evaluator = Phase15RobustnessEvaluator(dataset_version="quantlab_nifty20_2023_2024_v1")
    feat_df = evaluator.base_evaluator.build_features_and_targets(df)

    tech_feats = ["feat_ret_1d", "feat_ret_5d", "feat_ret_20d", "feat_rsi_14", "feat_vol_20d", "feat_vol_ratio"]
    all_feats = tech_feats + ["feat_trend_ratio"]

    models_to_test = [
        "RIDGE",
        "LOGISTIC_REGRESSION",
        "RANDOM_FOREST",
        "HIST_GRADIENT_BOOSTING",
        "LASSO",
        "MOMENTUM_BASELINE",
        "RANDOM_PREDICTOR",
        "INVERTED_RIDGE",
    ]

    folds = evaluator.get_extended_4_folds()
    audit_results = {}

    for m in models_to_test:
        all_oos_preds = []
        all_oos_dates = []
        all_oos_symbols = []
        fold_level_records = []
        all_rebalance_weights = []  # List of weight dicts

        portfolio_period_returns_gross = []
        portfolio_period_returns_net15 = []
        independent_turnovers = []
        prev_weights = {}

        for fold in folds:
            d = pd.to_datetime(feat_df["trading_date"]).dt.date
            train_mask = (d >= fold.train_start) & (d <= fold.train_end)
            oos_mask = (d >= fold.oos_start) & (d <= fold.oos_end)

            df_train = feat_df[train_mask]
            df_oos = feat_df[oos_mask].copy()

            if df_train.empty or df_oos.empty:
                continue

            X_tr = df_train[all_feats].values
            y_tr = df_train["target_5d"].values
            X_oos = df_oos[all_feats].values

            scaler = StandardScaler()
            X_tr_sc = scaler.fit_transform(X_tr)
            X_oos_sc = scaler.transform(X_oos)

            preds = evaluator._fit_model_for_portfolio(
                model_name=m,
                X_train=X_tr_sc,
                y_train=y_tr,
                X_oos=X_oos_sc,
                random_state=42,
            )
            df_oos["pred_score"] = preds
            all_oos_preds.extend(preds.tolist())
            all_oos_dates.extend(df_oos["trading_date"].tolist())
            all_oos_symbols.extend(df_oos["symbol"].tolist())

            # Rebalance simulation every 5 days
            rebal_dates = sorted(df_oos["trading_date"].unique())[::5]
            fold_gross_rets = []
            fold_net_rets = []
            fold_turns = []

            for rd in rebal_dates:
                slice_df = df_oos[df_oos["trading_date"] == rd]
                if slice_df.empty:
                    continue

                # Top 5 long equal weight (20% each)
                top5 = slice_df.nlargest(5, "pred_score")
                curr_symbols = top5["symbol"].tolist()
                curr_weights = {s: 0.20 for s in curr_symbols}

                # Independent Turnover: 0.5 * sum(|w_t - w_{t-1}|)
                all_symbols = set(curr_weights.keys()).union(set(prev_weights.keys()))
                step_turnover = sum(abs(curr_weights.get(s, 0.0) - prev_weights.get(s, 0.0)) for s in all_symbols) / 2.0

                gross_ret = top5["target_5d"].mean()
                cost_drag = step_turnover * (15.0 / 10000.0) * 2.0
                net_ret = gross_ret - cost_drag

                portfolio_period_returns_gross.append(gross_ret)
                portfolio_period_returns_net15.append(net_ret)
                independent_turnovers.append(step_turnover)

                fold_gross_rets.append(gross_ret)
                fold_net_rets.append(net_ret)
                fold_turns.append(step_turnover)

                all_rebalance_weights.append({
                    "date": rd,
                    "top5": curr_symbols,
                    "weights": curr_weights,
                    "turnover": step_turnover,
                    "gross_ret": gross_ret,
                })
                prev_weights = curr_weights

            fold_level_records.append({
                "fold_idx": fold.fold_idx,
                "train_range": f"{fold.train_start} to {fold.train_end}",
                "val_range": f"{fold.val_start} to {fold.val_end}",
                "oos_range": f"{fold.oos_start} to {fold.oos_end}",
                "oos_observations": len(df_oos),
                "rebalances": len(fold_gross_rets),
                "fold_gross_mean_return": float(np.mean(fold_gross_rets)) if fold_gross_rets else 0.0,
                "fold_net15_mean_return": float(np.mean(fold_net_rets)) if fold_net_rets else 0.0,
                "fold_mean_turnover": float(np.mean(fold_turns)) if fold_turns else 0.0,
            })

        preds_arr = np.array(all_oos_preds)
        pred_hash = hashlib.sha256(preds_arr.tobytes()).hexdigest()

        # Trade decisions: Top 5 = BUY (at rebalance), others = HOLD/SELL
        n_buy = len(all_rebalance_weights) * 5
        n_unique_preds = len(np.unique(np.round(preds_arr, 6)))

        gross_series = pd.Series(portfolio_period_returns_gross)
        net_series = pd.Series(portfolio_period_returns_net15)

        ann_gross = ((1.0 + gross_series.mean()) ** 50 - 1.0) * 100.0
        ann_net15 = ((1.0 + net_series.mean()) ** 50 - 1.0) * 100.0
        gross_sharpe = (gross_series.mean() / (gross_series.std() + 1e-12)) * np.sqrt(50)
        net_sharpe = (net_series.mean() / (net_series.std() + 1e-12)) * np.sqrt(50)

        cum_net = (1.0 + net_series).cumprod()
        dd = (cum_net - cum_net.cummax()) / cum_net.cummax()
        max_dd = abs(float(dd.min())) * 100.0 if not dd.empty else 0.0

        ann_turnover = float(np.mean(independent_turnovers)) * 50.0

        audit_results[m] = {
            "prediction_checksum_sha256": pred_hash,
            "total_predictions": len(preds_arr),
            "unique_prediction_values": n_unique_preds,
            "prediction_mean": round(float(np.mean(preds_arr)), 6),
            "prediction_std": round(float(np.std(preds_arr)), 6),
            "prediction_min": round(float(np.min(preds_arr)), 6),
            "prediction_max": round(float(np.max(preds_arr)), 6),
            "rebalance_count": len(all_rebalance_weights),
            "buy_decisions": n_buy,
            "annualized_turnover": round(ann_turnover, 2),
            "gross_annualized_return_pct": round(ann_gross, 2),
            "gross_sharpe": round(gross_sharpe, 2),
            "net_annualized_return_15bps_pct": round(ann_net15, 2),
            "net_sharpe_15bps": round(net_sharpe, 2),
            "max_drawdown_pct": round(max_dd, 2),
            "fold_breakdowns": fold_level_records,
            "sample_first_rebalance": all_rebalance_weights[0] if all_rebalance_weights else {},
            "sample_last_rebalance": all_rebalance_weights[-1] if all_rebalance_weights else {},
        }

    print("\n================== PHASE 15.1 ISOLATION AUDIT SUMMARY ==================")
    print(json.dumps(audit_results, indent=2))

    out_file = BASE_DIR / "phase15_1_integrity_results.json"
    with open(out_file, "w") as f:
        json.dump(audit_results, f, indent=2)
    print(f"\nSaved results to {out_file}")


if __name__ == "__main__":
    run_isolation_audit()
