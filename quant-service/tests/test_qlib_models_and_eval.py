"""Tests for Qlib Model Adapters, Evaluator, Backtest Comparator, and QuantLab Bridge."""

import pytest
import numpy as np
import pandas as pd

from app.qlib.provider import QuantLabQlibDataProvider
from app.qlib.loader import QuantLabDataLoader
from app.qlib.handler import QuantLabDataHandler, CSZScoreProcessor
from app.qlib.model_adapter import (
    QlibGBDTModel,
    QlibLinearModel,
    QlibRandomForestModel,
    QlibEnsembleModel,
    QuantLabQlibModelBridge,
)
from app.qlib.evaluator import QlibSignalEvaluator
from app.qlib.backtest_comparison import QlibBacktestComparator
from app.qlib.experiment_adapter import QlibExperimentManager


@pytest.fixture
def prepared_dataset():
    handler = QuantLabDataHandler(
        instruments=["RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK"],
        start_time="2023-01-01",
        end_time="2023-08-31",
        processors=[CSZScoreProcessor()],
    )
    return handler.setup_dataset(
        train_range=("2023-01-01", "2023-05-31"),
        test_range=("2023-06-01", "2023-08-31"),
    )


def test_qlib_model_fit_and_predict(prepared_dataset):
    # Test GBDT
    gbdt = QlibGBDTModel(hyperparameters={"max_iter": 20})
    gbdt.fit(prepared_dataset)
    preds = gbdt.predict(prepared_dataset, segment="test")
    assert len(preds) > 0
    assert preds.name == "score"

    # Test Linear (Ridge)
    linear = QlibLinearModel(hyperparameters={"kind": "ridge", "alpha": 1.0})
    linear.fit(prepared_dataset)
    l_preds = linear.predict(prepared_dataset, segment="test")
    assert len(l_preds) == len(preds)

    # Test Ensemble
    ens = QlibEnsembleModel(models=[(gbdt, 0.6), (linear, 0.4)])
    ens.fit(prepared_dataset)
    e_preds = ens.predict(prepared_dataset, segment="test")
    assert len(e_preds) == len(preds)


def test_quantlab_model_bridge(prepared_dataset):
    gbdt = QlibGBDTModel(hyperparameters={"max_iter": 10})
    bridge = QuantLabQlibModelBridge(qlib_model=gbdt, model_id="qlib_bridge_test")

    X_train, y_train = prepared_dataset.prepare("train", col_set=["feature", "label"])
    X_test, y_test = prepared_dataset.prepare("test", col_set=["feature", "label"])

    bridge.fit(X_train, y_train)
    assert bridge.is_fitted

    preds = bridge.predict(X_test)
    assert len(preds) == len(X_test)
    assert isinstance(preds, np.ndarray)

    eval_res = bridge.evaluate(X_test, y_test)
    assert "mae" in eval_res
    assert "rmse" in eval_res


def test_qlib_signal_evaluator(prepared_dataset):
    gbdt = QlibGBDTModel(hyperparameters={"max_iter": 10})
    gbdt.fit(prepared_dataset)
    preds = gbdt.predict(prepared_dataset, segment="test")
    y_test = prepared_dataset.prepare("test", col_set="label")

    evaluator = QlibSignalEvaluator(top_k=2, n_quantiles=3)
    metrics = evaluator.evaluate(preds, y_test)

    assert "ic_mean" in metrics
    assert "rank_ic_mean" in metrics
    assert "icir" in metrics
    assert "long_short_annualized_return" in metrics
    assert "long_short_sharpe" in metrics
    assert "mean_daily_turnover" in metrics
    assert "quantile_annualized_returns" in metrics


def test_qlib_backtest_comparator(prepared_dataset):
    gbdt = QlibGBDTModel(hyperparameters={"max_iter": 10})
    gbdt.fit(prepared_dataset)
    preds = gbdt.predict(prepared_dataset, segment="test")

    provider = QuantLabQlibDataProvider()
    price_panel = provider.get_market_data(
        instruments=["RELIANCE", "TCS", "INFY", "HDFCBANK", "ICICIBANK"],
        start_time="2023-06-01",
        end_time="2023-08-31",
    )

    comparator = QlibBacktestComparator(top_k=2, slippage_bps=5.0, brokerage_bps=3.0)
    res = comparator.run_comparison(preds, price_panel)

    assert "vectorized_qlib" in res
    assert "canonical_quantlab" in res
    assert "divergence" in res
    assert "cagr_drag_bps" in res["divergence"]
    assert "total_friction_costs_paid" in res["divergence"]


def test_qlib_experiment_manager(tmp_path):
    exp_mgr = QlibExperimentManager(base_dir=str(tmp_path / "experiments"))
    record = exp_mgr.record_experiment(
        experiment_name="test_alpha_exp",
        model_id="test_m1",
        model_type="GBDT",
        dataset_info={"n_samples": 100},
        hyperparameters={"lr": 0.05},
        metrics={"ic_mean": 0.08, "sharpe": 1.45},
    )

    assert record["experiment_name"] == "test_alpha_exp"
    experiments = exp_mgr.list_experiments()
    assert len(experiments) == 1
    assert experiments[0]["model_id"] == "test_m1"
