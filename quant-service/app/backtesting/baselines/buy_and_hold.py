"""
Buy & Hold Baseline Generator for Backtesting Comparison.
Allocates equal weight across the universe at start date and holds without rebalancing.
"""

from datetime import date
from typing import Dict, List
import pandas as pd


class BuyAndHoldBaseline:
    """
    Computes an exact equal-weight Buy & Hold equity curve for fair strategy comparison.
    """

    @staticmethod
    def generate_equity_curve(
        initial_capital: float,
        dates: List[date],
        universe_prices: Dict[str, pd.Series]  # symbol -> Series of close prices indexed by date
    ) -> List[float]:
        if not dates or not universe_prices:
            return [initial_capital] * len(dates)

        symbols = list(universe_prices.keys())
        capital_per_symbol = initial_capital / len(symbols)

        # Determine starting shares per symbol
        initial_shares = {}
        for s in symbols:
            series = universe_prices[s]
            first_valid_price = series.iloc[0] if len(series) > 0 else 1.0
            initial_shares[s] = capital_per_symbol / first_valid_price if first_valid_price > 0 else 0.0

        equity_curve = []
        for d in dates:
            total_val = 0.0
            for s in symbols:
                series = universe_prices[s]
                p = series.get(d, series.asof(d) if hasattr(series, 'asof') else (series.iloc[-1] if len(series) > 0 else 1.0))
                if pd.isna(p):
                    p = series.iloc[0] if len(series) > 0 else 1.0
                total_val += initial_shares[s] * float(p)
            equity_curve.append(total_val)

        return equity_curve
