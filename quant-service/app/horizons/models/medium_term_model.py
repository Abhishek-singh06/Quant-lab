"""Medium-Term Trading Model Implementation."""

from typing import Dict, Any, Optional
import numpy as np
import pandas as pd
from datetime import datetime
from sklearn.linear_model import Ridge, ElasticNet
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.preprocessing import StandardScaler

from app.horizons.schemas import TradingHorizon, ModelAlgorithm, TargetType, ModelStatus
from app.horizons.models.base import HorizonModel


class MediumTermModel(HorizonModel):
    """Medium-Term Trading Model for 1-12 Week horizons."""

    def __init__(
        self,
        model_id: str = "MEDIUM_TERM_GB_v1.0",
        model_version: str = "MT_v1.0.0",
        target_period: str = "FOUR_WEEKS",
        target_type: TargetType = TargetType.REGRESSION,
        algorithm: ModelAlgorithm = ModelAlgorithm.GRADIENT_BOOSTING,
        feature_set_version: str = "MEDIUM_TERM_FEATURE_SET_V1",
        target_set_version: str = "MEDIUM_TERM_TARGET_SET_V1",
        hyperparameters: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            model_id=model_id,
            model_version=model_version,
            horizon=TradingHorizon.MEDIUM_TERM,
            target_period=target_period,
            target_type=target_type,
            algorithm=algorithm,
            feature_set_version=feature_set_version,
            target_set_version=target_set_version,
            hyperparameters=hyperparameters
        )
        self._init_algorithm()

    def _init_algorithm(self):
        params = self.hyperparameters or {}
        if self.algorithm == ModelAlgorithm.RIDGE:
            alpha = params.get("alpha", 1.0)
            self.model = Ridge(alpha=alpha, random_state=42)
        elif self.algorithm == ModelAlgorithm.ELASTIC_NET:
            alpha = params.get("alpha", 0.5)
            l1_ratio = params.get("l1_ratio", 0.5)
            self.model = ElasticNet(alpha=alpha, l1_ratio=l1_ratio, random_state=42)
        elif self.algorithm == ModelAlgorithm.RANDOM_FOREST:
            n_estimators = params.get("n_estimators", 50)
            max_depth = params.get("max_depth", 4)
            self.model = RandomForestRegressor(n_estimators=n_estimators, max_depth=max_depth, random_state=42)
        else:
            n_estimators = params.get("n_estimators", 50)
            learning_rate = params.get("learning_rate", 0.05)
            max_depth = params.get("max_depth", 3)
            self.model = GradientBoostingRegressor(
                n_estimators=n_estimators,
                learning_rate=learning_rate,
                max_depth=max_depth,
                random_state=42
            )

    def fit(self, X: pd.DataFrame, y: pd.Series, available_timestamp: Optional[datetime] = None):
        self.feature_names = list(X.columns)
        self.scaler = StandardScaler()
        X_scaled = self.scaler.fit_transform(X.fillna(0.0))
        self.model.fit(X_scaled, y)
        self.is_fitted = True
        self.model_availability_timestamp = available_timestamp or datetime.utcnow()
        self.status = ModelStatus.VALIDATED

    def predict(self, X: pd.DataFrame) -> np.ndarray:
        if not self.is_fitted:
            raise RuntimeError("Model is not fitted.")
        X_scaled = self.scaler.transform(X[self.feature_names].fillna(0.0))
        return self.model.predict(X_scaled)

    def predict_proba(self, X: pd.DataFrame) -> Optional[np.ndarray]:
        return None
