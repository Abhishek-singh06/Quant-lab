"""Future Volatility Prediction Models.
"""

from typing import Dict, Any, Optional, List
import numpy as np
import pandas as pd
from sklearn.linear_model import Ridge
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor

from app.ml.models.base import QuantPredictionModel


class VolatilityModel(QuantPredictionModel):
    """Production Realized Volatility Prediction Model."""

    def __init__(
        self,
        model_id: str,
        algorithm: str = "GRADIENT_BOOSTING",
        model_version: str = "VOL_5D_v1.0",
        target_definition: str = "future_realized_volatility_5d",
        target_horizon: str = "5D",
        hyperparameters: Optional[Dict[str, Any]] = None,
        random_seed: int = 42
    ):
        super().__init__(
            model_id=model_id,
            model_name=f"Volatility Predictor ({algorithm})",
            model_type="VOLATILITY",
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

        if algo == "RIDGE":
            alpha = hp.get("alpha", 1.0)
            self.estimator = Ridge(alpha=alpha, random_state=rs)
        elif algo in ["RANDOM_FOREST", "RANDOM_FOREST_REGRESSOR"]:
            n_est = hp.get("n_estimators", 100)
            max_d = hp.get("max_depth", 5)
            self.estimator = RandomForestRegressor(n_estimators=n_est, max_depth=max_d, random_state=rs, n_jobs=-1)
        elif algo in ["GRADIENT_BOOSTING", "GRADIENT_BOOSTING_REGRESSOR"]:
            n_est = hp.get("n_estimators", 100)
            lr = hp.get("learning_rate", 0.05)
            max_d = hp.get("max_depth", 4)
            self.estimator = GradientBoostingRegressor(n_estimators=n_est, learning_rate=lr, max_depth=max_d, random_state=rs)
        elif algo == "PERSISTENCE_BASELINE":
            self.estimator = None
        elif algo == "ROLLING_HISTORICAL":
            self.estimator = None
            self.hist_mean_vol_ = 0.15
        else:
            raise ValueError(f"Unsupported volatility algorithm: {algo}")

    def fit(self, X: pd.DataFrame, y: Any) -> "VolatilityModel":
        self.feature_names = list(X.columns)
        X_clean = X.fillna(0.0)
        y_clean = pd.Series(y).fillna(0.15)

        if self.algorithm == "PERSISTENCE_BASELINE":
            pass
        elif self.algorithm == "ROLLING_HISTORICAL":
            self.hist_mean_vol_ = float(y_clean.mean())
        else:
            self.estimator.fit(X_clean, y_clean)
            if hasattr(self.estimator, "feature_importances_"):
                self.feature_importances_ = {
                    col: float(imp) for col, imp in zip(self.feature_names, self.estimator.feature_importances_)
                }

        self.is_fitted = True
        return self

    def predict(self, X: pd.DataFrame) -> np.ndarray:
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before predict()")

        X_clean = X.fillna(0.0)
        if self.algorithm == "PERSISTENCE_BASELINE":
            # Return past realized volatility feature if present, else 0.15
            for vol_col in ["VOLATILITY_20D", "volatility_20d", "realized_vol_20d"]:
                if vol_col in X_clean.columns:
                    return X_clean[vol_col].values * 0.01 if (X_clean[vol_col] > 1.0).any() else X_clean[vol_col].values
            return np.full(len(X_clean), 0.15)
        elif self.algorithm == "ROLLING_HISTORICAL":
            return np.full(len(X_clean), self.hist_mean_vol_)
        else:
            preds = self.estimator.predict(X_clean)
            return np.clip(preds, 0.01, 1.5)

    def explain(self, X: pd.DataFrame) -> List[Dict[str, float]]:
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before explain()")

        X_clean = X.fillna(0.0)
        top_feats = sorted(self.feature_importances_.items(), key=lambda x: x[1], reverse=True)[:5]
        explanations = []
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

        valid_mask = ~np.isnan(y_true) & (y_true > 0)
        if not valid_mask.any():
            return {"status": "NO_VALID_TARGETS"}

        y_true_v = y_true[valid_mask]
        y_pred_v = np.clip(y_pred[valid_mask], 1e-4, 2.0)

        mae = float(np.mean(np.abs(y_true_v - y_pred_v)))
        rmse = float(np.sqrt(np.mean((y_true_v - y_pred_v) ** 2)))

        # QLIKE Loss: mean(log(pred^2) + true^2 / pred^2)
        qlike = float(np.mean(np.log(y_pred_v ** 2) + (y_true_v ** 2) / (y_pred_v ** 2)))

        if np.std(y_pred_v) > 1e-8 and np.std(y_true_v) > 1e-8:
            corr = float(np.corrcoef(y_pred_v, y_true_v)[0, 1])
        else:
            corr = 0.0

        return {
            "observations": int(len(y_true_v)),
            "mae": round(mae, 6),
            "rmse": round(rmse, 6),
            "qlike": round(qlike, 4),
            "correlation": round(corr, 4),
            "top_features": sorted(self.feature_importances_.items(), key=lambda x: x[1], reverse=True)[:5]
        }
