"""QuantLab Quant Service — FastAPI application entry point."""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import get_settings
from app.api.health import router as health_router
from app.api.analysis import router as analysis_router
from app.api.qlib import router as qlib_router
from app.api.strategy_lifecycle import router as lifecycle_router
from app.api.strategy_validation import router as validation_router
from app.api.ai_research import router as research_router
from app.api.deep_learning import router as deep_learning_router
from app.api.data_acquisition import router as data_acquisition_router
from app.api.paper import router as paper_router
from app.api.terminal import router as terminal_router


def create_app() -> FastAPI:
    """Create and configure the FastAPI application."""
    settings = get_settings()

    app = FastAPI(
        title=settings.app_name,
        version=settings.app_version,
        description="Quantitative analysis service for QuantLab",
        docs_url="/docs" if settings.debug else None,
        redoc_url="/redoc" if settings.debug else None,
    )

    # CORS middleware
    app.add_middleware(
        CORSMiddleware,
        allow_origins=settings.cors_origins,
        allow_credentials=True,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    # Include routers
    app.include_router(health_router)
    app.include_router(analysis_router, prefix="/api/v1")
    app.include_router(qlib_router, prefix="/api/v1")
    app.include_router(lifecycle_router, prefix="/api/v1")
    app.include_router(validation_router, prefix="/api/v1")
    app.include_router(research_router, prefix="/api/v1")
    app.include_router(deep_learning_router, prefix="")
    app.include_router(data_acquisition_router, prefix="")
    app.include_router(paper_router, prefix="")
    app.include_router(terminal_router, prefix="")

    return app


app = create_app()
