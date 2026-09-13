"""Deep Learning Model Adapter for QuantLab Model Registry.

Bridges temporal deep learning sequence architectures (GRU, LSTM, 1D-CNN, Attention)
into QuantLab's canonical QuantPredictionModel interface with full serialization,
Point-In-Time scaling, explainability, and SHA-256 cryptographic provenance.
"""

from typing import Dict, Any, Optional, List, Tuple, Union
import hashlib
import json
import numpy as np
import pandas as pd

from app.ml.models.base import QuantPredictionModel
from app.deep_learning.scaler import PointInTimeScaler
from app.deep_learning.sequence_generator import TemporalSequenceGenerator, SequenceDataset
from app.deep_learning.models import (
    BaseTemporalDLModel,
    TemporalGRUModel,
    TemporalLSTMModel,
    Temporal1DCNNModel,
    TemporalAttentionModel,
)


class DeepLearningModelAdapter(QuantPredictionModel):
    """Canonical QuantPredictionModel wrapper for Deep Learning sequence models."""

    def __init__(
        self,
        model_type_name: str = "GRU",  # "GRU", "LSTM", "1DCNN", "ATTENTION"
        model_id: str = "dl_sequence_model",
        lookback: int = 60,
        horizon: int = 1,
        hidden_dim: int = 32,
        learning_rate: float = 0.005,
        epochs: int = 20,
        batch_size: int = 32,
        scaling_method: str = "standard",
        target_definition: str = "future_return_1d",
        target_horizon: str = "1D",
        model_version: str = "v1.0.0",
        feature_version: str = "TemporalSeq_v1.0",
        dataset_version: str = "DS_DL_v1.0",
        random_seed: int = 42,
    ):
        super().__init__(
            model_id=model_id,
            model_name=f"DL_{model_type_name}_{lookback}B",
            model_type="REGRESSION",
            algorithm=f"Temporal_{model_type_name}",
            model_version=model_version,
            feature_version=feature_version,
            dataset_version=dataset_version,
            target_definition=target_definition,
            target_horizon=target_horizon,
            hyperparameters={
                "model_type_name": model_type_name,
                "lookback": lookback,
                "horizon": horizon,
                "hidden_dim": hidden_dim,
                "learning_rate": learning_rate,
                "epochs": epochs,
                "batch_size": batch_size,
                "scaling_method": scaling_method,
            },
            random_seed=random_seed,
        )
        self.model_type_name = model_type_name.upper()
        self.lookback = lookback
        self.horizon = horizon
        self.hidden_dim = hidden_dim
        self.learning_rate = learning_rate
        self.epochs = epochs
        self.batch_size = batch_size
        self.scaling_method = scaling_method

        self.scaler = PointInTimeScaler(method=scaling_method)
        self.seq_gen = TemporalSequenceGenerator(lookback=lookback, horizon=horizon)
        self.dl_model: BaseTemporalDLModel = self._create_dl_model()
        self.provenance_hash: Optional[str] = None

    def _create_dl_model(self) -> BaseTemporalDLModel:
        if self.model_type_name == "GRU":
            return TemporalGRUModel(
                hidden_dim=self.hidden_dim,
                learning_rate=self.learning_rate,
                epochs=self.epochs,
                batch_size=self.batch_size,
                random_seed=self.random_seed,
            )
        elif self.model_type_name == "LSTM":
            return TemporalLSTMModel(
                hidden_dim=self.hidden_dim,
                learning_rate=self.learning_rate,
                epochs=self.epochs,
                batch_size=self.batch_size,
                random_seed=self.random_seed,
            )
        elif self.model_type_name in ("1DCNN", "CNN"):
            return Temporal1DCNNModel(
                num_filters=self.hidden_dim,
                learning_rate=self.learning_rate,
                epochs=self.epochs,
                batch_size=self.batch_size,
                random_seed=self.random_seed,
            )
        elif self.model_type_name in ("ATTENTION", "TRANSFORMER"):
            return TemporalAttentionModel(
                attn_dim=self.hidden_dim,
                learning_rate=self.learning_rate,
                epochs=self.epochs,
                batch_size=self.batch_size,
                random_seed=self.random_seed,
            )
        else:
            raise ValueError(f"Unsupported deep learning model type: {self.model_type_name}")

    def fit_sequence_dataset(self, dataset: SequenceDataset) -> "DeepLearningModelAdapter":
        """Fits model directly on a SequenceDataset."""
        self.feature_names = dataset.feature_names
        # Fit scaler strictly on training sequence X
        X_scaled = self.scaler.fit_transform(dataset.X)
        self.dl_model.fit(X_scaled, dataset.y)
        self.is_fitted = True
        self._compute_provenance()
        return self

    def fit(self, X: pd.DataFrame, y: Optional[pd.Series] = None) -> "DeepLearningModelAdapter":
        """Fits model from a panel DataFrame."""
        if y is not None:
            # Attach y to X for safe sequence slicing
            df = X.copy()
            df["target"] = y.values
        else:
            df = X.copy()

        seq_dataset = self.seq_gen.generate(df)
        return self.fit_sequence_dataset(seq_dataset)

    def predict(self, X: Union[pd.DataFrame, np.ndarray, SequenceDataset]) -> np.ndarray:
        """Generates predictions from DataFrame, 3D array, or SequenceDataset."""
        if not self.is_fitted:
            raise RuntimeError("DeepLearningModelAdapter is not fitted.")

        if isinstance(X, SequenceDataset):
            X_scaled = self.scaler.transform(X.X)
            return self.dl_model.predict(X_scaled)

        elif isinstance(X, np.ndarray) and X.ndim == 3:
            X_scaled = self.scaler.transform(X)
            return self.dl_model.predict(X_scaled)

        elif isinstance(X, pd.DataFrame):
            seq_dataset = self.seq_gen.generate(X)
            X_scaled = self.scaler.transform(seq_dataset.X)
            return self.dl_model.predict(X_scaled)

        elif isinstance(X, np.ndarray) and X.ndim == 2:
            # 2D input: construct synthetic single-step or repeated sequence if necessary
            N, D = X.shape
            X_3d = np.repeat(X[:, np.newaxis, :], self.lookback, axis=1)
            X_scaled = self.scaler.transform(X_3d)
            return self.dl_model.predict(X_scaled)

        else:
            raise ValueError(f"Unsupported input type: {type(X)}")

    def explain(self, X: Union[pd.DataFrame, np.ndarray, SequenceDataset]) -> List[Dict[str, float]]:
        """Extracts temporal attention weights and feature contributions."""
        if not self.is_fitted:
            raise RuntimeError("Model must be fitted before explain().")

        if isinstance(X, SequenceDataset):
            X_arr = X.X
        elif isinstance(X, np.ndarray) and X.ndim == 3:
            X_arr = X
        elif isinstance(X, pd.DataFrame):
            seq_dataset = self.seq_gen.generate(X)
            X_arr = seq_dataset.X
        else:
            return [{}]

        X_scaled = self.scaler.transform(X_arr)
        step_attn = self.dl_model.get_temporal_attention(X_scaled)  # (N, T)

        # Average temporal relevance across steps (e.g. t-59 to t-0)
        mean_attn = np.mean(step_attn, axis=0)  # (T,)
        explanations = []
        for i in range(len(X_arr)):
            sample_attn = {f"lookback_lag_{self.lookback - t - 1}": float(step_attn[i, t]) for t in range(self.lookback)}
            explanations.append(sample_attn)

        return explanations

    def evaluate(self, X: Union[pd.DataFrame, SequenceDataset], y: Optional[pd.Series] = None) -> Dict[str, Any]:
        """Calculates institutional quantitative metrics: IC, Rank IC, MAE, RMSE, Directional Accuracy."""
        if isinstance(X, SequenceDataset):
            preds = self.predict(X)
            targets = X.y
        else:
            preds = self.predict(X)
            targets = y.values if y is not None else np.zeros_like(preds)

        # Truncate to matching length
        n = min(len(preds), len(targets))
        p = preds[:n]
        t = targets[:n]

        mae = float(np.mean(np.abs(p - t)))
        rmse = float(np.sqrt(np.mean((p - t) ** 2)))

        # Directional Accuracy (Sign Match)
        dir_acc = float(np.mean(np.sign(p) == np.sign(t))) if n > 0 else 0.0

        # Information Coefficient (Pearson Correlation)
        std_p = np.std(p)
        std_t = np.std(t)
        if std_p > 1e-8 and std_t > 1e-8:
            ic = float(np.corrcoef(p, t)[0, 1])
        else:
            ic = 0.0

        # Rank IC (Spearman Correlation via rank order)
        rank_p = pd.Series(p).rank().values
        rank_t = pd.Series(t).rank().values
        std_rp = np.std(rank_p)
        std_rt = np.std(rank_t)
        if std_rp > 1e-8 and std_rt > 1e-8:
            rank_ic = float(np.corrcoef(rank_p, rank_t)[0, 1])
        else:
            rank_ic = 0.0

        return {
            "mae": round(mae, 6),
            "rmse": round(rmse, 6),
            "directional_accuracy": round(dir_acc, 4),
            "ic": round(ic, 4),
            "rank_ic": round(rank_ic, 4),
            "samples": n,
        }

    def _compute_provenance(self):
        """Generates SHA-256 cryptographic provenance hash for model auditability."""
        provenance_dict = {
            "model_id": self.model_id,
            "algorithm": self.algorithm,
            "hyperparameters": self.hyperparameters,
            "random_seed": self.random_seed,
            "scaler": self.scaler.to_dict(),
            "n_features": len(self.feature_names),
        }
        raw_str = json.dumps(provenance_dict, sort_keys=True)
        self.provenance_hash = hashlib.sha256(raw_str.encode("utf-8")).hexdigest()
