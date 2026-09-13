"""Tests for survivorship-bias protection and point-in-time constituent membership."""

import pytest
import pandas as pd


def test_point_in_time_index_membership():
    """Simulates point-in-time constituent membership tracking.

    Stock A: member from 2010 to 2020 (Delisted/Removed in 2020)
    Stock B: member from 2022 to present (New Entrant)
    Stock C: member from 2005 to present (Persistent)
    """
    membership_table = pd.DataFrame([
        {'symbol': 'STOCK_A', 'effective_from': '2010-01-01', 'effective_to': '2020-06-30'},
        {'symbol': 'STOCK_B', 'effective_from': '2022-01-01', 'effective_to': None},
        {'symbol': 'STOCK_C', 'effective_from': '2005-01-01', 'effective_to': None},
    ])

    def get_constituents_as_of(date_str: str) -> list[str]:
        query = membership_table[
            (membership_table['effective_from'] <= date_str) &
            (membership_table['effective_to'].isna() | (membership_table['effective_to'] >= date_str))
        ]
        return query['symbol'].tolist()

    # Backtest on 2015-01-01: Must include STOCK_A and STOCK_C, but NOT STOCK_B (survivorship-safe)
    constituents_2015 = get_constituents_as_of('2015-01-01')
    assert 'STOCK_A' in constituents_2015
    assert 'STOCK_C' in constituents_2015
    assert 'STOCK_B' not in constituents_2015

    # Backtest on 2023-01-01: Must include STOCK_B and STOCK_C, but NOT STOCK_A
    constituents_2023 = get_constituents_as_of('2023-01-01')
    assert 'STOCK_B' in constituents_2023
    assert 'STOCK_C' in constituents_2023
    assert 'STOCK_A' not in constituents_2023
