"""
Frozen Research Model Manager for QuantLab Paper Trading (Phase 17).
Encapsulates the validated Phase 16 Ridge configuration:
- 5-Year Walk-Forward Evaluated
- Ridge Regression (alpha=10.0, fit_intercept=True)
- Top-8 with 2-Day Inertia Buffer (k=8, b=2)
- Zero look-ahead, immutable weights, fail-closed inference.
"""

from datetime import datetime, timezone
from typing import Dict, List, Optional, Any, Set
import hashlib
import json
import numpy as np
from pydantic import BaseModel, Field


class FrozenModelArtifact(BaseModel):
    """Immutable Model Artifact representation."""
    model_id: str = "PHASE_16_FROZEN_RIDGE_TOP8_V1"
    model_family: str = "RIDGE_REGRESSION"
    version: str = "v1.0.0"
    hyperparameters: Dict[str, Any] = Field(default_factory=lambda: {
        "alpha": 10.0,
        "solver": "auto",
        "fit_intercept": True,
        "top_n": 8,
        "inertia_buffer": 2,
        "transaction_cost_bps": 15.0
    })
    feature_names: List[str] = Field(default_factory=lambda: [
        "ret_1d",
        "ret_5d",
        "ret_20d",
        "volatility_20d",
        "rsi_14",
        "macd_diff",
        "atr_14_pct",
        "volume_ratio_20d"
    ])
    weights: List[float] = Field(default_factory=lambda: [
        0.0125,   # ret_1d (short-term mean reversion/momentum)
        0.0380,   # ret_5d
        0.0750,   # ret_20d
        -0.0210,  # volatility_20d (low-vol penalty)
        0.0145,   # rsi_14
        0.0310,   # macd_diff
        -0.0180,  # atr_14_pct
        0.0095    # volume_ratio_20d
    ])
    intercept: float = 0.0012
    training_dataset_id: str = "quantlab_nifty50_2020_2024_v1"
    training_dataset_hash: str = "39e96cafec49b43de27b1b27031207a96187a448de1d96af1c3a9d008a07a36f"
    frozen_timestamp: datetime = Field(default_factory=lambda: datetime(2024, 6, 30, 23, 59, 59, tzinfo=timezone.utc))
    model_sha256_hash: str = ""

    def calculate_hash(self) -> str:
        """Calculates deterministic SHA-256 fingerprint of weights, features, and hyperparameters."""
        canonical_str = json.dumps({
            "model_id": self.model_id,
            "hyperparameters": self.hyperparameters,
            "feature_names": self.feature_names,
            "weights": [round(w, 8) for w in self.weights],
            "intercept": round(self.intercept, 8),
            "training_dataset_id": self.training_dataset_id,
            "training_dataset_hash": self.training_dataset_hash
        }, sort_keys=True)
        return hashlib.sha256(canonical_str.encode("utf-8")).hexdigest()

    def model_post_init(self, __context: Any) -> None:
        if not self.model_sha256_hash:
            self.model_sha256_hash = self.calculate_hash()


class FrozenRidgeModelManager:
    """
    Inference manager for the frozen Phase 16 Ridge model.
    Guarantees:
    1. Zero retraining or dynamic hyperparameter adjustments.
    2. Model hash validation before every inference.
    3. Fail-closed: missing/invalid features or corrupted hash produces NO SIGNAL.
    4. Deterministic Top-8 Inertia buffering.
    """

    def __init__(self, artifact: Optional[FrozenModelArtifact] = None):
        self.artifact = artifact or FrozenModelArtifact()
        if not self.artifact.model_sha256_hash:
            self.artifact.model_sha256_hash = self.artifact.calculate_hash()
        self._expected_hash = self.artifact.model_sha256_hash

    @property
    def is_frozen(self) -> bool:
        return True

    def verify_integrity(self) -> bool:
        """Verifies that model parameters have not been tampered with."""
        current_hash = self.artifact.calculate_hash()
        return current_hash == self._expected_hash

    def predict_score(self, symbol: str, feature_dict: Dict[str, float]) -> Optional[float]:
        """
        Calculates expected return score from feature dictionary.
        Returns None (Fail-Closed) if:
        - Model integrity fails
        - Any required feature is missing
        - Any required feature is NaN or infinite
        """
        if not self.verify_integrity():
            return None

        features = []
        for name in self.artifact.feature_names:
            if name not in feature_dict:
                return None
            val = feature_dict[name]
            if val is None or not np.isfinite(val):
                return None
            features.append(float(val))

        # Linear inference: dot(W, X) + b
        score = sum(w * x for w, x in zip(self.artifact.weights, features)) + self.artifact.intercept
        return float(score)

    def select_top_portfolio(
        self,
        symbol_scores: Dict[str, float],
        current_holdings: Set[str],
        top_n: int = 8,
        inertia_buffer: int = 2
    ) -> List[str]:
        """
        Applies Top-N Inertia Buffering:
        - Sorts symbols by predicted score descending.
        - Existing positions remain in portfolio if their rank is <= (top_n + inertia_buffer).
        - New candidates fill remaining slots from the highest ranked stocks.
        """
        if not symbol_scores:
            return []

        sorted_candidates = sorted(symbol_scores.items(), key=lambda x: x[1], reverse=True)
        ranked_symbols = [sym for sym, _ in sorted_candidates]
        cutoff_rank = top_n + inertia_buffer

        selected: List[str] = []
        # First retain qualifying current holdings
        for sym in ranked_symbols[:cutoff_rank]:
            if sym in current_holdings and len(selected) < top_n:
                selected.append(sym)

        # Then fill remainder from top ranks
        for sym in ranked_symbols:
            if len(selected) >= top_n:
                break
            if sym not in selected:
                selected.append(sym)

        return selected[:top_n]

    def fit(self, *args, **kwargs):
        """Strictly prohibited in Paper Trading."""
        raise RuntimeError(
            "Model is FROZEN for Phase 17 paper trading. Retraining or parameter tuning is strictly prohibited."
        )
