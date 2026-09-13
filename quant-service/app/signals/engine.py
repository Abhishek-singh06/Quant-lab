"""
Production Cross-Check / Signal Engine for QuantLab Part 13.
Coordinates multi-layer point-in-time evidence, conflict resolution, score/confidence formulation, and reasoning.
"""

from datetime import datetime, timezone
from typing import Any, Dict, List, Optional
import uuid

from app.signals.models import (
    ConflictSeverity,
    Evidence,
    EvidenceCategory,
    EvidenceDirection,
    SignalComponent,
    SignalConfiguration,
    SignalQualityStatus,
    SignalResult,
    SignalType,
)
from app.signals.providers import (
    CorporateEventEvidenceProvider,
    FundamentalEvidenceProvider,
    GlobalMarketEvidenceProvider,
    IndianMarketEvidenceProvider,
    InstitutionalEvidenceProvider,
    MacroEvidenceProvider,
    MLPredictionEvidenceProvider,
    MarketRegimeEvidenceProvider,
    MutualFundEvidenceProvider,
    NewsEvidenceProvider,
    SignalEvidenceProvider,
    TechnicalEvidenceProvider,
)
from app.signals.correlation import CorrelationGroupManager
from app.signals.conflict import ConflictDetector
from app.signals.reasoning import ReasoningEngine


class CrossCheckSignalEngine:
    """Master Cross-Check and Signal Engine."""

    def __init__(self, config: Optional[SignalConfiguration] = None):
        self.config = config or SignalConfiguration()
        self.correlation_mgr = CorrelationGroupManager(self.config)
        self.conflict_detector = ConflictDetector()
        self.reasoning_engine = ReasoningEngine()
        
        # Register default evidence providers
        self.providers: Dict[EvidenceCategory, SignalEvidenceProvider] = {
            EvidenceCategory.TECHNICAL: TechnicalEvidenceProvider(),
            EvidenceCategory.FUNDAMENTAL: FundamentalEvidenceProvider(),
            EvidenceCategory.NEWS: NewsEvidenceProvider(),
            EvidenceCategory.CORPORATE_EVENTS: CorporateEventEvidenceProvider(),
            EvidenceCategory.INSTITUTIONAL: InstitutionalEvidenceProvider(),
            EvidenceCategory.MUTUAL_FUNDS: MutualFundEvidenceProvider(),
            EvidenceCategory.INDIAN_MARKET: IndianMarketEvidenceProvider(),
            EvidenceCategory.GLOBAL_MARKET: GlobalMarketEvidenceProvider(),
            EvidenceCategory.MACRO: MacroEvidenceProvider(),
            EvidenceCategory.ML_PREDICTION: MLPredictionEvidenceProvider(),
            EvidenceCategory.MARKET_REGIME: MarketRegimeEvidenceProvider(),
        }

    def register_provider(self, provider: SignalEvidenceProvider) -> None:
        """Register custom or specialized evidence provider."""
        self.providers[provider.category] = provider

    def generate_signal(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> SignalResult:
        """
        Generate a point-in-time cross-checked signal for a single symbol.
        Strictly enforces T_avail <= as_of_timestamp for all inputs.
        """
        context = context or {}
        all_evidence: List[Evidence] = []
        components: Dict[str, SignalComponent] = {}
        
        # 1. Collect evidence from all providers
        for category, provider in self.providers.items():
            cat_name = category.value
            weight = self.config.category_weights.get(cat_name, 0.1)
            cap = self.config.category_caps.get(cat_name)
            
            ev_list = provider.collect_evidence(symbol, as_of_timestamp, context)
            all_evidence.extend(ev_list)
            
            comp = provider.build_component_summary(
                evidence_list=ev_list,
                configured_weight=weight,
                category_cap=cap
            )
            components[cat_name] = comp

        # 2. Apply Correlation and Double-Counting Controls
        components = self.correlation_mgr.apply_correlation_controls(components)

        # 3. Analyze Conflicts and Consensus
        conflict = self.conflict_detector.analyze_conflicts(components)

        # 4. Assess Data Quality and Freshness
        present_comps = [c for c in components.values() if c.is_present]
        total_comps = len(components)
        presence_ratio = len(present_comps) / total_comps if total_comps > 0 else 0.0
        
        avg_quality = sum(c.quality for c in present_comps) / len(present_comps) if present_comps else 0.0
        avg_freshness = sum(c.freshness for c in present_comps) / len(present_comps) if present_comps else 0.0
        
        if presence_ratio < 0.25:
            quality_status = SignalQualityStatus.INSUFFICIENT_DATA
        elif presence_ratio < 0.50 or avg_quality < 0.6:
            quality_status = SignalQualityStatus.LOW_QUALITY
        elif presence_ratio < 0.75 or avg_quality < 0.85:
            quality_status = SignalQualityStatus.MEDIUM_QUALITY
        else:
            quality_status = SignalQualityStatus.HIGH_QUALITY

        # 5. Calculate Final Normalized Signal Score (-100 to +100)
        total_effective_weight = sum(c.weight for c in present_comps) or 1.0
        raw_weighted_score = sum(c.weighted_contribution for c in present_comps) / total_effective_weight
        
        # Apply conflict and data quality dampening
        conflict_penalty = (conflict.conflict_score / 100.0) * 0.40  # up to 40% reduction for high conflict
        quality_penalty = (1.0 - avg_quality) * 0.30
        
        dampening_factor = max(0.2, (1.0 - conflict_penalty - quality_penalty))
        final_score = raw_weighted_score * dampening_factor
        final_score = max(-100.0, min(100.0, final_score))

        # 6. Calculate Confidence (0.0 to 1.0)
        # Factors: Evidence agreement, completeness, freshness, and low conflict
        base_confidence = min(1.0, presence_ratio * 0.4 + avg_quality * 0.3 + avg_freshness * 0.3)
        confidence = base_confidence * (1.0 - (conflict.conflict_score / 150.0))
        confidence = max(0.0, min(1.0, confidence))

        # 7. Extract ML Forecasts if available
        ml_comp = components.get(EvidenceCategory.ML_PREDICTION.value)
        expected_ret: Optional[float] = None
        expected_vol: Optional[float] = None
        ret_to_vol: Optional[float] = None
        
        if ml_comp and ml_comp.is_present:
            ml_preds = context.get("ml_predictions", {})
            expected_ret = ml_preds.get("predicted_return")
            expected_vol = ml_preds.get("predicted_volatility")
            if expected_ret is not None and expected_vol is not None and expected_vol > 0.0001:
                ret_to_vol = expected_ret / expected_vol

        # 8. Apply Decision Rules: MANDATORY RULES (NO BUY/SELL WITHOUT EVIDENCE)
        num_supporting = len(conflict.supporting_categories)
        num_opposing = len(conflict.opposing_categories)
        
        if quality_status == SignalQualityStatus.INSUFFICIENT_DATA:
            signal_type = SignalType.NO_SIGNAL
            direction = EvidenceDirection.NEUTRAL
        elif (
            final_score >= self.config.buy_threshold
            and confidence >= self.config.min_confidence
            and num_supporting >= self.config.min_supporting_categories
            and conflict.conflict_severity != ConflictSeverity.HIGH
        ):
            signal_type = SignalType.BUY
            direction = EvidenceDirection.BULLISH
        elif (
            final_score <= self.config.sell_threshold
            and confidence >= self.config.min_confidence
            and num_opposing >= self.config.min_supporting_categories
            and conflict.conflict_severity != ConflictSeverity.HIGH
        ):
            signal_type = SignalType.SELL
            direction = EvidenceDirection.BEARISH
        else:
            signal_type = SignalType.HOLD
            direction = EvidenceDirection.NEUTRAL

        # 9. Generate Auditable Reasoning & Traceability
        reasoning_text, structured_reasoning, supporting_items, opposing_items = (
            self.reasoning_engine.generate_reasoning(
                signal_type=signal_type,
                score=final_score,
                confidence=confidence,
                components=components,
                conflict=conflict,
                all_evidence=all_evidence
            )
        )

        model_ver = context.get("ml_predictions", {}).get("model_version")
        regime_ver = context.get("market_regime", {}).get("model_version", "REGIME_v1.0.0")

        return SignalResult(
            id=str(uuid.uuid4()),
            symbol=symbol,
            signal_timestamp=as_of_timestamp,
            information_available_at=as_of_timestamp,
            calculated_at=datetime.now(timezone.utc),
            signal=signal_type,
            signal_score=round(final_score, 2),
            confidence=round(confidence, 4),
            direction=direction,
            expected_return=expected_ret,
            expected_volatility=expected_vol,
            return_to_volatility_ratio=ret_to_vol,
            conflict_severity=conflict.conflict_severity,
            conflict_score=round(conflict.conflict_score, 2),
            data_quality_status=quality_status,
            freshness_score=round(avg_freshness, 4),
            reasoning=reasoning_text,
            structured_reasoning=structured_reasoning,
            supporting_evidence=supporting_items,
            opposing_evidence=opposing_items,
            components=components,
            all_evidence=all_evidence,
            signal_version="SIGNAL_v1.0.0",
            configuration_version=self.config.version,
            feature_version="1.0.0",
            model_version=model_ver,
            regime_version=regime_ver,
            data_version="1"
        )
