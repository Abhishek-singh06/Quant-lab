"""Tests for DeepLearningModelAdapter and WalkForwardEnsembleComparator."""

import pytest
import numpy as np
import pandas as pd

from app.deep_learning.adapter import DeepLearningModelAdapter
from app.deep_learning.ensemble import WalkForwardEnsembleComparator


@pytest.fixture
def synthetic_panel_df():
    rng = np.random.RandomState(42)
    n_bars = 120
    dfs = []
    for sym in ["HDFCBANK", "ICICIBANK"]:
        dates = pd.date_range("2024-01-01", periods=n_bars, freq="D")
        f1 = rng.randn(n_bars) * 0.02
        f2 = rng.randn(n_bars) * 0.05
        # Target
        target = f1 * 0.4 + rng.randn(n_bars) * 0.01

        df_sym = pd.DataFrame({
            "timestamp": dates,
            "symbol": sym,
            "feat_1": f1,
            "feat_2": f2,
            "target": target,
        })
        dfs.append(df_sym)

    return pd.concat(dfs, ignore_index=True)


class TestDeepLearningModelAdapter:

    def test_adapter_fit_predict_evaluate_explain(self, synthetic_panel_df):
        adapter = DeepLearningModelAdapter(
            model_type_name="GRU",
            lookback=15,
            horizon=1,
            hidden_dim=16,
            epochs=3,
            random_seed=42,
        )

        adapter.fit(synthetic_panel_df)
        assert adapter.is_fitted
        assert adapter.provenance_hash is not None
        assert len(adapter.provenance_hash) == 64  # SHA-256 length

        # Predict
        preds = adapter.predict(synthetic_panel_df)
        assert len(preds) > 0
        assert not np.any(np.isnan(preds))

        # Evaluate
        eval_metrics = adapter.evaluate(synthetic_panel_df)
        assert "ic" in eval_metrics
        assert "rank_ic" in eval_metrics
        assert "directional_accuracy" in eval_metrics
        assert "mae" in eval_metrics

        # Explain
        explanations = adapter.explain(synthetic_panel_df)
        assert len(explanations) == len(preds)
        assert "lookback_lag_0" in explanations[0]


class TestWalkForwardEnsembleComparator:

    def test_comparator_execution(self, synthetic_panel_df):
        comparator = WalkForwardEnsembleComparator(
            lookback=15,
            horizon=1,
            n_splits=2,
            cost_bps=15.0,
            random_seed=42,
        )

        results = comparator.run_comparison(synthetic_panel_df)
        assert "metrics" in results
        metrics = results["metrics"]

        assert "classical_ridge" in metrics
        assert "classical_gbdt" in metrics
        assert "dl_gru" in metrics
        assert "dl_lstm" in metrics
        assert "ensemble_hybrid" in metrics

        for m_name, d in metrics.items():
            assert "ic" in d
            assert "rank_ic" in d
            assert "directional_accuracy" in d
            assert "sharpe_net" in d
            assert "max_drawdown" in d
