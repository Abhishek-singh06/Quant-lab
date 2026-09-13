"""Return Regression Models for 1D and multi-horizon target forecasting.
"""

from typing import Dict, Any, Optional, List
import numpy as np
import pandas as pd
from scipy.stats import spearmanr
from sklearn.linear_model import LinearRegression, Ridge, ElasticNet
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor

from app.ml.models.base import QuantPredictionModel


class RegressionModel(QuantPredictionModel):
    """Production Return Regression Model."""

    def __init__(
        self,
        model_id: str,
        algorithm: str = "GRADIENT_BOOSTING",
        model_version: str = "REG_1D_v1.0",
        target_definition: str = "future_return_1d",
        target_horizon: str = "1D",
        hyperparameters: Optional[Dict[str, Any]] = None,
        random_seed: int = 42
    ):
        super().__init__(
            model_id=model_id,
            model_name=f"Return Regressor ({algorithm})",
            model_type="REGRESSION",
            algorithm=algorithm.upper(),
            model_version=model_version,
            target_definition=target_definition,
            target_horizon=target_horizon,
            hyperparameters=hyperparameters,
            random_seed=random_seed
        )
        self._init_estimator()

    def _init_estimator(self):
        algo = self.algorithm
        hp = self.hyperparameters
        rs = self.random_seed

        if algo == "LINEAR_REGRESSION":
            self.estimator = LinearRegression()
        elif algo == "RIDGE":
            alpha = hp.get("alpha", 1.0)
            self.estimator = Ridge(alpha=alpha, random_state=rs)
        elif algo == "ELASTIC_NET":
            alpha = hp.get("alpha", 1.0)
            l1_ratio = hp.get("l1_ratio", 0.5)
            self.estimator = ElasticNet(alpha=alpha, l1_ratio=l1_ratio, random_state=rs)
        elif algo == "RANDOM_FOREST":
            n_est = hp.get("n_estimators", 100)
            max_d = hp.get("max_depth", 6)
            min_leaf = hp.get("min_samples_leaf", 5)
            self.estimator = RandomForestRegressor(
                n_estimators=n_est, max_depth=max_d, min_samples_leaf=min_leaf, random_state=rs, n_jobs=-1
            )
        elif algo == "GRADIENT_BOOSTING":
            n_est = hp.get("n_estimators", 100)
            lr = hp.get("learning_rate", 0.05)
            max_d = hp.get("max_depth", 4)
            subsample = hp.get("subsample", 0.8)
            self.estimator = GradientBoostingRegressor(
                n_estimators=n_est, learning_rate=lr, max_depth=max_d, subsample=subsample, random_state=rs
            )
        elif algo == "ZERO_BASELINE":
            self.estimator = None
        elif algo == "HISTORICAL_MEAN":
            self.estimator = None
            self.hist_mean_ = 0.0
        else:
            raise ValueError(f"Unsupported regression algorithm: {algo}")

    def fit(self, X: pd.DataFrame, y: Any) -> "RegressionModel":
        self.feature_names = list(X.columns)
        X_clean = X.fillna(0.0)
        y_clean = pd.Series(y).fillna(0.0)

        if self.algorithm == "ZERO_BASELINE":
            pass
        elif self.algorithm == "HISTORICAL_MEAN":
            self.hist_mean_ = float(y_clean.mean())
        else:
            self.estimator.fit(X_clean, y_clean)

            # Store feature importances / coefficients
            if hasattr(self.estimator, "feature_importances_"):
                self.feature_importances_ = {
                    col: float(imp) for col, imp in zip(self.feature_names, self.estimator.feature_importances_)
                }
            elif hasattr(self.estimator, "coef_"):
                self.feature_importances_ = {
                    col: float(abs(c)) for col, c in zip(self.feature_names, self.estimator.coef_)
                }

        self.is_fitted = True
        return self

    def predict(self, X: pd.DataFrame) -> np.ndarray:
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before predict()")

        X_clean = X.fillna(0.0)
        if self.algorithm == "ZERO_BASELINE":
            return np.zeros(len(X_clean))
        elif self.algorithm == "HISTORICAL_MEAN":
            return np.full(len(X_clean), self.hist_mean_)
        else:
            return self.estimator.predict(X_clean)

    def explain(self, X: pd.DataFrame) -> List[Dict[str, float]]:
        """Calculates feature contributions."""
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before explain()")

        X_clean = X.fillna(0.0)
        preds = self.predict(X_clean)
        explanations = []

        top_feats = sorted(self.feature_importances_.items(), key=lambda x: x[1], reverse=True)[:5]
        for i in range(len(X_clean)):
            row_exp = {}
            for feat_name, imp in top_feats:
                val = X_clean.iloc[i].get(feat_name, 0.0)
                row_exp[feat_name] = round(float(val * imp * 0.01), 6)
            explanations.append(row_exp)
        return explanations

    def evaluate(self, X: pd.DataFrame, y: Any) -> Dict[str, Any]:
        y_pred = self.predict(X)
        y_true = np.asarray(y)

        valid_mask = ~np.isnan(y_true)
        if not valid_mask.any():
            return {"status": "NO_VALID_TARGETS"}

        y_true_v = y_true[valid_mask]
        y_pred_v = y_pred[valid_mask]

        mae = float(np.mean(np.abs(y_true_v - y_pred_v)))
        rmse = float(np.sqrt(np.mean((y_true_v - y_pred_v) ** 2)))
        ss_tot = np.sum((y_true_v - np.mean(y_true_v)) ** 2)
        ss_res = np.sum((y_true_v - y_pred_v) ** 2)
        r2 = float(1 - (ss_res / ss_tot)) if ss_tot > 0 else 0.0

        # Pearson IC
        if np.std(y_pred_v) > 1e-8 and np.std(y_true_v) > 1e-8:
            ic = float(np.corrcoef(y_pred_v, y_true_v)[0, 1])
            rank_ic = float(spearmanr(y_pred_v, y_true_v).statistic)
        else:
            ic = 0.0
            rank_ic = 0.0

        # Directional Accuracy (Sign agreement)
        dir_acc = float(np.mean(np.sign(y_pred_v) == np.sign(y_true_v)))

        return {
            "observations": int(len(y_true_v)),
            "mae": round(mae, 6),
            "rmse": round(rmse, 6),
            "r2": round(r2, 6),
            "ic": round(ic, 4),
            "rank_ic": round(rank_ic, 4),
            "directional_accuracy": round(dir_acc, 4),
            "top_features": sorted(self.feature_importances_.items(), key=lambda x: x[1], reverse=True)[:5]
        }
