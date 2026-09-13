"""Alpha158 and Alpha360 Factor Engines for QuantLab.

Implements standard mathematical formulations of Microsoft Qlib Alpha158 and Alpha360
factor libraries using vectorized pandas/numpy computations with strict PIT guarantees.
"""

from typing import List, Optional, Dict, Any
import numpy as np
import pandas as pd

from app.qlib.expressions import (
    ts_ref,
    ts_mean,
    ts_std,
    ts_var,
    ts_max,
    ts_min,
    ts_sum,
    ts_quantile,
    ts_corr,
    ts_slope,
    ts_rsquare,
    ts_resi,
    ts_rank,
    fn_log,
    fn_abs,
)


class Alpha158Builder:
    """Vectorized calculation of Qlib Alpha158 factors for multi-asset panels."""

    DEFAULT_WINDOWS = [5, 10, 20, 30, 60]

    def __init__(self, windows: Optional[List[int]] = None):
        self.windows = windows or self.DEFAULT_WINDOWS

    def build_features(self, df: pd.DataFrame) -> pd.DataFrame:
        """Compute all Alpha158 features on input panel DataFrame.
        
        Args:
            df: DataFrame containing columns: ['open', 'high', 'low', 'close', 'volume']
                Indexed either by DatetimeIndex or MultiIndex (datetime, instrument)
                
        Returns:
            DataFrame containing computed Alpha158 feature columns.
        """
        # Ensure column names are lowercase
        req_cols = ["open", "high", "low", "close", "volume"]
        for c in req_cols:
            if c not in df.columns:
                raise ValueError(f"Input DataFrame must contain '{c}' column. Found: {list(df.columns)}")

        if isinstance(df.index, pd.MultiIndex):
            # Process per instrument
            inst_level = "instrument" if "instrument" in df.index.names else 1
            res = df.groupby(level=inst_level, group_keys=False).apply(self._compute_single_instrument)
        else:
            res = self._compute_single_instrument(df)
            
        return res

    def _compute_single_instrument(self, df: pd.DataFrame) -> pd.DataFrame:
        """Computes Alpha158 features for a single instrument time series."""
        open_ = df["open"].astype(float)
        high = df["high"].astype(float)
        low = df["low"].astype(float)
        close = df["close"].astype(float)
        volume = df["volume"].astype(float)
        vwap = df["vwap"].astype(float) if "vwap" in df.columns else (open_ + high + low + close) / 4.0
        
        features: Dict[str, pd.Series] = {}
        eps = 1e-12

        # -------------------------------------------------------------------
        # 1. K-Bar Features (9 features)
        # -------------------------------------------------------------------
        hl_diff = high - low + eps
        features["KLEN"] = (high - low) / (open_ + eps)
        features["KMID"] = (close - open_) / (open_ + eps)
        features["KMID2"] = (close - open_) / hl_diff
        features["KUP"] = (high - np.maximum(open_, close)) / (open_ + eps)
        features["KUP2"] = (high - np.maximum(open_, close)) / hl_diff
        features["KLOW"] = (np.minimum(open_, close) - low) / (open_ + eps)
        features["KLOW2"] = (np.minimum(open_, close) - low) / hl_diff
        features["KSFT"] = (2 * close - high - low) / (open_ + eps)
        features["KSFT2"] = (2 * close - high - low) / hl_diff

        # -------------------------------------------------------------------
        # 2. Rolling Price & Trend Features across Windows
        # -------------------------------------------------------------------
        log_vol = fn_log(volume + 1.0)
        close_ret1 = (close / (ts_ref(close, 1) + eps)) - 1.0
        log_vol_ret1 = fn_log((volume / (ts_ref(volume, 1) + eps)) + 1.0)

        for w in self.windows:
            ref_open = ts_ref(open_, w)
            ref_high = ts_ref(high, w)
            ref_low = ts_ref(low, w)
            ref_close = ts_ref(close, w)

            features[f"OPEN{w}"] = ref_open / (close + eps)
            features[f"HIGH{w}"] = ref_high / (close + eps)
            features[f"LOW{w}"] = ref_low / (close + eps)
            features[f"CLOSE{w}"] = ref_close / (close + eps)

            ma_w = ts_mean(close, w)
            std_w = ts_std(close, w)
            features[f"MA{w}"] = ma_w / (close + eps)
            features[f"STD{w}"] = std_w / (close + eps)
            
            # Trend slope, rsquare, resi
            features[f"BETA{w}"] = ts_slope(close, w) / (close + eps)
            features[f"RSQR{w}"] = ts_rsquare(close, ts_ref(close, 1).fillna(close), w)
            features[f"RESI{w}"] = ts_resi(close, ts_ref(close, 1).fillna(close), w) / (close + eps)

            # Max, Min, Quantiles
            max_w = ts_max(high, w)
            min_w = ts_min(low, w)
            features[f"MAX{w}"] = max_w / (close + eps)
            features[f"MIN{w}"] = min_w / (close + eps)
            features[f"QTLU{w}"] = ts_quantile(close, w, 0.8) / (close + eps)
            features[f"QTLD{w}"] = ts_quantile(close, w, 0.2) / (close + eps)
            features[f"RANK{w}"] = ts_rank(close, w)
            features[f"RSV{w}"] = (close - min_w) / (max_w - min_w + eps)

            # Correlations & Counts
            features[f"CORR{w}"] = ts_corr(close, log_vol, w)
            features[f"CORD{w}"] = ts_corr(close_ret1, log_vol_ret1, w)

            diff_close = close - ts_ref(close, 1)
            pos_diff = np.maximum(diff_close, 0.0)
            neg_diff = np.maximum(-diff_close, 0.0)
            abs_diff = fn_abs(diff_close)

            cnt_pos = ts_sum((diff_close > 0).astype(float), w) / float(w)
            cnt_neg = ts_sum((diff_close < 0).astype(float), w) / float(w)
            features[f"CNTP{w}"] = cnt_pos
            features[f"CNTN{w}"] = cnt_neg
            features[f"CNTD{w}"] = cnt_pos - cnt_neg

            sum_pos = ts_sum(pos_diff, w)
            sum_neg = ts_sum(neg_diff, w)
            sum_abs = ts_sum(abs_diff, w) + eps
            features[f"SUMP{w}"] = sum_pos / sum_abs
            features[f"SUMN{w}"] = sum_neg / sum_abs
            features[f"SUMD{w}"] = (sum_pos - sum_neg) / sum_abs

            # ---------------------------------------------------------------
            # 3. Rolling Volume Features
            # ---------------------------------------------------------------
            vma_w = ts_mean(volume, w)
            vstd_w = ts_std(volume, w)
            features[f"VMA{w}"] = vma_w / (volume + eps)
            features[f"VSTD{w}"] = vstd_w / (volume + eps)

            wvma_num = ts_sum(fn_abs(close_ret1) * volume, w)
            features[f"WVMA{w}"] = wvma_num / (vma_w * w + eps)

            diff_vol = volume - ts_ref(volume, 1)
            pos_vdiff = np.maximum(diff_vol, 0.0)
            neg_vdiff = np.maximum(-diff_vol, 0.0)
            abs_vdiff = fn_abs(diff_vol)

            vsum_pos = ts_sum(pos_vdiff, w)
            vsum_neg = ts_sum(neg_vdiff, w)
            vsum_abs = ts_sum(abs_vdiff, w) + eps
            features[f"VSUMP{w}"] = vsum_pos / vsum_abs
            features[f"VSUMN{w}"] = vsum_neg / vsum_abs
            features[f"VSUMD{w}"] = (vsum_pos - vsum_neg) / vsum_abs

        result_df = pd.DataFrame(features, index=df.index)
        return result_df.fillna(0.0)


class Alpha360Builder:
    """Vectorized calculation of Qlib Alpha360 features (60 periods x 6 fields)."""

    LOOKBACK = 60

    def build_features(self, df: pd.DataFrame) -> pd.DataFrame:
        """Compute all 360 features on input panel DataFrame.
        
        Fields normalized:
        - open, high, low, close, vwap: normalized by current close: Ref(field, i) / close
        - volume: normalized by 60-day mean volume: Ref(volume, i) / Mean(volume, 60)
        """
        req_cols = ["open", "high", "low", "close", "volume"]
        for c in req_cols:
            if c not in df.columns:
                raise ValueError(f"Input DataFrame must contain '{c}' column. Found: {list(df.columns)}")

        if isinstance(df.index, pd.MultiIndex):
            inst_level = "instrument" if "instrument" in df.index.names else 1
            return df.groupby(level=inst_level, group_keys=False).apply(self._compute_single_instrument)
        else:
            return self._compute_single_instrument(df)

    def _compute_single_instrument(self, df: pd.DataFrame) -> pd.DataFrame:
        open_ = df["open"].astype(float)
        high = df["high"].astype(float)
        low = df["low"].astype(float)
        close = df["close"].astype(float)
        volume = df["volume"].astype(float)
        vwap = df["vwap"].astype(float) if "vwap" in df.columns else (open_ + high + low + close) / 4.0

        eps = 1e-12
        mean_vol_60 = ts_mean(volume, self.LOOKBACK) + eps
        features: Dict[str, pd.Series] = {}

        for i in range(self.LOOKBACK):
            features[f"CLOSE_{i}"] = ts_ref(close, i) / (close + eps)
            features[f"OPEN_{i}"] = ts_ref(open_, i) / (close + eps)
            features[f"HIGH_{i}"] = ts_ref(high, i) / (close + eps)
            features[f"LOW_{i}"] = ts_ref(low, i) / (close + eps)
            features[f"VWAP_{i}"] = ts_ref(vwap, i) / (close + eps)
            features[f"VOLUME_{i}"] = ts_ref(volume, i) / mean_vol_60

        result_df = pd.DataFrame(features, index=df.index)
        return result_df.fillna(0.0)
