"""Comprehensive Quant Model Metrics and Evaluation Functions.
"""

from typing import Dict, Any, Optional
import numpy as np
import pandas as pd
from scipy.stats import spearmanr
from sklearn.metrics import (
    accuracy_score, precision_score, recall_score, f1_score,
    roc_auc_score, average_precision_score, brier_score_loss, log_loss
)


class QuantMetrics:
    """Calculates econometric, classification, and ranking metrics."""

    @staticmethod
    def compute_regression_metrics(y_true: np.ndarray, y_pred: np.ndarray) -> Dict[str, Any]:
        valid = ~np.isnan(y_true) & ~np.isnan(y_pred)
        if not valid.any():
            return {"status": "EMPTY"}

        yt = y_true[valid]
        yp = y_pred[valid]

        mae = float(np.mean(np.abs(yt - yp)))
        rmse = float(np.sqrt(np.mean((yt - yp) ** 2)))
        ss_tot = np.sum((yt - np.mean(yt)) ** 2)
        ss_res = np.sum((yt - yp) ** 2)
        r2 = float(1 - (ss_res / ss_tot)) if ss_tot > 0 else 0.0

        if np.std(yp) > 1e-8 and np.std(yt) > 1e-8:
            ic = float(np.corrcoef(yp, yt)[0, 1])
            rank_ic = float(spearmanr(yp, yt).statistic)
        else:
            ic = 0.0
            rank_ic = 0.0

        dir_acc = float(np.mean(np.sign(yp) == np.sign(yt)))

        return {
            "observations": int(len(yt)),
            "mae": round(mae, 6),
            "rmse": round(rmse, 6),
            "r2": round(r2, 6),
            "ic": round(ic, 4),
            "rank_ic": round(rank_ic, 4),
            "directional_accuracy": round(dir_acc, 4)
        }

    @staticmethod
    def compute_decile_spread(y_true: np.ndarray, y_pred: np.ndarray) -> Dict[str, Any]:
        """Calculates Top Decile vs Bottom Decile forward returns."""
        valid = ~np.isnan(y_true) & ~np.isnan(y_pred)
        if np.sum(valid) < 10:
            return {"top_decile_return": 0.0, "bottom_decile_return": 0.0, "long_short_spread": 0.0}

        df = pd.DataFrame({'pred': y_pred[valid], 'true': y_true[valid]})
        df['decile'] = pd.qcut(df['pred'], 10, labels=False, duplicates='drop')

        top_ret = float(df[df['decile'] == df['decile'].max()]['true'].mean())
        bottom_ret = float(df[df['decile'] == df['decile'].min()]['true'].mean())

        return {
            "top_decile_return": round(top_ret, 6),
            "bottom_decile_return": round(bottom_ret, 6),
            "long_short_spread": round(top_ret - bottom_ret, 6)
        }
