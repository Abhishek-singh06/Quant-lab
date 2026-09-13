"""Walk-Forward Model Benchmark & Classical ML vs Deep Learning Ensemble.

Enforces institutional-grade empirical comparison between Classical ML Baselines
(HistGBDT / Ridge / Random Forest) and Deep Learning sequence models (GRU, LSTM, CNN, Attention)
under realistic Indian equity market transaction costs (15 bps round-trip).
"""

from typing import Dict, Any, List, Optional, Tuple
import numpy as np
import pandas as pd
from sklearn.linear_model import Ridge
from sklearn.ensemble import HistGradientBoostingRegressor, RandomForestRegressor

from app.deep_learning.scaler import PointInTimeScaler
from app.deep_learning.sequence_generator import TemporalSequenceGenerator, SequenceDataset
from app.deep_learning.purge_embargo import TimeSeriesSplitPurged
from app.deep_learning.adapter import DeepLearningModelAdapter


class WalkForwardEnsembleComparator:
    """Evaluates and compares Classical ML baselines against Deep Learning architectures."""

    def __init__(
        self,
        lookback: int = 60,
        horizon: int = 1,
        n_splits: int = 4,
        cost_bps: float = 15.0,  # 15 bps Indian equity round-trip costs (STT, broker, slippage)
        random_seed: int = 42,
    ):
        self.lookback = lookback
        self.horizon = horizon
        self.n_splits = n_splits
        self.cost_bps = cost_bps
        self.random_seed = random_seed
        self.cost_fraction = cost_bps / 10000.0

    def run_comparison(
        self,
        df: pd.DataFrame,
        feature_cols: Optional[List[str]] = None,
        target_col: Optional[str] = "target",
    ) -> Dict[str, Any]:
        """Runs purged walk-forward cross-validation comparing Classical ML vs Deep Learning."""
        seq_gen = TemporalSequenceGenerator(
            lookback=self.lookback,
            horizon=self.horizon,
            feature_cols=feature_cols,
            target_col=target_col,
        )
        dataset = seq_gen.generate(df)
        N, T, D = dataset.shape

        if N < 50:
            raise ValueError(f"Insufficient sequences ({N}) for walk-forward CV. Need at least 50.")

        cv = TimeSeriesSplitPurged(
            n_splits=self.n_splits,
            lookback=self.lookback,
            horizon=self.horizon,
        )

        results = {
            "classical_ridge": {"predictions": [], "targets": []},
            "classical_gbdt": {"predictions": [], "targets": []},
            "dl_gru": {"predictions": [], "targets": []},
            "dl_lstm": {"predictions": [], "targets": []},
            "dl_cnn": {"predictions": [], "targets": []},
            "dl_attention": {"predictions": [], "targets": []},
            "ensemble_hybrid": {"predictions": [], "targets": []},
        }

        # Tabular representation for classical models (using final bar or flattened sequence)
        X_2d_tabular = dataset.X[:, -1, :]  # (N, D) snapshot at time t

        fold_idx = 0
        for train_idx, test_idx in cv.split(dataset.X):
            fold_idx += 1
            # 1. Prepare data splits
            X_train_3d, y_train = dataset.X[train_idx], dataset.y[train_idx]
            X_test_3d, y_test = dataset.X[test_idx], dataset.y[test_idx]

            X_train_2d = X_2d_tabular[train_idx]
            X_test_2d = X_2d_tabular[test_idx]

            # Fit PIT scaler strictly on training 3D sequence
            scaler = PointInTimeScaler(method="standard")
            X_train_3d_scaled = scaler.fit_transform(X_train_3d)
            X_test_3d_scaled = scaler.transform(X_test_3d)

            # Fit tabular scaler on training 2D
            scaler_2d = PointInTimeScaler(method="standard")
            X_train_2d_scaled = scaler_2d.fit_transform(X_train_2d)
            X_test_2d_scaled = scaler_2d.transform(X_test_2d)

            # --- Model A: Ridge Regression (Classical Baseline) ---
            ridge = Ridge(alpha=1.0, random_state=self.random_seed)
            ridge.fit(X_train_2d_scaled, y_train)
            pred_ridge = ridge.predict(X_test_2d_scaled)

            # --- Model B: HistGradientBoosting (Classical GBDT Baseline) ---
            gbdt = HistGradientBoostingRegressor(max_iter=50, random_state=self.random_seed)
            gbdt.fit(X_train_2d_scaled, y_train)
            pred_gbdt = gbdt.predict(X_test_2d_scaled)

            # --- Model C: Deep Learning GRU ---
            gru_adapter = DeepLearningModelAdapter(
                model_type_name="GRU",
                lookback=self.lookback,
                horizon=self.horizon,
                hidden_dim=24,
                epochs=10,
                random_seed=self.random_seed,
            )
            gru_adapter.scaler = scaler
            gru_adapter.dl_model.fit(X_train_3d_scaled, y_train)
            gru_adapter.is_fitted = True
            pred_gru = gru_adapter.predict(X_test_3d)

            # --- Model D: Deep Learning LSTM ---
            lstm_adapter = DeepLearningModelAdapter(
                model_type_name="LSTM",
                lookback=self.lookback,
                horizon=self.horizon,
                hidden_dim=24,
                epochs=10,
                random_seed=self.random_seed,
            )
            lstm_adapter.scaler = scaler
            lstm_adapter.dl_model.fit(X_train_3d_scaled, y_train)
            lstm_adapter.is_fitted = True
            pred_lstm = lstm_adapter.predict(X_test_3d)

            # --- Model E: Deep Learning 1D-CNN ---
            cnn_adapter = DeepLearningModelAdapter(
                model_type_name="1DCNN",
                lookback=self.lookback,
                horizon=self.horizon,
                hidden_dim=24,
                epochs=10,
                random_seed=self.random_seed,
            )
            cnn_adapter.scaler = scaler
            cnn_adapter.dl_model.fit(X_train_3d_scaled, y_train)
            cnn_adapter.is_fitted = True
            pred_cnn = cnn_adapter.predict(X_test_3d)

            # --- Model F: Deep Learning Attention ---
            attn_adapter = DeepLearningModelAdapter(
                model_type_name="ATTENTION",
                lookback=self.lookback,
                horizon=self.horizon,
                hidden_dim=24,
                epochs=10,
                random_seed=self.random_seed,
            )
            attn_adapter.scaler = scaler
            attn_adapter.dl_model.fit(X_train_3d_scaled, y_train)
            attn_adapter.is_fitted = True
            pred_attn = attn_adapter.predict(X_test_3d)

            # --- Model G: Hybrid Ensemble (50% GBDT + 50% GRU) ---
            pred_hybrid = 0.5 * pred_gbdt + 0.5 * pred_gru

            # Store predictions
            results["classical_ridge"]["predictions"].extend(pred_ridge.tolist())
            results["classical_ridge"]["targets"].extend(y_test.tolist())

            results["classical_gbdt"]["predictions"].extend(pred_gbdt.tolist())
            results["classical_gbdt"]["targets"].extend(y_test.tolist())

            results["dl_gru"]["predictions"].extend(pred_gru.tolist())
            results["dl_gru"]["targets"].extend(y_test.tolist())

            results["dl_lstm"]["predictions"].extend(pred_lstm.tolist())
            results["dl_lstm"]["targets"].extend(y_test.tolist())

            results["dl_cnn"]["predictions"].extend(pred_cnn.tolist())
            results["dl_cnn"]["targets"].extend(y_test.tolist())

            results["dl_attention"]["predictions"].extend(pred_attn.tolist())
            results["dl_attention"]["targets"].extend(y_test.tolist())

            results["ensemble_hybrid"]["predictions"].extend(pred_hybrid.tolist())
            results["ensemble_hybrid"]["targets"].extend(y_test.tolist())

        # Calculate metrics summary
        summary: Dict[str, Any] = {}
        for m_name, d in results.items():
            p = np.array(d["predictions"])
            t = np.array(d["targets"])
            summary[m_name] = self._calculate_model_metrics(p, t)

        return {
            "dataset_samples": N,
            "lookback_bars": self.lookback,
            "horizon_bars": self.horizon,
            "n_splits": self.n_splits,
            "cost_bps": self.cost_bps,
            "metrics": summary,
        }

    def _calculate_model_metrics(self, preds: np.ndarray, targets: np.ndarray) -> Dict[str, float]:
        """Computes comprehensive quantitative alpha performance metrics."""
        n = len(preds)
        if n == 0:
            return {}

        mae = float(np.mean(np.abs(preds - targets)))
        rmse = float(np.sqrt(np.mean((preds - targets) ** 2)))
        dir_acc = float(np.mean(np.sign(preds) == np.sign(targets)))

        # IC (Information Coefficient)
        std_p = np.std(preds)
        std_t = np.std(targets)
        ic = float(np.corrcoef(preds, targets)[0, 1]) if (std_p > 1e-8 and std_t > 1e-8) else 0.0

        # Rank IC
        rank_p = pd.Series(preds).rank().values
        rank_t = pd.Series(targets).rank().values
        std_rp = np.std(rank_p)
        std_rt = np.std(rank_t)
        rank_ic = float(np.corrcoef(rank_p, rank_t)[0, 1]) if (std_rp > 1e-8 and std_rt > 1e-8) else 0.0

        # Backtest simulation with realistic transaction friction
        # Strategy returns: sign(prediction) * target_return - transaction_costs
        positions = np.sign(preds)
        trades = np.abs(np.diff(positions, prepend=0))
        friction = trades * self.cost_fraction
        strat_returns = positions * targets - friction

        mean_ret = float(np.mean(strat_returns))
        std_ret = float(np.std(strat_returns))
        ann_factor = np.sqrt(252)
        sharpe = float((mean_ret / (std_ret + 1e-8)) * ann_factor) if std_ret > 1e-8 else 0.0

        # Max Drawdown
        cum_ret = np.cumprod(1.0 + strat_returns)
        peak = np.maximum.accumulate(cum_ret)
        drawdown = (cum_ret - peak) / peak
        max_dd = float(np.min(drawdown)) if len(drawdown) > 0 else 0.0

        return {
            "ic": round(ic, 4),
            "rank_ic": round(rank_ic, 4),
            "directional_accuracy": round(dir_acc, 4),
            "mae": round(mae, 6),
            "rmse": round(rmse, 6),
            "sharpe_net": round(sharpe, 2),
            "max_drawdown": round(max_dd, 4),
        }
