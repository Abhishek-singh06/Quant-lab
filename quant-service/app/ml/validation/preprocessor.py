"""Leakage-Safe Preprocessing Suite.

Guarantees that all scalers, normalizers, winsorizers, and imputers are fitted
STRICTLY on training data and NEVER on validation or test sets.
"""

from typing import Dict, Any, Optional, List, Tuple
import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler, RobustScaler


class LeakageSafePreprocessor:
    """Scaler and Winsorizer fitted strictly on training data."""

    def __init__(
        self,
        scaler_type: str = "ROBUST",  # ROBUST, STANDARD, NONE
        winsorize_limits: Optional[Tuple[float, float]] = (0.01, 0.99)
    ):
        self.scaler_type = scaler_type.upper()
        self.winsorize_limits = winsorize_limits
        self.is_fitted = False
        self.feature_names: List[str] = []
        self.scaler = None
        self.lower_bounds_: Dict[str, float] = {}
        self.upper_bounds_: Dict[str, float] = {}

    def fit(self, X_train: pd.DataFrame) -> "LeakageSafePreprocessor":
        """Fits scalers and derives winsorization bounds strictly on X_train."""
        self.feature_names = list(X_train.columns)
        X_clean = X_train.copy().fillna(0.0)

        # 1. Derive Winsorization bounds on training partition only
        if self.winsorize_limits is not None:
            low_q, high_q = self.winsorize_limits
            for col in self.feature_names:
                self.lower_bounds_[col] = float(X_clean[col].quantile(low_q))
                self.upper_bounds_[col] = float(X_clean[col].quantile(high_q))

        # Apply training winsorization before scaler fit
        X_winsor = self._apply_winsorization(X_clean)

        # 2. Fit Scaler strictly on training partition
        if self.scaler_type == "ROBUST":
            self.scaler = RobustScaler()
            self.scaler.fit(X_winsor)
        elif self.scaler_type == "STANDARD":
            self.scaler = StandardScaler()
            self.scaler.fit(X_winsor)
        elif self.scaler_type == "NONE":
            self.scaler = None
        else:
            raise ValueError(f"Unknown scaler_type: {self.scaler_type}")

        self.is_fitted = True
        return self

    def transform(self, X: pd.DataFrame) -> pd.DataFrame:
        """Transforms data using training-fitted parameters."""
        if not self.is_fitted:
            raise RuntimeError("Preprocessor must be fitted before transform()")

        X_clean = X.copy().fillna(0.0)
        X_winsor = self._apply_winsorization(X_clean)

        if self.scaler is not None:
            scaled_vals = self.scaler.transform(X_winsor)
            return pd.DataFrame(scaled_vals, columns=self.feature_names, index=X.index)
        else:
            return X_winsor

    def fit_transform(self, X_train: pd.DataFrame) -> pd.DataFrame:
        return self.fit(X_train).transform(X_train)

    def _apply_winsorization(self, df: pd.DataFrame) -> pd.DataFrame:
        res = df.copy()
        for col in self.feature_names:
            if col in self.lower_bounds_ and col in self.upper_bounds_:
                low = self.lower_bounds_[col]
                high = self.upper_bounds_[col]
                res[col] = np.clip(res[col], low, high)
        return res
