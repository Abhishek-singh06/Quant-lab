"""Tests for Phase 15 Robustness Evaluator, Multi-Seed Stability, and Cost Drag Modeling."""

import pytest
import numpy as np
import pandas as pd
from datetime import date, timedelta

from app.ml.walk_forward.phase15_robustness_evaluator import (
    Phase15RobustnessEvaluator,
    RobustnessScorecard,
)


class TestPhase15RobustnessEvaluator:
    """Rigorous test suite for Phase 15 multi-fold, perturbation, and friction evaluation."""

    @pytest.fixture
    def evaluator(self):
        return Phase15RobustnessEvaluator()

    @pytest.fixture
    def sample_data(self):
        """Generates realistic synthetic multi-stock time-series data for testing."""
        np.random.seed(42)
        symbols = ["TCS", "INFY", "RELIANCE", "HDFCBANK", "ICICIBANK"]
        dates = pd.date_range("2023-01-01", "2024-12-31", freq="B")

        rows = []
        for s in symbols:
            price = 1000.0 + np.random.uniform(-50, 50)
            for d in dates:
                ret = np.random.normal(0.0005, 0.015)
                price = max(price * (1.0 + ret), 10.0)
                rows.append({
                    "symbol": s,
                    "trading_date": d.strftime("%Y-%m-%d"),
                    "open": round(price * 0.998, 2),
                    "high": round(price * 1.01, 2),
                    "low": round(price * 0.99, 2),
                    "close": round(price, 2),
                    "adj_close": round(price, 2),
                    "volume": int(np.random.uniform(50000, 500000)),
                })
        return pd.DataFrame(rows)

    def test_extended_4_folds_chronology_and_purging(self, evaluator):
        """Verify that extended 4-fold walk-forward strictly obeys chronological ordering and purging."""
        folds = evaluator.get_extended_4_folds()
        assert len(folds) == 4

        for f in folds:
            # Check fold chronological order: train_start <= train_end < val_start <= val_end < oos_start <= oos_end
            assert f.train_start < f.train_end
            assert f.val_start < f.val_end
            assert f.oos_start < f.oos_end

            # Purge/Embargo between train and val
            assert f.train_end < f.val_start
            purge_val_days = (f.val_start - f.train_end).days
            assert purge_val_days >= 7, f"Purge/embargo between train and val must be >= 7 days, got {purge_val_days}"

            # Purge/Embargo between val and OOS
            assert f.val_end < f.oos_start
            purge_oos_days = (f.oos_start - f.val_end).days
            assert purge_oos_days >= 7, f"Purge/embargo between val and OOS must be >= 7 days, got {purge_oos_days}"

    def test_multi_seed_evaluation_stability(self, evaluator, sample_data):
        """Verify multi-seed evaluation calculates dispersion metrics across seeds."""
        feat_df = evaluator.base_evaluator.build_features_and_targets(sample_data)
        feature_cols = ["feat_ret_1d", "feat_ret_5d", "feat_vol_20d", "feat_rsi_14"]

        seed_stats = evaluator.run_multi_seed_test(
            df=feat_df,
            model_name="RIDGE",
            feature_cols=feature_cols,
            target_col="target_5d",
            seeds=[42, 101, 2023],
        )

        assert "mean" in seed_stats
        assert "std" in seed_stats
        assert "min" in seed_stats
        assert "max" in seed_stats
        assert seed_stats["std"] >= 0.0

    def test_feature_perturbation_resilience(self, evaluator, sample_data):
        """Verify feature perturbation drops the strongest feature and computes retention percentage."""
        feat_df = evaluator.base_evaluator.build_features_and_targets(sample_data)
        feature_cols = ["feat_ret_1d", "feat_ret_5d", "feat_vol_20d", "feat_rsi_14"]

        retention = evaluator.run_feature_perturbation_test(
            df=feat_df,
            model_name="RIDGE",
            feature_cols=feature_cols,
            target_col="target_5d",
        )

        assert isinstance(retention, float)
        assert 0.0 <= retention <= 200.0

    def test_portfolio_cost_simulation_friction_scaling(self, evaluator, sample_data):
        """Verify that increasing friction (0 bps -> 15 bps -> 50 bps) monotonically decreases net returns."""
        feat_df = evaluator.base_evaluator.build_features_and_targets(sample_data)
        feature_cols = ["feat_ret_1d", "feat_ret_5d", "feat_vol_20d"]

        sim_0bps = evaluator.run_portfolio_cost_simulation(feat_df, "RIDGE", feature_cols, cost_bps=0.0)
        sim_15bps = evaluator.run_portfolio_cost_simulation(feat_df, "RIDGE", feature_cols, cost_bps=15.0)
        sim_50bps = evaluator.run_portfolio_cost_simulation(feat_df, "RIDGE", feature_cols, cost_bps=50.0)

        # Higher transaction costs must reduce or keep equal net annualized return
        assert sim_0bps["ann_net_ret_pct"] >= sim_15bps["ann_net_ret_pct"]
        assert sim_15bps["ann_net_ret_pct"] >= sim_50bps["ann_net_ret_pct"]

    def test_full_robustness_audit_scorecard(self, evaluator, sample_data):
        """Verify full robustness scorecard builds and assigns proper classification."""
        scorecard = evaluator.run_full_robustness_audit(sample_data, model_name="RIDGE")

        assert isinstance(scorecard, RobustnessScorecard)
        assert scorecard.model_name == "RIDGE"
        assert scorecard.target_horizon == "target_5d"
        assert scorecard.classification in ["REJECTED", "RESEARCH_CANDIDATE", "ROBUST_RESEARCH_CANDIDATE", "PAPER_TRADING_CANDIDATE"]
        assert "target_1d" in scorecard.decay_curve
        assert "target_5d" in scorecard.decay_curve
