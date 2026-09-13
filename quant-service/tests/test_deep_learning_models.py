"""Tests for Vectorized Temporal Deep Learning Architectures."""

import pytest
import numpy as np

from app.deep_learning.models import (
    TemporalGRUModel,
    TemporalLSTMModel,
    Temporal1DCNNModel,
    TemporalAttentionModel,
)


@pytest.fixture
def synthetic_sequence_data():
    rng = np.random.RandomState(42)
    N, T, D = 80, 15, 4
    X = rng.randn(N, T, D)
    # Target is correlated with final step features
    y = X[:, -1, 0] * 0.5 - X[:, -1, 1] * 0.3 + rng.randn(N) * 0.05
    return X, y


class TestTemporalGRUModel:

    def test_gru_fit_and_predict(self, synthetic_sequence_data):
        X, y = synthetic_sequence_data
        model = TemporalGRUModel(hidden_dim=16, epochs=5, batch_size=16, random_seed=42)
        model.fit(X, y)

        assert model.is_fitted
        preds = model.predict(X)
        assert preds.shape == (len(y),)
        assert not np.any(np.isnan(preds))
        assert not np.any(np.isinf(preds))

        # Check temporal attention
        attn = model.get_temporal_attention(X)
        assert attn.shape == (len(y), 15)
        # Softmax sum across time steps should be approx 1.0
        np.testing.assert_allclose(np.sum(attn, axis=-1), np.ones(len(y)), rtol=1e-4)

    def test_gru_reproducibility(self, synthetic_sequence_data):
        X, y = synthetic_sequence_data
        m1 = TemporalGRUModel(hidden_dim=8, epochs=3, random_seed=123).fit(X, y)
        m2 = TemporalGRUModel(hidden_dim=8, epochs=3, random_seed=123).fit(X, y)

        np.testing.assert_allclose(m1.predict(X), m2.predict(X))


class TestTemporalLSTMModel:

    def test_lstm_fit_and_predict(self, synthetic_sequence_data):
        X, y = synthetic_sequence_data
        model = TemporalLSTMModel(hidden_dim=16, epochs=5, batch_size=16, random_seed=42)
        model.fit(X, y)

        assert model.is_fitted
        preds = model.predict(X)
        assert preds.shape == (len(y),)
        assert not np.any(np.isnan(preds))

        attn = model.get_temporal_attention(X)
        assert attn.shape == (len(y), 15)


class TestTemporal1DCNNModel:

    def test_cnn_fit_and_predict(self, synthetic_sequence_data):
        X, y = synthetic_sequence_data
        model = Temporal1DCNNModel(num_filters=16, kernel_size=3, epochs=5, batch_size=16, random_seed=42)
        model.fit(X, y)

        assert model.is_fitted
        preds = model.predict(X)
        assert preds.shape == (len(y),)
        assert not np.any(np.isnan(preds))


class TestTemporalAttentionModel:

    def test_attention_fit_and_predict(self, synthetic_sequence_data):
        X, y = synthetic_sequence_data
        model = TemporalAttentionModel(attn_dim=16, epochs=5, batch_size=16, random_seed=42)
        model.fit(X, y)

        assert model.is_fitted
        preds = model.predict(X)
        assert preds.shape == (len(y),)
        assert not np.any(np.isnan(preds))

        step_attn = model.get_temporal_attention(X)
        assert step_attn.shape == (len(y), 15)
        np.testing.assert_allclose(np.sum(step_attn, axis=-1), np.ones(len(y)), rtol=1e-4)
