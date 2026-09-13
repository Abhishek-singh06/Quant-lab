"""
Mandatory Point-in-Time, Anti-Leakage, and Future-Data Invariance Tests for Part 13 Signal Engine.
"""

from datetime import datetime, timedelta
import copy
import pytest
from app.signals.models import SignalType
from app.signals.engine import CrossCheckSignalEngine


def test_mandatory_future_data_modification_invariance():
    """
    Mandatory Test: Take historical timestamp T. Generate SIGNAL(T).
    Append future data and modify future data dramatically.
    SIGNAL(T) MUST be strictly identical.
    """
    engine = CrossCheckSignalEngine()
    as_of = datetime(2024, 6, 15, 10, 0)

    base_context = {
        "technical_features": {
            "close": 2000.0,
            "sma_200": 1800.0,
            "rsi_14": 55.0,
            "macd_hist": 0.8,
            "position_52w": 0.70,
            "return_20d": 0.04,
        },
        "fundamental_ratios": {
            "available_at": datetime(2024, 5, 20),
            "eps_growth_yoy": 0.15,
            "roe": 0.18,
            "debt_to_equity": 0.40,
            "pe_ratio": 22.0,
        },
        "news_articles": [
            {
                "headline": "Product expansion announced in June",
                "published_at": datetime(2024, 6, 10, 14, 0),
                "sentiment_score": 0.60,
                "importance_score": 0.70,
            }
        ],
        "ml_predictions": {
            "model_version": "M1_RIDGE_2024_v1.0",
            "model_status": "VALIDATED",
            "predicted_return": 0.005,
            "probability_positive": 0.62,
            "predicted_volatility": 0.014,
        },
        "market_regime": {
            "model_version": "REGIME_v1.0.0",
            "direction_regime": "BULL",
            "direction_score": 50.0,
            "risk_regime": "RISK_ON",
            "risk_score": 40.0,
        }
    }

    # Step 1: Generate initial signal at T
    sig_1 = engine.generate_signal("RELIANCE", as_of, base_context)

    # Step 2: Append future data (after T = 2024-06-15)
    future_context = copy.deepcopy(base_context)
    future_context["news_articles"].append({
        "headline": "CATASTROPHIC FUTURE COLLAPSE IN AUGUST",
        "published_at": datetime(2024, 8, 20, 10, 0),  # > T
        "sentiment_score": -1.0,
        "importance_score": 1.0,
    })
    # Future fundamental filing
    future_context["future_fundamental"] = {
        "available_at": datetime(2024, 9, 1),
        "eps_growth_yoy": -0.80,
    }

    sig_2 = engine.generate_signal("RELIANCE", as_of, future_context)

    # Must be 100% identical
    assert sig_1.signal == sig_2.signal
    assert sig_1.signal_score == sig_2.signal_score
    assert sig_1.confidence == sig_2.confidence
    assert sig_1.direction == sig_2.direction


def test_mandatory_news_publication_timing():
    """
    Mandatory News Test:
    News published at 15:00.
    Signal at 14:30 MUST NOT use the news.
    Signal at 15:30 MAY use the news.
    """
    engine = CrossCheckSignalEngine()
    news_pub_time = datetime(2026, 8, 15, 15, 0)
    
    context = {
        "news_articles": [
            {
                "headline": "Major breakthrough contract signed",
                "published_at": news_pub_time,
                "sentiment_score": 0.90,
                "importance_score": 0.90,
            }
        ]
    }

    # Signal prior to publication (14:30)
    sig_before = engine.generate_signal("HDFCBANK", datetime(2026, 8, 15, 14, 30), context)
    news_ev_before = [e for e in sig_before.all_evidence if e.category.value == "NEWS"]
    assert len(news_ev_before) == 0

    # Signal after publication (15:30)
    sig_after = engine.generate_signal("HDFCBANK", datetime(2026, 8, 15, 15, 30), context)
    news_ev_after = [e for e in sig_after.all_evidence if e.category.value == "NEWS"]
    assert len(news_ev_after) == 1
    assert news_ev_after[0].normalized_score > 0


def test_mandatory_fundamental_disclosure_timing():
    """
    Mandatory Fundamental Test:
    Quarter period_end: 2024-03-31, published: 2024-05-15.
    Signal at 2024-04-10 MUST NOT use it.
    Signal at 2024-05-16 MAY use it.
    """
    engine = CrossCheckSignalEngine()
    
    context = {
        "fundamental_ratios": {
            "period_end": datetime(2024, 3, 31),
            "available_at": datetime(2024, 5, 15, 18, 0),
            "eps_growth_yoy": 0.35,
            "roe": 0.22,
        }
    }

    # Signal before publication (2024-04-10)
    sig_before = engine.generate_signal("INFY", datetime(2024, 4, 10, 10, 0), context)
    fund_before = [e for e in sig_before.all_evidence if e.category.value == "FUNDAMENTAL"]
    assert len(fund_before) == 0

    # Signal after publication (2024-05-16)
    sig_after = engine.generate_signal("INFY", datetime(2024, 5, 16, 10, 0), context)
    fund_after = [e for e in sig_after.all_evidence if e.category.value == "FUNDAMENTAL"]
    assert len(fund_after) >= 1


def test_mandatory_institutional_disclosure_timing():
    """
    Mandatory Institutional Test:
    MF disclosure data_as_of = August 31, published = September 10.
    Signal at September 1 MUST NOT use September 10 disclosure.
    """
    engine = CrossCheckSignalEngine()

    context = {
        "mutual_fund_data": {
            "data_as_of": datetime(2026, 8, 31),
            "published_at": datetime(2026, 9, 10, 17, 0),
            "net_buying_schemes_count": 25,
            "net_selling_schemes_count": 4,
        }
    }

    sig_before = engine.generate_signal("TATAMOTORS", datetime(2026, 9, 1, 10, 0), context)
    mf_before = [e for e in sig_before.all_evidence if e.category.value == "MUTUAL_FUNDS"]
    assert len(mf_before) == 0

    sig_after = engine.generate_signal("TATAMOTORS", datetime(2026, 9, 11, 10, 0), context)
    mf_after = [e for e in sig_after.all_evidence if e.category.value == "MUTUAL_FUNDS"]
    assert len(mf_after) == 1
