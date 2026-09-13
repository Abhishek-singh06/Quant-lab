"""
Expected vs Realized Analytics Engine for Paper Trading.
Calculates calibration errors, prediction accuracy, and outcome status after horizons elapse.
"""

from datetime import datetime
from typing import Dict, List, Optional
from uuid import uuid4
from app.paper.schemas import (
    PaperTradingDecision,
    PaperPredictionOutcome,
    PaperSignalOutcome,
    SignalOutcomeStatus
)


class ExpectedVsRealizedEngine:
    """
    Evaluates mathematical prediction accuracy and signal calibration against actual market outcomes.
    """

    @staticmethod
    def evaluate_decision_outcome(
        decision: PaperTradingDecision,
        entry_price: float,
        exit_or_current_price: float,
        evaluation_timestamp: datetime,
        is_horizon_elapsed: bool = True
    ) -> Optional[PaperPredictionOutcome]:
        if not is_horizon_elapsed or entry_price <= 0:
            return None

        expected_ret = decision.expected_return or 0.0
        realized_ret = (exit_or_current_price - entry_price) / entry_price
        pred_error = realized_ret - expected_ret

        # Direction match check
        if expected_ret > 0:
            is_dir_correct = realized_ret > 0
        elif expected_ret < 0:
            is_dir_correct = realized_ret < 0
        else:
            is_dir_correct = abs(realized_ret) < 0.005

        return PaperPredictionOutcome(
            id=uuid4(),
            decision_id=decision.id,
            prediction_id=decision.prediction_id,
            model_version=decision.model_version or "v1.0.0",
            symbol=decision.symbol,
            horizon=decision.horizon,
            prediction_timestamp=decision.timestamp,
            evaluation_timestamp=evaluation_timestamp,
            expected_return=expected_ret,
            realized_return=realized_ret,
            prediction_error=pred_error,
            absolute_error=abs(pred_error),
            squared_error=pred_error ** 2,
            expected_volatility=decision.expected_volatility,
            realized_volatility=abs(pred_error),
            is_direction_correct=is_dir_correct
        )

    @staticmethod
    def evaluate_signal_outcome(
        decision: PaperTradingDecision,
        entry_price: float,
        exit_or_current_price: float,
        evaluation_timestamp: datetime,
        is_horizon_elapsed: bool = True
    ) -> PaperSignalOutcome:
        if not is_horizon_elapsed:
            return PaperSignalOutcome(
                id=uuid4(),
                decision_id=decision.id,
                symbol=decision.symbol,
                horizon=decision.horizon,
                signal_timestamp=decision.timestamp,
                evaluation_timestamp=evaluation_timestamp,
                expected_direction=decision.decision,
                realized_direction="PENDING",
                expected_return=decision.expected_return or 0.0,
                realized_return=0.0,
                outcome_status=SignalOutcomeStatus.NO_OUTCOME_YET
            )

        expected_ret = decision.expected_return or 0.0
        realized_ret = ((exit_or_current_price - entry_price) / entry_price) if entry_price > 0 else 0.0

        if realized_ret > 0:
            realized_dir = "BULLISH"
        elif realized_ret < 0:
            realized_dir = "BEARISH"
        else:
            realized_dir = "NEUTRAL"

        # Signal outcome status
        if decision.decision == "BUY":
            status = SignalOutcomeStatus.CORRECT_DIRECTION if realized_ret > 0 else SignalOutcomeStatus.WRONG_DIRECTION
        elif decision.decision == "SELL":
            status = SignalOutcomeStatus.CORRECT_DIRECTION if realized_ret < 0 else SignalOutcomeStatus.WRONG_DIRECTION
        else:
            status = SignalOutcomeStatus.PARTIAL

        # Evidence attribution snapshot
        attribution = {
            "signal_score": decision.signal_score,
            "confidence": decision.signal_confidence,
            "supporting_count": len(decision.supporting_evidence) if decision.supporting_evidence else 0,
            "opposing_count": len(decision.opposing_evidence) if decision.opposing_evidence else 0,
            "realized_return_pct": round(realized_ret * 100.0, 2)
        }

        return PaperSignalOutcome(
            id=uuid4(),
            decision_id=decision.id,
            symbol=decision.symbol,
            horizon=decision.horizon,
            signal_timestamp=decision.timestamp,
            evaluation_timestamp=evaluation_timestamp,
            expected_direction=decision.decision,
            realized_direction=realized_dir,
            expected_return=expected_ret,
            realized_return=realized_ret,
            outcome_status=status,
            attribution=attribution
        )
