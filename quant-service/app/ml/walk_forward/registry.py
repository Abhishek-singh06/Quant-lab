"""Model Registry and Artifact Persistence Manager.
"""

import os
from typing import Dict, Any, Optional, List
import joblib
from app.ml.models.base import QuantPredictionModel


class LocalModelRegistry:
    """Manages local serialization and retrieval of model artifacts."""

    def __init__(self, base_dir: str = "artifacts/models"):
        self.base_dir = base_dir
        os.makedirs(self.base_dir, exist_ok=True)
        self.registry: Dict[str, Dict[str, Any]] = {}

    def save_model(self, model: QuantPredictionModel, fold_id: Optional[str] = None) -> str:
        """Saves model to disk and registers metadata."""
        sub_dir = os.path.join(self.base_dir, fold_id) if fold_id else self.base_dir
        os.makedirs(sub_dir, exist_ok=True)
        filepath = os.path.join(sub_dir, f"{model.model_id}.joblib")
        model.save(filepath)

        self.registry[model.model_id] = {
            "model_id": model.model_id,
            "model_version": model.model_version,
            "model_type": model.model_type,
            "algorithm": model.algorithm,
            "filepath": filepath,
            "target_definition": model.target_definition,
            "target_horizon": model.target_horizon,
            "hyperparameters": model.hyperparameters,
            "feature_importances": model.feature_importances_
        }
        return filepath

    def load_model(self, model_id: str) -> Optional[QuantPredictionModel]:
        if model_id in self.registry:
            path = self.registry[model_id]["filepath"]
            if os.path.exists(path):
                return QuantPredictionModel.load(path)
        return None
