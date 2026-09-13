"""Application configuration using pydantic-settings.

All configuration is loaded from environment variables.
No API keys or secrets are hardcoded.
"""

from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    """Application settings loaded from environment variables."""

    # Application
    app_name: str = "quantlab-quant-service"
    app_version: str = "0.1.0"
    debug: bool = False

    # Server
    host: str = "0.0.0.0"
    port: int = 8000

    # Database (for future use)
    database_url: str = "postgresql://quantlab:quantlab@localhost:5432/quantlab"

    # Redis (for future use)
    redis_url: str = "redis://localhost:6379/0"

    # Backend service URL
    backend_url: str = "http://localhost:8080"

    # CORS
    cors_origins: list[str] = [
        "http://localhost:3000",
        "http://localhost:5173",
        "http://localhost:8080",
    ]

    model_config = {
        "env_file": ".env",
        "env_prefix": "QUANT_",
        "case_sensitive": False,
    }


@lru_cache
def get_settings() -> Settings:
    """Get cached settings instance."""
    return Settings()
