"""Evaluation and Validation Engine for Trading and Investing Horizons."""

from typing import Dict, Any, Optional
import numpy as np
import pandas as pd
from scipy.stats import spearmanr, pearsonr
from datetime import datetime

from app.horizons.schemas import TradingHorizon, HorizonEvaluationResult, TargetType
from app.horizons.models.base import HorizonModel


class HorizonEvaluator:
    """Computes rigorous statistical evaluation metrics and baseline comparisons."""

    @staticmethod
    def evaluate(
        model: HorizonModel,
        X_test: pd.DataFrame,
        y_test: pd.Series,
        evaluation_type: str = "WALK_FORWARD",
        regimes: Optional[pd.Series] = None
    ) -> HorizonEvaluationResult:
        """Evaluate model against ground truth y_test."""
        y_pred = model.predict(X_test)
        y_true = y_test.values

        sample_size = len(y_true)
        if sample_size == 0:
            raise ValueError("Test dataset is empty.")

        # Baseline predictions
        mean_baseline = np.full_like(y_true, np.mean(y_true))
        zero_baseline = np.zeros_like(y_true)

        mae = float(np.mean(np.abs(y_pred - y_true)))
        rmse = float(np.sqrt(np.mean((y_pred - y_true) ** 2)))
        ss_tot = np.sum((y_true - np.mean(y_true)) ** 2)
        ss_res = np.sum((y_true - y_pred) ** 2)
        r2 = float(1.0 - (ss_res / ss_tot)) if ss_tot > 0 else 0.0

        # Directional accuracy
        dir_correct = np.sign(y_pred) == np.sign(y_true)
        directional_accuracy = float(np.mean(dir_correct))

        # IC and Rank IC
        ic = 0.0
        rank_ic = 0.0
        if sample_size >= 5 and np.std(y_pred) > 1e-9 and np.std(y_true) > 1e-9:
            try:
                ic_val, _ = pearsonr(y_pred, y_true)
                ic = float(ic_val) if not np.isnan(ic_val) else 0.0
                rank_ic_val, _ = spearmanr(y_pred, y_true)
                rank_ic = float(rank_ic_val) if not np.isnan(rank_ic_val) else 0.0
            except Exception:
                pass

        # Classification metrics if supported
        roc_auc = None
        brier_score = None
        log_loss = None
        proba = model.predict_proba(X_test)
        if proba is not None and proba.shape[1] >= 2:
            p1 = proba[:, 1]
            y_binary = np.where(y_true > 0, 1, 0)
            brier_score = float(np.mean((p1 - y_binary) ** 2))
            p_clipped = np.clip(p1, 1e-7, 1.0 - 1e-7)
            log_loss = float(-np.mean(y_binary * np.log(p_clipped) + (1 - y_binary) * np.log(1 - p_clipped)))
            try:
                from sklearn.metrics import roc_auc_score
                if len(np.unique(y_binary)) > 1:
                    roc_auc = float(roc_auc_score(y_binary, p1))
            except Exception:
                pass

        # Baselines
        base_mae = float(np.mean(np.abs(mean_baseline - y_true)))
        base_rmse = float(np.sqrt(np.mean((mean_baseline - y_true) ** 2)))
        baseline_metrics = {
            "mean_baseline_mae": base_mae,
            "mean_baseline_rmse": base_rmse,
            "zero_baseline_mae": float(np.mean(np.abs(zero_baseline - y_true))),
            "ic_vs_baseline_advantage": max(0.0, ic)
        }

        # Regime breakdown
        regime_metrics = {}
        if regimes is not None and len(regimes) == sample_size:
            for reg in np.unique(regimes):
                idx = np.where(regimes == reg)[0]
                if len(idx) >= 5:
                    r_pred = y_pred[idx]
                    r_true = y_true[idx]
                    r_ic, _ = spearmanr(r_pred, r_true)
                    regime_metrics[f"{reg}_rank_ic"] = float(r_ic) if not np.isnan(r_ic) else 0.0
                    regime_metrics[f"{reg}_mae"] = float(np.mean(np.abs(r_pred - r_true)))

        return HorizonEvaluationResult(
            model_id=model.model_id,
            model_version=model.model_version,
            horizon=model.horizon,
            target_period=model.target_period,
            evaluation_type=evaluation_type,
            sample_size=sample_size,
            mae=mae,
            rmse=rmse,
            r2=r2,
            directional_accuracy=directional_accuracy,
            ic=ic,
            rank_ic=rank_ic,
            roc_auc=roc_auc,
            brier_score=brier_score,
            log_loss=log_loss,
            baseline_metrics=baseline_metrics,
            regime_metrics=regime_metrics,
            sector_metrics={},
            evaluation_timestamp=datetime.utcnow()
        )
