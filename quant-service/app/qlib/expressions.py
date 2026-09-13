"""Expression Engine for Qlib-style Quantitative Feature and Target Engineering.

Implements pure-Python/NumPy/Pandas vectorized operations for financial time-series
and cross-sectional factor computations with strict Point-in-Time (PIT) integrity.
"""

from typing import Dict, Any, Union, Optional, List, Callable
import re
import numpy as np
import pandas as pd


class LookaheadBiasError(ValueError):
    """Raised when an expression attempts to access future data during feature computation."""
    pass


# ---------------------------------------------------------------------------
# Core Vectorized Time-Series Operators (Per Instrument)
# ---------------------------------------------------------------------------

def ts_ref(series: pd.Series, d: int, allow_future: bool = False) -> pd.Series:
    """Lag or lead of a series by d periods.
    
    Positive d: Past data (lag). Safe for features.
    Negative d: Future data (lead). ONLY permitted for target/label generation.
    """
    if d < 0 and not allow_future:
        raise LookaheadBiasError(f"Lookahead bias detected: Ref with negative shift ({d}) is forbidden in feature generation.")
    return series.shift(d)


def ts_delta(series: pd.Series, d: int = 1, allow_future: bool = False) -> pd.Series:
    """Change in series over d periods: series - Ref(series, d)."""
    return series - ts_ref(series, d, allow_future=allow_future)


def ts_mean(series: pd.Series, d: int) -> pd.Series:
    """Rolling mean over past d periods."""
    return series.rolling(window=d, min_periods=1).mean()


def ts_std(series: pd.Series, d: int) -> pd.Series:
    """Rolling standard deviation over past d periods."""
    res = series.rolling(window=d, min_periods=2).std(ddof=0)
    return res.fillna(0.0)


def ts_var(series: pd.Series, d: int) -> pd.Series:
    """Rolling variance over past d periods."""
    res = series.rolling(window=d, min_periods=2).var(ddof=0)
    return res.fillna(0.0)


def ts_skew(series: pd.Series, d: int) -> pd.Series:
    """Rolling skewness over past d periods."""
    res = series.rolling(window=d, min_periods=3).skew()
    return res.fillna(0.0)


def ts_kurt(series: pd.Series, d: int) -> pd.Series:
    """Rolling kurtosis over past d periods."""
    res = series.rolling(window=d, min_periods=4).kurt()
    return res.fillna(0.0)


def ts_max(series: pd.Series, d: int) -> pd.Series:
    """Rolling maximum over past d periods."""
    return series.rolling(window=d, min_periods=1).max()


def ts_min(series: pd.Series, d: int) -> pd.Series:
    """Rolling minimum over past d periods."""
    return series.rolling(window=d, min_periods=1).min()


def ts_sum(series: pd.Series, d: int) -> pd.Series:
    """Rolling sum over past d periods."""
    return series.rolling(window=d, min_periods=1).sum()


def ts_quantile(series: pd.Series, d: int, q: float) -> pd.Series:
    """Rolling quantile at level q over past d periods."""
    return series.rolling(window=d, min_periods=1).quantile(q)


def ts_wma(series: pd.Series, d: int) -> pd.Series:
    """Linearly Weighted Moving Average over past d periods."""
    weights = np.arange(1, d + 1, dtype=float)
    sum_weights = weights.sum()
    
    def _wma(x):
        if len(x) < d:
            w = weights[-len(x):]
            return np.dot(x, w) / w.sum()
        return np.dot(x, weights) / sum_weights
        
    return series.rolling(window=d, min_periods=1).apply(_wma, raw=True)


def ts_ema(series: pd.Series, span: int) -> pd.Series:
    """Exponential Moving Average."""
    return series.ewm(span=span, adjust=False).mean()


def ts_corr(s1: pd.Series, s2: pd.Series, d: int) -> pd.Series:
    """Rolling Pearson correlation between two series over past d periods."""
    res = s1.rolling(window=d, min_periods=2).corr(s2)
    return res.fillna(0.0)


def ts_cov(s1: pd.Series, s2: pd.Series, d: int) -> pd.Series:
    """Rolling covariance between two series over past d periods."""
    res = s1.rolling(window=d, min_periods=2).cov(s2)
    return res.fillna(0.0)


def ts_slope(series: pd.Series, d: int) -> pd.Series:
    """Rolling linear regression slope against time index over past d periods."""
    x = np.arange(d, dtype=float)
    x_mean = x.mean()
    x_var = np.sum((x - x_mean) ** 2)

    def _slope(y):
        n = len(y)
        if n < 2:
            return 0.0
        if n < d:
            curr_x = np.arange(n, dtype=float)
            curr_x_mean = curr_x.mean()
            denom = np.sum((curr_x - curr_x_mean) ** 2)
            if denom == 0:
                return 0.0
            return np.sum((curr_x - curr_x_mean) * (y - y.mean())) / denom
        return np.sum((x - x_mean) * (y - y.mean())) / (x_var + 1e-12)

    return series.rolling(window=d, min_periods=2).apply(_slope, raw=True).fillna(0.0)


def ts_rsquare(s1: pd.Series, s2: pd.Series, d: int) -> pd.Series:
    """Rolling R-squared between dependent s1 and independent s2 over past d periods."""
    corr = ts_corr(s1, s2, d)
    return (corr ** 2).fillna(0.0)


def ts_resi(s1: pd.Series, s2: pd.Series, d: int) -> pd.Series:
    """Rolling residual of s1 regressed on s2 over past d periods: s1 - (alpha + beta * s2)."""
    cov = ts_cov(s1, s2, d)
    var_s2 = ts_var(s2, d)
    beta = cov / (var_s2 + 1e-12)
    alpha = ts_mean(s1, d) - beta * ts_mean(s2, d)
    fitted = alpha + beta * s2
    return s1 - fitted


def ts_rank(series: pd.Series, d: int) -> pd.Series:
    """Rolling percentile rank of current value within past d periods (0 to 1)."""
    def _rank(x):
        if len(x) == 0:
            return 0.5
        val = x[-1]
        return np.sum(x <= val) / len(x)
        
    return series.rolling(window=d, min_periods=1).apply(_rank, raw=True)


# ---------------------------------------------------------------------------
# Cross-Sectional Operators
# ---------------------------------------------------------------------------

def cs_rank(df_or_series: pd.DataFrame, level: str = "datetime") -> pd.DataFrame:
    """Cross-sectional rank across instruments at each datetime normalized to [0, 1]."""
    if isinstance(df_or_series, pd.DataFrame):
        return df_or_series.groupby(level=level).rank(pct=True)
    return df_or_series.groupby(level=level).rank(pct=True)


def cs_zscore(df_or_series: pd.DataFrame, level: str = "datetime") -> pd.DataFrame:
    """Cross-sectional Z-Score standardization across instruments at each datetime."""
    if isinstance(df_or_series, pd.DataFrame):
        grouped = df_or_series.groupby(level=level)
        mean = grouped.transform("mean")
        std = grouped.transform("std").replace(0, 1.0)
        return (df_or_series - mean) / (std + 1e-12)
    grouped = df_or_series.groupby(level=level)
    mean = grouped.transform("mean")
    std = grouped.transform("std").replace(0, 1.0)
    return (df_or_series - mean) / (std + 1e-12)


# ---------------------------------------------------------------------------
# Mathematical Helper Functions
# ---------------------------------------------------------------------------

def fn_log(series: pd.Series) -> pd.Series:
    """Natural logarithm with safety clamping for non-positive values."""
    return np.log(np.maximum(series, 1e-12))


def fn_sign(series: pd.Series) -> pd.Series:
    """Sign of values (-1, 0, 1)."""
    return np.sign(series)


def fn_abs(series: pd.Series) -> pd.Series:
    """Absolute value."""
    return np.abs(series)


def fn_sqrt(series: pd.Series) -> pd.Series:
    """Square root with non-negative clamping."""
    return np.sqrt(np.maximum(series, 0.0))


def fn_if_then_else(condition: pd.Series, then_val: Union[pd.Series, float], else_val: Union[pd.Series, float]) -> pd.Series:
    """Vectorized conditional branch."""
    return pd.Series(np.where(condition, then_val, else_val), index=condition.index)


# ---------------------------------------------------------------------------
# Safe Expression Parser & Evaluator
# ---------------------------------------------------------------------------

class QlibExpressionEvaluator:
    """Evaluates Qlib alpha formula strings on multi-asset panel DataFrames."""

    def __init__(self, allow_future: bool = False):
        self.allow_future = allow_future

    def evaluate(self, expr: str, data: pd.DataFrame) -> pd.Series:
        """Evaluates an expression string against data.
        
        Args:
            expr: Formula string, e.g. "Ref($close, 1) / $close - 1", "Mean($close, 5) / $close"
            data: DataFrame with MultiIndex (datetime, instrument) or columns for fields ($open, $close, etc.)
            
        Returns:
            Computed pd.Series with same index.
        """
        expr = expr.strip()

        # Remove outer parentheses if wrapping whole expression
        if expr.startswith("(") and expr.endswith(")"):
            # Check if this pair of parentheses matches the whole expression
            depth = 0
            is_wrapped = True
            for i, char in enumerate(expr):
                if char == '(':
                    depth += 1
                elif char == ')':
                    depth -= 1
                    if depth == 0 and i < len(expr) - 1:
                        is_wrapped = False
                        break
            if is_wrapped:
                return self.evaluate(expr[1:-1], data)

        # Parse binary operators with proper precedence:
        # First check + and - (lower precedence, evaluated last)
        for op in ["+", "-"]:
            split_idx = self._find_top_level_operator(expr, op)
            if split_idx != -1:
                left_expr = expr[:split_idx].strip()
                right_expr = expr[split_idx + 1:].strip()
                left_res = self.evaluate(left_expr, data)
                right_res = self.evaluate(right_expr, data)
                if op == "+":
                    return left_res + right_res
                elif op == "-":
                    return left_res - right_res

        # Then check * and / (higher precedence)
        for op in ["*", "/"]:
            split_idx = self._find_top_level_operator(expr, op)
            if split_idx != -1:
                left_expr = expr[:split_idx].strip()
                right_expr = expr[split_idx + 1:].strip()
                left_res = self.evaluate(left_expr, data)
                right_res = self.evaluate(right_expr, data)
                if op == "*":
                    return left_res * right_res
                elif op == "/":
                    return left_res / (right_res.replace(0, np.nan) + 1e-12)

        # Check for single column reference like "$close"
        if re.match(r"^\$[A-Za-z0-9_]+$", expr):
            col_name = expr[1:].lower()
            lower_cols = {c.lower(): c for c in data.columns}
            if col_name in lower_cols:
                return data[lower_cols[col_name]]
            raise KeyError(f"Field '{col_name}' not found in data columns: {list(data.columns)}")

        # Check for numeric literal
        try:
            val = float(expr)
            return pd.Series(val, index=data.index)
        except ValueError:
            pass

        # Parse functions
        fn_match = re.match(r"^([A-Za-z0-9_]+)\((.*)\)$", expr, re.DOTALL)
        if fn_match:
            fn_name = fn_match.group(1)
            args_str = fn_match.group(2)
            args = self._split_args(args_str)
            return self._dispatch_func(fn_name, args, data)

        raise ValueError(f"Unable to parse expression: '{expr}'")

    def _split_args(self, args_str: str) -> List[str]:
        """Split argument string by top-level commas."""
        args = []
        depth = 0
        current = []
        for char in args_str:
            if char == '(':
                depth += 1
                current.append(char)
            elif char == ')':
                depth -= 1
                current.append(char)
            elif char == ',' and depth == 0:
                args.append("".join(current).strip())
                current = []
            else:
                current.append(char)
        if current:
            args.append("".join(current).strip())
        return args

    def _find_top_level_operator(self, expr: str, op: str) -> int:
        """Find the rightmost top-level occurrence of an operator for left-to-right evaluation."""
        depth = 0
        # For + and -, we search from right to left to maintain left-associativity
        for i in range(len(expr) - 1, -1, -1):
            char = expr[i]
            if char == ')':
                depth += 1
            elif char == '(':
                depth -= 1
            elif char == op and depth == 0:
                # Avoid matching unary minus at the very start
                if op == '-' and (i == 0 or expr[i-1] in '(+-*/'):
                    continue
                return i
        return -1

    def _apply_per_instrument(self, data: pd.DataFrame, func: Callable, *args) -> pd.Series:
        """Applies a time-series function per instrument group when MultiIndex is present."""
        if isinstance(data.index, pd.MultiIndex):
            if "instrument" in data.index.names:
                inst_level = "instrument"
            else:
                inst_level = 1
            return data.groupby(level=inst_level, group_keys=False).apply(
                lambda g: func(g, *args)
            )
        return func(data, *args)

    def _dispatch_func(self, fn_name: str, args: List[str], data: pd.DataFrame) -> pd.Series:
        """Dispatch function call to vectorized implementation."""
        fn_upper = fn_name.upper()

        if fn_upper == "REF":
            target = self.evaluate(args[0], data)
            shift_d = int(args[1])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(
                    lambda s: ts_ref(s, shift_d, allow_future=self.allow_future)
                )
            return ts_ref(target, shift_d, allow_future=self.allow_future)

        elif fn_upper == "DELTA":
            target = self.evaluate(args[0], data)
            shift_d = int(args[1]) if len(args) > 1 else 1
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(
                    lambda s: ts_delta(s, shift_d, allow_future=self.allow_future)
                )
            return ts_delta(target, shift_d, allow_future=self.allow_future)

        elif fn_upper == "MEAN":
            target = self.evaluate(args[0], data)
            d = int(args[1])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(lambda s: ts_mean(s, d))
            return ts_mean(target, d)

        elif fn_upper == "STD":
            target = self.evaluate(args[0], data)
            d = int(args[1])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(lambda s: ts_std(s, d))
            return ts_std(target, d)

        elif fn_upper == "VAR":
            target = self.evaluate(args[0], data)
            d = int(args[1])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(lambda s: ts_var(s, d))
            return ts_var(target, d)

        elif fn_upper == "MAX":
            target = self.evaluate(args[0], data)
            d = int(args[1])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(lambda s: ts_max(s, d))
            return ts_max(target, d)

        elif fn_upper == "MIN":
            target = self.evaluate(args[0], data)
            d = int(args[1])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(lambda s: ts_min(s, d))
            return ts_min(target, d)

        elif fn_upper == "SUM":
            target = self.evaluate(args[0], data)
            d = int(args[1])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(lambda s: ts_sum(s, d))
            return ts_sum(target, d)

        elif fn_upper == "SLOPE":
            target = self.evaluate(args[0], data)
            d = int(args[1])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                return target.groupby(level=inst_level, group_keys=False).apply(lambda s: ts_slope(s, d))
            return ts_slope(target, d)

        elif fn_upper == "CORR":
            s1 = self.evaluate(args[0], data)
            s2 = self.evaluate(args[1], data)
            d = int(args[2])
            if isinstance(data.index, pd.MultiIndex):
                inst_level = "instrument" if "instrument" in data.index.names else 1
                combined = pd.DataFrame({"s1": s1, "s2": s2})
                return combined.groupby(level=inst_level, group_keys=False).apply(
                    lambda g: ts_corr(g["s1"], g["s2"], d)
                )
            return ts_corr(s1, s2, d)

        elif fn_upper == "RANK":
            target = self.evaluate(args[0], data)
            if len(args) == 1:
                # Cross-sectional rank
                dt_level = "datetime" if isinstance(data.index, pd.MultiIndex) and "datetime" in data.index.names else 0
                return cs_rank(target, level=dt_level)
            else:
                # Time-series rank
                d = int(args[1])
                if isinstance(data.index, pd.MultiIndex):
                    inst_level = "instrument" if "instrument" in data.index.names else 1
                    return target.groupby(level=inst_level, group_keys=False).apply(lambda s: ts_rank(s, d))
                return ts_rank(target, d)

        elif fn_upper == "CSRANK":
            target = self.evaluate(args[0], data)
            dt_level = "datetime" if isinstance(data.index, pd.MultiIndex) and "datetime" in data.index.names else 0
            return cs_rank(target, level=dt_level)

        elif fn_upper == "CSZSCORE":
            target = self.evaluate(args[0], data)
            dt_level = "datetime" if isinstance(data.index, pd.MultiIndex) and "datetime" in data.index.names else 0
            return cs_zscore(target, level=dt_level)

        elif fn_upper == "LOG":
            target = self.evaluate(args[0], data)
            return fn_log(target)

        elif fn_upper == "ABS":
            target = self.evaluate(args[0], data)
            return fn_abs(target)

        elif fn_upper == "SIGN":
            target = self.evaluate(args[0], data)
            return fn_sign(target)

        elif fn_upper == "SQRT":
            target = self.evaluate(args[0], data)
            return fn_sqrt(target)

        elif fn_upper == "IF":
            cond = self.evaluate(args[0], data)
            then_v = self.evaluate(args[1], data)
            else_v = self.evaluate(args[2], data)
            return fn_if_then_else(cond, then_v, else_v)

        raise ValueError(f"Unsupported function: '{fn_name}'")
