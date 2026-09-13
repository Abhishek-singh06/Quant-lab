"""Mandatory Look-Ahead Bias Tests for News and Corporate Intelligence Pipeline.

Tests exact requirements 40.1 to 40.5:
- TEST 1: Query at 14:59 for article published at 15:00 -> NOT returned.
- TEST 2: Query at 15:01 -> Returned.
- TEST 3: Generate feature at 14:59, then insert future news -> Feature MUST NOT change.
- TEST 4: Corporate result for Q1 announced in May -> Must NOT appear in feature generated in April.
- TEST 5: Future dividend announcement -> Must NOT affect historical features before its available timestamp.
"""

import pytest
import pandas as pd
from app.warehouse.news_feature_generator import NewsFeatureGenerator


def test_1_article_not_available_prior_to_published_timestamp():
    """TEST 1: Insert article published at 2020-01-10 15:00 UTC.

    Query at 2020-01-10 14:59 UTC -> Article NOT returned.
    """
    articles = [
        {
            'title': 'Reliance bags major contract',
            'information_available_at': '2020-01-10T15:00:00Z',
            'sentiment_score': 0.8
        }
    ]

    features = NewsFeatureGenerator.calculate_news_features(
        articles=articles,
        events=[],
        feature_timestamp='2020-01-10T14:59:00Z'
    )

    assert features['news_count_1d'] == 0
    assert features['positive_news_count'] == 0
    assert features['weighted_decay_sentiment'] == 0.0


def test_2_article_available_after_published_timestamp():
    """TEST 2: Query at 2020-01-10 15:01 UTC -> Article IS returned."""
    articles = [
        {
            'title': 'Reliance bags major contract',
            'information_available_at': '2020-01-10T15:00:00Z',
            'sentiment_score': 0.8
        }
    ]

    features = NewsFeatureGenerator.calculate_news_features(
        articles=articles,
        events=[],
        feature_timestamp='2020-01-10T15:01:00Z'
    )

    assert features['news_count_1d'] == 1
    assert features['positive_news_count'] == 1
    assert features['weighted_decay_sentiment'] == 0.8


def test_3_historical_features_remain_invariant_when_future_news_added():
    """TEST 3: Generate feature for 2020-01-10 14:59 UTC.

    Then insert future news from 2020-01-10 15:00 to 2020-01-15.
    The historical feature at 14:59 MUST NOT change.
    """
    past_articles = [
        {'title': 'Article 1', 'information_available_at': '2020-01-09T10:00:00Z', 'sentiment_score': 0.5},
        {'title': 'Article 2', 'information_available_at': '2020-01-10T08:00:00Z', 'sentiment_score': 0.6}
    ]

    # Initial historical feature
    features_before = NewsFeatureGenerator.calculate_news_features(
        articles=past_articles,
        events=[],
        feature_timestamp='2020-01-10T14:59:00Z'
    )

    # Future news stream arrives
    future_articles = past_articles + [
        {'title': 'Breaking Future News', 'information_available_at': '2020-01-10T15:00:00Z', 'sentiment_score': -0.9},
        {'title': 'Next Day News', 'information_available_at': '2020-01-11T12:00:00Z', 'sentiment_score': -0.8},
        {'title': 'Next Week News', 'information_available_at': '2020-01-15T09:00:00Z', 'sentiment_score': 1.0}
    ]

    features_after = NewsFeatureGenerator.calculate_news_features(
        articles=future_articles,
        events=[],
        feature_timestamp='2020-01-10T14:59:00Z'
    )

    # Every feature value must be 100% identical
    for k in features_before:
        assert features_before[k] == features_after[k], (
            f"LOOK-AHEAD BIAS DETECTED in news feature '{k}'! "
            f"Before: {features_before[k]} vs After: {features_after[k]}"
        )


def test_4_q1_results_announced_in_may_not_visible_in_april():
    """TEST 4: A corporate result for Q1 (period ending March 31) announced on May 15

    MUST NOT appear in a feature generated on April 10.
    """
    events = [
        {
            'event_type': 'EARNINGS_RESULT',
            'period': 'Q1 2024',
            'event_date': '2024-03-31',
            'information_available_at': '2024-05-15T16:30:00Z',
            'importance_score': 0.90
        }
    ]

    # Query on April 10 (Before May 15 announcement)
    april_features = NewsFeatureGenerator.calculate_news_features(
        articles=[],
        events=events,
        feature_timestamp='2024-04-10T10:00:00Z'
    )
    assert april_features['earnings_event_flag'] == 0
    assert april_features['high_importance_event_count'] == 0

    # Query on May 16 (After announcement)
    may_features = NewsFeatureGenerator.calculate_news_features(
        articles=[],
        events=events,
        feature_timestamp='2024-05-16T10:00:00Z'
    )
    assert may_features['earnings_event_flag'] == 1
    assert may_features['high_importance_event_count'] == 1


def test_5_future_dividend_announcement_does_not_leak():
    """TEST 5: A future dividend announcement on 2024-06-01

    MUST NOT affect historical features on 2024-05-20.
    """
    events = [
        {
            'event_type': 'DIVIDEND',
            'information_available_at': '2024-06-01T11:00:00Z',
            'importance_score': 0.75
        }
    ]

    features_before_announcement = NewsFeatureGenerator.calculate_news_features(
        articles=[],
        events=events,
        feature_timestamp='2024-05-20T10:00:00Z'
    )
    assert features_before_announcement['dividend_event_flag'] == 0

    features_after_announcement = NewsFeatureGenerator.calculate_news_features(
        articles=[],
        events=events,
        feature_timestamp='2024-06-02T10:00:00Z'
    )
    assert features_after_announcement['dividend_event_flag'] == 1
