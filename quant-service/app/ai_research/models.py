"""
Data models for QuantLab Fundamental Intelligence & AI Research.
"""
from enum import Enum
from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field
from datetime import datetime


class ChecklistStatus(str, Enum):
    PASS = "PASS"
    WARNING = "WARNING"
    FAIL = "FAIL"
    UNKNOWN = "UNKNOWN"


class ResearchSourceType(str, Enum):
    FINANCIAL_STATEMENT = "FINANCIAL_STATEMENT"
    REGULATORY_FILING = "REGULATORY_FILING"
    MARKET_DATA = "MARKET_DATA"
    INSTITUTIONAL_DATA = "INSTITUTIONAL_DATA"
    NEWS_DISCLOSURE = "NEWS_DISCLOSURE"
    QUANT_MODEL = "QUANT_MODEL"
    MACRO_REGIME = "MACRO_REGIME"


class ResearchEvidence(BaseModel):
    id: str
    source_type: ResearchSourceType
    source_name: str
    document_id: Optional[str] = None
    period_end: Optional[str] = None
    published_at: Optional[datetime] = None
    information_available_at: datetime
    metric_name: str
    value: Any
    unit: Optional[str] = None
    url: Optional[str] = None
    checksum: Optional[str] = None


class ChecklistItem(BaseModel):
    item_id: int
    category: str
    name: str
    metric_name: str
    value: Optional[Any] = None
    threshold: str
    status: ChecklistStatus = ChecklistStatus.UNKNOWN
    evidence_id: Optional[str] = None
    explanation: str
    confidence: float = Field(default=1.0, ge=0.0, le=1.0)
    as_of: Optional[datetime] = None


class AgentPerspective(BaseModel):
    agent_name: str
    role: str
    score: float = Field(ge=0.0, le=5.0)
    thesis: str
    key_positives: List[str] = Field(default_factory=list)
    key_risks: List[str] = Field(default_factory=list)
    recommendation: str  # e.g. "FAVORABLE", "CAUTIOUS", "AVOID", "NEUTRAL"
    evidence_ids: List[str] = Field(default_factory=list)


class ConflictItem(BaseModel):
    dimension_a: str
    dimension_b: str
    description: str
    severity: str  # e.g. "HIGH", "MEDIUM", "LOW"
    evidence_ids: List[str] = Field(default_factory=list)
    recommended_action: str


class ResearchSection(BaseModel):
    section_id: str
    title: str
    content: str
    claims: List[str] = Field(default_factory=list)
    evidence_ids: List[str] = Field(default_factory=list)


class ResearchContext(BaseModel):
    symbol: str
    company_name: str
    context_as_of: datetime
    financial_ratios: Dict[str, Any] = Field(default_factory=dict)
    financial_statements: List[Dict[str, Any]] = Field(default_factory=list)
    corporate_filings: List[Dict[str, Any]] = Field(default_factory=list)
    news_disclosures: List[Dict[str, Any]] = Field(default_factory=list)
    market_data: Dict[str, Any] = Field(default_factory=dict)
    institutional_flows: Dict[str, Any] = Field(default_factory=dict)
    macro_regime: Dict[str, Any] = Field(default_factory=dict)
    quant_signals: Dict[str, Any] = Field(default_factory=dict)
    evidence_registry: List[ResearchEvidence] = Field(default_factory=list)


class ResearchReport(BaseModel):
    report_id: str
    run_id: str
    symbol: str
    company_name: str
    context_as_of: datetime
    generated_at: datetime = Field(default_factory=datetime.utcnow)
    business_quality_score: float = Field(ge=0.0, le=5.0)
    financial_health_score: float = Field(ge=0.0, le=5.0)
    valuation_score: float = Field(ge=0.0, le=5.0)
    risk_score: float = Field(ge=0.0, le=5.0)
    overall_score: float = Field(ge=0.0, le=5.0)
    checklist: List[ChecklistItem] = Field(default_factory=list)
    checklist_passed: int = 0
    checklist_warnings: int = 0
    checklist_failed: int = 0
    checklist_unknown: int = 0
    agent_reviews: List[AgentPerspective] = Field(default_factory=list)
    conflicts: List[ConflictItem] = Field(default_factory=list)
    sections: List[ResearchSection] = Field(default_factory=list)
    evidence_count: int = 0
    llm_provider: str
    execution_mode: str  # "LLM_AUGMENTED" or "DETERMINISTIC_FALLBACK"
    provenance_hash: str
