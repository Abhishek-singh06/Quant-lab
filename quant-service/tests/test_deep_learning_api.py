"""Tests for Deep Learning FastAPI endpoints."""

import pytest
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_get_architectures():
    response = client.get("/api/v1/deep-learning/architectures")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert len(data["architectures"]) == 4
    arch_types = [a["type"] for a in data["architectures"]]
    assert "GRU" in arch_types
    assert "LSTM" in arch_types
    assert "1DCNN" in arch_types
    assert "ATTENTION" in arch_types
    assert data["safeguards"]["point_in_time_scaling"] is True


def test_train_dl_model_endpoint():
    payload = {
        "model_type": "GRU",
        "lookback": 20,
        "horizon": 1,
        "hidden_dim": 16,
        "epochs": 2,
        "learning_rate": 0.005,
    }
    response = client.post("/api/v1/deep-learning/train", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert data["algorithm"] == "Temporal_GRU"
    assert "provenance_hash" in data
    assert "metrics" in data


def test_compare_models_endpoint():
    payload = {
        "lookback": 20,
        "horizon": 1,
        "n_splits": 2,
        "cost_bps": 15.0,
    }
    response = client.post("/api/v1/deep-learning/compare", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert "results" in data
    assert "metrics" in data["results"]
