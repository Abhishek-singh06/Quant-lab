"""Adversarial Tests for Point-In-Time Invariance and Restated Financial Data."""

import pytest
from datetime import date, datetime, timezone

from app.data_acquisition.models import (
    FinancialStatementRecord,
    InstitutionalFlowRecord,
    CorporateActionRecord,
    CorporateActionTypeEnum,
    ExchangeEnum,
)
from app.ai_research.pit_validator import ResearchContextPITValidator, PITViolationError
from app.ai_research.models import ResearchContext, ResearchEvidence, ResearchSourceType


class TestPointInTimeAdversarial:

    def test_future_filing_strictly_rejected(self):
        validator = ResearchContextPITValidator()
        decision_time = datetime(2024, 6, 1, 15, 30, tzinfo=timezone.utc)

        # Evidence published on June 5 (future)
        future_ev = ResearchEvidence(
            id="EVID_FUTURE_01",
            source_type=ResearchSourceType.REGULATORY_FILING,
            source_name="NSE_FILING",
            document_id="FILING_2024_06_05",
            information_available_at=datetime(2024, 6, 5, 10, 0, tzinfo=timezone.utc),
            metric_name="REVENUE_DISCLOSURE",
            value=50000.0,
        )

        context = ResearchContext(
            symbol="RELIANCE",
            company_name="Reliance Industries Ltd.",
            context_as_of=decision_time,
            evidence_registry=[future_ev],
        )

        with pytest.raises(PITViolationError, match="PIT Leakage: Evidence 'EVID_FUTURE_01'"):
            validator.validate_and_sanitize(context, strict=True)

    def test_future_financial_statement_strictly_rejected(self):
        validator = ResearchContextPITValidator()
        decision_time = datetime(2024, 4, 15, 15, 30, tzinfo=timezone.utc)

        # Financial statement available on May 2 (future)
        future_funda = ResearchEvidence(
            id="EVID_FUTURE_02",
            source_type=ResearchSourceType.FINANCIAL_STATEMENT,
            source_name="ANNUAL_REPORT",
            document_id="FS_2024_Q4",
            information_available_at=datetime(2024, 5, 2, 9, 0, tzinfo=timezone.utc),
            metric_name="PAT",
            value=8000.0,
        )

        context = ResearchContext(
            symbol="INFY",
            company_name="Infosys Ltd.",
            context_as_of=decision_time,
            evidence_registry=[future_funda],
        )

        with pytest.raises(PITViolationError, match="PIT Leakage: Evidence 'EVID_FUTURE_02'"):
            validator.validate_and_sanitize(context, strict=True)

    def test_restated_financial_statement_version_preservation(self):
        """Verify that restated financials maintain original vs restated version history."""
        # Original filing as of May 2023 (Version 1)
        original_stmt = FinancialStatementRecord(
            symbol="INFY",
            period_start=date(2023, 1, 1),
            period_end=date(2023, 3, 31),
            published_at=datetime(2023, 5, 1, 10, 0, tzinfo=timezone.utc),
            information_available_at=datetime(2023, 5, 1, 10, 0, tzinfo=timezone.utc),
            revenue=37441.0,
            ebitda=9000.0,
            ebit=8000.0,
            pat=6128.0,
            eps=14.7,
            total_assets=120000.0,
            total_liabilities=40000.0,
            total_debt=5000.0,
            total_equity=75000.0,
            operating_cash_flow=5500.0,
            free_cash_flow=4500.0,
            shares_outstanding=4150000000,
            source="BSE_ORIGINAL",
            is_restated=False,
            restatement_version=1,
        )

        # Restated filing published in November 2023 (Version 2)
        restated_stmt = FinancialStatementRecord(
            symbol="INFY",
            period_start=date(2023, 1, 1),
            period_end=date(2023, 3, 31),
            published_at=datetime(2023, 11, 15, 10, 0, tzinfo=timezone.utc),
            information_available_at=datetime(2023, 11, 15, 10, 0, tzinfo=timezone.utc),
            revenue=37300.0,  # Restated slightly downward
            ebitda=8950.0,
            ebit=7950.0,
            pat=6050.0,
            eps=14.5,
            total_assets=119800.0,
            total_liabilities=40000.0,
            total_debt=5000.0,
            total_equity=74800.0,
            operating_cash_flow=5450.0,
            free_cash_flow=4450.0,
            shares_outstanding=4150000000,
            source="BSE_RESTATED",
            is_restated=True,
            restatement_version=2,
        )

        # For decision time in June 2023: ONLY Version 1 is available!
        decision_time_june = datetime(2023, 6, 1, 15, 30, tzinfo=timezone.utc)
        assert original_stmt.information_available_at <= decision_time_june
        assert restated_stmt.information_available_at > decision_time_june

        # For decision time in December 2023: Version 2 is available
        decision_time_dec = datetime(2023, 12, 1, 15, 30, tzinfo=timezone.utc)
        assert restated_stmt.information_available_at <= decision_time_dec
