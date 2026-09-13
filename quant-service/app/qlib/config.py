"""Qlib Research Engine Configuration for QuantLab.

Provides configuration settings for the Qlib quantitative research layer,
expression engine, dataset caching, and PIT safeguards.
"""

import os
from typing import List, Optional
from pydantic import BaseModel, Field


class QlibConfig(BaseModel):
    """Configuration for Qlib research and modeling engine."""

    enabled: bool = Field(
        default=True,
        description="Whether the Qlib research engine layer is enabled."
    )
    cache_dir: str = Field(
        default="artifacts/qlib_cache",
        description="Directory for caching computed alpha expressions and datasets."
    )
    experiments_dir: str = Field(
        default="artifacts/qlib_experiments",
        description="Directory for persisting Qlib experiment records and metrics."
    )
    default_benchmark: str = Field(
        default="NIFTY50",
        description="Default benchmark symbol for relative alpha and beta calculations."
    )
    dataset_version: str = Field(
        default="QLIB_v1.0",
        description="Version tag for generated Qlib datasets."
    )
    feature_version: str = Field(
        default="Alpha158_v1.0",
        description="Version tag for Alpha158/Alpha360 feature sets."
    )
    enforce_pit: bool = Field(
        default=True,
        description="Enforce Point-in-Time timestamp validation (reject lookahead)."
    )
    max_expression_depth: int = Field(
        default=10,
        description="Maximum recursion depth for expression parsing to prevent stack overflow."
    )
    default_top_k: int = Field(
        default=5,
        description="Default number of top assets to hold in long-short evaluation."
    )
    default_n_drop: int = Field(
        default=0,
        description="Number of assets to drop from top before selecting top-k."
    )
    annualization_factor: int = Field(
        default=252,
        description="Trading days per year for Sharpe, volatility, and ICIR annualization."
    )


_qlib_config_instance: Optional[QlibConfig] = None


def get_qlib_config() -> QlibConfig:
    """Retrieve or create singleton QlibConfig instance."""
    global _qlib_config_instance
    if _qlib_config_instance is None:
        cache_dir = os.environ.get("QUANT_QLIB_CACHE_DIR", "artifacts/qlib_cache")
        exp_dir = os.environ.get("QUANT_QLIB_EXPERIMENTS_DIR", "artifacts/qlib_experiments")
        enabled = os.environ.get("QUANT_QLIB_ENABLED", "true").lower() in ("true", "1", "yes")
        _qlib_config_instance = QlibConfig(
            enabled=enabled,
            cache_dir=cache_dir,
            experiments_dir=exp_dir,
        )
    return _qlib_config_instance
