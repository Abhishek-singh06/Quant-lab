"""Purging and Embargo System for Overlapping Forward Labels.
"""

from typing import Tuple
import pandas as pd


class PurgeAndEmbargo:
    """Eliminates target overlap and auto-correlation leakage across fold boundaries."""

    @staticmethod
    def purge_train_overlap(
        train_df: pd.DataFrame,
        test_start_date: str,
        target_horizon_days: int = 5,
        date_col: str = "date"
    ) -> pd.DataFrame:
        """Purges training samples whose forward target window overlaps with test_start_date.

        For example, with a 5-day target horizon, training samples within 5 days of test_start_date
        have forward returns calculated from future data inside the test period.
        """
        if train_df.empty:
            return train_df

        df = train_df.copy()
        df[date_col] = pd.to_datetime(df[date_col])
        test_start = pd.to_datetime(test_start_date)

        # Cutoff is test_start - target_horizon_days
        cutoff = test_start - pd.Timedelta(days=target_horizon_days)
        purged_df = df[df[date_col] < cutoff].reset_index(drop=True)
        return purged_df

    @staticmethod
    def apply_embargo(
        test_df: pd.DataFrame,
        test_start_date: str,
        embargo_days: int = 2,
        date_col: str = "date"
    ) -> pd.DataFrame:
        """Applies an embargo buffer at the beginning of the test window to eliminate serial correlation."""
        if test_df.empty:
            return test_df

        df = test_df.copy()
        df[date_col] = pd.to_datetime(df[date_col])
        test_start = pd.to_datetime(test_start_date)

        embargo_cutoff = test_start + pd.Timedelta(days=embargo_days)
        embargoed_df = df[df[date_col] >= embargo_cutoff].reset_index(drop=True)
        return embargoed_df
