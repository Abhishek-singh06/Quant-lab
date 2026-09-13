"""Unit tests for Horizon Models (Short-Term, Medium-Term, Long-Term)."""

import pytest
import numpy as np
import pandas as pd
from datetime import datetime, timedelta

from app.horizons.schemas import (
    TradingHorizon, TargetType, ModelAlgorithm, ModelStatus, HorizonOutlook, HorizonPrediction
)
from app.horizons.features.short_term import ShortTermFeatureExtractor
from app.horizons.features.medium_term import MediumTermFeatureExtractor
from app.horizons.features.long_term import LongTermFeatureExtractor
from app.horizons.targets.short_term import ShortTermTargetBuilder
from app.horizons.targets.medium_term import MediumTermTargetBuilder
from app.horizons.targets.long_term import LongTermTargetBuilder
from app.horizons.datasets.short_term_builder import ShortTermDatasetBuilder
from app.horizons.datasets.medium_term_builder import MediumTermDatasetBuilder
from app.horizons.datasets.long_term_builder import LongTermDatasetBuilder
from app.horizons.models.short_term_model import ShortTermModel
from app.horizons.models.medium_term_model import MediumTermModel
from app.horizons.models.long_term_model import LongTermModel
from app.horizons.registry import HorizonModelRegistry
from app.horizons.evaluator import HorizonEvaluator
from app.horizons.conflict_detector import HorizonConflictDetector
from app.horizons.risk_integration import HorizonRiskIntegration
from app.horizons.engine import ProductionHorizonEngine


@pytest.fixture
def sample_ohlcv():
    dates = pd.date_range("2024-01-01", periods=300, freq="B")
    np.random.seed(42)
    rets = np.random.normal(0.0005, 0.015, size=len(dates))
    prices = 1000.0 * np.cumprod(1 + rets)
    return pd.DataFrame({
        "date": dates,
        "open": prices * 0.995,
        "high": prices * 1.01,
        "low": prices * 0.99,
        "close": prices,
        "volume": np.random.uniform(500000, 2000000, size=len(dates))
    })


def test_independent_feature_extractors(sample_ohlcv):
    """Verify each horizon has distinct features and does not mix logic."""
    as_of = pd.to_datetime("2024-08-01")
    st_extractor = ShortTermFeatureExtractor()
    mt_extractor = MediumTermFeatureExtractor()
    lt_extractor = LongTermFeatureExtractor()

    st_feats = st_extractor.extract_features("RELIANCE", as_of, sample_ohlcv)
    mt_feats = mt_extractor.extract_features("RELIANCE", as_of, sample_ohlcv)
    lt_feats = lt_extractor.extract_features("RELIANCE", as_of, sample_ohlcv)

    # Short-term features must have fast technicals (RSI, MACD, 1d return)
    assert "rsi_14" in st_feats
    assert "return_1d" in st_feats
    assert "volatility_5d" in st_feats

    # Medium-term features must have intermediate trends (SMA50, SMA200, 63d return)
    assert "price_vs_sma50" in mt_feats
    assert "return_63d" in mt_feats
    assert "quarterly_eps_growth_yoy" in mt_feats

    # Long-term features must have fundamental growth, ROE, ROCE, valuation
    assert "return_on_equity_ttm" in lt_feats
    assert "ttm_eps_growth_3y_cagr" in lt_feats
    assert "debt_to_equity" in lt_feats

    # Ensure feature sets are completely distinct
    assert set(st_extractor.get_feature_names()) != set(mt_extractor.get_feature_names())
    assert set(mt_extractor.get_feature_names()) != set(lt_extractor.get_feature_names())


def test_independent_target_builders(sample_ohlcv):
    """Verify target builders compute horizon-specific forward labels."""
    as_of = pd.to_datetime("2024-03-01")
    st_builder = ShortTermTargetBuilder()
    mt_builder = MediumTermTargetBuilder()
    lt_builder = LongTermTargetBuilder()

    st_targets = st_builder.calculate_targets("TCS", as_of, sample_ohlcv)
    mt_targets = mt_builder.calculate_targets("TCS", as_of, sample_ohlcv)
    lt_targets = lt_builder.calculate_targets("TCS", as_of, sample_ohlcv)

    assert "target_return_1d" in st_targets
    assert "target_class_1d" in st_targets
    assert "target_return_4w" in mt_targets
    assert "target_relative_return_4w" in mt_targets
    assert "target_return_1y" in lt_targets
    assert "target_cagr_3y" in lt_targets


def test_independent_models_fit_and_predict(sample_ohlcv):
    """Train independent models for each horizon and verify predictions."""
    dates = pd.date_range("2024-03-01", "2024-06-01", freq="W-FRI").to_pydatetime().tolist()
    symbols = {"INFY": sample_ohlcv}

    # 1. Short-Term
    st_builder = ShortTermDatasetBuilder()
    X_st, y_st, _ = st_builder.build_dataset(symbols, dates, "target_return_1d")
    st_model = ShortTermModel(algorithm=ModelAlgorithm.GRADIENT_BOOSTING)
    st_model.fit(X_st, y_st)
    assert st_model.is_fitted
    st_pred = st_model.predict(X_st)
    assert len(st_pred) == len(X_st)

    # 2. Medium-Term
    mt_builder = MediumTermDatasetBuilder()
    X_mt, y_mt, _ = mt_builder.build_dataset(symbols, dates, "target_return_4w")
    mt_model = MediumTermModel(algorithm=ModelAlgorithm.RIDGE)
    mt_model.fit(X_mt, y_mt)
    assert mt_model.is_fitted
    mt_pred = mt_model.predict(X_mt)
    assert len(mt_pred) == len(X_mt)

    # 3. Long-Term
    lt_builder = LongTermDatasetBuilder()
    X_lt, y_lt, _ = lt_builder.build_dataset(symbols, dates, "target_return_1y")
    lt_model = LongTermModel(algorithm=ModelAlgorithm.ELASTIC_NET)
    lt_model.fit(X_lt, y_lt)
    assert lt_model.is_fitted
    lt_pred = lt_model.predict(X_lt)
    assert len(lt_pred) == len(X_lt)


def test_horizon_evaluator(sample_ohlcv):
    """Verify statistical evaluation metrics and baseline comparisons."""
    dates = pd.date_range("2024-03-01", "2024-06-01", freq="W-FRI").to_pydatetime().tolist()
    symbols = {"HDFCBANK": sample_ohlcv}

    st_builder = ShortTermDatasetBuilder()
    X, y, _ = st_builder.build_dataset(symbols, dates, "target_return_1d")
    model = ShortTermModel()
    model.fit(X, y)

    res = HorizonEvaluator.evaluate(model, X, y)
    assert res.mae >= 0.0
    assert res.rmse >= 0.0
    assert 0.0 <= res.directional_accuracy <= 1.0
    assert "mean_baseline_mae" in res.baseline_metrics


def test_cross_horizon_conflict_detector():
    """Verify conflict detection handles confluence and divergences accurately."""
    as_of = datetime.utcnow()

    p_bull = HorizonPrediction(
        prediction_id="p1", symbol="ABC", prediction_timestamp=as_of,
        information_available_at=as_of, calculated_at=as_of,
        horizon=TradingHorizon.SHORT_TERM, horizon_period="1D",
        expected_return=0.015, confidence=0.85, outlook=HorizonOutlook.BULLISH,
        model_version="ST_v1", feature_set_version="ST_F1", target_set_version="ST_T1"
    )
    p_bear = HorizonPrediction(
        prediction_id="p2", symbol="ABC", prediction_timestamp=as_of,
        information_available_at=as_of, calculated_at=as_of,
        horizon=TradingHorizon.SHORT_TERM, horizon_period="1D",
        expected_return=-0.012, confidence=0.80, outlook=HorizonOutlook.BEARISH,
        model_version="ST_v1", feature_set_version="ST_F1", target_set_version="ST_T1"
    )

    # Bullish confluence
    conf1 = HorizonConflictDetector.detect_conflict("ABC", as_of, p_bull, p_bull, p_bull)
    assert not conf1.conflict_detected
    assert conf1.conflict_severity == "NONE"

    # Short-term dip in long-term bull thesis
    conf2 = HorizonConflictDetector.detect_conflict("ABC", as_of, p_bear, p_bull, p_bull)
    assert conf2.conflict_detected
    assert conf2.conflict_severity == "LOW"
    assert "retracement" in conf2.explanation.lower() or "weakness" in conf2.explanation.lower()

    # Short-term bull in long-term bear thesis (bull trap risk)
    conf3 = HorizonConflictDetector.detect_conflict("ABC", as_of, p_bull, p_bull, p_bear)
    assert conf3.conflict_detected
    assert conf3.conflict_severity == "HIGH"
    assert "bull trap" in conf3.explanation.lower() or "opposing" in conf3.explanation.lower()


def test_horizon_risk_integration():
    """Verify horizon risk parameters adapt stop distance and sizing tier."""
    as_of = datetime.utcnow()
    pred = HorizonPrediction(
        prediction_id="p1", symbol="SBIN", prediction_timestamp=as_of,
        information_available_at=as_of, calculated_at=as_of,
        horizon=TradingHorizon.SHORT_TERM, horizon_period="1D",
        expected_return=0.01, confidence=0.80, outlook=HorizonOutlook.BULLISH,
        model_version="ST_v1", feature_set_version="ST_F1", target_set_version="ST_T1"
    )

    st_risk = HorizonRiskIntegration.get_horizon_risk_parameters(
        TradingHorizon.SHORT_TERM, pred, current_price=800.0, atr=16.0
    )
    assert st_risk["stop_distance"] == 24.0 # 1.5 * 16
    assert st_risk["max_position_risk"] == 0.005 # 0.5% risk

    lt_risk = HorizonRiskIntegration.get_horizon_risk_parameters(
        TradingHorizon.LONG_TERM, pred, current_price=800.0, atr=16.0
    )
    assert lt_risk["stop_distance"] == 96.0 # 12% of 800
    assert lt_risk["max_position_risk"] == 0.02 # 2.0% risk
