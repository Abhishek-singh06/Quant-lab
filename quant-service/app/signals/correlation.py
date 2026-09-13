"""
Correlation and Double-Counting Manager for QuantLab Part 13.
Prevents over-weighting of correlated evidence sources (e.g. SMA + NIFTY + Regime).
"""

from typing import Dict, List
from app.signals.models import EvidenceCategory, SignalComponent, SignalConfiguration


class CorrelationGroupManager:
    """Applies correlation group caps and normalizes category influences."""

    def __init__(self, config: SignalConfiguration):
        self.config = config

    def apply_correlation_controls(
        self,
        components: Dict[str, SignalComponent]
    ) -> Dict[str, SignalComponent]:
        """
        Adjusts component effective weighted contributions by dampening
        excessive concentration in correlated categories.
        """
        # Group components by configured correlation groups
        for group_name, category_list in self.config.correlation_groups.items():
            active_in_group = [
                components[cat] for cat in category_list
                if cat in components and components[cat].is_present
            ]
            
            if len(active_in_group) > 1:
                # Calculate aggregate group contribution
                group_contrib_sum = sum(c.weighted_contribution for c in active_in_group)
                group_weight_sum = sum(c.weight for c in active_in_group)
                
                # Apply correlation dampening factor (0.85 for 2 items, 0.75 for 3+ items)
                dampening = 0.85 if len(active_in_group) == 2 else 0.75
                
                for comp in active_in_group:
                    comp.weighted_contribution *= dampening
                    
        return components
