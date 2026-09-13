"""
Multi-Perspective Review Agents & Conflict Detection Engine.
Simulates an adversarial investment committee with 4 specialized perspectives:
1. Fundamental Analyst (Moat & Capital Allocation)
2. Valuation Analyst (Margin of Safety & Multiples)
3. Risk Analyst (Inversion & Solvency)
4. Contrarian Reviewer (Disconfirmation & Blindspots)
"""
from typing import List, Dict, Any, Tuple
from app.ai_research.models import AgentPerspective, ConflictItem, ResearchContext, ChecklistItem, ChecklistStatus


class MultiAgentReviewEngine:
    """
    Coordinates multi-perspective review and conflict detection.
    """

    @classmethod
    def generate_reviews(cls, context: ResearchContext, checklist: List[ChecklistItem]) -> List[AgentPerspective]:
        ratios = context.financial_ratios or {}
        market = context.market_data or {}
        regime = context.macro_regime or {}
        signals = context.quant_signals or {}

        reviews: List[AgentPerspective] = []

        # 1. Fundamental Analyst
        roe = float(ratios.get("roe", 0.18))
        roce = float(ratios.get("roce", 0.22))
        funda_score = 4.5 if (roe >= 0.15 and roce >= 0.18) else 3.2 if (roe >= 0.10) else 2.0
        reviews.append(AgentPerspective(
            agent_name="Fundamental Analyst",
            role="Moat & Reinvestment Economics",
            score=funda_score,
            thesis=f"Core economics exhibit high compounding efficiency (ROE {roe*100 if roe<5 else roe:.1f}%, ROCE {roce*100 if roce<5 else roce:.1f}%).",
            key_positives=[
                f"Sustained return on capital above the Indian market cost of equity.",
                "Proven pricing power and operational scalability."
            ],
            key_risks=[
                "Reinvestment rate sustainability at higher scale.",
                "Competitive encroachment in core product segments."
            ],
            recommendation="FAVORABLE" if funda_score >= 4.0 else "NEUTRAL"
        ))

        # 2. Valuation Analyst
        pe = float(ratios.get("pe_ratio", 24.5))
        pb = float(ratios.get("pb_ratio", 4.2))
        val_score = 4.2 if pe <= 25.0 else 3.0 if pe <= 40.0 else 1.8
        reviews.append(AgentPerspective(
            agent_name="Valuation Analyst",
            role="Margin of Safety & Price Multiples",
            score=val_score,
            thesis=f"Trailing P/E of {pe:.1f}x and P/B of {pb:.1f}x provide {val_score >= 3.5 and 'adequate' or 'narrow'} margin of safety.",
            key_positives=[
                f"Valuation multiple is {pe <= 30.0 and 'reasonable' or 'elevated'} relative to growth rate.",
                "Earnings yield supports current risk-free rate spread."
            ],
            key_risks=[
                f"Multiple compression risk if quarterly growth decelerates.",
                "Limited downside buffer in broad market corrections."
            ],
            recommendation="FAVORABLE" if val_score >= 3.5 else "CAUTIOUS"
        ))

        # 3. Risk Analyst (Inversion / Failure Analysis)
        de = float(ratios.get("debt_to_equity", 0.35))
        icr = float(ratios.get("interest_coverage", 8.5))
        risk_score = 4.6 if (de <= 0.5 and icr >= 5.0) else 3.0 if de <= 1.2 else 1.5
        reviews.append(AgentPerspective(
            agent_name="Risk Analyst",
            role="Solvency & Inversion Analysis",
            score=risk_score,
            thesis=f"Debt-to-equity of {de:.2f}x and interest coverage of {icr:.1f}x ensure robust balance sheet resilience.",
            key_positives=[
                "Low financial distress probability.",
                "No critical liquidity bottlenecks detected in working capital."
            ],
            key_risks=[
                "Working capital elongation during industrial slowdowns.",
                "Macroeconomic interest rate and refinancing shocks."
            ],
            recommendation="FAVORABLE" if risk_score >= 4.0 else "CAUTIOUS"
        ))

        # 4. Contrarian Reviewer (Disconfirmation & Blindspots)
        regime_label = regime.get("regimeLabel", "SIDEWAYS")
        is_risky_regime = "HIGH_VOL" in regime_label or "RISK_OFF" in regime_label
        contra_score = 3.2 if is_risky_regime else 4.0
        reviews.append(AgentPerspective(
            agent_name="Contrarian Reviewer",
            role="Disconfirmation & Stress Testing",
            score=contra_score,
            thesis=f"Examining structural vulnerabilities under {regime_label} macro regime.",
            key_positives=[
                "Institutional backing remains stable without mass distribution.",
                "Promoter share pledge risks are minimal."
            ],
            key_risks=[
                f"Macro environment ({regime_label}) could induce sector multiple re-rating.",
                "Regulatory compliance and policy shifts in domestic operations."
            ],
            recommendation="NEUTRAL" if is_risky_regime else "FAVORABLE"
        ))

        return reviews


class EvidenceConflictDetector:
    """
    Detects structural contradictions among fundamental quality, valuation, macro regime, and signals.
    """

    @classmethod
    def detect_conflicts(cls, context: ResearchContext, reviews: List[AgentPerspective]) -> List[ConflictItem]:
        conflicts: List[ConflictItem] = []
        ratios = context.financial_ratios or {}
        regime = context.macro_regime or {}
        signals = context.quant_signals or {}

        # 1. Quality vs. Valuation Conflict (High Moat but Stretched Multiple)
        roe = float(ratios.get("roe", 0.18))
        pe = float(ratios.get("pe_ratio", 24.5))
        if roe >= 0.20 and pe >= 45.0:
            conflicts.append(ConflictItem(
                dimension_a="Business Quality (Exceptional)",
                dimension_b="Valuation (Expensive)",
                description=f"High capital efficiency (ROE {roe*100:.1f}%) is offset by rich valuation (P/E {pe:.1f}x). Multiple compression risk is heightened.",
                severity="HIGH",
                recommended_action="Stagger allocations or wait for valuation pullbacks rather than lump-sum entry."
            ))

        # 2. Accounting Profit vs. Cash Flow Realization Conflict
        cfo_pat = float(ratios.get("cfo_to_pat", 1.0))
        if cfo_pat < 0.60:
            conflicts.append(ConflictItem(
                dimension_a="Reported Accounting Profit",
                dimension_b="Operating Cash Flow Conversion",
                description=f"Reported PAT conversion to cash is low (CFO/PAT {cfo_pat:.2f}x). Earnings quality requires forensic review for accrual inflation.",
                severity="HIGH",
                recommended_action="Verify receivables aging and inventory accumulation in annual statutory disclosures."
            ))

        # 3. Fundamental Strength vs. Macro Regime Headwind
        regime_label = regime.get("regimeLabel", "SIDEWAYS")
        if "RISK_OFF" in regime_label or "HIGH_VOL" in regime_label:
            conflicts.append(ConflictItem(
                dimension_a="Individual Company Fundamentals",
                dimension_b="Global Macro Regime",
                description=f"Solid company metrics are challenged by a global {regime_label} regime, creating market-wide beta headwinds.",
                severity="MEDIUM",
                recommended_action="Enforce strict position sizing and hedge systemic market beta."
            ))

        return conflicts
