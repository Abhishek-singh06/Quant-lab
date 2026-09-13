"""Comprehensive Point-in-Time & Look-Ahead Bias Tests for Fundamental Engine.

Validates 7 critical financial data integrity invariants:
1. Publication Lag Barrier: Financials are inaccessible before their verified available_at timestamp.
2. Historical Feature Invariance: Feature values as of T_1 never change when subsequent earnings are published at T_2.
3. Restatement Versioning: Original numbers are returned for queries prior to restatement publication.
4. Point-in-Time Valuation: P/E and EV ratios reflect strictly contemporaneous prices and known earnings.
5. TTM Look-Ahead Invariance: TTM aggregates use strictly the 4 latest available quarters as of query time.
6. Reporting Basis Isolation: Standalone and Consolidated metrics are strictly partitioned.
7. Graceful Degradation: Missing historical records return structured fallbacks without crash.
"""

import pytest
import pandas as pd
from app.warehouse.fundamental_feature_generator import FundamentalFeatureGenerator


@pytest.fixture
def mock_fundamental_dataset():
    return [
        # Q1 FY25 (Ended 2024-06-30, Available 2024-07-25)
        {
            "symbol": "RELIANCE",
            "period_end": "2024-06-30",
            "period_type": "QUARTERLY",
            "reporting_basis": "CONSOLIDATED",
            "available_at": "2024-07-25T18:00:00Z",
            "revenue": 230000.0,
            "ebitda": 41000.0,
            "ebit": 30000.0,
            "net_profit": 17500.0,
            "total_assets": 1700000.0,
            "total_equity": 750000.0,
            "total_debt": 310000.0,
            "version": 1,
            "restatement": False
        },
        # Q2 FY25 (Ended 2024-09-30, Available 2024-10-20)
        {
            "symbol": "RELIANCE",
            "period_end": "2024-09-30",
            "period_type": "QUARTERLY",
            "reporting_basis": "CONSOLIDATED",
            "available_at": "2024-10-20T18:00:00Z",
            "revenue": 235000.0,
            "ebitda": 42000.0,
            "ebit": 31000.0,
            "net_profit": 18000.0,
            "total_assets": 1720000.0,
            "total_equity": 765000.0,
            "total_debt": 315000.0,
            "version": 1,
            "restatement": False
        },
        # Q3 FY25 (Ended 2024-12-31, Available 2025-01-20)
        {
            "symbol": "RELIANCE",
            "period_end": "2024-12-31",
            "period_type": "QUARTERLY",
            "reporting_basis": "CONSOLIDATED",
            "available_at": "2025-01-20T18:00:00Z",
            "revenue": 240000.0,
            "ebitda": 42500.0,
            "ebit": 31500.0,
            "net_profit": 18500.0,
            "total_assets": 1750000.0,
            "total_equity": 780000.0,
            "total_debt": 320000.0,
            "version": 1,
            "restatement": False
        },
        # Q4 FY25 Original (Ended 2025-03-31, Available 2025-05-15)
        {
            "symbol": "RELIANCE",
            "period_end": "2025-03-31",
            "period_type": "QUARTERLY",
            "reporting_basis": "CONSOLIDATED",
            "available_at": "2025-05-15T18:00:00Z",
            "revenue": 245000.0,
            "ebitda": 43500.0,
            "ebit": 32000.0,
            "net_profit": 19000.0,
            "total_assets": 1780000.0,
            "total_equity": 800000.0,
            "total_debt": 325000.0,
            "version": 1,
            "restatement": False
        },
        # Q4 FY25 Restated (Available 2025-08-10 during annual audit reconciliation)
        {
            "symbol": "RELIANCE",
            "period_end": "2025-03-31",
            "period_type": "QUARTERLY",
            "reporting_basis": "CONSOLIDATED",
            "available_at": "2025-08-10T18:00:00Z",
            "revenue": 248000.0,
            "ebitda": 44000.0,
            "ebit": 32500.0,
            "net_profit": 19500.0,
            "total_assets": 1785000.0,
            "total_equity": 805000.0,
            "total_debt": 325000.0,
            "version": 2,
            "restatement": True
        },
        # Standalone Filing for Q4 FY25 (Available 2025-05-15)
        {
            "symbol": "RELIANCE",
            "period_end": "2025-03-31",
            "period_type": "QUARTERLY",
            "reporting_basis": "STANDALONE",
            "available_at": "2025-05-15T18:00:00Z",
            "revenue": 140000.0,
            "ebitda": 26000.0,
            "ebit": 19000.0,
            "net_profit": 11000.0,
            "total_assets": 950000.0,
            "total_equity": 520000.0,
            "total_debt": 180000.0,
            "version": 1,
            "restatement": False
        }
    ]


def test_1_earnings_not_accessible_before_published_date(mock_fundamental_dataset):
    """Test 1: Financial results for Q4 (ended 2025-03-31, published 2025-05-15) must NOT be visible on 2025-05-10."""
    features_before = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-05-10T00:00:00Z"
    )
    # On May 10, latest available is Q3 FY25 (ended 2024-12-31)
    assert features_before['latest_period_end'] == "2024-12-31"

    # On May 16, Q4 FY25 is available
    features_after = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-05-16T00:00:00Z"
    )
    assert features_after['latest_period_end'] == "2025-03-31"


def test_2_historical_feature_invariance_after_new_earnings(mock_fundamental_dataset):
    """Test 2: Feature vector at 2024-11-01 must be strictly invariant regardless of future disclosures."""
    features_t1 = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset[:2],  # Only Q1 and Q2 exist
        feature_timestamp="2024-11-01T00:00:00Z"
    )

    features_t1_with_future = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,  # Full future dataset provided
        feature_timestamp="2024-11-01T00:00:00Z"
    )

    assert features_t1['latest_period_end'] == features_t1_with_future['latest_period_end']
    assert features_t1['ebitda_margin'] == features_t1_with_future['ebitda_margin']
    assert features_t1['roe'] == features_t1_with_future['roe']
    assert features_t1['debt_to_equity'] == features_t1_with_future['debt_to_equity']


def test_3_restatement_handling_point_in_time(mock_fundamental_dataset):
    """Test 3: Query between 2025-05-16 and 2025-08-09 returns Version 1 (PAT 19,000). Query on 2025-08-11 returns Version 2 (PAT 19,500)."""
    features_before_restatement = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-06-01T00:00:00Z"
    )
    assert features_before_restatement['is_restated'] is False
    # NPM for V1: 19000 / 245000 = 7.7551%
    assert round(features_before_restatement['net_profit_margin'], 2) == 7.76

    features_after_restatement = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-08-15T00:00:00Z"
    )
    assert features_after_restatement['is_restated'] is True
    # NPM for V2: 19500 / 248000 = 7.8629%
    assert round(features_after_restatement['net_profit_margin'], 2) == 7.86


def test_4_point_in_time_pe_ratio_computation(mock_fundamental_dataset):
    """Test 4: P/E ratio at 2025-05-10 uses Q3 annualised earnings, while at 2025-05-16 uses Q4 annualised earnings."""
    # Market Cap: 1,800,000 Cr
    mcap = 1800000.0

    pe_before = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-05-10T00:00:00Z",
        current_market_cap_cr=mcap
    )
    # Q3 annualized PAT: 18500 * 4 = 74,000 -> PE = 1800000 / 74000 = 24.32
    assert pe_before['pe_ratio'] == 24.32

    pe_after = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-05-16T00:00:00Z",
        current_market_cap_cr=mcap
    )
    # Q4 annualized PAT: 19000 * 4 = 76,000 -> PE = 1800000 / 76000 = 23.68
    assert pe_after['pe_ratio'] == 23.68


def test_5_ttm_lookahead_invariance(mock_fundamental_dataset):
    """Test 5: TTM calculation on 2025-05-10 must fail or use older quarters because 4 quarters are not yet available."""
    ttm_3_quarters = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-05-10T00:00:00Z"
    )
    # Only 3 quarters available on May 10 -> TTM is None (strict no lookahead)
    assert ttm_3_quarters['ttm_revenue_cr'] is None

    ttm_4_quarters = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-05-16T00:00:00Z"
    )
    # 4 quarters available on May 16: Q1 (230k) + Q2 (235k) + Q3 (240k) + Q4 (245k) = 950,000 Cr
    assert ttm_4_quarters['ttm_revenue_cr'] == 950000.0
    # PAT: 17.5k + 18k + 18.5k + 19k = 73,000 Cr
    assert ttm_4_quarters['ttm_net_profit_cr'] == 73000.0


def test_6_reporting_basis_isolation(mock_fundamental_dataset):
    """Test 6: Requesting STANDALONE basis isolates standalone statements and does not bleed consolidated figures."""
    cons_features = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-05-16T00:00:00Z",
        reporting_basis="CONSOLIDATED"
    )
    stand_features = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="RELIANCE",
        statements=mock_fundamental_dataset,
        feature_timestamp="2025-05-16T00:00:00Z",
        reporting_basis="STANDALONE"
    )

    assert cons_features['reporting_basis'] == "CONSOLIDATED"
    assert stand_features['reporting_basis'] == "STANDALONE"
    assert cons_features['net_profit_margin'] != stand_features['net_profit_margin']


def test_7_missing_fundamental_graceful_handling():
    """Test 7: Empty statement list returns safe structured dictionary without runtime exceptions."""
    empty_features = FundamentalFeatureGenerator.calculate_fundamental_features(
        symbol="UNKNOWN_CO",
        statements=[],
        feature_timestamp="2025-05-16T00:00:00Z"
    )
    assert empty_features['has_fundamental_data'] is False
    assert empty_features['pe_ratio'] is None
    assert empty_features['latest_period_end'] is None
