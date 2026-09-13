"""Master Production Horizon Engine.

Coordinates independent feature sets, datasets, models, predictions,
conflict analysis, and risk interfaces for Short-Term, Medium-Term, and Long-Term.
"""

from typing import Dict, List, Optional, Any, Tuple
import uuid
import numpy as np
import pandas as pd
from datetime import datetime

from app.horizons.schemas import (
    TradingHorizon,
    HorizonPrediction,
    HorizonOutlook,
    CrossHorizonView,
    HorizonConflict
)
from app.horizons.features.short_term import ShortTermFeatureExtractor
from app.horizons.features.medium_term import MediumTermFeatureExtractor
from app.horizons.features.long_term import LongTermFeatureExtractor
from app.horizons.registry import HorizonModelRegistry
from app.horizons.conflict_detector import HorizonConflictDetector
from app.horizons.risk_integration import HorizonRiskIntegration


class ProductionHorizonEngine:
    """Production Multi-Horizon Prediction and Analysis Orchestrator."""

    def __init__(self, registry: Optional[HorizonModelRegistry] = None):
        self.registry = registry or HorizonModelRegistry()
        self.st_extractor = ShortTermFeatureExtractor()
        self.mt_extractor = MediumTermFeatureExtractor()
        self.lt_extractor = LongTermFeatureExtractor()

    def predict_horizon(
        self,
        symbol: str,
        horizon: TradingHorizon,
        as_of: datetime,
        ohlcv_df: pd.DataFrame,
        regime_data: Optional[Dict[str, Any]] = None,
        fundamental_data: Optional[Dict[str, Any]] = None,
        institutional_data: Optional[Dict[str, Any]] = None,
        news_data: Optional[Dict[str, Any]] = None,
        global_data: Optional[Dict[str, Any]] = None,
    ) -> Optional[HorizonPrediction]:
        """Generate prediction for a specific horizon ensuring Point-in-Time safety."""
        model = self.registry.get_champion_model(horizon, as_of=as_of)
        if model is None or not model.is_fitted:
            return None

        # Extract horizon-specific features strictly at as_of
        if horizon == TradingHorizon.SHORT_TERM:
            feats = self.st_extractor.extract_features(
                symbol=symbol, as_of=as_of, ohlcv_df=ohlcv_df,
                regime_data=regime_data, news_data=news_data, global_data=global_data
            )
            feat_set_ver = self.st_extractor.feature_set_version
            horizon_period = "ONE_DAY"
        elif horizon == TradingHorizon.MEDIUM_TERM:
            feats = self.mt_extractor.extract_features(
                symbol=symbol, as_of=as_of, ohlcv_df=ohlcv_df,
                regime_data=regime_data, fundamental_data=fundamental_data,
                institutional_data=institutional_data, global_data=global_data
            )
            feat_set_ver = self.mt_extractor.feature_set_version
            horizon_period = "FOUR_WEEKS"
        else: # LONG_TERM
            feats = self.lt_extractor.extract_features(
                symbol=symbol, as_of=as_of, ohlcv_df=ohlcv_df,
                regime_data=regime_data, fundamental_data=fundamental_data,
                institutional_data=institutional_data
            )
            feat_set_ver = self.lt_extractor.feature_set_version
            horizon_period = "ONE_YEAR"

        X_df = pd.DataFrame([feats])
        pred_val = float(model.predict(X_df)[0])

        prob_pos = None
        prob_neg = None
        pred_class = None
        proba = model.predict_proba(X_df)
        if proba is not None and proba.shape[1] >= 2:
            prob_pos = float(proba[0, 1])
            prob_neg = float(proba[0, 0])
            pred_class = 1 if prob_pos > 0.5 else 0
        else:
            # Implied logistic probability from return
            prob_pos = float(1.0 / (1.0 + np.exp(-pred_val * 50.0)))
            prob_neg = 1.0 - prob_pos
            pred_class = 1 if pred_val > 0 else 0

        # Determine outlook
        if pred_val > 0.003:
            outlook = HorizonOutlook.BULLISH
        elif pred_val < -0.003:
            outlook = HorizonOutlook.BEARISH
        else:
            outlook = HorizonOutlook.NEUTRAL

        # Confidence heuristic (data completeness + model confidence)
        non_zero_ratio = float(np.mean([1.0 if v != 0.0 else 0.0 for v in feats.values()]))
        confidence = float(min(0.95, max(0.50, non_zero_ratio * 0.90)))

        # Feature contributions
        importances = model.get_feature_importance()
        contributions = {}
        for f_name, f_val in feats.items():
            if f_name in importances:
                contributions[f_name] = float(f_val * importances[f_name])

        return HorizonPrediction(
            prediction_id=f"PRED-{horizon.value}-{uuid.uuid4().hex[:8]}",
            symbol=symbol,
            prediction_timestamp=as_of,
            information_available_at=as_of,
            calculated_at=datetime.utcnow(),
            horizon=horizon,
            horizon_period=horizon_period,
            expected_return=pred_val,
            probability_positive=prob_pos,
            probability_negative=prob_neg,
            predicted_class=pred_class,
            expected_volatility=feats.get("volatility_20d", feats.get("volatility_21d", 0.20)),
            confidence=confidence,
            outlook=outlook,
            feature_contributions=contributions,
            model_version=model.model_version,
            feature_set_version=feat_set_ver,
            target_set_version=model.target_set_version,
            data_version="1"
        )

    def evaluate_cross_horizon(
        self,
        symbol: str,
        as_of: datetime,
        ohlcv_df: pd.DataFrame,
        regime_data: Optional[Dict[str, Any]] = None,
        fundamental_data: Optional[Dict[str, Any]] = None,
        institutional_data: Optional[Dict[str, Any]] = None,
        news_data: Optional[Dict[str, Any]] = None,
        global_data: Optional[Dict[str, Any]] = None,
    ) -> CrossHorizonView:
        """Generate unified Cross-Horizon View with independent predictions and conflict analysis."""
        st_pred = self.predict_horizon(
            symbol=symbol, horizon=TradingHorizon.SHORT_TERM, as_of=as_of,
            ohlcv_df=ohlcv_df, regime_data=regime_data, news_data=news_data, global_data=global_data
        )
        mt_pred = self.predict_horizon(
            symbol=symbol, horizon=TradingHorizon.MEDIUM_TERM, as_of=as_of,
            ohlcv_df=ohlcv_df, regime_data=regime_data, fundamental_data=fundamental_data,
            institutional_data=institutional_data, global_data=global_data
        )
        lt_pred = self.predict_horizon(
            symbol=symbol, horizon=TradingHorizon.LONG_TERM, as_of=as_of,
            ohlcv_df=ohlcv_df, regime_data=regime_data, fundamental_data=fundamental_data,
            institutional_data=institutional_data
        )

        conflict = HorizonConflictDetector.detect_conflict(
            symbol=symbol, timestamp=as_of,
            short_term=st_pred, medium_term=mt_pred, long_term=lt_pred
        )

        model_versions = {}
        if st_pred: model_versions["SHORT_TERM"] = st_pred.model_version
        if mt_pred: model_versions["MEDIUM_TERM"] = mt_pred.model_version
        if lt_pred: model_versions["LONG_TERM"] = lt_pred.model_version

        return CrossHorizonView(
            symbol=symbol,
            as_of=as_of,
            short_term=st_pred,
            medium_term=mt_pred,
            long_term=lt_pred,
            conflict=conflict,
            short_term_status="AVAILABLE" if st_pred else "NOT_TRAINED",
            medium_term_status="AVAILABLE" if mt_pred else "NOT_TRAINED",
            long_term_status="AVAILABLE" if lt_pred else "NOT_TRAINED",
            model_versions=model_versions
        )
