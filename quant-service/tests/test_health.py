"""Tests for health check endpoint."""


def test_health_check(client):
    """Test that health endpoint returns correct response."""
    response = client.get("/health")
    assert response.status_code == 200

    data = response.json()
    assert data["status"] == "UP"
    assert data["service"] == "quantlab-quant-service"
    assert data["version"] == "0.1.0"
    assert "timestamp" in data
    assert "uptime_seconds" in data


def test_health_check_returns_valid_timestamp(client):
    """Test that health endpoint returns ISO format timestamp."""
    response = client.get("/health")
    data = response.json()
    # Should not raise
    from datetime import datetime
    datetime.fromisoformat(data["timestamp"])
