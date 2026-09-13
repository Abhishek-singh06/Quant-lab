"""
Production Risk Engine and Position Sizing Orchestrator for QuantLab Part 14.
Coordinates multi-layer point-in-time risk calculations, position sizing strategies, and constraint resolution.
"""

from datetime import datetime, timezone
from typing import Any, Dict, List, Optional
import uuid
import numpy as np
import pandas as pd

from app.risk.models import (
    PortfolioPosition,
    PortfolioState,
    PositionRiskMetrics,
    PositionSizingMethod,
    RiskAdjustment,
    RiskAssessmentResult,
    RiskDecision,
    RiskLevel,
    RiskProfile,
    RiskTrace,
    RiskWarning,
    RiskWarningSeverity,
    StopMethod,
)
from app.risk.position_risk import PositionRiskCalculator
from app.risk.sizers import (
    FixedAllocationPositionSizer,
    FixedRiskPositionSizer,
    PortfolioAwarePositionSizer,
    PositionSizingStrategy,
    VolatilityAdjustedPositionSizer,
)
from app.risk.portfolio_risk import PortfolioRiskCalculator
from app.risk.correlation import CorrelationRiskCalculator
from app.risk.concentration import ConcentrationRiskCalculator
from app.risk.drawdown import DrawdownRiskCalculator, LiquidityRiskCalculator
from app.risk.constraints import RiskConstraintEngine


class ProductionRiskEngine:
    """Master Quantitative Risk Engine and Position Sizing System."""

    def __init__(self):
        self.pos_risk_calc = PositionRiskCalculator()
        self.port_risk_calc = PortfolioRiskCalculator()
        self.correl_calc = CorrelationRiskCalculator()
        self.concen_calc = ConcentrationRiskCalculator()
        self.drawdown_calc = DrawdownRiskCalculator()
        self.liquidity_calc = LiquidityRiskCalculator()
        self.constraint_engine = RiskConstraintEngine()

        self.sizers: Dict[PositionSizingMethod, PositionSizingStrategy] = {
            PositionSizingMethod.FIXED_ALLOCATION: FixedAllocationPositionSizer(),
            PositionSizingMethod.FIXED_RISK: FixedRiskPositionSizer(),
            PositionSizingMethod.VOLATILITY_ADJUSTED: VolatilityAdjustedPositionSizer(),
            PositionSizingMethod.PORTFOLIO_AWARE: PortfolioAwarePositionSizer(),
        }

    def assess_position_risk(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        portfolio_state: PortfolioState,
        risk_profile: RiskProfile,
        market_data: Dict[str, Any],
        signal_data: Optional[Dict[str, Any]] = None,
        returns_history_df: Optional[pd.DataFrame] = None
    ) -> RiskAssessmentResult:
        """
        Executes point-in-time risk assessment and position sizing.
        Enforces T_avail <= as_of_timestamp.
        """
        signal_data = signal_data or {}
        returns_df = returns_history_df if returns_history_df is not None else pd.DataFrame()

        # 1. Data Quality Gate
        entry_price = float(market_data.get("current_price", 0.0))
        atr = market_data.get("atr_14")
        sec_volatility = float(market_data.get("annualized_volatility", 0.22))
        expected_vol = signal_data.get("expected_volatility")
        expected_ret = signal_data.get("expected_return")
        adv = market_data.get("average_daily_volume")
        sector = market_data.get("sector", "UNKNOWN")
        industry = market_data.get("industry", "UNKNOWN")

        if entry_price <= 0 or portfolio_state.current_portfolio_value <= 0:
            return RiskAssessmentResult(
                id=str(uuid.uuid4()),
                symbol=symbol,
                timestamp=as_of_timestamp,
                information_available_at=as_of_timestamp,
                calculated_at=datetime.now(timezone.utc),
                risk_decision=RiskDecision.INSUFFICIENT_DATA,
                risk_level=RiskLevel.UNKNOWN,
                data_quality_status="INSUFFICIENT_DATA",
                reasoning="Assessment blocked: Missing or invalid pricing and portfolio valuation data."
            )

        # 2. Position Risk Calculations (Stop distance, risk per position)
        stop_method = risk_profile.default_stop_method
        risk_metrics = self.pos_risk_calc.calculate_position_risk(
            entry_price=entry_price,
            quantity=1.0,  # normalized baseline
            portfolio_value=portfolio_state.current_portfolio_value,
            stop_method=stop_method,
            atr=atr,
            atr_multiplier=2.0,
            target_price=signal_data.get("target_price")
        )

        # 3. Position Sizing Strategy Execution
        sizing_method = risk_profile.default_position_sizing_method
        sizer = self.sizers.get(sizing_method, self.sizers[PositionSizingMethod.FIXED_RISK])
        
        signal_conf = float(signal_data.get("signal_confidence", 0.70))
        signal_score = float(signal_data.get("signal_score", 50.0))

        raw_alloc, raw_qty, sizing_reason = sizer.calculate_allocation(
            symbol=symbol,
            entry_price=entry_price,
            portfolio_state=portfolio_state,
            risk_profile=risk_profile,
            risk_metrics=risk_metrics,
            security_volatility=sec_volatility,
            expected_volatility=expected_vol,
            signal_confidence=signal_conf,
            signal_score=signal_score
        )

        # 4. Correlation Risk
        max_corr, corr_multiplier, corr_details = self.correl_calc.calculate_correlation_risk(
            candidate_symbol=symbol,
            positions=portfolio_state.positions,
            returns_df=returns_df,
            risk_profile=risk_profile
        )

        # 5. Concentration Risk
        post_sec_w, post_sec_sector_w, max_sec_headroom, concen_multiplier, concen_reason = (
            self.concen_calc.evaluate_concentration(
                symbol=symbol,
                sector=sector,
                industry=industry,
                proposed_allocation=raw_alloc,
                existing_positions=portfolio_state.positions,
                risk_profile=risk_profile
            )
        )

        # 6. Drawdown Adjustment
        current_dd, dd_multiplier, dd_reason = self.drawdown_calc.calculate_drawdown_adjustment(
            portfolio_state=portfolio_state,
            risk_profile=risk_profile
        )

        # 7. Liquidity Limits
        max_liquid_qty, max_liquid_alloc, liq_reason = self.liquidity_calc.calculate_liquidity_limit(
            entry_price=entry_price,
            portfolio_value=portfolio_state.current_portfolio_value,
            average_daily_volume=adv,
            risk_profile=risk_profile
        )

        # 8. Constraint Resolution & Bounding
        final_suggested_alloc, max_permitted_alloc, binding_constraint, limiting_reasons = (
            self.constraint_engine.resolve_constraints(
                raw_suggested_allocation=raw_alloc,
                entry_price=entry_price,
                portfolio_state=portfolio_state,
                risk_profile=risk_profile,
                risk_metrics=risk_metrics,
                max_sector_headroom=max_sec_headroom,
                max_liquid_alloc=max_liquid_alloc,
                drawdown_multiplier=dd_multiplier,
                correlation_multiplier=corr_multiplier
            )
        )

        # Recommended quantity in round shares
        capital_to_invest = portfolio_state.current_portfolio_value * final_suggested_alloc
        recommended_quantity = int(capital_to_invest / entry_price) if entry_price > 0 else 0
        final_suggested_alloc = (recommended_quantity * entry_price) / portfolio_state.current_portfolio_value

        # Recalculate true monetary position risk with final recommended quantity
        position_risk_amount = risk_metrics.stop_distance * recommended_quantity
        position_risk_percent = position_risk_amount / portfolio_state.current_portfolio_value
        estimated_downside = position_risk_amount

        # 9. Portfolio Volatility Estimation
        port_volatility: Optional[float] = portfolio_state.portfolio_volatility
        if port_volatility is None and not returns_df.empty and len(portfolio_state.positions) > 1:
            try:
                active_syms = [p.symbol for p in portfolio_state.positions if p.is_active and p.symbol in returns_df.columns]
                if len(active_syms) > 1:
                    w_vec = np.array([p.weight for p in portfolio_state.positions if p.symbol in active_syms])
                    w_vec = w_vec / np.sum(w_vec)
                    cov_mat = returns_df[active_syms].cov().values
                    port_volatility = self.port_risk_calc.calculate_portfolio_volatility(w_vec, cov_mat)
            except Exception:
                pass

        # 10. Risk Warnings
        warnings: List[RiskWarning] = []
        if sec_volatility > 0.35:
            warnings.append(RiskWarning(
                warning_code="ELEVATED_VOLATILITY",
                severity=RiskWarningSeverity.MODERATE,
                message=f"Security volatility ({sec_volatility*100:.1f}%) is significantly above normal band"
            ))
        if max_corr is not None and max_corr > 0.65:
            warnings.append(RiskWarning(
                warning_code="HIGH_PORTFOLIO_CORRELATION",
                severity=RiskWarningSeverity.MODERATE,
                message=f"High correlation ({max_corr:.2f}) with existing portfolio holdings"
            ))
        if post_sec_sector_w > risk_profile.max_sector_allocation * 0.8:
            warnings.append(RiskWarning(
                warning_code="SECTOR_CONCENTRATION_WARNING",
                severity=RiskWarningSeverity.MODERATE,
                message=f"Post-trade sector exposure ({post_sec_sector_w*100:.1f}%) approaches ceiling"
            ))
        if current_dd > risk_profile.max_drawdown_tolerance * 0.7:
            warnings.append(RiskWarning(
                warning_code="PORTFOLIO_DRAWDOWN_WARNING",
                severity=RiskWarningSeverity.HIGH,
                message=f"Portfolio drawdown at {current_dd*100:.1f}%"
            ))

        # 11. Risk Decision Resolution
        if dd_multiplier == 0.0:
            decision = RiskDecision.BLOCKED_BY_DRAWDOWN
        elif final_suggested_alloc <= 0.001 or recommended_quantity == 0:
            if binding_constraint == "PORTFOLIO_RISK_BUDGET":
                decision = RiskDecision.BLOCKED_BY_RISK_LIMIT
            elif binding_constraint == "SECTOR_CONCENTRATION_LIMIT":
                decision = RiskDecision.BLOCKED_BY_CONCENTRATION
            else:
                decision = RiskDecision.REJECT
        elif final_suggested_alloc < raw_alloc * 0.9:
            decision = RiskDecision.APPROVE_REDUCED
        else:
            decision = RiskDecision.APPROVE

        # 12. Risk Level Assignment
        if sec_volatility > 0.40 or current_dd > 0.12 or (max_corr and max_corr > 0.80):
            risk_level = RiskLevel.HIGH
        elif sec_volatility > 0.25 or position_risk_percent > 0.008 or (max_corr and max_corr > 0.60):
            risk_level = RiskLevel.MODERATE
        else:
            risk_level = RiskLevel.LOW

        # 13. Trace and Adjustments
        adjustments = [
            RiskAdjustment("BASE_SIZING", 1.0, raw_alloc, raw_alloc, sizing_reason),
            RiskAdjustment("CORRELATION_SCALING", corr_multiplier, raw_alloc, raw_alloc * corr_multiplier, f"Max correlation: {max_corr or 0.0:.2f}"),
            RiskAdjustment("DRAWDOWN_SCALING", dd_multiplier, raw_alloc * corr_multiplier, raw_alloc * corr_multiplier * dd_multiplier, dd_reason),
            RiskAdjustment("FINAL_CONSTRAINT_BINDING", 1.0, raw_alloc * corr_multiplier * dd_multiplier, final_suggested_alloc, f"Bound by {binding_constraint}")
        ]

        risk_trace = RiskTrace(
            base_allocation=raw_alloc,
            signal_confidence_adjustment=signal_conf,
            volatility_adjustment=min(1.5, (risk_profile.max_portfolio_volatility or 0.2) / max(0.05, sec_volatility)),
            correlation_adjustment=corr_multiplier,
            concentration_adjustment=concen_multiplier,
            drawdown_adjustment=dd_multiplier,
            liquidity_adjustment=1.0,
            risk_budget_cap=max_permitted_alloc,
            final_suggested_allocation=final_suggested_alloc,
            limiting_constraint=binding_constraint
        )

        reasoning = (
            f"Risk Engine {decision.value}: Suggested allocation {final_suggested_alloc*100:.1f}% ({recommended_quantity} shares, ₹{capital_to_invest:,.0f}). "
            f"Position risk budget consumed: ₹{position_risk_amount:,.0f} ({position_risk_percent*100:.2f}% of portfolio). "
            f"Stop distance: ₹{risk_metrics.stop_distance:.2f} ({risk_metrics.stop_distance_pct*100:.1f}% via {stop_method.value}). "
            + (f"Limiting factor: {limiting_reasons[0]}." if limiting_reasons else "All risk constraints satisfied.")
        )

        return RiskAssessmentResult(
            id=str(uuid.uuid4()),
            timestamp=as_of_timestamp,
            information_available_at=as_of_timestamp,
            calculated_at=datetime.now(timezone.utc),
            portfolio_id=portfolio_state.portfolio_id,
            risk_profile_id=risk_profile.id,
            signal_id=signal_data.get("signal_id"),
            symbol=symbol,
            signal_type=signal_data.get("signal_type", "BUY"),
            signal_score=signal_score,
            signal_confidence=signal_conf,
            suggested_allocation=round(final_suggested_alloc, 4),
            maximum_allocation=round(max_permitted_alloc, 4),
            recommended_quantity=float(recommended_quantity),
            entry_price=entry_price,
            stop_price=round(risk_metrics.stop_price, 2),
            target_price=risk_metrics.target_price,
            stop_distance=round(risk_metrics.stop_distance, 2),
            stop_distance_pct=round(risk_metrics.stop_distance_pct, 4),
            stop_method=stop_method.value,
            position_risk_amount=round(position_risk_amount, 2),
            position_risk_percent=round(position_risk_percent, 4),
            estimated_downside=round(estimated_downside, 2),
            portfolio_value=portfolio_state.current_portfolio_value,
            remaining_risk_budget=round(max(0.0, portfolio_state.current_portfolio_value * risk_profile.max_portfolio_risk - position_risk_amount), 2),
            portfolio_volatility=round(port_volatility, 4) if port_volatility is not None else None,
            security_volatility=round(sec_volatility, 4),
            expected_volatility=round(expected_vol, 4) if expected_vol is not None else None,
            max_correlation=round(max_corr, 3) if max_corr is not None else None,
            sector_exposure_after_trade=round(post_sec_sector_w, 4),
            current_drawdown=round(current_dd, 4),
            risk_reward_ratio=round(risk_metrics.risk_reward_ratio, 2) if risk_metrics.risk_reward_ratio is not None else None,
            expected_return=expected_ret,
            risk_decision=decision,
            risk_level=risk_level,
            risk_trace=risk_trace,
            adjustments=adjustments,
            limiting_constraints=limiting_reasons,
            risk_warnings=warnings,
            reasoning=reasoning,
            data_quality_status="HIGH_QUALITY",
            risk_engine_version="RISK_v1.0.0",
            risk_profile_version=risk_profile.version,
            signal_version=signal_data.get("signal_version", "SIGNAL_v1.0.0"),
            data_version="1"
        )
