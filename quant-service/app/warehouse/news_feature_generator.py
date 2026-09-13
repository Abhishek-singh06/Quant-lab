"""Point-in-Time News and Corporate Intelligence Feature Generator.

CRITICAL FINANCIAL INTEGRITY RULE:
Every feature is calculated using ONLY observations where:
information_available_at <= feature_timestamp
"""

from typing import List, Dict, Any, Optional
import numpy as np
import pandas as pd
from datetime import datetime, timezone


class NewsFeatureGenerator:
    """Generates point-in-time quantitative sentiment and news signals

    without introducing forward-looking look-ahead bias.
    """

    @staticmethod
    def calculate_news_features(
        articles: List[Dict[str, Any]],
        events: List[Dict[str, Any]],
        feature_timestamp: str,
        half_life_hours: float = 24.0
    ) -> Dict[str, Any]:
        """Calculates point-in-time news and corporate event features as of `feature_timestamp`.

        STRICT FILTER: Rejects any article or event where information_available_at > feature_timestamp.
        """
        target_time = pd.to_datetime(feature_timestamp, utc=True)

        # 1. Point-in-time filtering
        valid_articles = [
            a for a in articles
            if pd.to_datetime(a['information_available_at'], utc=True) <= target_time
        ]

        valid_events = [
            e for e in events
            if pd.to_datetime(e['information_available_at'], utc=True) <= target_time
        ]

        # 2. News Volume Signals
        news_count_1d = 0
        news_count_7d = 0
        pos_count = 0
        neg_count = 0
        weighted_sentiment_sum = 0.0
        total_weight = 0.0

        one_day_ago = target_time - pd.Timedelta(days=1)
        seven_days_ago = target_time - pd.Timedelta(days=7)

        for a in valid_articles:
            pub_time = pd.to_datetime(a['information_available_at'], utc=True)
            age_hours = max(0.0, (target_time - pub_time).total_seconds() / 3600.0)

            if pub_time >= one_day_ago:
                news_count_1d += 1
            if pub_time >= seven_days_ago:
                news_count_7d += 1

            sent = float(a.get('sentiment_score', 0.0))
            if sent > 0.2:
                pos_count += 1
            elif sent < -0.2:
                neg_count += 1

            # Exponential time decay: weight = exp(-ln(2) * age / half_life)
            weight = np.exp(-np.log(2.0) * (age_hours / max(1.0, half_life_hours)))
            weighted_sentiment_sum += sent * weight
            total_weight += weight

        avg_decayed_sentiment = (weighted_sentiment_sum / total_weight) if total_weight > 0 else 0.0

        # 3. Corporate Event Flags (Point-in-time)
        high_importance_count = 0
        earnings_event_flag = 0
        dividend_event_flag = 0
        management_change_flag = 0
        regulatory_risk_score = 0.0

        for e in valid_events:
            evt_time = pd.to_datetime(e['information_available_at'], utc=True)
            if evt_time >= seven_days_ago:
                etype = str(e.get('event_type', '')).upper()
                importance = float(e.get('importance_score', 0.5))

                if importance >= 0.70:
                    high_importance_count += 1

                if 'EARNINGS' in etype or 'RESULT' in etype:
                    earnings_event_flag = 1
                elif 'DIVIDEND' in etype:
                    dividend_event_flag = 1
                elif 'MANAGEMENT' in etype or 'CEO' in etype or 'CFO' in etype:
                    management_change_flag = 1
                elif 'REGULATORY' in etype or 'PENALTY' in etype or 'SEBI' in etype:
                    regulatory_risk_score = max(regulatory_risk_score, float(e.get('financial_impact_score', -0.8)))

        return {
            'feature_timestamp': feature_timestamp,
            'news_count_1d': news_count_1d,
            'news_count_7d': news_count_7d,
            'positive_news_count': pos_count,
            'negative_news_count': neg_count,
            'weighted_decay_sentiment': round(avg_decayed_sentiment, 4),
            'high_importance_event_count': high_importance_count,
            'earnings_event_flag': earnings_event_flag,
            'dividend_event_flag': dividend_event_flag,
            'management_change_flag': management_change_flag,
            'regulatory_risk_score': round(regulatory_risk_score, 4)
        }
