# QuantLab Part 15: Trading + Investing Models Architecture

## 1. Executive Summary & Objective

Part 15 establishes **Three Independent Quantitative Model Families** designed for distinct Indian market decision regimes:

1. **Short-Term Trading**: Intraday to 1–5 trading days.
2. **Medium-Term Trading**: 1 to 12 weeks (quarterly swing & earnings cycle).
3. **Long-Term Investing**: 6 months to 5+ years (business compounding & fundamental quality).

### Core Architectural Principle

> [!IMPORTANT]
> **Short-Term $\neq$ Medium-Term $\neq$ Long-Term**
> - Features are **not shared** indiscriminately across horizons.
> - Models are **not single unified monolithic algorithms** with parameter switches.
> - Training schedules, preprocessing scalers, target definitions, and evaluation metrics remain strictly isolated.
> - A stock having `SHORT_TERM = BEARISH` and `LONG_TERM = BULLISH` is **not a contradiction**; it reflects a short-term pullback in a high-quality multi-year growth asset. The system never forcibly averages horizons.

---

## 2. Independent Horizon Specifications

| Dimension | Short-Term Trading | Medium-Term Trading | Long-Term Investing |
| :--- | :--- | :--- | :--- |
| **Typical Horizon** | 1 to 5 Trading Days | 1 to 12 Weeks | 6 Months to 5+ Years |
| **Primary Drivers** | Price action, RSI, MACD, 5D/20D volatility, India VIX, breaking news, overnight global signals | 20D/63D momentum, 50D/200D moving averages, FII/DII net flows, quarterly EPS/sales growth, sector alpha | ROE, ROCE, 3Y EPS/FCF CAGR, EBITDA margins, Debt/Equity, P/E, P/B, EV/EBITDA, institutional ownership |
| **Target Labels** | Future 1D/5D return, direction ($R > 0$), future realized volatility | Future 4W/12W return, relative return vs NIFTY, 4W max drawdown | Future 1Y/3Y/5Y return, 3Y CAGR, NIFTY alpha, 1Y max drawdown |
| **Algorithm Family** | Gradient Boosting, Random Forest, Ridge, Logistic Regression | Gradient Boosting, Random Forest, ElasticNet, Ridge | Ridge, ElasticNet, Random Forest, Gradient Boosting |
| **Retraining Cadence** | Daily / Weekly | Weekly / Monthly | Monthly / Quarterly |
| **Risk & Stop Policy** | Tight ATR Stop (1.5× ATR), 0.5% risk budget, high turnover | Intermediate Swing Stop (2.5× ATR), 1.0% risk budget, volatility-adjusted | Wide Thesis Stop (12% fixed or support/resistance), 2.0% risk budget |
| **Feature Set Version** | `SHORT_TERM_FEATURE_SET_V1` | `MEDIUM_TERM_FEATURE_SET_V1` | `LONG_TERM_FEATURE_SET_V1` |
| **Target Set Version** | `SHORT_TERM_TARGET_SET_V1` | `MEDIUM_TERM_TARGET_SET_V1` | `LONG_TERM_TARGET_SET_V1` |

---

## 3. Cross-Horizon Pipeline Architecture

```
                 REAL-WORLD INDIAN MARKET DATA
                               ↓
                   POINT-IN-TIME GATING GATE (T_avail <= T)
                               ↓
         ┌─────────────────────┼─────────────────────┐
         ↓                     ↓                     ↓
    SHORT-TERM            MEDIUM-TERM           LONG-TERM
  FEATURE EXTRACTOR     FEATURE EXTRACTOR     FEATURE EXTRACTOR
         ↓                     ↓                     ↓
    SHORT-TERM            MEDIUM-TERM           LONG-TERM
  TARGET BUILDER        TARGET BUILDER        TARGET BUILDER
         ↓                     ↓                     ↓
    SHORT-TERM            MEDIUM-TERM           LONG-TERM
  DATASET BUILDER       DATASET BUILDER       DATASET BUILDER
         ↓                     ↓                     ↓
    SHORT-TERM            MEDIUM-TERM           LONG-TERM
  CHAMPION MODEL        CHAMPION MODEL        CHAMPION MODEL
         ↓                     ↓                     ↓
  PREDICTION & VOL      PREDICTION & VOL      PREDICTION & VOL
         └─────────────────────┬─────────────────────┘
                               ↓
                  CROSS-HORIZON CONFLICT DETECTOR
                               ↓
                  CROSS-HORIZON VIEW TERMINAL
                               ↓
                    PART 14 RISK ENGINE
```

---

## 4. Point-in-Time & Anti-Leakage Guarantees

1. **Future Mutation Invariance**: Tested and verified in `test_horizon_pit_and_leakage.py`. Injecting future market bars, restated earnings, or news after timestamp $T$ leaves all historical features and dataset rows at $T$ strictly invariant ($\Delta = 0.0$).
2. **Model Availability Timestamping**: Models maintain a `model_availability_timestamp`. A model trained at $T_1$ cannot be retrieved or used to generate predictions for historical dates $T_0 < T_1$.
3. **Restatement Isolation**: Financial metrics use only the point-in-time disclosure available at $T$. Future restatements are invisible to historical predictions made prior to the restatement filing date.
4. **Walk-Forward Validation**: Model selection uses expanding-window walk-forward splits with purging and embargo windows to prevent overlap leakage.
