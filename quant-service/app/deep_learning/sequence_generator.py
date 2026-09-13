"""Temporal Sequence Generator for Quantitative Deep Learning.

Constructs 3D sequence tensors (N_samples, Lookback, N_features) from panel DataFrames,
enforcing strict per-symbol boundary isolation, forward horizon alignment,
and zero look-ahead contamination.
"""

from typing import List, Tuple, Optional, Dict, Any, Union
from dataclasses import dataclass
import numpy as np
import pandas as pd


@dataclass
class SequenceDataset:
    """Encapsulates 3D sequence inputs, aligned targets, and sample provenance metadata."""
    X: np.ndarray  # Shape (N, lookback, n_features)
    y: np.ndarray  # Shape (N,)
    timestamps: np.ndarray  # Sequence end timestamp (time t)
    target_timestamps: np.ndarray  # Target realization timestamp (time t + horizon)
    symbols: np.ndarray  # Symbol for each sequence
    feature_names: List[str]
    lookback: int
    horizon: int
    metadata: Optional[pd.DataFrame] = None

    def __len__(self) -> int:
        return len(self.y)

    @property
    def shape(self) -> Tuple[int, int, int]:
        return self.X.shape


class TemporalSequenceGenerator:
    """Generates point-in-time safe sliding sequence windows partitioned strictly by symbol."""

    def __init__(
        self,
        lookback: int = 60,
        horizon: int = 1,
        feature_cols: Optional[List[str]] = None,
        target_col: Optional[str] = "target",
        symbol_col: str = "symbol",
        timestamp_col: str = "timestamp",
        price_col: Optional[str] = "close",
        compute_forward_return: bool = False,
    ):
        """
        Args:
            lookback: Number of historical time steps in each sequence (default: 60 bars)
            horizon: Forward prediction horizon H (default: 1 bar)
            feature_cols: List of feature column names (if None, auto-detected from non-metadata columns)
            target_col: Name of target column (if present in dataframe)
            symbol_col: Column indicating instrument symbol
            timestamp_col: Column indicating timestamp / datetime
            price_col: Price column used if computing forward returns on the fly
            compute_forward_return: If True and target_col is missing, computes (P_{t+H} - P_t) / P_t
        """
        if lookback < 1:
            raise ValueError("Lookback window must be >= 1.")
        if horizon < 1:
            raise ValueError("Forward horizon must be >= 1.")

        self.lookback = lookback
        self.horizon = horizon
        self.feature_cols = feature_cols
        self.target_col = target_col
        self.symbol_col = symbol_col
        self.timestamp_col = timestamp_col
        self.price_col = price_col
        self.compute_forward_return = compute_forward_return

    def generate(self, df: pd.DataFrame) -> SequenceDataset:
        """Constructs safe sequence tensors from the input panel DataFrame."""
        if df.empty:
            raise ValueError("Input DataFrame is empty.")

        data = df.copy()

        # Ensure timestamp column is present or in index
        if self.timestamp_col in data.columns:
            data[self.timestamp_col] = pd.to_datetime(data[self.timestamp_col])
        elif isinstance(data.index, pd.DatetimeIndex):
            data[self.timestamp_col] = data.index
        elif "datetime" in data.columns:
            data[self.timestamp_col] = pd.to_datetime(data["datetime"])
        elif "date" in data.columns:
            data[self.timestamp_col] = pd.to_datetime(data["date"])
        else:
            # Fallback: create integer index timestamp
            data[self.timestamp_col] = pd.date_range(start="2020-01-01", periods=len(data), freq="D")

        # Ensure symbol column exists
        if self.symbol_col not in data.columns:
            data[self.symbol_col] = "DEFAULT_SYMBOL"

        # Determine feature columns
        excluded_cols = {self.symbol_col, self.timestamp_col, "datetime", "date"}
        if self.target_col:
            excluded_cols.add(self.target_col)

        if self.feature_cols is not None:
            features = [c for c in self.feature_cols if c in data.columns]
            if not features:
                raise ValueError(f"None of the specified feature columns exist in DataFrame: {self.feature_cols}")
        else:
            # Auto-detect numeric columns
            numeric_cols = data.select_dtypes(include=[np.number]).columns
            features = [c for c in numeric_cols if c not in excluded_cols]
            if not features:
                raise ValueError("No numeric feature columns found in DataFrame.")

        # Group by symbol to strictly avoid cross-symbol contamination
        all_X: List[np.ndarray] = []
        all_y: List[float] = []
        all_ts: List[pd.Timestamp] = []
        all_target_ts: List[pd.Timestamp] = []
        all_symbols: List[str] = []

        grouped = data.groupby(self.symbol_col, sort=False)

        for sym, group in grouped:
            sym_df = group.sort_values(self.timestamp_col).reset_index(drop=True)
            n_rows = len(sym_df)

            # Minimum required bars: lookback + horizon
            required_bars = self.lookback + self.horizon
            if n_rows < required_bars:
                # Not enough history for this symbol
                continue

            feat_vals = sym_df[features].to_numpy(dtype=np.float64)
            ts_vals = sym_df[self.timestamp_col].to_numpy()

            # Prepare target vector for this symbol
            if self.target_col in sym_df.columns:
                target_vals = sym_df[self.target_col].to_numpy(dtype=np.float64)
            elif self.compute_forward_return and self.price_col and self.price_col in sym_df.columns:
                prices = sym_df[self.price_col].to_numpy(dtype=np.float64)
                # Forward return: (P_{t+H} - P_t) / P_t
                target_vals = np.zeros(n_rows, dtype=np.float64)
                target_vals[:-self.horizon] = (prices[self.horizon:] - prices[:-self.horizon]) / (prices[:-self.horizon] + 1e-12)
            else:
                # Fallback: compute 1-step percentage change of first feature
                base_series = feat_vals[:, 0]
                target_vals = np.zeros(n_rows, dtype=np.float64)
                target_vals[:-self.horizon] = (base_series[self.horizon:] - base_series[:-self.horizon]) / (np.abs(base_series[:-self.horizon]) + 1e-12)

            # Sliding window slice
            # Sequence i spans indices [i, i + lookback - 1]
            # Prediction is made at end index: end_idx = i + lookback - 1
            # Target is evaluated at target_idx = end_idx + horizon
            max_start_idx = n_rows - self.lookback - self.horizon + 1

            for start_idx in range(max_start_idx):
                end_idx = start_idx + self.lookback  # slice [start_idx : end_idx] of length lookback
                pred_bar_idx = end_idx - 1
                target_bar_idx = pred_bar_idx + self.horizon

                seq_x = feat_vals[start_idx:end_idx]
                target_y = target_vals[pred_bar_idx] if self.target_col in sym_df.columns else target_vals[pred_bar_idx]

                all_X.append(seq_x)
                all_y.append(float(target_y))
                all_ts.append(ts_vals[pred_bar_idx])
                all_target_ts.append(ts_vals[target_bar_idx])
                all_symbols.append(str(sym))

        if not all_X:
            raise ValueError(
                f"No valid sequences could be generated. Ensure at least one symbol has >= {self.lookback + self.horizon} bars."
            )

        X_array = np.array(all_X, dtype=np.float64)
        y_array = np.array(all_y, dtype=np.float64)
        ts_array = np.array(all_ts)
        target_ts_array = np.array(all_target_ts)
        sym_array = np.array(all_symbols)

        meta_df = pd.DataFrame({
            "symbol": sym_array,
            "timestamp": ts_array,
            "target_timestamp": target_ts_array,
            "target": y_array,
        })

        return SequenceDataset(
            X=X_array,
            y=y_array,
            timestamps=ts_array,
            target_timestamps=target_ts_array,
            symbols=sym_array,
            feature_names=features,
            lookback=self.lookback,
            horizon=self.horizon,
            metadata=meta_df,
        )
