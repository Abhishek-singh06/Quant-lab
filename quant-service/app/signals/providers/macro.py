"""
Macroeconomic Evidence Provider for QuantLab Part 13.
Consumes macroeconomic data (CPI, GDP, Repo Rate, PMI) strictly point-in-time.
"""

from datetime import datetime
from typing import Any, Dict, List, Optional
from app.signals.models import Evidence, EvidenceCategory, EvidenceDirection
from app.signals.providers.base import SignalEvidenceProvider


class MacroEvidenceProvider(SignalEvidenceProvider):
    """Calculates normalized macroeconomic evidence with publication timestamp enforcement."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.MACRO

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        macro_data = context.get("macro_data", {})
        if not macro_data:
            return evidence_list

        avail_at = macro_data.get("available_at")
        if avail_at and isinstance(avail_at, datetime) and avail_at > as_of_timestamp:
            return evidence_list

        # 1. Manufacturing & Services Composite PMI
        pmi = macro_data.get("composite_pmi")
        if pmi is not None:
            # >50 is expansion (+score), <50 is contraction (-score)
            score = max(-100.0, min(100.0, (pmi - 50.0) * 10.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="MACRO_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="COMPOSITE_PMI",
                raw_value=float(pmi),
                raw_value_str=f"PMI {pmi:.1f}",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=0.9,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"Composite PMI of {pmi:.1f} indicates economic {'expansion' if pmi>=50 else 'contraction'}"
            ))

        # 2. CPI Inflation vs RBI Target Band (4.0% +/- 2%)
        cpi = macro_data.get("cpi_inflation_pct")
        if cpi is not None:
            # 4.0% is target (+score), >6.0% breach upper band (-score)
            score = max(-100.0, min(100.0, (5.0 - cpi) * 40.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="MACRO_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="CPI_INFLATION",
                raw_value=float(cpi),
                raw_value_str=f"CPI {cpi:.1f}%",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=0.9,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"Headline CPI Inflation at {cpi:.1f}%"
            ))

        return evidence_list
