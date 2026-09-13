"""
News and Corporate Event Evidence Providers for QuantLab Part 13.
Consumes Part 5 News and Corporate Intelligence Engine outputs strictly point-in-time.
"""

from datetime import datetime
from typing import Any, Dict, List, Optional
from app.signals.models import Evidence, EvidenceCategory, EvidenceDirection
from app.signals.providers.base import SignalEvidenceProvider


class NewsEvidenceProvider(SignalEvidenceProvider):
    """Calculates normalized news evidence with recency decay and deduplication."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.NEWS

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        articles = context.get("news_articles", [])
        if not articles:
            return evidence_list

        # Deduplicate and filter strictly: published_at <= as_of_timestamp
        seen_clusters = set()
        for article in articles:
            pub_at = article.get("published_at")
            if not pub_at:
                continue
            if isinstance(pub_at, str):
                pub_at = datetime.fromisoformat(pub_at)
            
            # Strict point-in-time safety
            if pub_at > as_of_timestamp:
                continue
                
            cluster_id = article.get("cluster_id") or article.get("headline", "")
            if cluster_id in seen_clusters:
                continue
            seen_clusters.add(cluster_id)

            sentiment = float(article.get("sentiment_score", 0.0))  # -1.0 to +1.0
            importance = float(article.get("importance_score", 0.5))  # 0.0 to 1.0
            
            # Recency decay: exponential decay with half-life of 3 days (72 hours)
            hours_old = max(0.0, (as_of_timestamp - pub_at).total_seconds() / 3600.0)
            recency_weight = max(0.05, 0.5 ** (hours_old / 72.0))
            
            # Score combines sentiment, importance, and recency decay
            score = sentiment * importance * 100.0 * recency_weight
            dir_enum = EvidenceDirection.BULLISH if score > 15 else (EvidenceDirection.BEARISH if score < -15 else EvidenceDirection.NEUTRAL)
            
            evidence_list.append(Evidence(
                source="NEWS_INTELLIGENCE_ENGINE",
                category=self.category,
                feature=f"NEWS_EVENT_{article.get('category', 'GENERAL')}",
                raw_value=sentiment,
                raw_value_str=f"{sentiment:+.2f} sentiment (imp: {importance:.2f})",
                normalized_score=float(score),
                direction=dir_enum,
                strength=min(1.0, abs(score) / 100.0),
                quality=float(article.get("credibility_score", 0.9)),
                freshness=recency_weight,
                weight=importance * 1.5,
                timestamp=pub_at,
                available_at=pub_at,
                version="1.0.0",
                reason=f"{article.get('headline', 'News event')} (sentiment: {sentiment:+.2f}, age: {hours_old/24.0:.1f}d)"
            ))

        return evidence_list


class CorporateEventEvidenceProvider(SignalEvidenceProvider):
    """Calculates normalized corporate event evidence (earnings, dividends, buybacks)."""
    
    @property
    def category(self) -> EvidenceCategory:
        return EvidenceCategory.CORPORATE_EVENTS

    def collect_evidence(
        self,
        symbol: str,
        as_of_timestamp: datetime,
        context: Optional[Dict[str, Any]] = None
    ) -> List[Evidence]:
        evidence_list: List[Evidence] = []
        if not context:
            return evidence_list
            
        events = context.get("corporate_events", [])
        for event in events:
            announced_at = event.get("announced_at")
            if not announced_at:
                continue
            if isinstance(announced_at, str):
                announced_at = datetime.fromisoformat(announced_at)
            
            # Strict point-in-time check
            if announced_at > as_of_timestamp:
                continue

            event_type = event.get("event_type", "GENERAL")
            impact_score = float(event.get("impact_score", 0.0))  # -100 to +100
            
            days_old = max(0.0, (as_of_timestamp - announced_at).total_seconds() / 86400.0)
            freshness = max(0.1, 1.0 - (days_old / 30.0))
            
            dir_enum = EvidenceDirection.BULLISH if impact_score > 15 else (EvidenceDirection.BEARISH if impact_score < -15 else EvidenceDirection.NEUTRAL)
            
            evidence_list.append(Evidence(
                source="CORPORATE_ACTIONS_ENGINE",
                category=self.category,
                feature=f"CORP_EVENT_{event_type}",
                raw_value=impact_score,
                raw_value_str=f"{event_type} (impact: {impact_score:+.1f})",
                normalized_score=float(impact_score * freshness),
                direction=dir_enum,
                strength=min(1.0, abs(impact_score) / 100.0),
                quality=1.0,
                freshness=freshness,
                weight=1.2,
                timestamp=announced_at,
                available_at=announced_at,
                version="1.0.0",
                reason=f"Corporate Action {event_type}: {event.get('description', '')}"
            ))

        return evidence_list
