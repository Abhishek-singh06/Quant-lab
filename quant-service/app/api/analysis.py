"""Analysis API endpoints.

Phase 1: Stub endpoints for future implementation.
No fake predictions or fake market data.
"""

from fastapi import APIRouter
from pydantic import BaseModel

router = APIRouter(tags=["analysis"])


class ServiceInfo(BaseModel):
    """Service information response."""
    service: str
    status: str
    available_endpoints: list[str]
    note: str


@router.get("/info", response_model=ServiceInfo)
async def service_info() -> ServiceInfo:
    """Get information about available analysis capabilities."""
    return ServiceInfo(
        service="quantlab-quant-service",
        status="operational",
        available_endpoints=[
            "/health",
            "/api/v1/info",
        ],
        note="Phase 1: Architecture setup. Analysis endpoints will be added in subsequent phases.",
    )
