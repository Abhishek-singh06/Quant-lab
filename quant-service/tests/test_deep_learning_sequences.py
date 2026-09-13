"""Tests for Point-in-Time Scaler and Temporal Sequence Generator."""

import pytest
import numpy as np
import pandas as pd

from app.deep_learning.scaler import PointInTimeScaler
from app.deep_learning.sequence_generator import TemporalSequenceGenerator, SequenceDataset


class TestPointInTimeScaler:
    """Adversarial and functional test suite for PointInTimeScaler."""

    def test_standard_scaler_2d_and_3d(self):
        rng = np.random.RandomState(42)
        X_train_2d = rng.randn(100, 5) * 2.0 + 3.0
        scaler = PointInTimeScaler(method="standard")
        scaler.fit(X_train_2d)

        assert scaler.is_fitted
        assert scaler.n_features_ == 5
        np.testing.assert_allclose(scaler.center_, np.mean(X_train_2d, axis=0), rtol=1e-5)

        # Test transform on 3D tensor
        X_test_3d = rng.randn(20, 10, 5) * 2.0 + 3.0
        scaled_3d = scaler.transform(X_test_3d)
        assert scaled_3d.shape == (20, 10, 5)
        # Verify mean of transformed features is close to 0
        flat_scaled = scaled_3d.reshape(-1, 5)
        assert np.abs(np.mean(flat_scaled)) < 0.5

    def test_robust_scaler(self):
        rng = np.random.RandomState(42)
        X = rng.randn(200, 3)
        # Inject extreme outlier
        X[0, 0] = 1000.0

        scaler = PointInTimeScaler(method="robust", clip_outliers=5.0)
        scaled = scaler.fit_transform(X)
        assert np.max(scaled) <= 5.0
        assert np.min(scaled) >= -5.0

    def test_scaler_not_fitted_error(self):
        scaler = PointInTimeScaler(method="standard")
        with pytest.raises(RuntimeError, match="must be fitted"):
            scaler.transform(np.ones((10, 2)))

    def test_scaler_dimension_mismatch(self):
        scaler = PointInTimeScaler(method="standard")
        scaler.fit(np.ones((10, 4)))
        with pytest.raises(ValueError, match="Feature dimension mismatch"):
            scaler.transform(np.ones((10, 2)))

    def test_scaler_serialization(self):
        scaler = PointInTimeScaler(method="standard", clip_outliers=4.0)
        scaler.fit(np.array([[1.0, 2.0], [3.0, 4.0], [5.0, 6.0]]))
        d = scaler.to_dict()

        restored = PointInTimeScaler.from_dict(d)
        assert restored.is_fitted
        assert restored.clip_outliers == 4.0
        np.testing.assert_allclose(restored.center_, scaler.center_)
        np.testing.assert_allclose(restored.scale_, scaler.scale_)


class TestTemporalSequenceGenerator:
    """Adversarial tests ensuring zero cross-symbol leakage and zero future look-ahead."""

    def test_single_symbol_sequence_generation(self):
        n_bars = 100
        lookback = 20
        horizon = 1
        dates = pd.date_range("2024-01-01", periods=n_bars, freq="D")
        df = pd.DataFrame({
            "timestamp": dates,
            "symbol": "RELIANCE",
            "feat_a": np.arange(n_bars, dtype=float),
            "feat_b": np.arange(n_bars, dtype=float) * 2.0,
            "target": np.ones(n_bars),
        })

        gen = TemporalSequenceGenerator(lookback=lookback, horizon=horizon)
        dataset = gen.generate(df)

        expected_samples = n_bars - lookback - horizon + 1
        assert len(dataset) == expected_samples
        assert dataset.X.shape == (expected_samples, lookback, 2)
        assert dataset.y.shape == (expected_samples,)

        # Verify exact sequence values for first sample
        np.testing.assert_array_equal(dataset.X[0, :, 0], np.arange(0, lookback))
        # Verify prediction timestamp matches bar t = lookback - 1
        assert dataset.timestamps[0] == dates[lookback - 1]
        # Verify target timestamp matches bar t + H = lookback
        assert dataset.target_timestamps[0] == dates[lookback]

    def test_multi_symbol_cross_contamination_protection(self):
        """Adversarially verify that sequences from Symbol B NEVER contain rows from Symbol A."""
        n_bars = 50
        lookback = 10
        horizon = 1

        dates = pd.date_range("2024-01-01", periods=n_bars, freq="D")
        df_a = pd.DataFrame({
            "timestamp": dates,
            "symbol": "TCS",
            "feat": np.full(n_bars, 100.0),
            "target": np.zeros(n_bars),
        })
        df_b = pd.DataFrame({
            "timestamp": dates,
            "symbol": "INFY",
            "feat": np.full(n_bars, 200.0),
            "target": np.zeros(n_bars),
        })
        df = pd.concat([df_a, df_b], ignore_index=True)

        gen = TemporalSequenceGenerator(lookback=lookback, horizon=horizon)
        dataset = gen.generate(df)

        # Separate symbols
        tcs_indices = np.where(dataset.symbols == "TCS")[0]
        infy_indices = np.where(dataset.symbols == "INFY")[0]

        assert len(tcs_indices) > 0
        assert len(infy_indices) > 0

        # Every TCS sequence must ONLY have 100.0
        for idx in tcs_indices:
            assert np.all(dataset.X[idx] == 100.0)

        # Every INFY sequence must ONLY have 200.0 — ZERO 100.0 values allowed!
        for idx in infy_indices:
            assert np.all(dataset.X[idx] == 200.0)

    def test_insufficient_bars_rejection(self):
        """Symbols with fewer bars than lookback + horizon are cleanly skipped."""
        df = pd.DataFrame({
            "timestamp": pd.date_range("2024-01-01", periods=5, freq="D"),
            "symbol": "SMALL_SYM",
            "feat": np.ones(5),
            "target": np.zeros(5),
        })
        gen = TemporalSequenceGenerator(lookback=20, horizon=1)
        with pytest.raises(ValueError, match="No valid sequences"):
            gen.generate(df)
