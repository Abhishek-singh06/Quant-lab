"""Data models and enums for QuantLab Part 15: Trading + Investing Models."""

from enum import Enum
from typing import Dict, List, Optional, Any
from pydantic import BaseModel, Field
from datetime import datetime, date


class TradingHorizon(str, Enum):
    SHORT_TERM = "SHORT_TERM"       # Intraday to 1-5 trading days
    MEDIUM_TERM = "MEDIUM_TERM"     # 1 to 12 weeks
    LONG_TERM = "LONG_TERM"         # 6 months to 5+ years


class ShortTermHorizon(str, Enum):
    INTRADAY = "INTRADAY"
    ONE_DAY = "ONE_DAY"
    TWO_DAYS = "TWO_DAYS"
    THREE_DAYS = "THREE_DAYS"
    FIVE_DAYS = "FIVE_DAYS"


class MediumTermHorizon(str, Enum):
    ONE_WEEK = "ONE_WEEK"
    TWO_WEEKS = "TWO_WEEKS"
    FOUR_WEEKS = "FOUR_WEEKS"
    EIGHT_WEEKS = "EIGHT_WEEKS"
    TWELVE_WEEKS = "TWELVE_WEEKS"


class LongTermHorizon(str, Enum):
    SIX_MONTHS = "SIX_MONTHS"
    ONE_YEAR = "ONE_YEAR"
    TWO_YEARS = "TWO_YEARS"
    THREE_YEARS = "THREE_YEARS"
    FIVE_YEARS = "FIVE_YEARS"


class TargetType(str, Enum):
    REGRESSION = "REGRESSION"
    CLASSIFICATION = "CLASSIFICATION"
    VOLATILITY = "VOLATILITY"
    RELATIVE_RETURN = "RELATIVE_RETURN"


class ModelStatus(str, Enum):
    CANDIDATE = "CANDIDATE"
    VALIDATED = "VALIDATED"
    CHAMPION = "CHAMPION"
    RETIRED = "RETIRED"


class ModelAlgorithm(str, Enum):
    RIDGE = "RIDGE"
    ELASTIC_NET = "ELASTIC_NET"
    LOGISTIC_REGRESSION = "LOGISTIC_REGRESSION"
    RANDOM_FOREST = "RANDOM_FOREST"
    GRADIENT_BOOSTING = "GRADIENT_BOOSTING"


class HorizonOutlook(str, Enum):
    BULLISH = "BULLISH"
    BEARISH = "BEARISH"
    NEUTRAL = "NEUTRAL"


class HorizonFeatureSet(BaseModel):
    name: str
    horizon: TradingHorizon
    feature_names: List[str]
    feature_definitions: Dict[str, str]
    lookback_days: int
    sources: List[str]
    version: str = "v1.0.0"
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)


class HorizonTargetSet(BaseModel):
    name: str
    horizon: TradingHorizon
    target_period: str
    target_type: TargetType
    formula: str
    threshold: Optional[float] = None
    benchmark_symbol: Optional[str] = None
    version: str = "v1.0.0"
    is_active: bool = True
    created_at: datetime = Field(default_factory=datetime.utcnow)


class HorizonModelConfig(BaseModel):
    name: str
    horizon: TradingHorizon
    target_period: str
    target_type: TargetType
    feature_set_version: str
    target_set_version: str
    algorithm: ModelAlgorithm
    hyperparameters: Dict[str, Any] = Field(default_factory=dict)
    retrain_frequency: str = "WEEKLY"
    version: str = "v1.0.0"
    is_active: bool = True


class HorizonDataset(BaseModel):
    dataset_name: str
    horizon: TradingHorizon
    feature_set_version: str
    target_set_version: str
    start_date: date
    end_date: date
    row_count: int
    column_count: int
    universe_version: str = "UNIVERSE_v1.0.0"
    data_version: str = "1"
    created_at: datetime = Field(default_factory=datetime.utcnow)


class HorizonPrediction(BaseModel):
    prediction_id: str
    symbol: str
    instrument_id: Optional[int] = None
    prediction_timestamp: datetime
    information_available_at: datetime
    calculated_at: datetime
    horizon: TradingHorizon
    horizon_period: str
    expected_return: Optional[float] = None
    probability_positive: Optional[float] = None
    probability_negative: Optional[float] = None
    predicted_class: Optional[int] = None
    expected_volatility: Optional[float] = None
    expected_drawdown: Optional[float] = None
    relative_return: Optional[float] = None
    confidence: float
    outlook: HorizonOutlook
    feature_contributions: Optional[Dict[str, float]] = None
    model_version: str
    feature_set_version: str
    target_set_version: str
    data_version: str = "1"


class HorizonEvaluationResult(BaseModel):
    model_id: str
    model_version: str
    horizon: TradingHorizon
    target_period: str
    evaluation_type: str = "WALK_FORWARD"
    sample_size: int
    mae: Optional[float] = None
    rmse: Optional[float] = None
    r2: Optional[float] = None
    directional_accuracy: Optional[float] = None
    ic: Optional[float] = None
    rank_ic: Optional[float] = None
    roc_auc: Optional[float] = None
    brier_score: Optional[float] = None
    log_loss: Optional[float] = None
    hit_rate: Optional[float] = None
    max_drawdown: Optional[float] = None
    baseline_metrics: Dict[str, Any] = Field(default_factory=dict)
    regime_metrics: Dict[str, Any] = Field(default_factory=dict)
    sector_metrics: Dict[str, Any] = Field(default_factory=dict)
    evaluation_timestamp: datetime = Field(default_factory=datetime.utcnow)


class HorizonConflict(BaseModel):
    symbol: str
    timestamp: datetime
    short_term_outlook: HorizonOutlook
    medium_term_outlook: HorizonOutlook
    long_term_outlook: HorizonOutlook
    conflict_detected: bool
    conflict_severity: str = "NONE"
    explanation: str
    short_term_pred_id: Optional[str] = None
    medium_term_pred_id: Optional[str] = None
    long_term_pred_id: Optional[str] = None


class CrossHorizonView(BaseModel):
    symbol: str
    as_of: datetime
    short_term: Optional[HorizonPrediction] = None
    medium_term: Optional[HorizonPrediction] = None
    long_term: Optional[HorizonPrediction] = None
    conflict: Optional[HorizonConflict] = None
    short_term_status: str = "AVAILABLE"
    medium_term_status: str = "AVAILABLE"
    long_term_status: str = "AVAILABLE"
    model_versions: Dict[str, str] = Field(default_factory=dict)
