"""
Fundamental Evidence Provider for QuantLab Part 13.
Consumes Part 8 Fundamental Intelligence Engine outputs strictly point-in-time.
"""

from datetime import datetime
from typing import Any, Dict, List, Optional
from app.signals.models import Evidence, EvidenceCategory, EvidenceDirection
from app.signals.providers.base import SignalEvidenceProvider


class FundamentalEvidenceProvider(SignalEvidenceProvider):
    """Calculates normalized fundamental evidence from Part 8 point-in-time metrics."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.FUNDAMENTAL

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        ratios = context.get("fundamental_ratios", {})
        if not ratios:
            return evidence_list

        # Verify point-in-time availability
        avail_at = ratios.get("available_at")
        if avail_at and isinstance(avail_at, datetime) and avail_at > as_of_timestamp:
            # Future fundamental data - strictly skip
            return evidence_list

        # Calculate Freshness (fundamentals update quarterly, valid for ~100 days)
        freshness = 1.0
        if avail_at and isinstance(avail_at, datetime):
            days_old = (as_of_timestamp - avail_at).days
            freshness = max(0.2, 1.0 - (days_old / 180.0))

        # 1. Earnings Growth YoY
        eps_growth = ratios.get("eps_growth_yoy")
        if eps_growth is not None:
            # +30% growth -> +100 score, -30% growth -> -100 score
            score = max(-100.0, min(100.0, eps_growth * 333.3))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="FUNDAMENTAL_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="EPS_GROWTH_YOY",
                raw_value=float(eps_growth),
                raw_value_str=f"{eps_growth*100:+.1f}% YoY",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=freshness,
                weight=1.5,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"EPS growth YoY is {eps_growth*100:+.1f}% based on point-in-time filing"
            ))

        # 2. Return on Equity (ROE)
        roe = ratios.get("roe")
        if roe is not None:
            # >15% is strong in India, >25% very strong, <8% weak
            score = max(-100.0, min(100.0, (roe - 0.15) * 666.6))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="FUNDAMENTAL_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="RETURN_ON_EQUITY",
                raw_value=float(roe),
                raw_value_str=f"{roe*100:.1f}% ROE",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=freshness,
                weight=1.3,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"Return on Equity of {roe*100:.1f}% indicates {dir_enum.value.lower()} capital efficiency"
            ))

        # 3. Debt to Equity (Solvency & Capital Structure)
        de = ratios.get("debt_to_equity")
        if de is not None:
            # <0.5 is very healthy, >1.5 is high leverage
            score = max(-100.0, min(100.0, (0.8 - de) * 100.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="FUNDAMENTAL_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="DEBT_TO_EQUITY",
                raw_value=float(de),
                raw_value_str=f"{de:.2f}x D/E",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=freshness,
                weight=1.0,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"Debt-to-Equity at {de:.2f}x reflects balance sheet solvency profile"
            ))

        # 4. Valuation: P/E Ratio vs Benchmark
        pe = ratios.get("pe_ratio")
        if pe is not None and pe > 0:
            # Typical market benchmark ~22x. Not treating low PE naively as bullish value trap
            score = max(-100.0, min(100.0, (24.0 - pe) * 4.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source="FUNDAMENTAL_INTELLIGENCE_ENGINE",
                category=self.category,
                feature="PE_VALUATION",
                raw_value=float(pe),
                raw_value_str=f"{pe:.1f}x P/E",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=1.0,
                freshness=freshness,
                weight=1.2,
                timestamp=as_of_timestamp,
                available_at=avail_at or as_of_timestamp,
                version="1.0.0",
                reason=f"Point-in-time Price-to-Earnings of {pe:.1f}x relative to historical norm"
            ))

        return evidence_list
