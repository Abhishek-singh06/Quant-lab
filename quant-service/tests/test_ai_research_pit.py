"""
Adversarial tests for AI Fundamental Research Point-in-Time (PIT) Safety.
Verifies that all future financial statements, filings, and news are strictly rejected.
"""
import pytest
from datetime import datetime, timedelta
from app.ai_research.models import ResearchContext, ResearchEvidence, ResearchSourceType
from app.ai_research.pit_validator import ResearchContextPITValidator, PITViolationError
from app.ai_research.engine import CompanyResearchEngine


def test_future_evidence_item_rejected():
    as_of = datetime(2024, 3, 31, 0, 0, 0)
    future_time = datetime(2024, 4, 15, 0, 0, 0)

    future_evidence = ResearchEvidence(
        id="EV-FUTURE-01",
        source_type=ResearchSourceType.FINANCIAL_STATEMENT,
        source_name="Q4_FINANCIAL_REPORT",
        information_available_at=future_time,
        metric_name="roe",
        value=0.22
    )

    ctx = ResearchContext(
        symbol="RELIANCE",
        company_name="Reliance Industries Ltd",
        context_as_of=as_of,
        evidence_registry=[future_evidence]
    )

    with pytest.raises(PITViolationError) as exc_info:
        ResearchContextPITValidator.validate_and_sanitize(ctx, strict=True)
    assert "PIT Leakage: Evidence 'EV-FUTURE-01'" in str(exc_info.value)


def test_future_corporate_filing_rejected():
    as_of = datetime(2024, 3, 31, 0, 0, 0)
    ctx = ResearchContext(
        symbol="TCS",
        company_name="Tata Consultancy Services Ltd",
        context_as_of=as_of,
        corporate_filings=[
            {
                "document_id": "SEBI-FILING-20240410",
                "headline": "Annual Board Meeting Outcome",
                "information_available_at": "2024-04-10T10:00:00Z"
            }
        ]
    )

    with pytest.raises(PITViolationError) as exc_info:
        ResearchContextPITValidator.validate_and_sanitize(ctx, strict=True)
    assert "PIT Leakage: Filing 'SEBI-FILING-20240410'" in str(exc_info.value)


def test_future_news_article_rejected():
    as_of = datetime(2024, 3, 31, 0, 0, 0)
    ctx = ResearchContext(
        symbol="INFY",
        company_name="Infosys Ltd",
        context_as_of=as_of,
        news_disclosures=[
            {
                "headline": "Q1 Revenue Exceeds Guidance",
                "published_at": "2024-05-15T09:00:00Z",
                "information_available_at": "2024-05-15T09:00:00Z"
            }
        ]
    )

    with pytest.raises(PITViolationError) as exc_info:
        ResearchContextPITValidator.validate_and_sanitize(ctx, strict=True)
    assert "PIT Leakage: News article 'Q1 Revenue Exceeds Guidance'" in str(exc_info.value)


def test_valid_historical_context_accepted():
    as_of = datetime(2024, 3, 31, 0, 0, 0)
    past_time = datetime(2024, 1, 15, 0, 0, 0)

    past_evidence = ResearchEvidence(
        id="EV-PAST-01",
        source_type=ResearchSourceType.FINANCIAL_STATEMENT,
        source_name="Q3_FINANCIAL_REPORT",
        information_available_at=past_time,
        metric_name="roe",
        value=0.19
    )

    ctx = ResearchContext(
        symbol="HDFCBANK",
        company_name="HDFC Bank Ltd",
        context_as_of=as_of,
        evidence_registry=[past_evidence],
        corporate_filings=[
            {
                "document_id": "SEBI-FILING-20240120",
                "headline": "Q3 Results",
                "information_available_at": "2024-01-20T10:00:00Z"
            }
        ],
        news_disclosures=[
            {
                "headline": "Branch Expansion Update",
                "published_at": "2024-02-10T09:00:00Z",
                "information_available_at": "2024-02-10T09:00:00Z"
            }
        ]
    )

    sanitized, violations = ResearchContextPITValidator.validate_and_sanitize(ctx, strict=True)
    assert len(violations) == 0
    assert len(sanitized.evidence_registry) == 1
    assert len(sanitized.corporate_filings) == 1
    assert len(sanitized.news_disclosures) == 1


def test_non_strict_sanitization_filters_future_leakage():
    as_of = datetime(2024, 3, 31, 0, 0, 0)
    ctx = ResearchContext(
        symbol="ICICIBANK",
        company_name="ICICI Bank Ltd",
        context_as_of=as_of,
        corporate_filings=[
            {"document_id": "PAST-01", "information_available_at": "2024-01-10T00:00:00Z"},
            {"document_id": "FUTURE-01", "information_available_at": "2024-04-10T00:00:00Z"}
        ]
    )

    sanitized, violations = ResearchContextPITValidator.validate_and_sanitize(ctx, strict=False)
    assert len(violations) == 1
    assert len(sanitized.corporate_filings) == 1
    assert sanitized.corporate_filings[0]["document_id"] == "PAST-01"
