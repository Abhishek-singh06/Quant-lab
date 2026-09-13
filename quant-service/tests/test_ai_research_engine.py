"""
Unit and integration tests for AI Fundamental Research Engine,
investment checklist, multi-agent reviews, conflict detector, and REST API.
"""
import pytest
from datetime import datetime
from fastapi.testclient import TestClient

from app.main import app
from app.ai_research.models import ResearchContext, ChecklistStatus
from app.ai_research.checklist import InvestmentChecklistEngine
from app.ai_research.agents import MultiAgentReviewEngine, EvidenceConflictDetector
from app.ai_research.engine import CompanyResearchEngine
from app.ai_research.llm_provider import DeterministicRuleBasedProvider

client = TestClient(app)


def test_investment_checklist_20_items():
    ctx = ResearchContext(
        symbol="RELIANCE",
        company_name="Reliance Industries Ltd",
        context_as_of=datetime(2025, 3, 31),
        financial_ratios={
            "roe": 0.18,
            "roce": 0.21,
            "roa": 0.08,
            "revenue_growth_3y": 0.14,
            "operating_margin": 0.19,
            "debt_to_equity": 0.35,
            "interest_coverage": 8.0,
            "cfo_to_pat": 0.95,
            "free_cash_flow": 5000000000.0,
            "working_capital_days": 45.0,
            "pe_ratio": 22.0,
            "pb_ratio": 3.8,
            "ev_to_ebitda": 13.5,
            "promoter_pledging_pct": 0.0,
            "institutional_holding_pct": 35.0,
            "governance_score": 1.0
        }
    )

    items, counts = InvestmentChecklistEngine.evaluate(ctx)
    assert len(items) == 20
    assert counts["PASS"] >= 15
    assert counts["FAIL"] == 0

    # Verify key item IDs
    item_map = {i.item_id: i for i in items}
    assert item_map[4].name == "Return on Equity (ROE)"
    assert item_map[4].status == ChecklistStatus.PASS
    assert item_map[7].name == "Debt-to-Equity Leverage"
    assert item_map[7].status == ChecklistStatus.PASS


def test_missing_data_returns_unknown():
    ctx = ResearchContext(
        symbol="UNKNOWN_CO",
        company_name="Unknown Entity Ltd",
        context_as_of=datetime(2025, 3, 31),
        financial_ratios={}  # empty ratios
    )

    items, counts = InvestmentChecklistEngine.evaluate(ctx)
    assert len(items) == 20
    assert counts["UNKNOWN"] >= 10


def test_multi_agent_review_and_conflict_detection():
    # Scenario: High ROE (0.24) but Extreme PE (65.0) -> Triggers Quality vs Valuation Conflict
    ctx = ResearchContext(
        symbol="GROWTH_STOCK",
        company_name="High Growth Tech Ltd",
        context_as_of=datetime(2025, 3, 31),
        financial_ratios={
            "roe": 0.24,
            "roce": 0.28,
            "pe_ratio": 65.0,
            "cfo_to_pat": 0.45,  # also triggers cash realization conflict
            "debt_to_equity": 0.2
        },
        macro_regime={"regimeLabel": "HIGH_VOL_RISK_OFF"}
    )

    items, _ = InvestmentChecklistEngine.evaluate(ctx)
    reviews = MultiAgentReviewEngine.generate_reviews(ctx, items)
    assert len(reviews) == 4

    conflicts = EvidenceConflictDetector.detect_conflicts(ctx, reviews)
    assert len(conflicts) >= 2
    dim_pairs = [(c.dimension_a, c.dimension_b) for c in conflicts]
    assert any("Business Quality" in d[0] and "Valuation" in d[1] for d in dim_pairs)
    assert any("Accounting Profit" in d[0] for d in dim_pairs)


def test_company_research_engine_reproducibility():
    engine = CompanyResearchEngine()
    ctx = ResearchContext(
        symbol="TCS",
        company_name="Tata Consultancy Services Ltd",
        context_as_of=datetime(2025, 3, 31),
        financial_ratios={
            "roe": 0.38,
            "roce": 0.45,
            "roa": 0.18,
            "revenue_growth_3y": 0.11,
            "operating_margin": 0.25,
            "debt_to_equity": 0.0,
            "interest_coverage": 50.0,
            "cfo_to_pat": 1.05,
            "free_cash_flow": 40000000000.0,
            "working_capital_days": 60.0,
            "pe_ratio": 28.0,
            "pb_ratio": 10.5,
            "ev_to_ebitda": 18.0,
            "promoter_pledging_pct": 0.0,
            "institutional_holding_pct": 25.0
        }
    )

    rep1 = engine.generate_report(ctx)
    rep2 = engine.generate_report(ctx)

    assert rep1.symbol == "TCS"
    assert rep1.overall_score > 3.5
    assert rep1.execution_mode == "DETERMINISTIC_FALLBACK"
    assert rep1.provenance_hash is not None
    assert len(rep1.provenance_hash) == 64


def test_api_generate_and_get_research():
    resp = client.post("/api/v1/research/company/INFY", json={
        "company_name": "Infosys Ltd",
        "financial_ratios": {
            "roe": 0.28,
            "roce": 0.35,
            "debt_to_equity": 0.05,
            "pe_ratio": 25.0
        }
    })
    assert resp.status_code == 200
    data = resp.json()
    assert data["symbol"] == "INFY"
    assert "checklist" in data
    assert len(data["checklist"]) == 20
    assert "agent_reviews" in data
    assert len(data["agent_reviews"]) == 4
    assert "provenance_hash" in data

    # Test latest endpoint
    get_resp = client.get("/api/v1/research/INFY/latest")
    assert get_resp.status_code == 200
    get_data = get_resp.json()
    assert get_data["symbol"] == "INFY"
