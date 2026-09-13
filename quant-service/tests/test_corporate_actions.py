"""Tests for corporate action price adjustments and total return calculations."""

import pytest
import pandas as pd
import numpy as np


def test_stock_split_adjustment():
    """Validates 2:1 stock split adjustment (factor = 0.5 for historical dates before ex-date)."""
    # 2:1 split on 2024-03-01
    prices = pd.DataFrame([
        {'date': '2024-02-28', 'raw_close': 2000.0},
        {'date': '2024-02-29', 'raw_close': 2020.0},
        {'date': '2024-03-01', 'raw_close': 1015.0}, # Post-split ex-date
        {'date': '2024-03-02', 'raw_close': 1025.0},
    ])

    split_ex_date = '2024-03-01'
    split_factor = 0.5

    # Compute split-adjusted price
    prices['adj_close'] = prices.apply(
        lambda r: r['raw_close'] * split_factor if r['date'] < split_ex_date else r['raw_close'],
        axis=1
    )

    assert prices.iloc[0]['adj_close'] == 1000.0
    assert prices.iloc[1]['adj_close'] == 1010.0
    assert prices.iloc[2]['adj_close'] == 1015.0
    assert prices.iloc[3]['adj_close'] == 1025.0

    # Ensure continuous returns without artificial -50% shock on ex-date
    returns = prices['adj_close'].pct_change().dropna().tolist()
    # (1010-1000)/1000 = +1%, (1015-1010)/1010 = +0.495%, (1025-1015)/1015 = +0.985%
    for ret in returns:
        assert ret > -0.05, f"Unexpected return drop in adjusted series: {ret}"
