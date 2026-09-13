"""
ML Prediction and Market Regime Evidence Providers for QuantLab Part 13.
Consumes Part 10 Market Regime Engine and Part 11/12 Quant ML Prediction outputs.
"""

from datetime import datetime
from typing import Any, Dict, List, Optional
from app.signals.models import Evidence, EvidenceCategory, EvidenceDirection
from app.signals.providers.base import SignalEvidenceProvider


class MLPredictionEvidenceProvider(SignalEvidenceProvider):
    """Calculates ML evidence with strict model status and quality gates."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.ML_PREDICTION

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        pred = context.get("ml_predictions", {})
        if not pred:
            return evidence_list

        # Model Quality Gate: Must be active, validated model
        model_status = pred.get("model_status", "VALIDATED")
        if model_status not in ["VALIDATED", "ACTIVE", "CHAMPION"]:
            # Model not validated / retired - disqualified from contributing strong evidence
            return evidence_list

        model_version = pred.get("model_version", "M1_v1.0.0")
        
        # 1. Model 1: Expected 1-Day Forward Return E[R_(t+1)]
        pred_ret = pred.get("predicted_return")
        if pred_ret is not None:
            # +/- 2.0% daily predicted return maps to +/- 100
            score = max(-100.0, min(100.0, pred_ret * 5000.0))
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source=f"ML_PREDICTION_REGRESSION ({model_version})",
                category=self.category,
                feature="ML_EXPECTED_RETURN_1D",
                raw_value=float(pred_ret),
                raw_value_str=f"{pred_ret*100:+.2f}% E[R]",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=0.95,
                freshness=1.0,
                weight=1.5,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version=model_version,
                reason=f"Model 1 ({model_version}) forecast 1-day expected return of {pred_ret*100:+.2f}%"
            ))

        # 2. Model 2: Direction Probability P(Return > 0)
        prob_pos = pred.get("probability_positive")
        if prob_pos is not None:
            score = (prob_pos - 0.5) * 200.0  # 1.0 -> +100, 0.0 -> -100
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            evidence_list.append(Evidence(
                source=f"ML_PREDICTION_CLASSIFICATION ({model_version})",
                category=self.category,
                feature="ML_PROBABILITY_POSITIVE",
                raw_value=float(prob_pos),
                raw_value_str=f"P(R>0) = {prob_pos*100:.1f}%",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=0.95,
                freshness=1.0,
                weight=1.4,
                timestamp=as_of_timestamp,
                available_at=as_of_timestamp,
                version=model_version,
                reason=f"Model 2 calibrated upward probability is {prob_pos*100:.1f}%"
            ))

        return evidence_list


class MarketRegimeEvidenceProvider(SignalEvidenceProvider):
    """Calculates Market Regime evidence from Part 10 multi-dimensional classifications."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.MARKET_REGIME

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        regime = context.get("market_regime", {})
        if not regime:
            return evidence_list

        dir_regime = regime.get("direction_regime", "SIDEWAYS")
        risk_regime = regime.get("risk_regime", "NEUTRAL")
        confidence = float(regime.get("confidence", 0.8))
        regime_version = regime.get("model_version", "REGIME_v1.0.0")

        # 1. Direction Regime (BULL, BEAR, SIDEWAYS)
        dir_score = float(regime.get("direction_score", 0.0))
        dir_enum = EvidenceDirection.BULLISH if dir_score > 15 else (EvidenceDirection.BEARISH if dir_score < -15 else EvidenceDirection.NEUTRAL)
        evidence_list.append(Evidence(
            source=f"MARKET_REGIME_ENGINE ({regime_version})",
            category=self.category,
            feature="REGIME_DIRECTION",
            raw_value=dir_score,
            raw_value_str=f"{dir_regime} ({dir_score:+.1f})",
            normalized_score=float(dir_score),
            direction=dir_enum,
            strength=min(1.0, abs(dir_score) / 100.0),
            quality=1.0,
            freshness=1.0,
            weight=1.5,
            timestamp=as_of_timestamp,
            available_at=as_of_timestamp,
            version=regime_version,
            reason=f"Indian market direction regime classified as {dir_regime} (confidence: {confidence*100:.0f}%)"
        ))

        # 2. Risk Regime (RISK_ON, RISK_OFF)
        risk_score = float(regime.get("risk_score", 0.0))
        risk_dir = EvidenceDirection.BULLISH if risk_score > 15 else (EvidenceDirection.BEARISH if risk_score < -15 else EvidenceDirection.NEUTRAL)
        evidence_list.append(Evidence(
            source=f"MARKET_REGIME_ENGINE ({regime_version})",
            category=self.category,
            feature="REGIME_RISK_STATE",
            raw_value=risk_score,
            raw_value_str=f"{risk_regime} ({risk_score:+.1f})",
            normalized_score=float(risk_score),
            direction=risk_dir,
            strength=min(1.0, abs(risk_score) / 100.0),
            quality=1.0,
            freshness=1.0,
            weight=1.2,
            timestamp=as_of_timestamp,
            available_at=as_of_timestamp,
            version=regime_version,
            reason=f"Macro risk environment is {risk_regime}"
        ))

        return evidence_list
