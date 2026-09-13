"""Cross-Horizon Conflict Detection Engine."""

from typing import Optional
from datetime import datetime

from app.horizons.schemas import (
    HorizonPrediction,
    HorizonConflict,
    HorizonOutlook
)


class HorizonConflictDetector:
    """Detects alignment and divergences across Short, Medium, and Long Term horizons."""

    @staticmethod
    def detect_conflict(
        symbol: str,
        timestamp: datetime,
        short_term: Optional[HorizonPrediction],
        medium_term: Optional[HorizonPrediction],
        long_term: Optional[HorizonPrediction]
    ) -> HorizonConflict:
        st_out = short_term.outlook if short_term else HorizonOutlook.NEUTRAL
        mt_out = medium_term.outlook if medium_term else HorizonOutlook.NEUTRAL
        lt_out = long_term.outlook if long_term else HorizonOutlook.NEUTRAL

        outlooks = [st_out, mt_out, lt_out]
        has_bullish = HorizonOutlook.BULLISH in outlooks
        has_bearish = HorizonOutlook.BEARISH in outlooks

        conflict_detected = False
        severity = "NONE"
        explanation = "All available horizons are aligned in direction."

        if has_bullish and has_bearish:
            conflict_detected = True
            if st_out == HorizonOutlook.BEARISH and mt_out == HorizonOutlook.BULLISH and lt_out == HorizonOutlook.BULLISH:
                severity = "LOW"
                explanation = "Short-term technical weakness exists despite strong medium/long-term bullish thesis. Typical retracement or accumulation opportunity."
            elif st_out == HorizonOutlook.BULLISH and lt_out == HorizonOutlook.BEARISH:
                severity = "HIGH"
                explanation = "Short-term momentum breakout is opposing a long-term deteriorating fundamental thesis. High risk of bull trap."
            elif st_out == HorizonOutlook.BEARISH and lt_out == HorizonOutlook.BEARISH and mt_out == HorizonOutlook.BULLISH:
                severity = "MEDIUM"
                explanation = "Medium-term counter-trend bounce within a broader multi-year bear cycle."
            else:
                severity = "MEDIUM"
                explanation = f"Cross-horizon divergence: Short-term ({st_out.value}), Medium-term ({mt_out.value}), Long-term ({lt_out.value})."
        elif all(o == HorizonOutlook.BULLISH for o in outlooks if o != HorizonOutlook.NEUTRAL):
            explanation = "Full bullish confluence across active trading and investment horizons."
        elif all(o == HorizonOutlook.BEARISH for o in outlooks if o != HorizonOutlook.NEUTRAL):
            explanation = "Full bearish confluence across active trading and investment horizons."

        return HorizonConflict(
            symbol=symbol,
            timestamp=timestamp,
            short_term_outlook=st_out,
            medium_term_outlook=mt_out,
            long_term_outlook=lt_out,
            conflict_detected=conflict_detected,
            conflict_severity=severity,
            explanation=explanation,
            short_term_pred_id=short_term.prediction_id if short_term else None,
            medium_term_pred_id=medium_term.prediction_id if medium_term else None,
            long_term_pred_id=long_term.prediction_id if long_term else None
        )
