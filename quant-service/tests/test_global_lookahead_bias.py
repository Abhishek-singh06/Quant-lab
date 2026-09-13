"""Mandatory Point-in-Time Look-Ahead Bias Tests for Global Market Features & Regimes."""

import pytest
import pandas as pd
from app.warehouse.global_feature_generator import GlobalFeatureGenerator


def test_1_historical_regime_invariant_when_future_observation_inserted():
    """TEST 1: Create a GlobalMarketRegime for 2026-01-10 09:15 IST (03:45 UTC).

    Insert a market observation that occurred later that day (e.g. 21:00 UTC).
    The previously calculated regime MUST NOT change.
    """
    initial_snapshots = [
        # Previous US close on Jan 9, 21:00 UTC (16:00 EST)
        {
            'canonical_symbol': 'SPX',
            'close': 4800.0,
            'change_percent': 0.50,
            'source_timestamp': '2026-01-09T21:00:00Z'
        },
        {
            'canonical_symbol': 'VIX',
            'close': 16.5,
            'change_percent': -2.0,
            'source_timestamp': '2026-01-09T21:00:00Z'
        },
        # Asian morning on Jan 10, 02:00 UTC (11:00 JST)
        {
            'canonical_symbol': 'N225',
            'close': 36000.0,
            'change_percent': 0.80,
            'source_timestamp': '2026-01-10T02:00:00Z'
        }
    ]

    regime_before = GlobalFeatureGenerator.calculate_global_features(
        snapshots=initial_snapshots,
        feature_timestamp='2026-01-10T03:45:00Z'  # 09:15 IST
    )

    # Future US session on Jan 10, 21:00 UTC added later
    updated_snapshots = initial_snapshots + [
        {
            'canonical_symbol': 'SPX',
            'close': 4700.0,
            'change_percent': -2.08,
            'source_timestamp': '2026-01-10T21:00:00Z'  # Future!
        },
        {
            'canonical_symbol': 'VIX',
            'close': 23.0,
            'change_percent': 39.39,
            'source_timestamp': '2026-01-10T21:00:00Z'  # Future!
        }
    ]

    regime_after = GlobalFeatureGenerator.calculate_global_features(
        snapshots=updated_snapshots,
        feature_timestamp='2026-01-10T03:45:00Z'  # 09:15 IST
    )

    assert regime_before == regime_after
    assert regime_after['spx_1d_return'] == 0.50
    assert regime_after['vix_level'] == 16.50


def test_2_future_us_close_cannot_appear_in_morning_indian_snapshot():
    """TEST 2: Ensure US market close data from today's future session cannot appear

    in an Indian morning snapshot (09:15 IST / 03:45 UTC).
    """
    snapshots = [
        # Yesterday's US close (Jan 9, 21:00 UTC)
        {
            'canonical_symbol': 'NASDAQ',
            'close': 15000.0,
            'change_percent': 1.20,
            'source_timestamp': '2026-01-09T21:00:00Z'
        },
        # Today's US close (Jan 10, 21:00 UTC - 16:00 EST)
        {
            'canonical_symbol': 'NASDAQ',
            'close': 14600.0,
            'change_percent': -2.67,
            'source_timestamp': '2026-01-10T21:00:00Z'
        }
    ]

    # Morning evaluation at 09:15 IST on Jan 10 (03:45 UTC)
    morning_feat = GlobalFeatureGenerator.calculate_global_features(
        snapshots=snapshots,
        feature_timestamp='2026-01-10T03:45:00Z'
    )

    assert morning_feat['nasdaq_1d_return'] == 1.20

    # Evening evaluation at 22:00 UTC on Jan 10 (after US close)
    evening_feat = GlobalFeatureGenerator.calculate_global_features(
        snapshots=snapshots,
        feature_timestamp='2026-01-10T22:00:00Z'
    )

    assert evening_feat['nasdaq_1d_return'] == -2.67


def test_3_rolling_returns_do_not_use_future_observations():
    """TEST 3: Ensure rolling multi-day features do not use future observations."""
    snapshots = [
        {'canonical_symbol': 'SPX', 'close': 4800.0, 'change_percent': 0.0, 'source_timestamp': '2026-01-01T21:00:00Z'},
        {'canonical_symbol': 'SPX', 'close': 4850.0, 'change_percent': 1.04, 'source_timestamp': '2026-01-05T21:00:00Z'},
        {'canonical_symbol': 'SPX', 'close': 4900.0, 'change_percent': 1.03, 'source_timestamp': '2026-01-10T21:00:00Z'},
        {'canonical_symbol': 'SPX', 'close': 5000.0, 'change_percent': 2.04, 'source_timestamp': '2026-01-15T21:00:00Z'}
    ]

    # Feature as of Jan 11 (must not see Jan 15 return)
    feat_jan11 = GlobalFeatureGenerator.calculate_global_features(
        snapshots=snapshots,
        feature_timestamp='2026-01-11T00:00:00Z'
    )

    assert feat_jan11['spx_1d_return'] == 1.03
    # 4800 -> 4900 is +2.0833%
    assert feat_jan11['spx_20d_return'] == 2.0833


def test_4_regime_reproducible_strictly_point_in_time():
    """TEST 4: Ensure regime calculation is perfectly reproducible using only snapshots

    with source_timestamp <= regime_timestamp.
    """
    snapshots = [
        {'canonical_symbol': 'SPX', 'close': 4800.0, 'change_percent': 1.5, 'source_timestamp': '2026-01-09T21:00:00Z'},
        {'canonical_symbol': 'VIX', 'close': 13.5, 'change_percent': -5.0, 'source_timestamp': '2026-01-09T21:00:00Z'},
        {'canonical_symbol': 'DXY', 'close': 102.0, 'change_percent': -0.4, 'source_timestamp': '2026-01-09T21:00:00Z'}
    ]

    run1 = GlobalFeatureGenerator.calculate_global_features(snapshots, '2026-01-10T03:45:00Z')
    run2 = GlobalFeatureGenerator.calculate_global_features(snapshots, '2026-01-10T03:45:00Z')

    assert run1 == run2
    assert run1['regime_label'] == 'RISK_ON'


def test_5_removing_post_cutoff_data_leaves_regime_invariant():
    """TEST 5: Remove all data after timestamp T. The regime at T must remain identical."""
    full_dataset = [
        {'canonical_symbol': 'GOLD', 'close': 2600.0, 'change_percent': 0.5, 'source_timestamp': '2026-01-08T21:00:00Z'},
        {'canonical_symbol': 'GOLD', 'close': 2620.0, 'change_percent': 0.77, 'source_timestamp': '2026-01-09T21:00:00Z'},
        # Future
        {'canonical_symbol': 'GOLD', 'close': 2700.0, 'change_percent': 3.05, 'source_timestamp': '2026-01-12T21:00:00Z'}
    ]

    # Evaluated at 2026-01-10 00:00:00Z
    cutoff_time = '2026-01-10T00:00:00Z'
    result_with_future = GlobalFeatureGenerator.calculate_global_features(full_dataset, cutoff_time)

    # Pruned dataset (all records > cutoff_time removed)
    pruned_dataset = [s for s in full_dataset if s['source_timestamp'] <= cutoff_time]
    result_with_pruned = GlobalFeatureGenerator.calculate_global_features(pruned_dataset, cutoff_time)

    assert result_with_future == result_with_pruned
