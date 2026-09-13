"""Production Technical Feature Engine for Indian Equity Markets.

Calculates standardized, versioned, point-in-time technical and statistical features
from historical OHLCV price series with STRICT zero look-ahead bias.
"""

from typing import List, Dict, Any, Optional
import numpy as np
import pandas as pd


class TechnicalFeatureEngine:
    """Vectorized Point-in-Time Technical Feature Generator."""

    @staticmethod
    def calculate_features(
        df: pd.DataFrame,
        benchmark_df: Optional[pd.DataFrame] = None,
        as_of_date: Optional[str] = None
    ) -> pd.DataFrame:
        """Calculates point-in-time technical features on a price DataFrame.

        Required columns in df: ['date', 'open', 'high', 'low', 'close', 'volume']
        Optional: ['adj_open', 'adj_high', 'adj_low', 'adj_close']
        """
        if df.empty:
            return pd.DataFrame()

        # Strict Point-in-Time filter
        data = df.copy()
        data['date'] = pd.to_datetime(data['date'])
        if as_of_date is not None:
            cutoff = pd.to_datetime(as_of_date)
            data = data[data['date'] <= cutoff]

        if data.empty:
            return pd.DataFrame()

        data = data.sort_values('date').reset_index(drop=True)

        close = data['adj_close'] if 'adj_close' in data.columns else data['close']
        high = data['adj_high'] if 'adj_high' in data.columns else data['high']
        low = data['adj_low'] if 'adj_low' in data.columns else data['low']
        volume = data['volume']

        # 1. Returns (Simple & Log)
        for p in [1, 5, 10, 20, 21, 63, 126, 252]:
            data[f'return_{p}d'] = close.pct_change(periods=p)
        data['log_return_1d'] = np.log(close / close.shift(1))

        # 2. Simple Moving Averages (SMA)
        for p in [5, 10, 20, 50, 100, 200]:
            data[f'sma_{p}'] = close.rolling(window=p, min_periods=p).mean()

        # 3. Exponential Moving Averages (EMA)
        for p in [5, 10, 20, 50, 100, 200]:
            data[f'ema_{p}'] = close.ewm(span=p, adjust=False).mean()

        # 4. Relative Strength Index (RSI 14 with Wilder smoothing)
        delta = close.diff()
        gain = delta.clip(lower=0)
        loss = -delta.clip(upper=0)
        # Wilder exponential smoothing alpha = 1 / 14
        avg_gain = gain.ewm(alpha=1.0 / 14.0, min_periods=14, adjust=False).mean()
        avg_loss = loss.ewm(alpha=1.0 / 14.0, min_periods=14, adjust=False).mean()
        rs = avg_gain / (avg_loss + 1e-12)
        data['rsi_14'] = 100.0 - (100.0 / (1.0 + rs))

        # 5. MACD (12, 26, 9)
        fast_ema = close.ewm(span=12, adjust=False).mean()
        slow_ema = close.ewm(span=26, adjust=False).mean()
        macd_line = fast_ema - slow_ema
        signal_line = macd_line.ewm(span=9, adjust=False).mean()
        data['macd_line_12_26'] = macd_line
        data['macd_signal_9'] = signal_line
        data['macd_histogram_12_26_9'] = macd_line - signal_line

        # 6. Average True Range (ATR 14)
        prev_close = close.shift(1)
        tr1 = high - low
        tr2 = (high - prev_close).abs()
        tr3 = (low - prev_close).abs()
        tr = pd.concat([tr1, tr2, tr3], axis=1).max(axis=1)
        atr_14 = tr.ewm(alpha=1.0 / 14.0, min_periods=14, adjust=False).mean()
        data['atr_14'] = atr_14
        data['atr_percent_14'] = (atr_14 / close) * 100.0

        # 7. Volatility (Annualized Standard Deviation of 1-Day Returns)
        daily_ret = data['return_1d']
        for p in [10, 20, 21, 63, 252]:
            data[f'volatility_{p}d'] = daily_ret.rolling(window=p, min_periods=p).std() * np.sqrt(252.0) * 100.0

        # 8. Momentum (Price Percentage Change)
        for p in [5, 10, 20, 63, 126, 252]:
            data[f'momentum_{p}'] = (close / close.shift(p) - 1.0) * 100.0

        # 9. Volume Ratios
        for p in [5, 10, 20, 50]:
            vol_sma = volume.rolling(window=p, min_periods=p).mean()
            data[f'volume_ratio_{p}'] = volume / (vol_sma + 1e-9)

        # 10. 52-Week Range (252 Trading Days)
        w52_high = high.rolling(window=252, min_periods=1).max()
        w52_low = low.rolling(window=252, min_periods=1).min()
        data['week_52_high'] = w52_high
        data['week_52_low'] = w52_low
        high_low_diff = w52_high - w52_low
        data['week_52_position'] = np.where(
            high_low_diff > 1e-6,
            (close - w52_low) / high_low_diff,
            0.5
        )
        data['distance_from_52w_high'] = ((close - w52_high) / w52_high) * 100.0
        data['distance_from_52w_low'] = ((close - w52_low) / w52_low) * 100.0

        # 11. Drawdown
        rolling_peak = close.cummax()
        data['drawdown'] = ((close - rolling_peak) / rolling_peak) * 100.0
        for p in [20, 63, 126, 252]:
            win_peak = close.rolling(window=p, min_periods=1).max()
            win_dd = ((close - win_peak) / win_peak) * 100.0
            data[f'max_drawdown_{p}'] = win_dd.rolling(window=p, min_periods=1).min()

        # 12. Relative Strength vs Benchmark
        if benchmark_df is not None and not benchmark_df.empty:
            b_df = benchmark_df.copy()
            b_df['date'] = pd.to_datetime(b_df['date'])
            if as_of_date is not None:
                b_df = b_df[b_df['date'] <= cutoff]
            b_df = b_df.sort_values('date').reset_index(drop=True)
            b_close = b_df['adj_close'] if 'adj_close' in b_df.columns else b_df['close']
            b_ret_20 = b_close.pct_change(periods=20)
            b_ret_63 = b_close.pct_change(periods=63)
            b_aligned = pd.DataFrame({'date': b_df['date'], 'bench_ret_20': b_ret_20, 'bench_ret_63': b_ret_63})

            merged = pd.merge(data, b_aligned, on='date', how='left')
            data['rs_nifty_20'] = (data['return_20d'] - merged['bench_ret_20']) * 100.0
            data['rs_nifty_63'] = (data['return_63d'] - merged['bench_ret_63']) * 100.0

        return data

    @staticmethod
    def get_features_as_of(
        df: pd.DataFrame,
        as_of_date: str,
        benchmark_df: Optional[pd.DataFrame] = None
    ) -> Optional[Dict[str, Any]]:
        """Extracts strictly point-in-time technical feature snapshot as of `as_of_date`."""
        features_df = TechnicalFeatureEngine.calculate_features(df, benchmark_df, as_of_date=as_of_date)
        if features_df.empty:
            return None
        last_row = features_df.iloc[-1]
        res = last_row.to_dict()
        res['date'] = str(pd.to_datetime(res['date']).date())
        return res
