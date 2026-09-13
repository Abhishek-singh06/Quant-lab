"""Chronological Dataset Splitter and Walk-Forward Validation Engine.

FINANCIAL DATA SPLITTING RULES:
1. Time series data must NEVER be randomly shuffled.
2. Train, validation, and test splits are strictly chronological.
3. Feature scalers and preprocessors must be fitted ONLY on the training split.
"""

from typing import Tuple, List, Generator
import pandas as pd
from sklearn.preprocessing import StandardScaler


class ChronologicalDatasetSplitter:
    """Splits financial time series into leak-free chronological datasets."""

    @staticmethod
    def train_val_test_split(
        df: pd.DataFrame,
        train_ratio: float = 0.70,
        val_ratio: float = 0.15,
        test_ratio: float = 0.15
    ) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
        """Chronologically splits a DataFrame into Train, Validation, and Test partitions."""
        total_len = len(df)
        if total_len == 0:
            return pd.DataFrame(), pd.DataFrame(), pd.DataFrame()

        sorted_df = df.sort_values('date').reset_index(drop=True)

        train_end = int(total_len * train_ratio)
        val_end = train_end + int(total_len * val_ratio)

        train_df = sorted_df.iloc[:train_end].copy()
        val_df = sorted_df.iloc[train_end:val_end].copy()
        test_df = sorted_df.iloc[val_end:].copy()

        return train_df, val_df, test_df

    @staticmethod
    def split_by_dates(
        df: pd.DataFrame,
        train_end_date: str,
        val_end_date: str
    ) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
        """Split DataFrame by explicit point-in-time boundary dates."""
        sorted_df = df.sort_values('date').reset_index(drop=True)

        train_df = sorted_df[sorted_df['date'] <= train_end_date].copy()
        val_df = sorted_df[(sorted_df['date'] > train_end_date) & (sorted_df['date'] <= val_end_date)].copy()
        test_df = sorted_df[sorted_df['date'] > val_end_date].copy()

        return train_df, val_df, test_df

    @staticmethod
    def fit_transform_scaler_safe(
        train_df: pd.DataFrame,
        val_df: pd.DataFrame,
        test_df: pd.DataFrame,
        feature_cols: List[str]
    ) -> Tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame, StandardScaler]:
        """Fits StandardScaler ONLY on train_df, then transforms val_df and test_df

        to prevent distribution leakage from future partitions.
        """
        scaler = StandardScaler()

        # Fit on training data ONLY
        train_scaled = train_df.copy()
        train_scaled[feature_cols] = scaler.fit_transform(train_df[feature_cols])

        # Transform validation and test data
        val_scaled = val_df.copy()
        if not val_df.empty:
            val_scaled[feature_cols] = scaler.transform(val_df[feature_cols])

        test_scaled = test_df.copy()
        if not test_df.empty:
            test_scaled[feature_cols] = scaler.transform(test_df[feature_cols])

        return train_scaled, val_scaled, test_scaled, scaler
