"""Tests for Data Acquisition FastAPI endpoints."""

import pytest
from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_data_acquisition_status_endpoint():
    response = client.get("/api/v1/data-acquisition/status")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert "YAHOO_FINANCE" in data["providers"]
    assert "AMFI_INDIA" in data["providers"]
    assert data["safeguards"]["fail_closed_mode"] is True
    assert data["safeguards"]["zero_synthetic_fallback"] is True


def test_get_historical_universe_endpoint():
    response = client.get("/api/v1/data-acquisition/universe?as_of=2009-06-01&index_name=NIFTY_50")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert data["as_of_date"] == "2009-06-01"
    constituents = data["constituents"]
    assert "RCOM" in constituents
    assert "SUZLON" in constituents
    assert "RELIANCE" in constituents
    assert len(data["historical_exits_included"]) > 0


def test_dataset_snapshots_endpoint():
    response = client.get("/api/v1/data-acquisition/snapshots")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "success"
    assert isinstance(data["snapshots"], list)
