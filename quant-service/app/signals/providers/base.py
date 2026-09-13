"""
Abstract Base Class for Signal Evidence Providers.
"""

from abc import ABC, abstractmethod
from datetime import datetime
from typing import Any, Dict, List, Optional
from app.signals.models import Evidence, EvidenceCategory, SignalComponent, EvidenceDirection


class SignalEvidenceProvider(ABC):
    """Abstract evidence provider interface."""
    
    @property
    @abstractmethod
    def category(self) -> EvidenceCategory:
        """Category of evidence provided."""
        pass
    
    @abstractmethod
    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        """
        Collect point-in-time evidence strictly available at or before as_of_timestamp.
        Must enforce T_avail <= as_of_timestamp.
        """
        pass
    
    def build_component_summary(
        self,
        evidence_list: List[Evidence],
        configured_weight: float = 1.0,
        category_cap: Optional[float] = None
    ) -> SignalComponent:
        """Summarize collected evidence into category component score."""
        if not evidence_list:
            return SignalComponent(
                category=self.category,
                category_score=0.0,
                weight=configured_weight,
                weighted_contribution=0.0,
                is_present=False,
                missing_reason=f"No point-in-time {self.category.value} data available"
            )
            
        valid_evidence = [e for e in evidence_list if e.is_valid]
        if not valid_evidence:
            return SignalComponent(
                category=self.category,
                category_score=0.0,
                weight=configured_weight,
                weighted_contribution=0.0,
                is_present=False,
                missing_reason=f"All {self.category.value} evidence invalidated by data quality checks"
            )
            
        total_weight = sum(e.weight for e in valid_evidence) or 1.0
        weighted_score = sum(e.normalized_score * e.weight for e in valid_evidence) / total_weight
        
        # Apply category cap if configured
        if category_cap is not None:
            clamped_score = max(-category_cap, min(category_cap, weighted_score))
        else:
            clamped_score = max(-100.0, min(100.0, weighted_score))
            
        avg_quality = sum(e.quality for e in valid_evidence) / len(valid_evidence)
        avg_freshness = sum(e.freshness for e in valid_evidence) / len(valid_evidence)
        avg_strength = sum(e.strength for e in valid_evidence) / len(valid_evidence)
        
        direction = (
            EvidenceDirection.BULLISH if clamped_score > 10.0
            else EvidenceDirection.BEARISH if clamped_score < -10.0
            else EvidenceDirection.NEUTRAL
        )
        
        weighted_contribution = clamped_score * configured_weight
        
        return SignalComponent(
            category=self.category,
            category_score=clamped_score,
            weight=configured_weight,
            weighted_contribution=weighted_contribution,
            direction=direction,
            strength=avg_strength,
            quality=avg_quality,
            freshness=avg_freshness,
            is_present=True,
            evidence_items=valid_evidence
        )
