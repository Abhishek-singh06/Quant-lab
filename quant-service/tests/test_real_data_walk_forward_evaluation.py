"""Tests for RealDataWalkForwardEvaluator and Experiment Registry."""

import pytest
from datetime import date, datetime, timezone
import numpy as np
import pandas as pd

from app.ml.walk_forward.real_data_evaluator import RealDataWalkForwardEvaluator


@pytest.fixture
def test_market_df():
    # 2 years of daily data for 3 symbols
    dates = pd.date_range("2023-01-01", "2024-12-31", freq="B").date
    rows = []
    for s in ["RELIANCE", "TCS", "INFY"]:
        p = 1500.0
        for d in dates:
            p += np.random.randn() * 10.0
            p = max(p, 50.0)
            rows.append({
                "trading_date": d,
                "timestamp": datetime(d.year, d.month, d.day, 10, 0, tzinfo=timezone.utc),
                "symbol": s,
                "adj_close": round(p, 2),
                "volume": 2000000,
            })
    return pd.DataFrame(rows)


class TestRealDataWalkForwardEvaluation:

    def test_feature_engineering_pit_integrity(self, test_market_df):
        evaluator = RealDataWalkForwardEvaluator()
        feat_df = evaluator.build_features_and_targets(test_market_df)

        assert not feat_df.empty
        assert "feat_ret_1d" in feat_df.columns
        assert "feat_rsi_14" in feat_df.columns
        assert "feat_vol_20d" in feat_df.columns
        assert "feat_trend_ratio" in feat_df.columns
        assert "target_1d" in feat_df.columns
        assert "target_5d" in feat_df.columns
        assert "target_20d" in feat_df.columns

    def test_model_comparisons_and_experiment_registry(self, test_market_df):
        evaluator = RealDataWalkForwardEvaluator()
        feat_df = evaluator.build_features_and_targets(test_market_df)

        tech_features = ["feat_ret_1d", "feat_rsi_14", "feat_vol_20d"]
        all_features = ["feat_ret_1d", "feat_rsi_14", "feat_vol_20d", "feat_trend_ratio"]

        # Run Ridge on tech features
        r_ridge = evaluator.evaluate_model(
            df=feat_df,
            model_name="RIDGE",
            feature_cols=tech_features,
            target_col="target_1d",
            hyperparameters={"alpha": 10.0},
        )

        # Run Random Forest on all features (ablation)
        r_rf = evaluator.evaluate_model(
            df=feat_df,
            model_name="RANDOM_FOREST",
            feature_cols=all_features,
            target_col="target_1d",
            hyperparameters={"n_estimators": 20, "max_depth": 3},
        )

        # Run HistGradientBoosting on all features
        r_gb = evaluator.evaluate_model(
            df=feat_df,
            model_name="HIST_GRADIENT_BOOSTING",
            feature_cols=all_features,
            target_col="target_1d",
            hyperparameters={"max_depth": 3, "learning_rate": 0.05},
        )

        # Run Logistic Regression on directional target
        r_log = evaluator.evaluate_model(
            df=feat_df,
            model_name="LOGISTIC_REGRESSION",
            feature_cols=all_features,
            target_col="target_1d",
        )

        # Registry should have 4 logged experiments
        assert len(evaluator.experiments_registry) == 4
        assert r_ridge.experiment_id.startswith("EXP_")
        assert r_rf.experiment_id.startswith("EXP_")
        assert r_gb.experiment_id.startswith("EXP_")
        assert r_log.experiment_id.startswith("EXP_")
