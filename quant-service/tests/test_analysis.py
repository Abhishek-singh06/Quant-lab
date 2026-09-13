"""Tests for analysis endpoints."""


def test_service_info(client):
    """Test that service info endpoint returns correct response."""
    response = client.get("/api/v1/info")
    assert response.status_code == 200

    data = response.json()
    assert data["service"] == "quantlab-quant-service"
    assert data["status"] == "operational"
    assert isinstance(data["available_endpoints"], list)
