"""Target and Label Generator for Quantitative Machine Learning Models.

SEPARATION OF FEATURES (X) AND TARGET (y):
- Feature vectors are calculated at timestamp T: X(T)
- Target label represents future price return over horizon h: y(T) = (Close(T+h) - Close(T)) / Close(T)
- y(T) is strictly the evaluation/training label and must NEVER be present in feature matrix X.
"""

from typing import List
import pandas as pd


class TargetGenerator:
    """Generates forward-looking return targets for model training."""

    @staticmethod
    def add_forward_returns(df: pd.DataFrame, horizons: List[int] = [1, 5, 10, 20]) -> pd.DataFrame:
        """Adds forward return targets (shifted backward so row T has future return T -> T+h).

        Required column: 'close'
        """
        data = df.sort_values('date').copy().reset_index(drop=True)
        close = data['close']

        for h in horizons:
            # Future price at T+h
            future_close = close.shift(-h)
            data[f'target_forward_return_{h}d'] = (future_close - close) / close
            # Binary classification target (1 if positive return, 0 otherwise)
            data[f'target_direction_{h}d'] = (data[f'target_forward_return_{h}d'] > 0).astype(int)

        return data
