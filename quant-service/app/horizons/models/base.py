"""Base HorizonModel abstraction for trading and investing horizons."""

from abc import ABC, abstractmethod
from typing import Dict, Any, Optional
import numpy as np
import pandas as pd
from datetime import datetime

from app.horizons.schemas import TradingHorizon, ModelStatus, ModelAlgorithm, TargetType


class HorizonModel(ABC):
    """Abstract base class for independent horizon models."""

    def __init__(
        self,
        model_id: str,
        model_version: str,
        horizon: TradingHorizon,
        target_period: str,
        target_type: TargetType,
        algorithm: ModelAlgorithm,
        feature_set_version: str,
        target_set_version: str,
        hyperparameters: Optional[Dict[str, Any]] = None
    ):
        self.model_id = model_id
        self.model_version = model_version
        self.horizon = horizon
        self.target_period = target_period
        self.target_type = target_type
        self.algorithm = algorithm
        self.feature_set_version = feature_set_version
        self.target_set_version = target_set_version
        self.hyperparameters = hyperparameters or {}
        self.status = ModelStatus.CANDIDATE
        self.is_fitted = False
        self.model = None
        self.scaler = None
        self.feature_names: list = []
        self.model_availability_timestamp: Optional[datetime] = None

    @abstractmethod
    def fit(self, X: pd.DataFrame, y: pd.Series, available_timestamp: Optional[datetime] = None):
        """Fit model strictly on training data."""
        pass

    @abstractmethod
    def predict(self, X: pd.DataFrame) -> np.ndarray:
        """Generate predictions."""
        pass

    @abstractmethod
    def predict_proba(self, X: pd.DataFrame) -> Optional[np.ndarray]:
        """Generate class probabilities for classification models."""
        pass

    def get_feature_importance(self) -> Dict[str, float]:
        """Return normalized feature importances if supported."""
        if not self.is_fitted or self.model is None:
            return {}
        if hasattr(self.model, "feature_importances_"):
            importances = self.model.feature_importances_
            total = np.sum(importances)
            if total > 0:
                importances = importances / total
            return {name: float(imp) for name, imp in zip(self.feature_names, importances)}
        elif hasattr(self.model, "coef_"):
            coefs = np.abs(self.model.coef_).ravel()
            total = np.sum(coefs)
            if total > 0:
                coefs = coefs / total
            return {name: float(c) for name, c in zip(self.feature_names, coefs)}
        return {}
