"""API Integration Tests for Qlib REST Endpoints."""

import pytest
from fastapi.testclient import TestClient
from app.main import app


@pytest.fixture
def client():
    return TestClient(app)


def test_api_alpha_compute(client):
    payload = {
        "instruments": ["RELIANCE", "TCS"],
        "start_time": "2023-01-01",
        "end_time": "2023-03-31",
        "feature_type": "Alpha158"
    }
    response = client.post("/api/v1/qlib/alpha/compute", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["feature_type"] == "Alpha158"
    assert data["n_instruments"] == 2
    assert data["n_features"] > 0
    assert "KLEN" in data["feature_names"]


def test_api_model_train_and_evaluate(client):
    payload = {
        "instruments": ["RELIANCE", "TCS", "INFY"],
        "train_start": "2023-01-01",
        "train_end": "2023-04-30",
        "test_start": "2023-05-01",
        "test_end": "2023-06-30",
        "model_type": "GBDT",
        "feature_type": "Alpha158",
        "hyperparameters": {"max_iter": 10}
    }
    response = client.post("/api/v1/qlib/model/train", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert "model_id" in data
    assert "experiment_id" in data
    assert "evaluation_metrics" in data
    assert "ic_mean" in data["evaluation_metrics"]
    assert "backtest_comparison" in data
    assert "vectorized_qlib" in data["backtest_comparison"]
    assert "canonical_quantlab" in data["backtest_comparison"]


def test_api_list_experiments(client):
    response = client.get("/api/v1/qlib/experiments")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
