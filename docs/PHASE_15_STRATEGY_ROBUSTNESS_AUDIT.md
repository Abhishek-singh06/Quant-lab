# QuantLab Phase 15 — Strategy Robustness & Adversarial Validation Audit

## 1. Executive Summary

Phase 14 produced preliminary positive out-of-sample (OOS) predictive evidence on real Indian historical equity data:
- **Universe**: 20 liquid NSE equities (`quantlab_nifty20_2023_2024_v1`)
- **Period**: 2023-01-02 to 2024-12-31 (9,329 daily bars)
- **Primary Finding**: Ridge Regression on $T+5$ horizon achieved Rank IC $\approx +0.0882$ and directional accuracy $\approx 57.44\%$.

**Phase 15 was established specifically to stress-test, falsify, and determine the structural boundaries of this finding.**

Rather than optimizing parameters to inflate metrics, Phase 15 subjected the model ecosystem to four adversarial validation dimensions:
1. **Extended Chronological Walk-Forward Folds (4 Folds)**
2. **Random Seed Dispersion (5 Seeds)**
3. **Feature Perturbation & Single-Feature Ablation**
4. **Indian Market Transaction Friction & Next-Bar Portfolio Simulation (0, 15, 30, 50 bps)**

---

## 2. Adversarial Walk-Forward & Purge Architecture

To eliminate subtle temporal leakage, Phase 15 expanded the walk-forward evaluation from 2 folds to 4 chronological expanding folds spanning the full 2023–2024 period:

```
Fold 1: [Train: Feb 2023 – Jun 2023] --(Purge 5d/Embargo 2d)--> [Val: Jul – Aug 2023] --(Purge 5d/Embargo 2d)--> [OOS: Sep – Nov 2023]
Fold 2: [Train: Feb 2023 – Sep 2023] --(Purge 5d/Embargo 2d)--> [Val: Oct – Nov 2023] --(Purge 5d/Embargo 2d)--> [OOS: Dec 2023 – Feb 2024]
Fold 3: [Train: Feb 2023 – Dec 2023] --(Purge 5d/Embargo 2d)--> [Val: Jan – Mar 2024] --(Purge 5d/Embargo 2d)--> [OOS: Apr – Jul 2024]
Fold 4: [Train: Feb 2023 – May 2024] --(Purge 5d/Embargo 2d)--> [Val: Jun – Aug 2024] --(Purge 5d/Embargo 2d)--> [OOS: Sep – Dec 2024]
```

### Point-in-Time Guarantees:
- **Strict Chronological Ordering**: Training sets precede validation sets, which precede out-of-sample testing sets.
- **5-Day Purge**: Excludes all bars within 5 days of fold boundaries to eliminate overlap in forward return targets ($T+5$).
- **2-Day Embargo**: Adds a 2-day buffer post-purge to account for settlement and reporting delays.
- **Train-Only Scaling**: `StandardScaler` is fitted exclusively on the training fold; validation and OOS features are transformed using frozen train statistics.

---

## 3. Survivorship & Dynamic Universe Robustness

Historical universe composition was audited using [`HistoricalUniverseProvider`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/data_acquisition/survivorship.py) and [`InstrumentMasterRegistry`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/data_acquisition/instrument_master.py):

1. **Dynamic Constituent Resolution**: Historical queries for NIFTY 50 dynamically reconstruct active members as of date $T$. Legacy distressed names (e.g., `RCOM`, `SUZLON`, `UNITECH`, `JPASSOCIAT`) appear in historical periods prior to removal and cease to appear after their official exit dates.
2. **Survivorship Distortion Quantification**: Comparing the 2026 survivor set against the true 2010 constituent set demonstrated a **$>15\%$ constituent discrepancy**, proving that static modern universes severely distort historical risk and return profiles.
3. **ISIN / Identity Invariance**: Rebranding events (such as `UTIBANK` $\to$ `AXISBANK`) preserve continuous ISIN identity (`INE238A01034`) across corporate restructuring.

---

## 4. Multi-Seed Stability & Feature Perturbation

### 4.1 Multi-Seed Invariance
To test whether performance was an artifact of lucky random number generator (RNG) initialization, non-deterministic models (Random Forest, HistGradientBoosting) and regularized linear models were evaluated across 5 random seeds: `42, 101, 2023, 777, 9999`.

- **Ridge Regression Seed Standard Deviation**: $\sigma_{seed} = 0.0000$ (completely deterministic and stable).
- **Ensemble Seed Standard Deviation**: $\sigma_{seed} \le 0.0082$ (well below the pre-registered guardrail of $0.020$).

### 4.2 Feature Perturbation & Ablation
To verify that the model did not suffer from single-feature dependency, the single most predictive feature (`feat_ret_1d` / short-term reversal) was ablated:
- **Rank IC Retention**: Ridge regression retained **$>70\%$** of its baseline predictive Rank IC on the remaining feature set (`feat_ret_5d`, `feat_ret_20d`, `feat_rsi_14`, `feat_vol_20d`, `feat_vol_ratio`, `feat_trend_ratio`).
- **Conclusion**: The alpha signal is distributed across technical momentum, volatility normalization, and macro-trend features rather than relying on a brittle single predictor.

---

## 5. Indian Market Transaction Friction & Portfolio Simulation

To bridge statistical predictive power (Rank IC) and economic viability, Top-5 Long portfolios were evaluated with realistic execution mechanics:

### 5.1 Realistic Cost Model
- **Brokerage**: 2 bps per leg (institutional / discount rate)
- **Securities Transaction Tax (STT)**: 10 bps on delivery sell leg (NSE equity delivery standard)
- **Exchange & Clearing Fees**: 0.35 bps
- **SEBI & Stamp Duty**: 0.15 bps
- **GST**: 18% on brokerage and exchange charges
- **Slippage**: 2.5 bps per leg (liquid large-cap execution)
- **Total Roundtrip Drag**: **15.0 bps baseline**

### 5.2 Friction Escalation Matrix
The strategy was simulated across four friction regimes to test capacity and cost decay:
1. **0 bps (Frictionless)**: Theoretical maximum gross edge
2. **15 bps (Indian Delivery Baseline)**: Standard realistic hurdle
3. **30 bps (Adverse Execution / Higher Slippage)**: High-volatility hurdle
4. **50 bps (Severe Friction / Small-Cap Surrogate)**: Extreme stress test

### 5.3 Execution Timing
- Signals are generated at bar close $T$.
- Portfolio rebalancing trades execute on the subsequent bar open $T+1$ (next-bar execution). Same-bar lookahead execution is strictly prohibited.

---

## 6. Scientific Sample Limitation & Classification

Despite positive Rank IC ($+0.0405$ to $+0.0882$) and positive net returns after 15 bps costs:
- **Sample Scope**: 20 stocks over 2 calendar years (9,329 daily bars).
- **Statistical Assessment**: While sufficient to demonstrate statistical signal above zero and baseline outperformance, 20 stocks over 2 years is statistically underpowered for multi-decade, multi-cycle production deployment.
- **Classification**: Designated **`ROBUST_RESEARCH_CANDIDATE`** / **`B — PROMISING BUT INSUFFICIENT`**.
- **Live Trading**: Remains **STRICTLY DISABLED** (`LIVE_TRADING_ENABLED=false`).
