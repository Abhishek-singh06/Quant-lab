"""
Conflict Detection and Consensus Engine for QuantLab Part 13.
Detects disagreements across technical, fundamental, news, flow, and regime layers.
"""

from typing import Dict, List
from app.signals.models import ConflictAnalysis, ConflictSeverity, EvidenceDirection, SignalComponent


class ConflictDetector:
    """Calculates cross-layer consensus and conflict penalties."""

    def analyze_conflicts(
        self,
        components: Dict[str, SignalComponent]
    ) -> ConflictAnalysis:
        present_components = [c for c in components.values() if c.is_present]
        
        supporting_cats: List[str] = []
        opposing_cats: List[str] = []
        neutral_cats: List[str] = []
        conflict_details: List[str] = []
        
        pos_sum = 0.0
        neg_sum = 0.0
        
        for c in present_components:
            if c.category_score > 10.0:
                supporting_cats.append(c.category.value)
                pos_sum += c.category_score * c.weight
            elif c.category_score < -10.0:
                opposing_cats.append(c.category.value)
                neg_sum += abs(c.category_score) * c.weight
            else:
                neutral_cats.append(c.category.value)

        # Conflict arises when both strong positive and strong negative evidence coexist
        total_energy = pos_sum + neg_sum
        if total_energy > 0:
            # Ratio of the minority opposing side to total energy
            conflict_ratio = (2.0 * min(pos_sum, neg_sum)) / total_energy
            conflict_score = conflict_ratio * 100.0
        else:
            conflict_score = 0.0

        if conflict_score < 25.0:
            severity = ConflictSeverity.LOW
        elif conflict_score < 55.0:
            severity = ConflictSeverity.MEDIUM
        else:
            severity = ConflictSeverity.HIGH

        if severity == ConflictSeverity.HIGH:
            conflict_details.append(
                f"Severe cross-layer disagreement: {len(supporting_cats)} bullish categories vs {len(opposing_cats)} bearish categories"
            )
        elif severity == ConflictSeverity.MEDIUM:
            conflict_details.append(
                f"Moderate evidence tension between {', '.join(supporting_cats[:2])} and {', '.join(opposing_cats[:2])}"
            )

        consensus_score = pos_sum - neg_sum

        return ConflictAnalysis(
            conflict_severity=severity,
            conflict_score=conflict_score,
            consensus_score=consensus_score,
            supporting_categories=supporting_cats,
            opposing_categories=opposing_cats,
            neutral_categories=neutral_cats,
            conflict_details=conflict_details
        )
