# QuantLab Part 9 — Production Technical Feature Engine

## 1. Overview
The **Technical Feature Engine** provides a production-quality, modular, point-in-time safe feature extraction and time-series analytics pipeline for Indian equities and market indices.

The engine transforms raw and split-adjusted OHLCV price series into versioned mathematical features and statistical indicators for algorithmic signal generation, quantitative ranking, backtesting, and machine learning models.

---

## 2. Core Architectural Principles

```
Historical Data Warehouse (Raw & Split-Adjusted OHLCV)
                        ↓
             Price Series Selection
   (Split-Adjusted vs Total Return vs Raw Unadjusted)
                        ↓
            Point-in-Time Barrier
      (Strictly Bars <= Evaluation Timestamp T)
                        ↓
          Technical Feature Pipeline
                        ↓
        Technical Feature Calculators
 (Returns, SMAs, EMAs, RSI, MACD, ATR, Volatility,
  Momentum, Volume Ratios, 52W Range, Drawdown, RS)
                        ↓
     Technical Feature Validator Service
  (Domain constraint checking: RSI in [0, 100], etc.)
                        ↓
       PostgreSQL Feature Store (V7)
 (feature_definitions, technical_features,
  feature_calculation_runs, feature_data_quality)
                        ↓
       Feature Query API & Frontend View
```

---

## 3. Registered Technical Feature Sets

| Feature Name | Category | Lookback | Description | Formula / Methodology |
|---|---|---|---|---|
| `RETURN_1D` .. `RETURN_252D` | `MOMENTUM` | $N+1$ | Multi-horizon simple price return | $(C_t / C_{t-n}) - 1$ |
| `LOG_RETURN_1D` | `MOMENTUM` | 2 | 1-Day logarithmic return | $\ln(C_t / C_{t-1})$ |
| `SMA_5` .. `SMA_200` | `TREND` | $N$ | Simple Moving Average | $\frac{1}{N} \sum_{i=0}^{N-1} C_{t-i}$ |
| `EMA_5` .. `EMA_200` | `TREND` | $N$ | Exponential Moving Average | $\alpha = \frac{2}{N+1}$, initialized with $N$-bar SMA |
| `RSI_14` | `MOMENTUM` | 15 | Relative Strength Index | Wilder exponential smoothing ($\alpha = 1/14$) |
| `MACD_LINE_12_26` | `TREND` | 26 | MACD Line | $\text{EMA}_{12} - \text{EMA}_{26}$ |
| `MACD_SIGNAL_9` | `TREND` | 35 | MACD Signal Line | 9-day EMA of MACD Line |
| `MACD_HISTOGRAM_12_26_9` | `TREND` | 35 | MACD Histogram | $\text{MACD Line} - \text{Signal Line}$ |
| `ATR_14` | `VOLATILITY` | 15 | Average True Range | Wilder smoothed True Range |
| `ATR_PERCENT_14` | `VOLATILITY` | 15 | Normalized ATR | $(\text{ATR}_{14} / C_t) \times 100\%$ |
| `VOLATILITY_10D` .. `252D` | `VOLATILITY` | $N+1$ | Realized Volatility | $\text{std}(R_{1d}) \times \sqrt{252} \times 100\%$ |
| `MOMENTUM_5` .. `252` | `MOMENTUM` | $N+1$ | Price Momentum | $((C_t / C_{t-n}) - 1) \times 100\%$ |
| `VOLUME_RATIO_5` .. `50` | `VOLUME` | $N$ | Relative Volume Ratio | $V_t / \text{SMA}(V, N)$ |
| `WEEK_52_HIGH` | `STATISTICAL` | 252 | 52-Week High Price | $\max_{0 \le i < 252} H_{t-i}$ |
| `WEEK_52_LOW` | `STATISTICAL` | 252 | 52-Week Low Price | $\min_{0 \le i < 252} L_{t-i}$ |
| `WEEK_52_POSITION` | `STATISTICAL` | 252 | Normalized 52W Position | $\frac{C_t - \text{Low}_{52}}{\text{High}_{52} - \text{Low}_{52}} \in [0, 1]$ |
| `DRAWDOWN` | `STATISTICAL` | 252 | Current Drawdown | $(C_t / \text{Peak}_{252}) - 1 \le 0$ |
| `MAX_DRAWDOWN_20` .. `252` | `STATISTICAL` | $N$ | Rolling Max Drawdown | $\min_{i \in [t-N, t]} \text{DD}_i \le 0$ |
| `RS_NIFTY_20`, `RS_NIFTY_63` | `RELATIVE_STRENGTH`| $N+1$ | Relative Strength vs NIFTY 50 | $(R_{\text{stock}, N} - R_{\text{NIFTY}, N}) \times 100\%$ |

---

## 4. Database Schema (`V7__technical_feature_engine.sql`)

1. **`market_data.feature_definitions`**: Central registry storing category, lookback, versions, default price series type (`SPLIT_ADJUSTED`), and enablement status.
2. **`market_data.technical_features`**: Normalized time series table partitioned/indexed by `(instrument_id, feature_name, feature_timestamp DESC)` and `(information_available_at)`.
3. **`market_data.feature_calculation_runs`**: Audits computation batch runs, row counts, durations, and statuses.
4. **`market_data.feature_data_quality`**: Logs out-of-bounds metrics and calculation validation failures.

---

## 5. REST Endpoints

- `GET /api/v1/features/technical/{symbol}/latest?timeframe=1D` — Returns the most recent feature vector for `symbol`.
- `GET /api/v1/features/technical/{symbol}/point-in-time?timeframe=1D&asOf=...` — Returns features strictly available as of `asOf`.
- `GET /api/v1/features/technical/series?instrumentId=...&featureName=...&from=...&to=...` — Returns historical feature time series.
- `GET /api/v1/features/definitions` — Returns all registered technical feature definitions.
- `GET /api/v1/features/runs` — Returns recent calculation run logs.
- `POST /api/v1/features/calculate/{symbol}` — Triggers on-demand feature recalculation.
- `POST /api/v1/features/calculate-batch` — Triggers batch feature calculation for a universe.

---

## 6. Point-in-Time & Look-Ahead Bias Verification
The Python quant engine enforces 7 strict temporal invariants verified by unit tests:
1. Future price modifications have 0.0 effect on historical values at $T$.
2. Appending new future bars preserves past feature vectors at $T$.
3. 52-week High/Low and Position do not leak future all-time highs.
4. Rolling volatility utilizes strictly backward-looking return windows.
5. Relative strength synchronizes strictly contemporaneously with benchmark prices without future leakage.
6. Moving averages (SMA/EMA) and MACD depend strictly on bars $\le T$.
7. Insufficient lookback records `INSUFFICIENT_HISTORY` or `NaN` without fabricating values.
