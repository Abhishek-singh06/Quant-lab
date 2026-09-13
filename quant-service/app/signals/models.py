"""
Signal Engine Domain Models and Types for QuantLab Part 13.
"""

from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum
from typing import Any, Dict, List, Optional
import uuid


class SignalType(str, Enum):
    BUY = "BUY"
    HOLD = "HOLD"
    SELL = "SELL"
    NO_SIGNAL = "NO_SIGNAL"


class EvidenceDirection(str, Enum):
    BULLISH = "BULLISH"
    BEARISH = "BEARISH"
    NEUTRAL = "NEUTRAL"


class ConflictSeverity(str, Enum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class SignalQualityStatus(str, Enum):
    HIGH_QUALITY = "HIGH_QUALITY"
    MEDIUM_QUALITY = "MEDIUM_QUALITY"
    LOW_QUALITY = "LOW_QUALITY"
    INSUFFICIENT_DATA = "INSUFFICIENT_DATA"


class EvidenceCategory(str, Enum):
    TECHNICAL = "TECHNICAL"
    FUNDAMENTAL = "FUNDAMENTAL"
    NEWS = "NEWS"
    CORPORATE_EVENTS = "CORPORATE_EVENTS"
    INSTITUTIONAL = "INSTITUTIONAL"
    MUTUAL_FUNDS = "MUTUAL_FUNDS"
    INDIAN_MARKET = "INDIAN_MARKET"
    GLOBAL_MARKET = "GLOBAL_MARKET"
    MACRO = "MACRO"
    ML_PREDICTION = "ML_PREDICTION"
    MARKET_REGIME = "MARKET_REGIME"


@dataclass
class Evidence:
    """Individual atomic piece of point-in-time evidence."""
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    source: str = ""
    category: EvidenceCategory = EvidenceCategory.TECHNICAL
    feature: str = ""
    raw_value: Optional[float] = None
    raw_value_str: Optional[str] = None
    normalized_score: float = 0.0  # -100 to +100
    direction: EvidenceDirection = EvidenceDirection.NEUTRAL
    strength: float = 0.0  # 0.0 to 1.0
    quality: float = 1.0  # 0.0 to 1.0
    freshness: float = 1.0  # 0.0 to 1.0
    confidence: float = 1.0  # 0.0 to 1.0
    weight: float = 1.0
    contribution: float = 0.0
    timestamp: Optional[datetime] = None
    available_at: Optional[datetime] = None
    version: str = "1.0.0"
    reason: str = ""
    is_valid: bool = True
    metadata: Dict[str, Any] = field(default_factory=dict)


@dataclass
class SignalComponent:
    """Category-level aggregated score and summary."""
    category: EvidenceCategory
    category_score: float = 0.0  # -100 to +100
    weight: float = 1.0
    weighted_contribution: float = 0.0
    direction: EvidenceDirection = EvidenceDirection.NEUTRAL
    strength: float = 0.0
    quality: float = 1.0
    freshness: float = 1.0
    is_present: bool = True
    missing_reason: Optional[str] = None
    evidence_items: List[Evidence] = field(default_factory=list)


@dataclass
class ConflictAnalysis:
    """Detailed conflict and consensus breakdown."""
    conflict_severity: ConflictSeverity = ConflictSeverity.LOW
    conflict_score: float = 0.0  # 0.0 to 100.0
    consensus_score: float = 0.0  # -100.0 to +100.0
    supporting_categories: List[str] = field(default_factory=list)
    opposing_categories: List[str] = field(default_factory=list)
    neutral_categories: List[str] = field(default_factory=list)
    conflict_details: List[str] = field(default_factory=list)


@dataclass
class SignalConfiguration:
    """Configuration parameters and category weights."""
    version: str = "SIGNAL_CFG_v1.0"
    min_supporting_categories: int = 3
    buy_threshold: float = 35.0
    sell_threshold: float = -35.0
    min_confidence: float = 0.50
    category_weights: Dict[str, float] = field(default_factory=lambda: {
        EvidenceCategory.TECHNICAL.value: 0.18,
        EvidenceCategory.FUNDAMENTAL.value: 0.16,
        EvidenceCategory.ML_PREDICTION.value: 0.16,
        EvidenceCategory.MARKET_REGIME.value: 0.14,
        EvidenceCategory.INSTITUTIONAL.value: 0.10,
        EvidenceCategory.NEWS.value: 0.08,
        EvidenceCategory.CORPORATE_EVENTS.value: 0.06,
        EvidenceCategory.GLOBAL_MARKET.value: 0.06,
        EvidenceCategory.INDIAN_MARKET.value: 0.04,
        EvidenceCategory.MACRO.value: 0.02,
    })
    category_caps: Dict[str, float] = field(default_factory=lambda: {
        EvidenceCategory.TECHNICAL.value: 100.0,
        EvidenceCategory.FUNDAMENTAL.value: 100.0,
        EvidenceCategory.ML_PREDICTION.value: 100.0,
        EvidenceCategory.MARKET_REGIME.value: 100.0,
        EvidenceCategory.INSTITUTIONAL.value: 100.0,
        EvidenceCategory.NEWS.value: 100.0,
    })
    correlation_groups: Dict[str, List[str]] = field(default_factory=lambda: {
        "TREND_GROUP": [EvidenceCategory.TECHNICAL.value, EvidenceCategory.INDIAN_MARKET.value, EvidenceCategory.MARKET_REGIME.value],
        "FUNDAMENTAL_VALUATION_GROUP": [EvidenceCategory.FUNDAMENTAL.value, EvidenceCategory.MACRO.value],
        "FLOW_GROUP": [EvidenceCategory.INSTITUTIONAL.value, EvidenceCategory.MUTUAL_FUNDS.value],
        "SENTIMENT_GROUP": [EvidenceCategory.NEWS.value, EvidenceCategory.CORPORATE_EVENTS.value],
    })


@dataclass
class SignalResult:
    """Final Point-in-Time Signal Result."""
    id: str = field(default_factory=lambda: str(uuid.uuid4()))
    symbol: str = ""
    signal_timestamp: datetime = field(default_factory=datetime.utcnow)
    information_available_at: datetime = field(default_factory=datetime.utcnow)
    calculated_at: datetime = field(default_factory=datetime.utcnow)
    
    # Core Decision
    signal: SignalType = SignalType.HOLD
    signal_score: float = 0.0  # -100 to +100
    confidence: float = 0.0    # 0.0 to 1.0
    direction: EvidenceDirection = EvidenceDirection.NEUTRAL
    
    # Quantitative Forecasts
    expected_return: Optional[float] = None
    expected_volatility: Optional[float] = None
    return_to_volatility_ratio: Optional[float] = None
    
    # Quality and Conflict
    conflict_severity: ConflictSeverity = ConflictSeverity.LOW
    conflict_score: float = 0.0
    data_quality_status: SignalQualityStatus = SignalQualityStatus.HIGH_QUALITY
    freshness_score: float = 1.0
    
    # Reasoning & Traceability
    reasoning: str = ""
    structured_reasoning: List[Dict[str, Any]] = field(default_factory=list)
    supporting_evidence: List[Dict[str, Any]] = field(default_factory=list)
    opposing_evidence: List[Dict[str, Any]] = field(default_factory=list)
    
    # Breakdown
    components: Dict[str, SignalComponent] = field(default_factory=dict)
    all_evidence: List[Evidence] = field(default_factory=list)
    
    # Lineage and Versions
    signal_version: str = "SIGNAL_v1.0.0"
    configuration_version: str = "SIGNAL_CFG_v1.0"
    feature_version: Optional[str] = "1.0.0"
    model_version: Optional[str] = None
    regime_version: Optional[str] = "REGIME_v1.0.0"
    data_version: Optional[str] = "1"
