"""Point-in-Time Feature Store Generator.

CRITICAL RULE:
Any rolling feature at historical timestamp T must strictly use observations
available at or before timestamp T (i.e. [T-N+1 ... T]).
Zero look-ahead or forward-looking information is permitted.
"""

from typing import List, Optional
import numpy as np
import pandas as pd


class PointInTimeFeatureStore:
    """Calculates point-in-time quantitative technical and statistical features

    from historical price time series without future look-ahead bias.
    """

    @staticmethod
    def calculate_features(df: pd.DataFrame) -> pd.DataFrame:
        """Calculate point-in-time features on a historical price DataFrame.

        Required columns in df: ['date', 'open', 'high', 'low', 'close', 'volume']
        The DataFrame is sorted chronologically by date.
        """
        if df.empty or len(df) == 0:
            return pd.DataFrame()

        # Ensure sorted chronologically
        data = df.sort_values('date').copy().reset_index(drop=True)

        close = data['close']
        high = data['high']
        low = data['low']
        volume = data['volume']

        # 1. Moving Averages (Strict historical rolling windows: T-N+1 to T)
        data['sma_5'] = close.rolling(window=5, min_periods=5).mean()
        data['sma_20'] = close.rolling(window=20, min_periods=20).mean()
        data['sma_50'] = close.rolling(window=50, min_periods=50).mean()
        data['ema_20'] = close.ewm(span=20, adjust=False).mean()

        # 2. Historical Returns (backward-looking only)
        data['return_1d'] = close.pct_change(periods=1)
        data['return_5d'] = close.pct_change(periods=5)
        data['return_20d'] = close.pct_change(periods=20)

        # 3. Historical Volatility (20-day rolling standard deviation of daily returns)
        data['volatility_20'] = data['return_1d'].rolling(window=20, min_periods=20).std() * np.sqrt(252)

        # 4. Momentum (10-day price ratio)
        data['momentum_10'] = close / close.shift(10) - 1.0

        # 5. Relative Strength Index (RSI 14-period)
        delta = close.diff()
        gain = delta.clip(lower=0)
        loss = -delta.clip(upper=0)
        avg_gain = gain.rolling(window=14, min_periods=14).mean()
        avg_loss = loss.rolling(window=14, min_periods=14).mean()
        rs = avg_gain / (avg_loss + 1e-9)
        data['rsi_14'] = 100 - (100 / (1 + rs))

        # 6. Volume SMA
        data['volume_sma_20'] = volume.rolling(window=20, min_periods=20).mean()
        data['volume_ratio_20'] = volume / (data['volume_sma_20'] + 1e-9)

        return data

    @staticmethod
    def get_features_as_of(df: pd.DataFrame, as_of_date: str) -> Optional[dict]:
        """Extract exact feature vector available at point-in-time `as_of_date`.

        Guarantees no records after `as_of_date` are included in computation.
        """
        historical_subset = df[df['date'] <= as_of_date].copy()
        if historical_subset.empty:
            return None

        features_df = PointInTimeFeatureStore.calculate_features(historical_subset)
        last_row = features_df.iloc[-1]
        return last_row.to_dict()
