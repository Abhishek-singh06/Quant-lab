"""Qlib Model Adapters and QuantLab Model Registry Interoperability.

Implements Qlib-compatible model wrappers (GBDT, Linear, Random Forest, Ensemble)
and seamless bidirectional conversion to QuantLab's canonical QuantPredictionModel.
"""

from typing import Dict, Any, Optional, List, Tuple, Union
from abc import ABC, abstractmethod
import numpy as np
import pandas as pd
from sklearn.ensemble import HistGradientBoostingRegressor, RandomForestRegressor, GradientBoostingRegressor
from sklearn.linear_model import Ridge, Lasso, ElasticNet, LinearRegression
import joblib

from app.qlib.dataset import QuantLabDatasetH
from app.ml.models.base import QuantPredictionModel


class QlibModel(ABC):
    """Abstract Base Class for Qlib Research Models."""

    def __init__(self, model_id: str, hyperparameters: Optional[Dict[str, Any]] = None):
        self.model_id = model_id
        self.hyperparameters = hyperparameters or {}
        self.is_fitted = False
        self.feature_names: List[str] = []
        self.fitted_model: Any = None

    @abstractmethod
    def fit(self, dataset: QuantLabDatasetH) -> "QlibModel":
        """Fits model on the training segment of the dataset."""
        pass

    @abstractmethod
    def predict(self, dataset: QuantLabDatasetH, segment: str = "test") -> pd.Series:
        """Generates predictions for the specified dataset segment."""
        pass

    def get_feature_importances(self) -> Dict[str, float]:
        """Returns feature importance mapping if supported."""
        return {}


class QlibGBDTModel(QlibModel):
    """Gradient Boosted Decision Tree model for Qlib research."""

    def __init__(self, model_id: str = "qlib_gbdt", hyperparameters: Optional[Dict[str, Any]] = None):
        super().__init__(model_id, hyperparameters)
        learning_rate = self.hyperparameters.get("learning_rate", 0.05)
        max_iter = self.hyperparameters.get("max_iter", 100)
        max_depth = self.hyperparameters.get("max_depth", 6)
        min_samples_leaf = self.hyperparameters.get("min_samples_leaf", 20)
        l2_reg = self.hyperparameters.get("l2_regularization", 0.1)
        random_state = self.hyperparameters.get("random_state", 42)

        self.fitted_model = HistGradientBoostingRegressor(
            learning_rate=learning_rate,
            max_iter=max_iter,
            max_depth=max_depth,
            min_samples_leaf=min_samples_leaf,
            l2_regularization=l2_reg,
            random_state=random_state,
        )

    def fit(self, dataset: QuantLabDatasetH) -> "QlibGBDTModel":
        X_train, y_train = dataset.prepare("train", col_set=["feature", "label"])
        self.feature_names = list(X_train.columns)
        X_clean = np.nan_to_num(X_train.values, nan=0.0, posinf=0.0, neginf=0.0)
        y_clean = np.nan_to_num(y_train.values, nan=0.0, posinf=0.0, neginf=0.0)
        self.fitted_model.fit(X_clean, y_clean)
        self.is_fitted = True
        return self

    def predict(self, dataset: QuantLabDatasetH, segment: str = "test") -> pd.Series:
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before predict().")
        X = dataset.prepare(segment, col_set="feature")
        X_clean = np.nan_to_num(X.values, nan=0.0, posinf=0.0, neginf=0.0)
        preds = self.fitted_model.predict(X_clean)
        return pd.Series(preds, index=X.index, name="score")


class QlibLinearModel(QlibModel):
    """Linear / Ridge / Lasso regression model for Qlib research."""

    def __init__(self, model_id: str = "qlib_linear", hyperparameters: Optional[Dict[str, Any]] = None):
        super().__init__(model_id, hyperparameters)
        model_kind = self.hyperparameters.get("kind", "ridge").lower()
        alpha = self.hyperparameters.get("alpha", 1.0)
        random_state = self.hyperparameters.get("random_state", 42)

        if model_kind == "lasso":
            self.fitted_model = Lasso(alpha=alpha, random_state=random_state)
        elif model_kind == "elasticnet":
            l1_ratio = self.hyperparameters.get("l1_ratio", 0.5)
            self.fitted_model = ElasticNet(alpha=alpha, l1_ratio=l1_ratio, random_state=random_state)
        elif model_kind == "ridge":
            self.fitted_model = Ridge(alpha=alpha, random_state=random_state)
        else:
            self.fitted_model = LinearRegression()

    def fit(self, dataset: QuantLabDatasetH) -> "QlibLinearModel":
        X_train, y_train = dataset.prepare("train", col_set=["feature", "label"])
        self.feature_names = list(X_train.columns)
        X_clean = np.nan_to_num(X_train.values, nan=0.0, posinf=0.0, neginf=0.0)
        y_clean = np.nan_to_num(y_train.values, nan=0.0, posinf=0.0, neginf=0.0)
        self.fitted_model.fit(X_clean, y_clean)
        self.is_fitted = True
        return self

    def predict(self, dataset: QuantLabDatasetH, segment: str = "test") -> pd.Series:
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before predict().")
        X = dataset.prepare(segment, col_set="feature")
        X_clean = np.nan_to_num(X.values, nan=0.0, posinf=0.0, neginf=0.0)
        preds = self.fitted_model.predict(X_clean)
        return pd.Series(preds, index=X.index, name="score")


class QlibRandomForestModel(QlibModel):
    """Random Forest regressor model for Qlib research."""

    def __init__(self, model_id: str = "qlib_rf", hyperparameters: Optional[Dict[str, Any]] = None):
        super().__init__(model_id, hyperparameters)
        n_estimators = self.hyperparameters.get("n_estimators", 100)
        max_depth = self.hyperparameters.get("max_depth", 8)
        min_samples_leaf = self.hyperparameters.get("min_samples_leaf", 10)
        random_state = self.hyperparameters.get("random_state", 42)

        self.fitted_model = RandomForestRegressor(
            n_estimators=n_estimators,
            max_depth=max_depth,
            min_samples_leaf=min_samples_leaf,
            random_state=random_state,
            n_jobs=-1,
        )

    def fit(self, dataset: QuantLabDatasetH) -> "QlibRandomForestModel":
        X_train, y_train = dataset.prepare("train", col_set=["feature", "label"])
        self.feature_names = list(X_train.columns)
        X_clean = np.nan_to_num(X_train.values, nan=0.0, posinf=0.0, neginf=0.0)
        y_clean = np.nan_to_num(y_train.values, nan=0.0, posinf=0.0, neginf=0.0)
        self.fitted_model.fit(X_clean, y_clean)
        self.is_fitted = True
        return self

    def predict(self, dataset: QuantLabDatasetH, segment: str = "test") -> pd.Series:
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before predict().")
        X = dataset.prepare(segment, col_set="feature")
        X_clean = np.nan_to_num(X.values, nan=0.0, posinf=0.0, neginf=0.0)
        preds = self.fitted_model.predict(X_clean)
        return pd.Series(preds, index=X.index, name="score")

    def get_feature_importances(self) -> Dict[str, float]:
        if not self.is_fitted or not hasattr(self.fitted_model, "feature_importances_"):
            return {}
        return dict(zip(self.feature_names, [float(v) for v in self.fitted_model.feature_importances_]))


class QlibEnsembleModel(QlibModel):
    """Weighted ensemble of multiple Qlib models."""

    def __init__(
        self,
        model_id: str = "qlib_ensemble",
        models: Optional[List[Tuple[QlibModel, float]]] = None,
    ):
        super().__init__(model_id)
        self.models = models or []

    def fit(self, dataset: QuantLabDatasetH) -> "QlibEnsembleModel":
        for model, _ in self.models:
            model.fit(dataset)
        self.is_fitted = True
        if self.models:
            self.feature_names = self.models[0][0].feature_names
        return self

    def predict(self, dataset: QuantLabDatasetH, segment: str = "test") -> pd.Series:
        if not self.is_fitted or not self.models:
            raise RuntimeError("Ensemble must be fitted with at least one model.")
        
        total_weight = sum(w for _, w in self.models)
        ensemble_preds = None

        for model, weight in self.models:
            p = model.predict(dataset, segment=segment)
            normalized_w = weight / (total_weight + 1e-12)
            if ensemble_preds is None:
                ensemble_preds = p * normalized_w
            else:
                ensemble_preds += p * normalized_w

        ensemble_preds.name = "score"
        return ensemble_preds


# ---------------------------------------------------------------------------
# Bridge Adapter to QuantLab Canonical QuantPredictionModel
# ---------------------------------------------------------------------------

class QuantLabQlibModelBridge(QuantPredictionModel):
    """Wraps a QlibModel into QuantLab's canonical QuantPredictionModel."""

    def __init__(
        self,
        qlib_model: QlibModel,
        model_id: str,
        target_definition: str = "future_return_1d",
        target_horizon: str = "1D",
        model_version: str = "v1.0.0",
        feature_version: str = "Alpha158_v1.0",
        dataset_version: str = "QLIB_v1.0",
    ):
        super().__init__(
            model_id=model_id,
            model_name=f"Qlib_{qlib_model.model_id}",
            model_type="REGRESSION",
            algorithm=qlib_model.__class__.__name__,
            model_version=model_version,
            feature_version=feature_version,
            dataset_version=dataset_version,
            target_definition=target_definition,
            target_horizon=target_horizon,
            hyperparameters=qlib_model.hyperparameters,
        )
        self.qlib_model = qlib_model

    def fit(self, X: pd.DataFrame, y: pd.Series) -> "QuantLabQlibModelBridge":
        # Package into single-segment dataset
        segments_data = {"train": (X, y, pd.DataFrame(index=X.index))}
        dataset = QuantLabDatasetH(segments_data=segments_data, feature_names=list(X.columns))
        self.qlib_model.fit(dataset)
        self.is_fitted = True
        self.feature_names = list(X.columns)
        self.feature_importances_ = self.qlib_model.get_feature_importances()
        return self

    def predict(self, X: pd.DataFrame) -> np.ndarray:
        if not self.is_fitted:
            raise RuntimeError("Model is not fitted.")
        dummy_y = pd.Series(0.0, index=X.index)
        segments_data = {"test": (X, dummy_y, pd.DataFrame(index=X.index))}
        dataset = QuantLabDatasetH(segments_data=segments_data, feature_names=list(X.columns))
        preds = self.qlib_model.predict(dataset, segment="test")
        return preds.values

    def explain(self, X: pd.DataFrame) -> List[Dict[str, float]]:
        """Returns feature importances for predictions."""
        importances = self.qlib_model.get_feature_importances()
        return [importances for _ in range(len(X))]

    def evaluate(self, X: pd.DataFrame, y: pd.Series) -> Dict[str, Any]:
        preds = self.predict(X)
        mae = float(np.mean(np.abs(preds - y.values)))
        rmse = float(np.sqrt(np.mean((preds - y.values) ** 2)))
        ss_res = np.sum((y.values - preds) ** 2)
        ss_tot = np.sum((y.values - np.mean(y.values)) ** 2)
        r2 = float(1.0 - (ss_res / (ss_tot + 1e-12)))
        return {"mae": mae, "rmse": rmse, "r2": r2}
