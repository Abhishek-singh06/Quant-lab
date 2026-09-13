"""
Point-In-Time (PIT) Safety Validator for AI Fundamental Research.
Guarantees that all financial statements, regulatory filings, news, and market data
were strictly available on or before the specified context_as_of timestamp.
"""
from datetime import datetime
from typing import List, Tuple, Dict, Any
from app.ai_research.models import ResearchContext, ResearchEvidence


class PITViolationError(Exception):
    """Raised when an evidence item violates point-in-time causality."""
    pass


class ResearchContextPITValidator:
    """
    Validates and sanitizes research context against future look-ahead leakage.
    """

    @staticmethod
    def _normalize_dt(dt: datetime) -> datetime:
        if dt is None:
            return datetime.utcnow()
        if dt.tzinfo is not None:
            return dt.replace(tzinfo=None)
        return dt

    @classmethod
    def _parse_timestamp(cls, ts: Any) -> datetime:
        if isinstance(ts, datetime):
            return cls._normalize_dt(ts)
        if isinstance(ts, str):
            clean_ts = ts.replace("Z", "")
            try:
                parsed = datetime.fromisoformat(clean_ts)
                return cls._normalize_dt(parsed)
            except Exception:
                pass
        return datetime.utcnow()

    @classmethod
    def validate_and_sanitize(cls, context: ResearchContext, strict: bool = True) -> Tuple[ResearchContext, List[str]]:
        """
        Validates the research context.
        If strict=True, raises PITViolationError on any future leak.
        If strict=False, filters out future items and records violations.
        """
        as_of = cls._normalize_dt(context.context_as_of)
        violations = []
        valid_evidence: List[ResearchEvidence] = []

        # 1. Validate Evidence Registry
        for ev in context.evidence_registry:
            ev_avail = cls._normalize_dt(ev.information_available_at)
            if ev_avail > as_of:
                msg = (f"PIT Leakage: Evidence '{ev.id}' ({ev.metric_name}) available at "
                       f"{ev_avail.isoformat()} is after context as_of {as_of.isoformat()}")
                violations.append(msg)
                if strict:
                    raise PITViolationError(msg)
            else:
                valid_evidence.append(ev)

        # 2. Validate Corporate Filings
        valid_filings = []
        for filing in context.corporate_filings:
            info_avail_raw = filing.get("information_available_at")
            if info_avail_raw:
                info_avail = cls._parse_timestamp(info_avail_raw)
                if info_avail > as_of:
                    msg = (f"PIT Leakage: Filing '{filing.get('document_id', 'unknown')}' "
                           f"available at {info_avail.isoformat()} is after {as_of.isoformat()}")
                    violations.append(msg)
                    if strict:
                        raise PITViolationError(msg)
                    continue
            valid_filings.append(filing)

        # 3. Validate News / Disclosures
        valid_news = []
        for news in context.news_disclosures:
            info_avail_raw = news.get("information_available_at") or news.get("published_at")
            if info_avail_raw:
                info_avail = cls._parse_timestamp(info_avail_raw)
                if info_avail > as_of:
                    msg = (f"PIT Leakage: News article '{news.get('headline', 'unknown')}' "
                           f"available at {info_avail.isoformat()} is after {as_of.isoformat()}")
                    violations.append(msg)
                    if strict:
                        raise PITViolationError(msg)
                    continue
            valid_news.append(news)

        # 4. Validate Financial Statements
        valid_statements = []
        for stmt in context.financial_statements:
            info_avail_raw = stmt.get("information_available_at") or stmt.get("filing_date")
            if info_avail_raw:
                info_avail = cls._parse_timestamp(info_avail_raw)
                if info_avail > as_of:
                    msg = (f"PIT Leakage: Financial statement for period '{stmt.get('period_end', 'unknown')}' "
                           f"available at {info_avail.isoformat()} is after {as_of.isoformat()}")
                    violations.append(msg)
                    if strict:
                        raise PITViolationError(msg)
                    continue
            valid_statements.append(stmt)

        # Build sanitized copy
        sanitized = context.model_copy(update={
            "evidence_registry": valid_evidence,
            "corporate_filings": valid_filings,
            "news_disclosures": valid_news,
            "financial_statements": valid_statements
        })

        return sanitized, violations
