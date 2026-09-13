"""Tests for Part 11 Quant Prediction Models.

Verifies:
1. Return Regression Models (Ridge, Random Forest, Gradient Boosting)
2. Direction Classification Models (Logistic, Random Forest, Gradient Boosting)
3. Realized Volatility Prediction Models (Persistence, Random Forest, Gradient Boosting)
4. Feature importance and Explainability
5. Model serialization and deserialization roundtrip
"""

import os
import pytest
import numpy as np
import pandas as pd

from app.ml.models.regression import RegressionModel
from app.ml.models.classification import ClassificationModel
from app.ml.models.volatility import VolatilityModel
from app.ml.walk_forward.registry import LocalModelRegistry


def generate_synthetic_feature_data(n_samples=200):
    np.random.seed(42)
    X = pd.DataFrame({
        'RSI_14': np.random.uniform(20, 80, size=n_samples),
        'MOMENTUM_20D': np.random.normal(0.01, 0.03, size=n_samples),
        'SMA_50_DIST': np.random.normal(0.0, 0.05, size=n_samples),
        'INDIA_VIX': np.random.uniform(11, 25, size=n_samples),
        'FII_FLOW_SCORE': np.random.uniform(-50, 50, size=n_samples)
    })
    # Target linearly correlated with momentum and RSI with noise
    y_reg = X['MOMENTUM_20D'] * 0.4 + (X['RSI_14'] - 50) * 0.0005 + np.random.normal(0, 0.005, size=n_samples)
    y_cls = (y_reg > 0).astype(int)
    y_vol = np.abs(np.random.normal(0.14, 0.04, size=n_samples))

    return X, y_reg, y_cls, y_vol


def test_regression_models_fit_and_predict():
    X, y_reg, _, _ = generate_synthetic_feature_data(200)

    for algo in ["RIDGE", "RANDOM_FOREST", "GRADIENT_BOOSTING", "HISTORICAL_MEAN"]:
        model = RegressionModel(model_id=f"TEST-REG-{algo}", algorithm=algo)
        model.fit(X, y_reg)
        preds = model.predict(X)

        assert len(preds) == len(X)
        eval_metrics = model.evaluate(X, y_reg)
        assert "mae" in eval_metrics
        assert "rmse" in eval_metrics
        assert "ic" in eval_metrics
        assert "directional_accuracy" in eval_metrics


def test_classification_models_fit_and_predict_proba():
    X, _, y_cls, _ = generate_synthetic_feature_data(200)

    for algo in ["LOGISTIC_REGRESSION", "RANDOM_FOREST_CLASSIFIER", "GRADIENT_BOOSTING_CLASSIFIER", "MAJORITY_BASELINE"]:
        model = ClassificationModel(model_id=f"TEST-CLS-{algo}", algorithm=algo)
        model.fit(X, y_cls)
        preds = model.predict(X)
        probs = model.predict_proba(X)

        assert len(preds) == len(X)
        assert probs.shape == (len(X), 2)
        # Check probabilities sum to 1
        assert np.allclose(probs.sum(axis=1), 1.0)

        eval_metrics = model.evaluate(X, y_cls)
        assert "accuracy" in eval_metrics
        assert "roc_auc" in eval_metrics
        assert "brier_score" in eval_metrics


def test_volatility_models_fit_and_predict():
    X, _, _, y_vol = generate_synthetic_feature_data(200)

    for algo in ["RIDGE", "RANDOM_FOREST", "GRADIENT_BOOSTING", "PERSISTENCE_BASELINE"]:
        model = VolatilityModel(model_id=f"TEST-VOL-{algo}", algorithm=algo)
        model.fit(X, y_vol)
        preds = model.predict(X)

        assert len(preds) == len(X)
        assert (preds > 0).all()

        eval_metrics = model.evaluate(X, y_vol)
        assert "mae" in eval_metrics
        assert "qlike" in eval_metrics


def test_model_explainability_and_feature_contributions():
    X, y_reg, _, _ = generate_synthetic_feature_data(100)
    model = RegressionModel(model_id="TEST-EXP", algorithm="GRADIENT_BOOSTING")
    model.fit(X, y_reg)

    explanations = model.explain(X)
    assert len(explanations) == len(X)
    assert len(explanations[0]) > 0
    # Top features must exist
    assert len(model.feature_importances_) > 0


def test_model_serialization_and_loading(tmp_path):
    X, y_reg, _, _ = generate_synthetic_feature_data(100)
    model = RegressionModel(model_id="TEST-SER", algorithm="RIDGE")
    model.fit(X, y_reg)

    save_path = os.path.join(tmp_path, "model.joblib")
    model.save(save_path)
    assert os.path.exists(save_path)

    loaded_model = RegressionModel.load(save_path)
    assert loaded_model.model_id == "TEST-SER"
    assert loaded_model.algorithm == "RIDGE"

    preds_orig = model.predict(X)
    preds_loaded = loaded_model.predict(X)
    np.testing.assert_allclose(preds_orig, preds_loaded)
