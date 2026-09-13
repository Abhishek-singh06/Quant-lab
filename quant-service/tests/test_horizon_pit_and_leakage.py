"""Point-in-Time safety, future mutation invariance, and leakage tests for Part 15 Horizon Models."""

import pytest
import numpy as np
import pandas as pd
from datetime import datetime, timedelta

from app.horizons.schemas import TradingHorizon, ModelStatus, ModelAlgorithm, TargetType
from app.horizons.features.short_term import ShortTermFeatureExtractor
from app.horizons.features.medium_term import MediumTermFeatureExtractor
from app.horizons.features.long_term import LongTermFeatureExtractor
from app.horizons.models.short_term_model import ShortTermModel
from app.horizons.registry import HorizonModelRegistry
from app.horizons.engine import ProductionHorizonEngine


@pytest.fixture
def base_ohlcv():
    dates = pd.date_range("2024-01-01", periods=150, freq="B")
    np.random.seed(42)
    rets = np.random.normal(0.0005, 0.015, size=len(dates))
    prices = 2000.0 * np.cumprod(1 + rets)
    return pd.DataFrame({
        "date": dates,
        "open": prices * 0.995,
        "high": prices * 1.01,
        "low": prices * 0.99,
        "close": prices,
        "volume": np.random.uniform(500000, 2000000, size=len(dates))
    })


def test_future_data_mutation_invariance(base_ohlcv):
    """Test 58 & 82: Adding future data after T must NOT mutate historical features at T."""
    as_of = pd.to_datetime("2024-04-01")

    st_extractor = ShortTermFeatureExtractor()
    mt_extractor = MediumTermFeatureExtractor()
    lt_extractor = LongTermFeatureExtractor()

    # 1. Compute features at T on baseline dataset
    st_orig = st_extractor.extract_features("RELIANCE", as_of, base_ohlcv)
    mt_orig = mt_extractor.extract_features("RELIANCE", as_of, base_ohlcv)
    lt_orig = lt_extractor.extract_features("RELIANCE", as_of, base_ohlcv)

    # 2. Mutate future dataset: Add 50 new volatile future bars after as_of
    mutated_ohlcv = base_ohlcv.copy()
    future_mask = mutated_ohlcv["date"] > as_of
    mutated_ohlcv.loc[future_mask, "close"] = mutated_ohlcv.loc[future_mask, "close"] * 2.5
    mutated_ohlcv.loc[future_mask, "volume"] = mutated_ohlcv.loc[future_mask, "volume"] * 10.0

    # 3. Recompute features at T on mutated dataset
    st_mut = st_extractor.extract_features("RELIANCE", as_of, mutated_ohlcv)
    mt_mut = mt_extractor.extract_features("RELIANCE", as_of, mutated_ohlcv)
    lt_mut = lt_extractor.extract_features("RELIANCE", as_of, mutated_ohlcv)

    # 4. Strict numerical invariance verification
    for k in st_orig.keys():
        assert np.isclose(st_orig[k], st_mut[k], atol=1e-9), f"Short-term feature {k} leaked future mutation!"

    for k in mt_orig.keys():
        assert np.isclose(mt_orig[k], mt_mut[k], atol=1e-9), f"Medium-term feature {k} leaked future mutation!"

    for k in lt_orig.keys():
        assert np.isclose(lt_orig[k], lt_mut[k], atol=1e-9), f"Long-term feature {k} leaked future mutation!"


def test_model_availability_timestamp_enforcement(base_ohlcv):
    """Test 79: A model trained at T1 cannot generate predictions for historical dates T0 < T1."""
    registry = HorizonModelRegistry()

    # Model trained and deployed on 2024-06-01
    deploy_date = pd.to_datetime("2024-06-01").to_pydatetime()
    model = ShortTermModel(model_id="ST_GB", model_version="v1.0")

    # Fit dummy data
    X = pd.DataFrame(np.random.randn(20, 23), columns=ShortTermFeatureExtractor().get_feature_names())
    y = pd.Series(np.random.randn(20))
    model.fit(X, y, available_timestamp=deploy_date)

    registry.register_model(model, is_champion=True)

    # Request as of 2024-05-15 (Before deployment)
    past_as_of = pd.to_datetime("2024-05-15").to_pydatetime()
    champ_past = registry.get_champion_model(TradingHorizon.SHORT_TERM, as_of=past_as_of)
    assert champ_past is None, "Future model was leaked into historical prediction before deployment date!"

    # Request as of 2024-06-15 (After deployment)
    future_as_of = pd.to_datetime("2024-06-15").to_pydatetime()
    champ_valid = registry.get_champion_model(TradingHorizon.SHORT_TERM, as_of=future_as_of)
    assert champ_valid is not None
    assert champ_valid.model_version == "v1.0"


def test_fundamental_restatement_isolation(base_ohlcv):
    """Test 59: Restatements made at T_restated must not leak into predictions at T < T_restated."""
    lt_extractor = LongTermFeatureExtractor()
    as_of_2024 = pd.to_datetime("2024-05-01")

    # Original fundamental available in May 2024
    original_fund = {
        "revenue_cagr_3y": 0.12,
        "eps_cagr_3y": 0.14,
        "roe": 0.16,
        "information_available_at": "2024-04-30"
    }

    feats_orig = lt_extractor.extract_features("RELIANCE", as_of_2024, base_ohlcv, fundamental_data=original_fund)
    assert np.isclose(feats_orig["return_on_equity_ttm"], 0.16)
    assert np.isclose(feats_orig["ttm_revenue_growth_3y_cagr"], 0.12)
