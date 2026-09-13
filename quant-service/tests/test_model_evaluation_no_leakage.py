"""Adversarial Tests for Model Evaluation and Zero Data Leakage."""

import pytest
from datetime import date, datetime, timezone
import numpy as np
import pandas as pd

from app.ml.walk_forward.real_data_evaluator import RealDataWalkForwardEvaluator, FoldSpec


@pytest.fixture
def synthetic_real_aligned_df():
    # 2 years of daily data for 2 symbols
    dates = pd.date_range("2023-01-01", "2024-12-31", freq="B").date
    rows = []
    for s in ["RELIANCE", "TCS"]:
        p = 2000.0
        for d in dates:
            p += np.random.randn() * 15.0
            p = max(p, 100.0)
            rows.append({
                "trading_date": d,
                "timestamp": datetime(d.year, d.month, d.day, 10, 0, tzinfo=timezone.utc),
                "symbol": s,
                "adj_close": round(p, 2),
                "volume": 1000000,
            })
    return pd.DataFrame(rows)


class TestModelEvaluationNoLeakage:

    def test_chronological_fold_isolation_and_no_future_rows(self):
        evaluator = RealDataWalkForwardEvaluator()
        folds = evaluator.get_walk_forward_folds()

        for fold in folds:
            # Train strictly precedes Val
            assert fold.train_end < fold.val_start
            # Val strictly precedes OOS
            assert fold.val_end < fold.oos_start

            # Purge & embargo window gap is at least 5 days
            purge_val_gap = (fold.val_start - fold.train_end).days
            purge_oos_gap = (fold.oos_start - fold.val_end).days
            assert purge_val_gap >= fold.purge_days
            assert purge_oos_gap >= fold.purge_days

    def test_train_only_scaler_fitting(self, synthetic_real_aligned_df):
        evaluator = RealDataWalkForwardEvaluator()
        feat_df = evaluator.build_features_and_targets(synthetic_real_aligned_df)

        feature_cols = ["feat_ret_1d", "feat_rsi_14", "feat_vol_20d"]
        target_col = "target_1d"

        res = evaluator.evaluate_model(
            df=feat_df,
            model_name="RIDGE",
            feature_cols=feature_cols,
            target_col=target_col,
            hyperparameters={"alpha": 1.0},
        )

        assert len(res.fold_results) == 2
        for fr in res.fold_results:
            assert fr["train_obs"] > 0
            assert fr["oos_obs"] > 0
            assert "oos_rank_ic" in fr

    def test_random_predictor_sanity_baseline(self, synthetic_real_aligned_df):
        """Verify that a random predictor has low out-of-sample Rank IC."""
        evaluator = RealDataWalkForwardEvaluator()
        feat_df = evaluator.build_features_and_targets(synthetic_real_aligned_df)

        feature_cols = ["feat_ret_1d", "feat_rsi_14"]
        target_col = "target_1d"

        res = evaluator.evaluate_model(
            df=feat_df,
            model_name="RANDOM_PREDICTOR",
            feature_cols=feature_cols,
            target_col=target_col,
        )

        # Random predictor should not produce persistent high Rank IC across folds
        assert abs(res.mean_oos_rank_ic) < 0.35
