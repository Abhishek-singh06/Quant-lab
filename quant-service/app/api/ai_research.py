"""
FastAPI router for AI Fundamental Research & Investment Intelligence.
"""
from fastapi import APIRouter, HTTPException, Query
from typing import Optional, Dict, Any
from datetime import datetime

from app.ai_research.models import ResearchContext, ResearchReport
from app.ai_research.engine import CompanyResearchEngine
from app.ai_research.pit_validator import PITViolationError

router = APIRouter(prefix="/research", tags=["AI Fundamental Research"])
engine = CompanyResearchEngine()


@router.post("/company/{symbol}", response_model=ResearchReport)
def generate_company_research(
    symbol: str,
    payload: Optional[Dict[str, Any]] = None,
    strict_pit: bool = Query(default=True, description="Strictly reject future look-ahead evidence")
):
    """
    Generate an auditable, PIT-enforced fundamental research report for an Indian equity.
    """
    data = payload or {}
    as_of_str = data.get("context_as_of")
    if as_of_str:
        try:
            as_of = datetime.fromisoformat(as_of_str.replace("Z", "+00:00"))
        except Exception:
            as_of = datetime.utcnow()
    else:
        as_of = datetime.utcnow()

    # Build context
    ctx = ResearchContext(
        symbol=symbol.upper(),
        company_name=data.get("company_name", f"{symbol.upper()} Ltd"),
        context_as_of=as_of,
        financial_ratios=data.get("financial_ratios", {
            "roe": 0.185,
            "roce": 0.224,
            "roa": 0.082,
            "revenue_growth_3y": 0.125,
            "operating_margin": 0.182,
            "debt_to_equity": 0.32,
            "interest_coverage": 9.4,
            "cfo_to_pat": 0.92,
            "free_cash_flow": 12500000000.0,
            "working_capital_days": 54.0,
            "pe_ratio": 24.5,
            "pb_ratio": 4.1,
            "ev_to_ebitda": 14.8,
            "promoter_pledging_pct": 0.0,
            "institutional_holding_pct": 38.5,
            "governance_score": 1.0
        }),
        financial_statements=data.get("financial_statements", []),
        corporate_filings=data.get("corporate_filings", []),
        news_disclosures=data.get("news_disclosures", []),
        market_data=data.get("market_data", {
            "close": 2450.0,
            "rsi_14": 54.2
        }),
        institutional_flows=data.get("institutional_flows", {
            "fii_net_30d": 150000000.0,
            "dii_net_30d": 320000000.0
        }),
        macro_regime=data.get("macro_regime", {
            "regimeLabel": "BULL_TREND",
            "compositeScore": 0.65
        }),
        quant_signals=data.get("quant_signals", {
            "predicted_return": 0.0062,
            "confidence": 0.78
        }),
        evidence_registry=data.get("evidence_registry", [])
    )

    try:
        report = engine.generate_report(ctx, strict_pit=strict_pit)
        return report
    except PITViolationError as e:
        raise HTTPException(status_code=422, detail=f"Point-in-Time causality violation: {str(e)}")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Research generation error: {str(e)}")


@router.get("/{symbol}/latest", response_model=ResearchReport)
def get_latest_research(symbol: str):
    """
    Retrieve latest cached research report for a security.
    """
    report = engine.get_cached_report(symbol.upper())
    if not report:
        # Generate default baseline report if not in cache
        return generate_company_research(symbol)
    return report


@router.get("/report/{report_id}", response_model=ResearchReport)
def get_report_by_id(report_id: str):
    """
    Retrieve research report by report_id.
    """
    report = engine.get_cached_report(report_id)
    if not report:
        raise HTTPException(status_code=404, detail=f"Research report '{report_id}' not found")
    return report
