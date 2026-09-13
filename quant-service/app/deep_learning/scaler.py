"""Point-In-Time Feature Scaler for Financial Time Series.

Guarantees zero future look-ahead by strictly computing normalization statistics
(mean, std, median, IQR, min, max) on training partitions only, then freezing them
for out-of-sample validation and production inference.
"""

from typing import Dict, Any, Optional, Union
import numpy as np


class PointInTimeScaler:
    """Scaler ensuring point-in-time safety across 2D (N, D) and 3D (N, T, D) financial tensors."""

    def __init__(self, method: str = "standard", clip_outliers: float = 5.0):
        """
        Args:
            method: 'standard' (mean/std), 'robust' (median/IQR), or 'minmax'
            clip_outliers: Max absolute z-score/iqr-score before clipping (e.g. 5.0 to suppress extreme spikes)
        """
        if method not in ("standard", "robust", "minmax"):
            raise ValueError(f"Unknown scaling method: {method}. Must be 'standard', 'robust', or 'minmax'.")
        self.method = method
        self.clip_outliers = clip_outliers
        self.is_fitted = False

        # Fitted statistics per feature (D-dimensional vectors)
        self.center_: Optional[np.ndarray] = None
        self.scale_: Optional[np.ndarray] = None
        self.min_: Optional[np.ndarray] = None
        self.max_: Optional[np.ndarray] = None
        self.n_features_: int = 0

    def fit(self, X: np.ndarray) -> "PointInTimeScaler":
        """Computes scaling parameters from training data X.

        Args:
            X: Array of shape (N, D) or (N, T, D)
        """
        X_arr = np.asarray(X, dtype=np.float64)
        if X_arr.ndim == 2:
            # Shape (N, D)
            N, D = X_arr.shape
            flat_X = X_arr
        elif X_arr.ndim == 3:
            # Shape (N, T, D) -> flatten time and batch dimensions for feature statistics
            N, T, D = X_arr.shape
            flat_X = X_arr.reshape(-1, D)
        else:
            raise ValueError(f"Expected 2D or 3D array, got shape {X_arr.shape}")

        self.n_features_ = D

        # Replace any NaNs/Infs with 0 before computing statistics
        clean_X = np.nan_to_num(flat_X, nan=0.0, posinf=0.0, neginf=0.0)

        if self.method == "standard":
            self.center_ = np.mean(clean_X, axis=0)
            std = np.std(clean_X, axis=0)
            self.scale_ = np.where(std < 1e-8, 1.0, std)

        elif self.method == "robust":
            self.center_ = np.median(clean_X, axis=0)
            q75 = np.percentile(clean_X, 75, axis=0)
            q25 = np.percentile(clean_X, 25, axis=0)
            iqr = q75 - q25
            self.scale_ = np.where(iqr < 1e-8, 1.0, iqr)

        elif self.method == "minmax":
            self.min_ = np.min(clean_X, axis=0)
            self.max_ = np.max(clean_X, axis=0)
            diff = self.max_ - self.min_
            self.scale_ = np.where(diff < 1e-8, 1.0, diff)
            self.center_ = self.min_

        self.is_fitted = True
        return self

    def transform(self, X: np.ndarray) -> np.ndarray:
        """Transforms X using fitted parameters.

        Args:
            X: Array of shape (N, D) or (N, T, D)
        """
        if not self.is_fitted:
            raise RuntimeError("PointInTimeScaler must be fitted before transform().")

        X_arr = np.asarray(X, dtype=np.float64)
        clean_X = np.nan_to_num(X_arr, nan=0.0, posinf=0.0, neginf=0.0)

        if clean_X.shape[-1] != self.n_features_:
            raise ValueError(
                f"Feature dimension mismatch: expected {self.n_features_}, got {clean_X.shape[-1]}"
            )

        if self.method in ("standard", "robust"):
            scaled = (clean_X - self.center_) / self.scale_
            if self.clip_outliers and self.clip_outliers > 0:
                scaled = np.clip(scaled, -self.clip_outliers, self.clip_outliers)
            return scaled

        elif self.method == "minmax":
            scaled = (clean_X - self.min_) / self.scale_
            return np.clip(scaled, 0.0, 1.0)

        return clean_X

    def fit_transform(self, X: np.ndarray) -> np.ndarray:
        """Fits to X and transforms X."""
        return self.fit(X).transform(X)

    def inverse_transform(self, X: np.ndarray) -> np.ndarray:
        """Reverts scaling on X."""
        if not self.is_fitted:
            raise RuntimeError("PointInTimeScaler must be fitted before inverse_transform().")

        X_arr = np.asarray(X, dtype=np.float64)
        if self.method in ("standard", "robust"):
            return X_arr * self.scale_ + self.center_
        elif self.method == "minmax":
            return X_arr * self.scale_ + self.min_
        return X_arr

    def to_dict(self) -> Dict[str, Any]:
        """Serializes scaler state for persistence."""
        return {
            "method": self.method,
            "clip_outliers": self.clip_outliers,
            "is_fitted": self.is_fitted,
            "n_features_": self.n_features_,
            "center_": self.center_.tolist() if self.center_ is not None else None,
            "scale_": self.scale_.tolist() if self.scale_ is not None else None,
            "min_": self.min_.tolist() if self.min_ is not None else None,
            "max_": self.max_.tolist() if self.max_ is not None else None,
        }

    @classmethod
    def from_dict(cls, data: Dict[str, Any]) -> "PointInTimeScaler":
        """Deserializes scaler from dictionary."""
        scaler = cls(method=data["method"], clip_outliers=data.get("clip_outliers", 5.0))
        scaler.is_fitted = data["is_fitted"]
        scaler.n_features_ = data["n_features_"]
        scaler.center_ = np.array(data["center_"], dtype=np.float64) if data.get("center_") is not None else None
        scaler.scale_ = np.array(data["scale_"], dtype=np.float64) if data.get("scale_") is not None else None
        scaler.min_ = np.array(data["min_"], dtype=np.float64) if data.get("min_") is not None else None
        scaler.max_ = np.array(data["max_"], dtype=np.float64) if data.get("max_") is not None else None
        return scaler
