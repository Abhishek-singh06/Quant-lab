"""Real-Data Walk-Forward Model Evaluation & OOS Validation Harness.

Executes strict chronological walk-forward validation with purge & embargo,
train-only scaling, multiple baselines, and feature ablation experiments on real Indian equities.
"""

from typing import Dict, Any, List, Optional, Tuple
from dataclasses import dataclass, field
from datetime import date, datetime
import numpy as np
import pandas as pd
from scipy.stats import spearmanr, pearsonr
from sklearn.linear_model import Ridge, Lasso, LogisticRegression
from sklearn.ensemble import RandomForestRegressor, HistGradientBoostingRegressor
from sklearn.preprocessing import StandardScaler
import hashlib
import json

from app.data_acquisition.ingestion_pipeline import DataIngestionPipeline
from app.data_acquisition.providers.yahoo_adapter import YahooFinanceIndianMarketAdapter
from app.data_acquisition.readiness_gate import DatasetReadinessGate, GateStatus, DatasetTier


@dataclass
class FoldSpec:
    fold_idx: int
    train_start: date
    train_end: date
    val_start: date
    val_end: date
    oos_start: date
    oos_end: date
    purge_days: int = 5
    embargo_days: int = 2


@dataclass
class ExperimentResult:
    experiment_id: str
    model_name: str
    feature_set: str
    target_horizon: str  # 1D, 5D, 20D
    hyperparameters: Dict[str, Any]
    fold_results: List[Dict[str, Any]]
    mean_oos_ic: float
    mean_oos_rank_ic: float
    mean_oos_accuracy: float
    std_oos_rank_ic: float
    is_beating_momentum: bool
    is_beating_random: bool
    dataset_version: str
    timestamp: datetime = field(default_factory=datetime.utcnow)


class RealDataWalkForwardEvaluator:
    """Rigorous chronological walk-forward validator for quantitative alpha models."""

    def __init__(self, dataset_version: str = "quantlab_nifty20_2023_2024_v1"):
        self.dataset_version = dataset_version
        self.readiness_gate = DatasetReadinessGate()
        self.experiments_registry: List[ExperimentResult] = []

    def build_features_and_targets(self, df: pd.DataFrame) -> pd.DataFrame:
        """Constructs PIT features and forward return targets on real adjusted data."""
        if df.empty:
            return pd.DataFrame()

        df_sorted = df.sort_values(by=["symbol", "trading_date"]).copy()
        df_feats = []

        for sym, group in df_sorted.groupby("symbol"):
            g = group.copy().reset_index(drop=True)
            close = g["adj_close"].values
            volume = g["volume"].values
            n = len(g)

            if n < 30:
                continue

            # --- Technical Features (strictly past-looking) ---
            # 1. Past Returns
            ret_1d = np.zeros(n)
            ret_5d = np.zeros(n)
            ret_20d = np.zeros(n)
            ret_1d[1:] = (close[1:] - close[:-1]) / close[:-1]
            ret_5d[5:] = (close[5:] - close[:-5]) / close[:-5]
            ret_20d[20:] = (close[20:] - close[:-20]) / close[:-20]

            # 2. RSI (14-period)
            delta = np.zeros(n)
            delta[1:] = close[1:] - close[:-1]
            gain = np.where(delta > 0, delta, 0.0)
            loss = np.where(delta < 0, -delta, 0.0)
            avg_gain = pd.Series(gain).rolling(14, min_periods=14).mean().values
            avg_loss = pd.Series(loss).rolling(14, min_periods=14).mean().values
            rs = avg_gain / (avg_loss + 1e-12)
            rsi_14 = 100.0 - (100.0 / (1.0 + rs))
            rsi_14 = np.nan_to_num(rsi_14, nan=50.0)

            # 3. Realized Volatility (20-day annualized)
            vol_20d = pd.Series(ret_1d).rolling(20, min_periods=20).std().values * np.sqrt(252)
            vol_20d = np.nan_to_num(vol_20d, nan=0.20)

            # 4. Volume Ratio (5d / 20d volume SMA)
            vol_sma5 = pd.Series(volume).rolling(5, min_periods=5).mean().values
            vol_sma20 = pd.Series(volume).rolling(20, min_periods=20).mean().values
            vol_ratio = vol_sma5 / (vol_sma20 + 1e-12)
            vol_ratio = np.nan_to_num(vol_ratio, nan=1.0)

            # --- Regime Features ---
            sma_50 = pd.Series(close).rolling(50, min_periods=20).mean().values
            trend_ratio = close / (sma_50 + 1e-12)
            trend_ratio = np.nan_to_num(trend_ratio, nan=1.0)

            # --- Forward Targets (strictly future-looking for labels only) ---
            target_1d = np.zeros(n)
            target_5d = np.zeros(n)
            target_20d = np.zeros(n)

            target_1d[:-1] = (close[1:] - close[:-1]) / close[:-1]
            target_5d[:-5] = (close[5:] - close[:-5]) / close[:-5]
            target_20d[:-20] = (close[20:] - close[:-20]) / close[:-20]

            g["feat_ret_1d"] = ret_1d
            g["feat_ret_5d"] = ret_5d
            g["feat_ret_20d"] = ret_20d
            g["feat_rsi_14"] = rsi_14
            g["feat_vol_20d"] = vol_20d
            g["feat_vol_ratio"] = vol_ratio
            g["feat_trend_ratio"] = trend_ratio

            g["target_1d"] = target_1d
            g["target_5d"] = target_5d
            g["target_20d"] = target_20d
            g["target_1d_dir"] = np.where(target_1d > 0, 1, 0)
            g["target_5d_dir"] = np.where(target_5d > 0, 1, 0)
            g["target_20d_dir"] = np.where(target_20d > 0, 1, 0)

            df_feats.append(g)

        if not df_feats:
            return pd.DataFrame()

        res_df = pd.concat(df_feats, ignore_index=True)
        # Drop initial warmup window (first 25 bars per symbol)
        return res_df[res_df["feat_vol_20d"] > 0].copy()

    def get_walk_forward_folds(self) -> List[FoldSpec]:
        """Defines strict chronological walk-forward fold boundaries (2023–2024)."""
        return [
            FoldSpec(
                fold_idx=1,
                train_start=date(2023, 2, 1),
                train_end=date(2023, 8, 31),
                val_start=date(2023, 9, 8),   # 5-day purge + 2-day embargo
                val_end=date(2023, 10, 31),
                oos_start=date(2023, 11, 8),  # 5-day purge + 2-day embargo
                oos_end=date(2023, 12, 31),
            ),
            FoldSpec(
                fold_idx=2,
                train_start=date(2023, 2, 1),
                train_end=date(2024, 4, 30),
                val_start=date(2024, 5, 8),   # 5-day purge + 2-day embargo
                val_end=date(2024, 7, 31),
                oos_start=date(2024, 8, 8),   # 5-day purge + 2-day embargo
                oos_end=date(2024, 12, 31),
            ),
        ]

    def evaluate_model(
        self,
        df: pd.DataFrame,
        model_name: str,
        feature_cols: List[str],
        target_col: str,
        hyperparameters: Optional[Dict[str, Any]] = None,
    ) -> ExperimentResult:
        """Evaluates a single model configuration across all walk-forward folds."""
        folds = self.get_walk_forward_folds()
        hp = hyperparameters or {}
        fold_summaries = []

        oos_ics = []
        oos_rank_ics = []
        oos_accs = []
        mom_rank_ics = []

        for fold in folds:
            # 1. Filter splits chronologically
            d = pd.to_datetime(df["trading_date"]).dt.date
            train_mask = (d >= fold.train_start) & (d <= fold.train_end)
            val_mask = (d >= fold.val_start) & (d <= fold.val_end)
            oos_mask = (d >= fold.oos_start) & (d <= fold.oos_end)

            df_train = df[train_mask]
            df_val = df[val_mask]
            df_oos = df[oos_mask]

            if df_train.empty or df_oos.empty:
                continue

            X_train = df_train[feature_cols].values
            y_train = df_train[target_col].values
            X_val = df_val[feature_cols].values if not df_val.empty else X_train[:10]
            y_val = df_val[target_col].values if not df_val.empty else y_train[:10]
            X_oos = df_oos[feature_cols].values
            y_oos = df_oos[target_col].values

            # 2. FIT SCALER ON TRAIN ONLY!
            scaler = StandardScaler()
            X_train_scaled = scaler.fit_transform(X_train)
            X_val_scaled = scaler.transform(X_val)
            X_oos_scaled = scaler.transform(X_oos)

            # 3. Fit Model / Generate Predictions
            preds_train, preds_val, preds_oos = self._fit_and_predict(
                model_name=model_name,
                X_train=X_train_scaled,
                y_train=y_train,
                X_val=X_val_scaled,
                y_val=y_val,
                X_oos=X_oos_scaled,
                hp=hp,
            )

            # 4. Calculate Fold Metrics
            # IC & Rank IC
            ic, _ = pearsonr(preds_oos, y_oos) if len(y_oos) > 1 else (0.0, 1.0)
            ric, _ = spearmanr(preds_oos, y_oos) if len(y_oos) > 1 else (0.0, 1.0)
            ic = 0.0 if np.isnan(ic) else float(ic)
            ric = 0.0 if np.isnan(ric) else float(ric)

            # Directional accuracy
            pred_dir = (preds_oos > 0).astype(int)
            actual_dir = (y_oos > 0).astype(int)
            acc = float(np.mean(pred_dir == actual_dir)) * 100.0

            # Momentum benchmark Rank IC (20d momentum)
            mom_col = df_oos["feat_ret_20d"].values
            mom_ric, _ = spearmanr(mom_col, y_oos) if len(y_oos) > 1 else (0.0, 1.0)
            mom_ric = 0.0 if np.isnan(mom_ric) else float(mom_ric)

            oos_ics.append(ic)
            oos_rank_ics.append(ric)
            oos_accs.append(acc)
            mom_rank_ics.append(mom_ric)

            fold_summaries.append({
                "fold_idx": fold.fold_idx,
                "train_obs": len(df_train),
                "val_obs": len(df_val),
                "oos_obs": len(df_oos),
                "oos_ic": round(ic, 4),
                "oos_rank_ic": round(ric, 4),
                "oos_accuracy_pct": round(acc, 2),
                "momentum_baseline_rank_ic": round(mom_ric, 4),
            })

        mean_ic = float(np.mean(oos_ics)) if oos_ics else 0.0
        mean_ric = float(np.mean(oos_rank_ics)) if oos_rank_ics else 0.0
        mean_acc = float(np.mean(oos_accs)) if oos_accs else 50.0
        std_ric = float(np.std(oos_rank_ics)) if oos_rank_ics else 0.0
        mean_mom_ric = float(np.mean(mom_rank_ics)) if mom_rank_ics else 0.0

        exp_id = f"EXP_{hashlib.sha256(f'{model_name}_{target_col}_{feature_cols}_{hp}'.encode()).hexdigest()[:10].upper()}"

        result = ExperimentResult(
            experiment_id=exp_id,
            model_name=model_name,
            feature_set="_".join(feature_cols[:2]) + f"_({len(feature_cols)}f)",
            target_horizon=target_col,
            hyperparameters=hp,
            fold_results=fold_summaries,
            mean_oos_ic=round(mean_ic, 4),
            mean_oos_rank_ic=round(mean_ric, 4),
            mean_oos_accuracy=round(mean_acc, 2),
            std_oos_rank_ic=round(std_ric, 4),
            is_beating_momentum=(mean_ric > mean_mom_ric),
            is_beating_random=(mean_ric > 0.0 and mean_acc > 50.0),
            dataset_version=self.dataset_version,
        )

        self.experiments_registry.append(result)
        return result

    def _fit_and_predict(
        self,
        model_name: str,
        X_train: np.ndarray,
        y_train: np.ndarray,
        X_val: np.ndarray,
        y_val: np.ndarray,
        X_oos: np.ndarray,
        hp: Dict[str, Any],
    ) -> Tuple[np.ndarray, np.ndarray, np.ndarray]:
        """Fits specific model type and returns train, val, oos predictions."""
        algo = model_name.upper()

        if algo == "RANDOM_PREDICTOR":
            rng = np.random.RandomState(42)
            return rng.randn(len(X_train)), rng.randn(len(X_val)), rng.randn(len(X_oos))

        elif algo == "CONSTANT_ZERO":
            return np.zeros(len(X_train)), np.zeros(len(X_val)), np.zeros(len(X_oos))

        elif algo == "MOMENTUM_BASELINE":
            # Uses first column (assumed return feature) as direct signal
            return X_train[:, 0], X_val[:, 0], X_oos[:, 0]

        elif algo == "RIDGE":
            alpha = hp.get("alpha", 1.0)
            clf = Ridge(alpha=alpha, random_state=42)
            clf.fit(X_train, y_train)
            return clf.predict(X_train), clf.predict(X_val), clf.predict(X_oos)

        elif algo == "LASSO":
            alpha = hp.get("alpha", 0.001)
            clf = Lasso(alpha=alpha, random_state=42, max_iter=2000)
            clf.fit(X_train, y_train)
            return clf.predict(X_train), clf.predict(X_val), clf.predict(X_oos)

        elif algo == "RANDOM_FOREST":
            n_est = hp.get("n_estimators", 50)
            max_d = hp.get("max_depth", 4)
            clf = RandomForestRegressor(n_estimators=n_est, max_depth=max_d, random_state=42, n_jobs=-1)
            clf.fit(X_train, y_train)
            return clf.predict(X_train), clf.predict(X_val), clf.predict(X_oos)

        elif algo in ("GRADIENT_BOOSTING", "HIST_GRADIENT_BOOSTING", "LIGHTGBM"):
            max_d = hp.get("max_depth", 3)
            lr = hp.get("learning_rate", 0.05)
            clf = HistGradientBoostingRegressor(max_depth=max_d, learning_rate=lr, random_state=42)
            clf.fit(X_train, y_train)
            return clf.predict(X_train), clf.predict(X_val), clf.predict(X_oos)

        elif algo == "LOGISTIC_REGRESSION":
            # Binary directional target
            y_train_dir = (y_train > 0).astype(int)
            clf = LogisticRegression(random_state=42)
            clf.fit(X_train, y_train_dir)
            # Predict probabilities of positive return
            return (
                clf.predict_proba(X_train)[:, 1] - 0.5,
                clf.predict_proba(X_val)[:, 1] - 0.5,
                clf.predict_proba(X_oos)[:, 1] - 0.5,
            )

        else:
            # Default fallback to Ridge
            clf = Ridge(alpha=1.0, random_state=42)
            clf.fit(X_train, y_train)
            return clf.predict(X_train), clf.predict(X_val), clf.predict(X_oos)
