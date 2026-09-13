"""Tests for Purge and Embargo Validation and CV splits."""

import pytest
import numpy as np
import pandas as pd

from app.deep_learning.purge_embargo import PurgeAndEmbargoValidator, TimeSeriesSplitPurged


class TestPurgeAndEmbargoValidator:

    def test_purge_removes_overlapping_training_labels(self):
        validator = PurgeAndEmbargoValidator(purge_horizon=3)
        train_indices = np.arange(100)
        test_start_idx = 80

        # Horizon is 3, so sample at 77 resolves at 80 (overlapping with test_start_idx 80)
        # Purged train should only contain indices < (80 - 3) = 77
        purged = validator.apply_purge(train_indices, test_start_idx=test_start_idx, horizon=3)
        assert np.max(purged) == 76
        assert len(purged) == 77

    def test_leakage_detection_adversarial_error(self):
        validator = PurgeAndEmbargoValidator(purge_horizon=5)
        dates = pd.date_range("2024-01-01", periods=100, freq="D").values

        # Leaking scenario: train target timestamp at index 78 resolves at date 83, but test starts at date 80
        train_ts = dates[:79]
        train_target_ts = dates[5:84]  # index 78 resolves at 78+5 = 83 >= 80
        test_ts = dates[80:]

        with pytest.raises(ValueError, match="Data leakage detected"):
            validator.validate_no_leakage(train_ts, train_target_ts, test_ts)

    def test_clean_split_no_leakage(self):
        validator = PurgeAndEmbargoValidator(purge_horizon=2)
        dates = pd.date_range("2024-01-01", periods=100, freq="D").values

        # Safe scenario: train ends at 60, target resolves at 62 < test start 70
        train_ts = dates[:60]
        train_target_ts = dates[2:62]
        test_ts = dates[70:]

        assert validator.validate_no_leakage(train_ts, train_target_ts, test_ts) is True

    def test_time_series_split_purged_generator(self):
        n_samples = 200
        X = np.random.randn(n_samples, 10, 4)
        cv = TimeSeriesSplitPurged(n_splits=3, lookback=10, horizon=2)

        splits = list(cv.split(X))
        assert len(splits) == 3

        for train_idx, test_idx in splits:
            assert len(train_idx) > 0
            assert len(test_idx) > 0
            # Test start must be strictly greater than max train index + horizon
            test_start = test_idx[0]
            max_train = np.max(train_idx)
            assert max_train + 2 <= test_start
