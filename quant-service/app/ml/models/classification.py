"""Return Direction Classification Models for P(R_future > 0).
"""

from typing import Dict, Any, Optional, List
import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score,
    roc_auc_score, average_precision_score, brier_score_loss, log_loss
)

from app.ml.models.base import QuantPredictionModel


class ClassificationModel(QuantPredictionModel):
    """Production Return Direction Classification Model."""

    def __init__(
        self,
        model_id: str,
        algorithm: str = "GRADIENT_BOOSTING_CLASSIFIER",
        model_version: str = "CLS_1D_v1.0",
        target_definition: str = "future_return_1d > 0",
        target_horizon: str = "1D",
        hyperparameters: Optional[Dict[str, Any]] = None,
        random_seed: int = 42
    ):
        super().__init__(
            model_id=model_id,
            model_name=f"Direction Classifier ({algorithm})",
            model_type="CLASSIFICATION",
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

        if algo in ["LOGISTIC", "LOGISTIC_REGRESSION"]:
            c_val = hp.get("C", 1.0)
            self.estimator = LogisticRegression(C=c_val, max_iter=500, random_state=rs)
        elif algo in ["RANDOM_FOREST", "RANDOM_FOREST_CLASSIFIER"]:
            n_est = hp.get("n_estimators", 100)
            max_d = hp.get("max_depth", 6)
            min_leaf = hp.get("min_samples_leaf", 5)
            self.estimator = RandomForestClassifier(
                n_estimators=n_est, max_depth=max_d, min_samples_leaf=min_leaf, random_state=rs, n_jobs=-1
            )
        elif algo in ["GRADIENT_BOOSTING", "GRADIENT_BOOSTING_CLASSIFIER"]:
            n_est = hp.get("n_estimators", 100)
            lr = hp.get("learning_rate", 0.05)
            max_d = hp.get("max_depth", 4)
            subsample = hp.get("subsample", 0.8)
            self.estimator = GradientBoostingClassifier(
                n_estimators=n_est, learning_rate=lr, max_depth=max_d, subsample=subsample, random_state=rs
            )
        elif algo == "MAJORITY_BASELINE":
            self.estimator = None
            self.majority_class_ = 1
        else:
            raise ValueError(f"Unsupported classification algorithm: {algo}")

    def fit(self, X: pd.DataFrame, y: Any) -> "ClassificationModel":
        self.feature_names = list(X.columns)
        X_clean = X.fillna(0.0)
        y_clean = pd.Series(y).fillna(0).astype(int)

        if self.algorithm == "MAJORITY_BASELINE":
            counts = y_clean.value_counts()
            self.majority_class_ = int(counts.idxmax()) if not counts.empty else 1
        else:
            self.estimator.fit(X_clean, y_clean)

            if hasattr(self.estimator, "feature_importances_"):
                self.feature_importances_ = {
                    col: float(imp) for col, imp in zip(self.feature_names, self.estimator.feature_importances_)
                }
            elif hasattr(self.estimator, "coef_"):
                self.feature_importances_ = {
                    col: float(abs(c)) for col, c in zip(self.feature_names, self.estimator.coef_[0])
                }

        self.is_fitted = True
        return self

    def predict(self, X: pd.DataFrame) -> np.ndarray:
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before predict()")

        X_clean = X.fillna(0.0)
        if self.algorithm == "MAJORITY_BASELINE":
            return np.full(len(X_clean), self.majority_class_)
        else:
            return self.estimator.predict(X_clean)

    def predict_proba(self, X: pd.DataFrame) -> np.ndarray:
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before predict_proba()")

        X_clean = X.fillna(0.0)
        if self.algorithm == "MAJORITY_BASELINE":
            probs = np.zeros((len(X_clean), 2))
            probs[:, self.majority_class_] = 1.0
            probs[:, 1 - self.majority_class_] = 0.0
            return probs
        else:
            return self.estimator.predict_proba(X_clean)

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
        y_prob = self.predict_proba(X)[:, 1]
        y_true = np.asarray(y).astype(int)

        acc = float(accuracy_score(y_true, y_pred))
        prec = float(precision_score(y_true, y_pred, zero_division=0))
        rec = float(recall_score(y_true, y_pred, zero_division=0))
        f1 = float(f1_score(y_true, y_pred, zero_division=0))

        try:
            roc_auc = float(roc_auc_score(y_true, y_prob)) if len(np.unique(y_true)) > 1 else 0.5
            pr_auc = float(average_precision_score(y_true, y_prob)) if len(np.unique(y_true)) > 1 else 0.5
            brier = float(brier_score_loss(y_true, y_prob))
            loss = float(log_loss(y_true, y_prob))
        except Exception:
            roc_auc = 0.5
            pr_auc = 0.5
            brier = 0.25
            loss = 0.693

        return {
            "observations": int(len(y_true)),
            "accuracy": round(acc, 4),
            "precision": round(prec, 4),
            "recall": round(rec, 4),
            "f1": round(f1, 4),
            "roc_auc": round(roc_auc, 4),
            "pr_auc": round(pr_auc, 4),
            "brier_score": round(brier, 4),
            "log_loss": round(loss, 4),
            "top_features": sorted(self.feature_importances_.items(), key=lambda x: x[1], reverse=True)[:5]
        }
