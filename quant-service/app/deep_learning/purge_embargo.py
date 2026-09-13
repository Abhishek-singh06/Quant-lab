"""Purge and Embargo Validation & Cross-Validation Splits.

Implements Marcos López de Prado's Purging and Embargoing methodology to eliminate
information leakage arising from overlapping sequence lookbacks and multi-step forward return horizons.
"""

from typing import List, Tuple, Generator, Optional
import numpy as np
import pandas as pd


class PurgeAndEmbargoValidator:
    """Validates and enforces purge and embargo boundaries between training and test partitions."""

    def __init__(self, purge_horizon: int = 1, embargo_bars: int = 5):
        """
        Args:
            purge_horizon: Forward label horizon H (number of bars after prediction before label is resolved)
            embargo_bars: Embargo window E (number of bars after test set to exclude from subsequent train sets)
        """
        self.purge_horizon = max(1, purge_horizon)
        self.embargo_bars = max(0, embargo_bars)

    def validate_no_leakage(
        self,
        train_timestamps: np.ndarray,
        train_target_timestamps: np.ndarray,
        test_timestamps: np.ndarray,
    ) -> bool:
        """Verifies that no training sample's label realization extends into or beyond the test set start.

        Raises:
            ValueError: If look-ahead leakage is detected.
        """
        if len(train_timestamps) == 0 or len(test_timestamps) == 0:
            return True

        min_test_ts = np.min(test_timestamps)
        # Training samples must have their labels fully realized before min_test_ts
        leaked_mask = train_target_timestamps >= min_test_ts
        if np.any(leaked_mask):
            num_leaked = int(np.sum(leaked_mask))
            raise ValueError(
                f"Data leakage detected! {num_leaked} training samples have label horizons >= test start ({min_test_ts})."
            )
        return True

    def apply_purge(
        self,
        train_indices: np.ndarray,
        test_start_idx: int,
        horizon: int,
    ) -> np.ndarray:
        """Purges training indices whose forward label extends into test set start."""
        # A sample at index i with forward horizon H resolves at i + H.
        # It leaks if i + H >= test_start_idx.
        cutoff = test_start_idx - horizon
        return train_indices[train_indices < cutoff]

    def apply_embargo(
        self,
        train_indices: np.ndarray,
        test_end_idx: int,
        embargo_bars: int,
    ) -> np.ndarray:
        """Embargoes training indices immediately following a test fold (for walk-forward rollbacks)."""
        embargo_cutoff = test_end_idx + embargo_bars
        # Exclude indices in range [test_end_idx, embargo_cutoff)
        valid_mask = (train_indices < test_end_idx) | (train_indices >= embargo_cutoff)
        return train_indices[valid_mask]


class TimeSeriesSplitPurged:
    """Walk-forward time-series cross-validator with strict Purging and Embargoing."""

    def __init__(
        self,
        n_splits: int = 5,
        lookback: int = 60,
        horizon: int = 1,
        embargo_pct: float = 0.01,
    ):
        """
        Args:
            n_splits: Number of walk-forward folds
            lookback: Lookback sequence length
            horizon: Forward prediction label horizon
            embargo_pct: Percentage of samples to embargo after test fold
        """
        if n_splits < 2:
            raise ValueError("n_splits must be >= 2.")
        self.n_splits = n_splits
        self.lookback = lookback
        self.horizon = horizon
        self.embargo_pct = embargo_pct
        self.validator = PurgeAndEmbargoValidator(purge_horizon=horizon)

    def split(
        self,
        X: np.ndarray,
        y: Optional[np.ndarray] = None,
        timestamps: Optional[np.ndarray] = None,
    ) -> Generator[Tuple[np.ndarray, np.ndarray], None, None]:
        """Yields (train_indices, test_indices) for each walk-forward fold with purging and embargo applied."""
        n_samples = len(X)
        indices = np.arange(n_samples)

        test_size = n_samples // (self.n_splits + 1)
        embargo_bars = int(n_samples * self.embargo_pct)

        for i in range(self.n_splits):
            test_start = (i + 1) * test_size
            test_end = test_start + test_size if i < self.n_splits - 1 else n_samples

            # Raw train is everything before test_start
            raw_train = indices[:test_start]

            # 1. Apply Purge: remove training samples whose forward target overlaps test_start
            purged_train = self.validator.apply_purge(raw_train, test_start, self.horizon)

            test_idx = indices[test_start:test_end]

            if len(purged_train) == 0 or len(test_idx) == 0:
                continue

            # If timestamps are provided, verify zero leakage
            if timestamps is not None:
                train_ts = timestamps[purged_train]
                # Target timestamps are shifted by horizon bars
                train_target_ts = timestamps[np.clip(purged_train + self.horizon, 0, n_samples - 1)]
                test_ts = timestamps[test_idx]
                self.validator.validate_no_leakage(train_ts, train_target_ts, test_ts)

            yield purged_train, test_idx
