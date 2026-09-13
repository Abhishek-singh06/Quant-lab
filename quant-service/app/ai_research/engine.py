"""
Company Research Engine for QuantLab.
Orchestrates PIT context validation, checklist evaluation, multi-agent debate,
conflict detection, section synthesis, and cryptographic provenance generation.
"""
import hashlib
import json
import uuid
from datetime import datetime
from typing import Optional, Dict, Any

from app.ai_research.models import (
    ResearchContext,
    ResearchReport,
    ChecklistStatus,
)
from app.ai_research.pit_validator import ResearchContextPITValidator
from app.ai_research.checklist import InvestmentChecklistEngine
from app.ai_research.agents import MultiAgentReviewEngine, EvidenceConflictDetector
from app.ai_research.llm_provider import ResearchLLMProvider, DeterministicRuleBasedProvider


class CompanyResearchEngine:
    """
    Main entry point for generating reproducible, PIT-safe fundamental research reports.
    """

    def __init__(self, default_llm_provider: Optional[ResearchLLMProvider] = None):
        self.default_llm_provider = default_llm_provider or DeterministicRuleBasedProvider()
        self._report_cache: Dict[str, ResearchReport] = {}

    def generate_report(
        self,
        context: ResearchContext,
        llm_provider: Optional[ResearchLLMProvider] = None,
        strict_pit: bool = True
    ) -> ResearchReport:
        # 1. Point-in-Time Validation & Sanitization
        sanitized_ctx, violations = ResearchContextPITValidator.validate_and_sanitize(context, strict=strict_pit)

        # 2. Evaluate 20-Point Investment Checklist
        checklist_items, summary_counts = InvestmentChecklistEngine.evaluate(sanitized_ctx)

        # 3. Multi-Agent Perspective Reviews
        agent_reviews = MultiAgentReviewEngine.generate_reviews(sanitized_ctx, checklist_items)

        # 4. Evidence Conflict Detection
        conflicts = EvidenceConflictDetector.detect_conflicts(sanitized_ctx, agent_reviews)

        # 5. Report Synthesis
        provider = llm_provider or self.default_llm_provider
        sections = provider.synthesize_report(sanitized_ctx, checklist_items)

        # 6. Calculate Dimension Scores (0-5 scale)
        funda_review = next((r for r in agent_reviews if r.agent_name == "Fundamental Analyst"), None)
        val_review = next((r for r in agent_reviews if r.agent_name == "Valuation Analyst"), None)
        risk_review = next((r for r in agent_reviews if r.agent_name == "Risk Analyst"), None)

        biz_quality_score = funda_review.score if funda_review else 4.0
        fin_health_score = risk_review.score if risk_review else 4.2
        valuation_score = val_review.score if val_review else 3.5
        risk_score = 5.0 - (len(conflicts) * 0.8)
        risk_score = max(1.0, min(5.0, risk_score))

        # Overall composite score (weighted average)
        overall_score = round(
            (biz_quality_score * 0.35) +
            (fin_health_score * 0.25) +
            (valuation_score * 0.25) +
            (risk_score * 0.15),
            2
        )

        # 7. Generate Cryptographic Provenance Hash
        run_id = f"RUN-RES-{uuid.uuid4().hex[:10].upper()}"
        report_id = f"REP-{sanitized_ctx.symbol}-{sanitized_ctx.context_as_of.strftime('%Y%m%d')}"

        provenance_payload = {
            "run_id": run_id,
            "report_id": report_id,
            "symbol": sanitized_ctx.symbol,
            "as_of": sanitized_ctx.context_as_of.isoformat(),
            "checklist_counts": summary_counts,
            "overall_score": overall_score,
            "provider": provider.get_provider_name()
        }
        provenance_hash = hashlib.sha256(json.dumps(provenance_payload, sort_keys=True).encode("utf-8")).hexdigest()

        report = ResearchReport(
            report_id=report_id,
            run_id=run_id,
            symbol=sanitized_ctx.symbol,
            company_name=sanitized_ctx.company_name,
            context_as_of=sanitized_ctx.context_as_of,
            generated_at=datetime.utcnow(),
            business_quality_score=biz_quality_score,
            financial_health_score=fin_health_score,
            valuation_score=valuation_score,
            risk_score=risk_score,
            overall_score=overall_score,
            checklist=checklist_items,
            checklist_passed=summary_counts.get("PASS", 0),
            checklist_warnings=summary_counts.get("WARNING", 0),
            checklist_failed=summary_counts.get("FAIL", 0),
            checklist_unknown=summary_counts.get("UNKNOWN", 0),
            agent_reviews=agent_reviews,
            conflicts=conflicts,
            sections=sections,
            evidence_count=len(sanitized_ctx.evidence_registry),
            llm_provider=provider.get_provider_name(),
            execution_mode="DETERMINISTIC_FALLBACK" if "DETERMINISTIC" in provider.get_provider_name() else "LLM_AUGMENTED",
            provenance_hash=provenance_hash
        )

        # Cache report
        self._report_cache[report_id] = report
        self._report_cache[sanitized_ctx.symbol.upper()] = report

        return report

    def get_cached_report(self, key: str) -> Optional[ResearchReport]:
        return self._report_cache.get(key)
