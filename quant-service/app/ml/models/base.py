"""Base abstraction for Quant Prediction Models.
"""

from abc import ABC, abstractmethod
from typing import Dict, Any, Optional, List, Tuple
import numpy as np
import pandas as pd
import joblib


class QuantPredictionModel(ABC):
    """Abstract Base Class for all Quant Prediction Models."""

    def __init__(
        self,
        model_id: str,
        model_name: str,
        model_type: str,
        algorithm: str,
        model_version: str = "v1.0.0",
        feature_version: str = "1.0.0",
        dataset_version: str = "DS_v1.0",
        target_definition: str = "future_return_1d",
        target_horizon: str = "1D",
        hyperparameters: Optional[Dict[str, Any]] = None,
        random_seed: int = 42
    ):
        self.model_id = model_id
        self.model_name = model_name
        self.model_type = model_type  # REGRESSION, CLASSIFICATION, VOLATILITY
        self.algorithm = algorithm
        self.model_version = model_version
        self.feature_version = feature_version
        self.dataset_version = dataset_version
        self.target_definition = target_definition
        self.target_horizon = target_horizon
        self.hyperparameters = hyperparameters or {}
        self.random_seed = random_seed
        self.is_fitted = False
        self.feature_names: List[str] = []
        self.feature_importances_: Dict[str, float] = {}

    @abstractmethod
    def fit(self, X: pd.DataFrame, y: pd.Series) -> "QuantPredictionModel":
        """Fits model on training data."""
        pass

    @abstractmethod
    def predict(self, X: pd.DataFrame) -> np.ndarray:
        """Generates point predictions."""
        pass

    def predict_proba(self, X: pd.DataFrame) -> Optional[np.ndarray]:
        """Generates prediction probabilities (for classification models)."""
        return None

    @abstractmethod
    def explain(self, X: pd.DataFrame) -> List[Dict[str, float]]:
        """Generates feature contributions for predictions."""
        pass

    @abstractmethod
    def evaluate(self, X: pd.DataFrame, y: pd.Series) -> Dict[str, Any]:
        """Calculates model metrics on provided test/validation dataset."""
        pass

    def save(self, filepath: str) -> None:
        """Serializes model artifact to disk."""
        joblib.dump(self, filepath)

    @classmethod
    def load(cls, filepath: str) -> "QuantPredictionModel":
        """Loads model artifact from disk."""
        model = joblib.load(filepath)
        if not isinstance(model, QuantPredictionModel):
            raise ValueError("Loaded object is not an instance of QuantPredictionModel")
        return model
