"""Health check endpoints for the quant service."""

import time
from datetime import datetime, timezone

from fastapi import APIRouter
from pydantic import BaseModel

from app.core.config import get_settings

router = APIRouter(tags=["health"])

_start_time = time.time()


class HealthResponse(BaseModel):
    """Health check response model."""
    status: str
    service: str
    version: str
    timestamp: str
    uptime_seconds: float


@router.get("/health", response_model=HealthResponse)
@router.get("/api/health", response_model=HealthResponse)
@router.get("/api/quant/health", response_model=HealthResponse)
@router.get("/quant/health", response_model=HealthResponse)
@router.get("/api/v1/health", response_model=HealthResponse)
async def health_check() -> HealthResponse:
    """Check the health status of the quant service."""
    settings = get_settings()
    return HealthResponse(
        status="UP",
        service=settings.app_name,
        version=settings.app_version,
        timestamp=datetime.now(timezone.utc).isoformat(),
        uptime_seconds=round(time.time() - _start_time, 2),
    )

