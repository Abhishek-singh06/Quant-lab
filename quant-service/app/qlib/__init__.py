"""QuantLab Qlib Quantitative Research Engine Integration.

Provides high-performance factor engineering (Alpha158, Alpha360, custom expressions),
leak-free Point-in-Time datasets, model adapters (GBDT, Linear, RF, Ensemble),
signal evaluation (IC, Rank IC, Long-Short), and backtest comparison against
QuantLab's realistic Next-Bar T+1 execution engine.
"""

from app.qlib.config import QlibConfig, get_qlib_config
from app.qlib.expressions import (
    QlibExpressionEvaluator,
    LookaheadBiasError,
    ts_ref,
    ts_mean,
    ts_std,
    ts_var,
    ts_max,
    ts_min,
    ts_sum,
    ts_quantile,
    ts_wma,
    ts_ema,
    ts_corr,
    ts_slope,
    ts_rsquare,
    ts_resi,
    ts_rank,
    cs_rank,
    cs_zscore,
)
from app.qlib.alpha158 import Alpha158Builder, Alpha360Builder
from app.qlib.provider import QuantLabQlibDataProvider
from app.qlib.loader import QuantLabDataLoader
from app.qlib.dataset import QuantLabDatasetH
from app.qlib.handler import (
    QuantLabDataHandler,
    FeatureProcessor,
    CSZScoreProcessor,
    CSRankProcessor,
    RobustZScoreProcessor,
    MinMaxProcessor,
    WinsorizeProcessor,
)
from app.qlib.model_adapter import (
    QlibModel,
    QlibGBDTModel,
    QlibLinearModel,
    QlibRandomForestModel,
    QlibEnsembleModel,
    QuantLabQlibModelBridge,
)
from app.qlib.evaluator import QlibSignalEvaluator
from app.qlib.backtest_comparison import QlibBacktestComparator
from app.qlib.experiment_adapter import QlibExperimentManager

__all__ = [
    "QlibConfig",
    "get_qlib_config",
    "QlibExpressionEvaluator",
    "LookaheadBiasError",
    "ts_ref",
    "ts_mean",
    "ts_std",
    "ts_var",
    "ts_max",
    "ts_min",
    "ts_sum",
    "ts_quantile",
    "ts_wma",
    "ts_ema",
    "ts_corr",
    "ts_slope",
    "ts_rsquare",
    "ts_resi",
    "ts_rank",
    "cs_rank",
    "cs_zscore",
    "Alpha158Builder",
    "Alpha360Builder",
    "QuantLabQlibDataProvider",
    "QuantLabDataLoader",
    "QuantLabDatasetH",
    "QuantLabDataHandler",
    "FeatureProcessor",
    "CSZScoreProcessor",
    "CSRankProcessor",
    "RobustZScoreProcessor",
    "MinMaxProcessor",
    "WinsorizeProcessor",
    "QlibModel",
    "QlibGBDTModel",
    "QlibLinearModel",
    "QlibRandomForestModel",
    "QlibEnsembleModel",
    "QuantLabQlibModelBridge",
    "QlibSignalEvaluator",
    "QlibBacktestComparator",
    "QlibExperimentManager",
]
