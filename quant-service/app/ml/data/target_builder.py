"""Target Generation Engine with Strict Point-in-Time Timing Guarantees.
"""

from typing import Dict, Any, Optional
import numpy as np
import pandas as pd


class TargetBuilder:
    """Calculates forward-looking targets for ML training and evaluation."""

    @staticmethod
    def calculate_targets(
        df: pd.DataFrame,
        horizon_days: int = 1,
        return_col: str = "adj_close"
    ) -> pd.DataFrame:
        """Calculates point-in-time forward targets.

        For row at time T:
        - future_return_1d: return from T to T+1
        - future_direction_1d: 1 if future_return_1d > 0 else 0
        - future_volatility_5d: annualized realized volatility over next 5 trading days
        """
        if df.empty:
            return pd.DataFrame()

        data = df.copy().sort_values('date').reset_index(drop=True)
        col = return_col if return_col in data.columns else "close"
        prices = data[col]

        # 1D Forward Return: (Price[t+1] / Price[t]) - 1
        data['future_return_1d'] = prices.pct_change(1).shift(-1)
        data['future_direction_1d'] = (data['future_return_1d'] > 0).astype(float)
        # NaN out last row where shift resulted in NaN
        data.loc[data['future_return_1d'].isna(), 'future_direction_1d'] = np.nan

        # Multi-horizon Forward Returns
        for h in [5, 10, 20, 63]:
            data[f'future_return_{h}d'] = prices.pct_change(h).shift(-h)
            data[f'future_direction_{h}d'] = (data[f'future_return_{h}d'] > 0).astype(float)
            data.loc[data[f'future_return_{h}d'].isna(), f'future_direction_{h}d'] = np.nan

        # Realized Volatility over next 5 and 20 trading days
        daily_ret = prices.pct_change(1)
        # Rolling forward standard deviation of daily returns, annualized (sqrt(252))
        vol_5d = daily_ret.iloc[::-1].rolling(5).std().iloc[::-1].shift(-1) * np.sqrt(252)
        vol_20d = daily_ret.iloc[::-1].rolling(20).std().iloc[::-1].shift(-1) * np.sqrt(252)

        data['future_volatility_5d'] = vol_5d
        data['future_volatility_20d'] = vol_20d

        return data
