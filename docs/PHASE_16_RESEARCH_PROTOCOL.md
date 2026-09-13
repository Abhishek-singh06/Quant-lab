# QuantLab Phase 16 — Pre-Registered Research Protocol

**Protocol Version**: `PHASE_16_RESEARCH_PROTOCOL_V1`  
**Creation Timestamp**: `2026-09-13T15:50:00Z`  
**Status**: **FROZEN & LOCKED**  
**Protocol Hash (SHA-256)**: `8f3d19be6a2419c8f0e5b4129e924a275727145241b184d0b13d2fa15c3272d1`  

---

## 1. Frozen Feature & Target Specifications

### 1.1 Predictive Features (Strictly Past-Looking)
All features are computed using only past information available up to bar close $T$:
1. `feat_ret_1d`: 1-day percentage price return $\frac{P_t - P_{t-1}}{P_{t-1}}$
2. `feat_ret_5d`: 5-day percentage price return $\frac{P_t - P_{t-5}}{P_{t-5}}$
3. `feat_ret_20d`: 20-day percentage price return $\frac{P_t - P_{t-20}}{P_{t-20}}$
4. `feat_rsi_14`: 14-period Relative Strength Index normalized to $[0, 100]$
5. `feat_vol_20d`: 20-day annualized realized volatility $\sigma_{20d} \times \sqrt{252}$
6. `feat_vol_ratio`: Volume moving average ratio $\frac{\text{SMA}_5(\text{Volume})}{\text{SMA}_{20}(\text{Volume})}$
7. `feat_trend_ratio`: Distance to macro moving average $\frac{P_t}{\text{SMA}_{50}(P_t)}$

### 1.2 Forward Target Labels (Future Return for Training Only)
Forward return labels are computed strictly for supervisory training and out-of-sample evaluation:
- **$T+1$**: 1-day forward return $\frac{P_{t+1} - P_t}{P_t}$
- **$T+5$ (Primary Research Horizon)**: 5-day forward return $\frac{P_{t+5} - P_t}{P_t}$
- **$T+20$**: 20-day forward return $\frac{P_{t+20} - P_t}{P_t}$

---

## 2. Walk-Forward Fold Boundaries & Leakage Elimination

All primary evaluations utilize 4 expanding chronological walk-forward folds:

| Fold Index | Training Interval | Validation Interval | Out-of-Sample (OOS) Interval | Purge / Embargo |
| :---: | :---: | :---: | :---: | :---: |
| **Fold 1** | 2023-02-01 to 2023-06-30 | 2023-07-08 to 2023-08-31 | 2023-09-08 to 2023-11-15 | 5d Purge + 2d Embargo |
| **Fold 2** | 2023-02-01 to 2023-09-30 | 2023-10-08 to 2023-11-30 | 2023-12-08 to 2024-02-28 | 5d Purge + 2d Embargo |
| **Fold 3** | 2023-02-01 to 2023-12-31 | 2024-01-08 to 2024-03-31 | 2024-04-08 to 2024-07-15 | 5d Purge + 2d Embargo |
| **Fold 4** | 2023-02-01 to 2024-05-31 | 2024-06-08 to 2024-08-31 | 2024-09-08 to 2024-12-31 | 5d Purge + 2d Embargo |

### Preprocessing Invariant:
`StandardScaler` is fitted **EXCLUSIVELY** on `X_train`. Validation and OOS feature vectors are normalized using the frozen mean and variance from `X_train`.

---

## 3. Pre-Registered Model Candidate Roster

1. **Ridge Regression**: L2-regularized linear model ($\alpha=10.0$).
2. **Logistic Regression**: Directional probability classifier ($\text{Threshold}=0.50$).
3. **Random Forest**: Ensemble of 40 decision trees ($\text{max\_depth}=4$).
4. **HistGradientBoosting**: Gradient boosted trees ($\text{max\_depth}=3, \text{learning\_rate}=0.05$).
5. **Lasso Regression**: L1-regularized sparse model ($\alpha=0.001$).
6. **Momentum Baseline**: Naïve past 5-day return predictor.
7. **Random Baseline**: Uniform normal noise predictor ($N(0, 1)$).

---

## 4. Indian Market Transaction Cost & Friction Schedule

| Cost Component | Standard Delivery Rate | Model Implementation |
| :--- | :--- | :--- |
| **Brokerage** | 2.0 bps per leg | Institutional flat rate |
| **Securities Transaction Tax (STT)** | 10.0 bps on sell leg | NSE equity delivery statutory rate |
| **Exchange & Clearing Charges** | 0.35 bps roundtrip | NSE transaction fee schedule |
| **SEBI Turnover & Stamp Duty** | 0.15 bps roundtrip | Statutory stamp duty |
| **Goods & Services Tax (GST)** | 18% on brokerage/exchange | $\approx 0.42$ bps roundtrip |
| **Execution Slippage** | 2.5 bps per leg | 5.0 bps roundtrip liquid equity baseline |
| **Total Baseline Drag** | **15.0 bps Roundtrip** | Applied per unit of portfolio turnover |

### Friction Sensitivity Levels:
Evaluated across **$0\text{ bps}, 15\text{ bps}, 30\text{ bps}, 50\text{ bps}$**.

---

## 5. Pre-Registered Turnover Control Experiments

1. **Regime A (Raw / Unconstrained)**: Immediate rebalance to Top-5 highest ranked assets on every 5-day cycle.
2. **Regime B (Conservative Turnover Damping)**: Maintain position if asset remains within Top-8 rank; replace only if it drops below rank 8.
3. **Regime C (Moderate Turnover Penalty)**: Portfolio quadratic turnover penalty regularizer.

---

## 6. Pre-Registered Model Selection Gates

To qualify as a **`ROBUST_RESEARCH_CANDIDATE`** or **`PAPER_TRADING_CANDIDATE`**, a model configuration must satisfy all of the following without exception:

1. **Primary Gate 1**: Median OOS Rank IC $> +0.030$.
2. **Primary Gate 2**: Mean OOS Directional Accuracy $> 52.0\%$.
3. **Secondary Gate**: Net Annualized Return $> 0.0\%$ under 15 bps Indian delivery friction.
4. **Guardrail 1**: Maximum Drawdown $< 25.0\%$.
5. **Guardrail 2**: Multi-Seed Stability $\sigma_{seed} < 0.020$ across 5 seeds.
6. **Guardrail 3**: Single-Feature Perturbation Retention $> 50.0\%$.
7. **Sample Limitation Check**: If dataset contains $< 50$ stocks or $< 5$ years of history, the maximum allowable promotion is **`ROBUST_RESEARCH_CANDIDATE`** / **`PAPER_TRADING_READY_WITH_LIMITATIONS`**.
