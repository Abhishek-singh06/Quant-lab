"""
Reasoning and Traceability Engine for QuantLab Part 13.
Builds deterministic, structured explanations where every claim links to a specific evidence ID.
"""

from typing import Any, Dict, List, Tuple
from app.signals.models import ConflictAnalysis, Evidence, SignalComponent, SignalType


class ReasoningEngine:
    """Produces auditable structured reasoning traceable to individual evidence records."""

    def generate_reasoning(
        self,
        signal_type: SignalType,
        score: float,
        confidence: float,
        components: Dict[str, SignalComponent],
        conflict: ConflictAnalysis,
        all_evidence: List[Evidence]
    ) -> Tuple[str, List[Dict[str, Any]], List[Dict[str, Any]], List[Dict[str, Any]]]:
        structured_reasoning: List[Dict[str, Any]] = []
        supporting_items: List[Dict[str, Any]] = []
        opposing_items: List[Dict[str, Any]] = []

        # Sort evidence by contribution magnitude
        sorted_evidence = sorted(all_evidence, key=lambda e: abs(e.normalized_score * e.weight), reverse=True)

        for ev in sorted_evidence:
            item = {
                "evidence_id": ev.id,
                "category": ev.category.value,
                "feature": ev.feature,
                "normalized_score": round(ev.normalized_score, 1),
                "direction": ev.direction.value,
                "reason": ev.reason,
                "source": ev.source,
                "freshness": round(ev.freshness, 2),
                "available_at": ev.available_at.isoformat() if ev.available_at else None
            }
            if ev.normalized_score > 15.0:
                supporting_items.append(item)
            elif ev.normalized_score < -15.0:
                opposing_items.append(item)

        # Build structured statements with traceability
        if signal_type == SignalType.BUY:
            top_support = [e for e in sorted_evidence if e.normalized_score > 15.0][:3]
            support_categories = list(set(e.category.value for e in top_support))
            stmt = f"BUY decision (Score: {score:+.1f}, Confidence: {confidence*100:.0f}%) is supported by strong alignment in {', '.join(support_categories)}."
            structured_reasoning.append({
                "statement": stmt,
                "traceable_evidence_ids": [e.id for e in top_support]
            })

            top_oppose = [e for e in sorted_evidence if e.normalized_score < -15.0][:2]
            if top_oppose:
                opp_stmt = f"Key risk factors and opposing headwinds: {'; '.join(e.reason for e in top_oppose)}."
                structured_reasoning.append({
                    "statement": opp_stmt,
                    "traceable_evidence_ids": [e.id for e in top_oppose]
                })

        elif signal_type == SignalType.SELL:
            top_bearish = [e for e in sorted_evidence if e.normalized_score < -15.0][:3]
            bear_categories = list(set(e.category.value for e in top_bearish))
            stmt = f"SELL decision (Score: {score:+.1f}, Confidence: {confidence*100:.0f}%) is driven by deterioration in {', '.join(bear_categories)}."
            structured_reasoning.append({
                "statement": stmt,
                "traceable_evidence_ids": [e.id for e in top_bearish]
            })

        else:  # HOLD or NO_SIGNAL
            if conflict.conflict_score > 50.0:
                stmt = f"HOLD decision due to high cross-layer conflict ({conflict.conflict_score:.0f}% conflict). Bullish drivers in {', '.join(conflict.supporting_categories[:2])} offset by bearish signals in {', '.join(conflict.opposing_categories[:2])}."
            else:
                stmt = f"HOLD decision due to balanced/neutral evidence (Score: {score:+.1f}) with insufficient directional conviction for execution."
            structured_reasoning.append({
                "statement": stmt,
                "traceable_evidence_ids": [e.id for e in sorted_evidence[:4]]
            })

        summary_text = " ".join(s["statement"] for s in structured_reasoning)

        return summary_text, structured_reasoning, supporting_items, opposing_items
