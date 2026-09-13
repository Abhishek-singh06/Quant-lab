"""Mandatory Point-in-Time Look-Ahead Bias Tests for Institutional and Mutual Fund Features."""

import pytest
import pandas as pd
from app.warehouse.institutional_feature_generator import InstitutionalFeatureGenerator


def test_july_portfolio_published_in_august_lookahead_bias():
    """TEST 1: A mutual fund portfolio as of July 31, 2026 was published on August 15, 2026.

    Feature generated on August 1, 2026 must NOT see July 31 data (it sees June 30 data).
    Feature generated on August 16, 2026 MUST see July 31 data.
    """
    holdings = [
        # June 30 portfolio, published July 15
        {
            'scheme_id': 101,
            'symbol': 'HDFCBANK',
            'data_as_of': '2026-06-30',
            'published_at': '2026-07-15',
            'available_at': '2026-07-15T18:30:00Z',
            'portfolio_weight': 8.50,
            'weight_change_pp': 0.0,
            'change_type': 'UNCHANGED'
        },
        # July 31 portfolio, published August 15
        {
            'scheme_id': 101,
            'symbol': 'HDFCBANK',
            'data_as_of': '2026-07-31',
            'published_at': '2026-08-15',
            'available_at': '2026-08-15T18:30:00Z',
            'portfolio_weight': 9.20,
            'weight_change_pp': 0.70,
            'change_type': 'INCREASED'
        }
    ]

    # As of August 1, 2026
    features_aug1 = InstitutionalFeatureGenerator.calculate_institutional_features(
        symbol='HDFCBANK',
        holdings=holdings,
        flows=[],
        ownership=[],
        feature_timestamp='2026-08-01T10:00:00Z'
    )

    # Must reflect June 30 weight (8.50%), NOT July 31 weight (9.20%)
    assert features_aug1['total_funds_holding'] == 1
    assert features_aug1['avg_fund_portfolio_weight'] == 8.50
    assert features_aug1['funds_increasing_stake'] == 0

    # As of August 16, 2026 (after publication on Aug 15)
    features_aug16 = InstitutionalFeatureGenerator.calculate_institutional_features(
        symbol='HDFCBANK',
        holdings=holdings,
        flows=[],
        ownership=[],
        feature_timestamp='2026-08-16T10:00:00Z'
    )

    # Must now reflect July 31 weight (9.20%) and stake increase
    assert features_aug16['total_funds_holding'] == 1
    assert features_aug16['avg_fund_portfolio_weight'] == 9.20
    assert features_aug16['funds_increasing_stake'] == 1
    assert features_aug16['sum_fund_weight_change_pp'] == 0.70


def test_reproducibility_historical_invariance():
    """TEST 2: Invariance Test. Adding future disclosure records must NOT change

    historical feature output for a timestamp prior to the new disclosure.
    """
    initial_holdings = [
        {
            'scheme_id': 101,
            'symbol': 'RELIANCE',
            'data_as_of': '2026-06-30',
            'published_at': '2026-07-15',
            'available_at': '2026-07-15T18:30:00Z',
            'portfolio_weight': 7.10,
            'weight_change_pp': -0.20,
            'change_type': 'DECREASED'
        }
    ]

    features_before = InstitutionalFeatureGenerator.calculate_institutional_features(
        symbol='RELIANCE',
        holdings=initial_holdings,
        flows=[],
        ownership=[],
        feature_timestamp='2026-08-01T10:00:00Z'
    )

    # Future holding added later to the database
    updated_holdings = initial_holdings + [
        {
            'scheme_id': 101,
            'symbol': 'RELIANCE',
            'data_as_of': '2026-07-31',
            'published_at': '2026-08-15',
            'available_at': '2026-08-15T18:30:00Z',
            'portfolio_weight': 8.00,
            'weight_change_pp': 0.90,
            'change_type': 'INCREASED'
        }
    ]

    features_after = InstitutionalFeatureGenerator.calculate_institutional_features(
        symbol='RELIANCE',
        holdings=updated_holdings,
        flows=[],
        ownership=[],
        feature_timestamp='2026-08-01T10:00:00Z'
    )

    assert features_before == features_after


def test_fii_dii_intraday_cutoff_leakage():
    """TEST 3: Daily FII/DII flow for date T published at 18:30 IST (13:00 UTC)

    cannot be accessed during market hours (e.g. 09:15-15:30 IST / 03:45-10:00 UTC).
    """
    flows = [
        {
            'trade_date': '2026-08-10',
            'institution_type': 'FII',
            'net_value': 1500.0,
            'available_at': '2026-08-10T13:00:00Z'  # 18:30 IST
        },
        {
            'trade_date': '2026-08-10',
            'institution_type': 'DII',
            'net_value': 800.0,
            'available_at': '2026-08-10T13:00:00Z'  # 18:30 IST
        }
    ]

    # Market close check on 2026-08-10 at 15:30 IST (10:00 UTC)
    features_midday = InstitutionalFeatureGenerator.calculate_institutional_features(
        symbol='INFY',
        holdings=[],
        flows=flows,
        ownership=[],
        feature_timestamp='2026-08-10T10:00:00Z'
    )

    assert features_midday['fii_net_flow_5d_cr'] == 0.0
    assert features_midday['dii_net_flow_5d_cr'] == 0.0

    # Post-market check on 2026-08-10 at 19:00 IST (13:30 UTC)
    features_post_market = InstitutionalFeatureGenerator.calculate_institutional_features(
        symbol='INFY',
        holdings=[],
        flows=flows,
        ownership=[],
        feature_timestamp='2026-08-10T13:30:00Z'
    )

    assert features_post_market['fii_net_flow_5d_cr'] == 1500.0
    assert features_post_market['dii_net_flow_5d_cr'] == 800.0


def test_quarterly_shareholding_pattern_lag():
    """TEST 4: Q1 (period ending June 30) shareholding pattern published on July 21.

    Feature on July 10 cannot see Q1 pattern; feature on July 22 sees Q1 pattern.
    """
    ownership = [
        # Q4 Previous year (ended March 31), published April 21
        {
            'symbol': 'TCS',
            'period_end': '2026-03-31',
            'published_at': '2026-04-21',
            'available_at': '2026-04-21T18:30:00Z',
            'institution_type': 'FII',
            'ownership_percentage': 12.50
        },
        # Q1 Current year (ended June 30), published July 21
        {
            'symbol': 'TCS',
            'period_end': '2026-06-30',
            'published_at': '2026-07-21',
            'available_at': '2026-07-21T18:30:00Z',
            'institution_type': 'FII',
            'ownership_percentage': 14.10
        }
    ]

    # As of July 10, 2026
    feat_july10 = InstitutionalFeatureGenerator.calculate_institutional_features(
        symbol='TCS',
        holdings=[],
        flows=[],
        ownership=ownership,
        feature_timestamp='2026-07-10T10:00:00Z'
    )
    assert feat_july10['fii_ownership_pct'] == 12.50

    # As of July 22, 2026
    feat_july22 = InstitutionalFeatureGenerator.calculate_institutional_features(
        symbol='TCS',
        holdings=[],
        flows=[],
        ownership=ownership,
        feature_timestamp='2026-07-22T10:00:00Z'
    )
    assert feat_july22['fii_ownership_pct'] == 14.10
