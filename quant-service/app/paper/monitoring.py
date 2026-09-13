"""
Model and Risk Monitoring Engine for Paper Trading.
Continuously calculates rolling calibration, drift, and risk boundary compliance.
"""

from datetime import date, datetime
from typing import List, Dict, Optional
import numpy as np
from uuid import uuid4
from app.paper.schemas import (
    PaperPredictionOutcome,
    PaperModelMonitoringRecord,
    PaperRiskMonitoringRecord,
    ModelMonitoringStatus,
    ModelStatus
)


class ModelMonitoringEngine:
    """
    Evaluates live model performance metrics (MAE, RMSE, Directional Accuracy, IC)
    and flags model drift or degraded states.
    """

    @staticmethod
    def evaluate_model_health(
        model_version: str,
        horizon: str,
        outcomes: List[PaperPredictionOutcome],
        window_start: date,
        window_end: date
    ) -> PaperModelMonitoringRecord:
        if not outcomes or len(outcomes) < 5:
            return PaperModelMonitoringRecord(
                id=uuid4(),
                model_version=model_version,
                horizon=horizon,
                evaluation_window_start=window_start,
                evaluation_window_end=window_end,
                sample_size=len(outcomes),
                directional_accuracy=0.0,
                mae=0.0,
                rmse=0.0,
                drift_status=ModelMonitoringStatus.INSUFFICIENT_DATA,
                model_status=ModelStatus.ACTIVE
            )

        n = len(outcomes)
        correct_count = sum(1 for o in outcomes if o.is_direction_correct)
        dir_acc = (correct_count / n) * 100.0

        maes = [o.absolute_error for o in outcomes]
        rmses = [o.squared_error for o in outcomes]
        mae = float(np.mean(maes))
        rmse = float(np.sqrt(np.mean(rmses)))

        # IC calculation (Pearson correlation between expected & realized)
        exp_rets = np.array([o.expected_return for o in outcomes])
        real_rets = np.array([o.realized_return for o in outcomes])

        ic = None
        if len(exp_rets) > 5 and np.std(exp_rets) > 1e-6 and np.std(real_rets) > 1e-6:
            ic = float(np.corrcoef(exp_rets, real_rets)[0, 1])

        # Drift status assessment
        drift_status = ModelMonitoringStatus.NORMAL
        model_status = ModelStatus.ACTIVE

        if dir_acc < 45.0 or (ic is not None and ic < -0.05):
            drift_status = ModelMonitoringStatus.DRIFT
            model_status = ModelStatus.WARNING
        elif dir_acc < 50.0:
            drift_status = ModelMonitoringStatus.WARNING
            model_status = ModelStatus.ACTIVE

        return PaperModelMonitoringRecord(
            id=uuid4(),
            model_version=model_version,
            horizon=horizon,
            evaluation_window_start=window_start,
            evaluation_window_end=window_end,
            sample_size=n,
            directional_accuracy=round(dir_acc, 2),
            mae=round(mae, 4),
            rmse=round(rmse, 4),
            ic=round(ic, 4) if ic is not None else None,
            drift_status=drift_status,
            model_status=model_status
        )
