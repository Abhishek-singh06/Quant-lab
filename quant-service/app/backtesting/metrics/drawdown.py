"""
Drawdown analytics and duration tracking for backtesting.
"""

from typing import List, Tuple
import numpy as np


class DrawdownCalculator:
    """
    Computes peak-to-trough drawdowns, max drawdown %, drawdown durations, and recovery curves.
    """

    @staticmethod
    def calculate_drawdowns(equity_curve: List[float]) -> Tuple[float, int, List[float]]:
        """
        Returns (max_drawdown_pct, max_drawdown_duration_bars, drawdown_series).
        """
        if not equity_curve or len(equity_curve) < 2:
            return 0.0, 0, [0.0]

        equities = np.array(equity_curve, dtype=float)
        running_max = np.maximum.accumulate(equities)

        # Avoid zero division
        with np.errstate(divide='ignore', invalid='ignore'):
            drawdown_series = np.where(running_max > 0, (running_max - equities) / running_max * 100.0, 0.0)

        max_dd = float(np.max(drawdown_series))

        # Calculate max duration (in bars/days) underwater
        max_duration = 0
        current_duration = 0
        for dd in drawdown_series:
            if dd > 0.0001:
                current_duration += 1
                if current_duration > max_duration:
                    max_duration = current_duration
            else:
                current_duration = 0

        return max_dd, max_duration, drawdown_series.tolist()
