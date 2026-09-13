# PART 13: Production Cross-Check / Signal Engine

## 1. Overview & Architectural Philosophy

The **QuantLab Cross-Check / Signal Engine** is the evidence aggregation and decision layer of the platform. It systematically fuses observations and models across:
1. **Technical Features** (Part 9)
2. **Fundamental Metrics** (Part 8)
3. **News & Sentiment** (Part 5)
4. **Corporate Actions & Events** (Part 5)
5. **Institutional FII/DII Flows** (Part 6)
6. **Mutual Fund Disclosures** (Part 6)
7. **Indian Market Context & Breadth** (Part 3/4)
8. **Global Macro & Indices** (Part 7)
9. **Macroeconomic Indicators** (Part 8)
10. **ML Predictive Models** (Part 11/12)
11. **Multi-Dimensional Market Regime** (Part 10)

```
                    DATA SOURCES (Point-in-Time Safe)
                         │
      ┌──────────────────┼──────────────────┐
      ▼                  ▼                  ▼
 Technical          Fundamental          News
      │                  │                  │
      ├──────────────────┼──────────────────┤
      ▼                  ▼                  ▼
 Institutional       Global              Macro
      │                  │                  │
      ├──────────────────┼──────────────────┤
      ▼                  ▼                  ▼
       ML Prediction + Market Regime
                         │
                         ▼
                POINT-IN-TIME GATE (T_avail ≤ T)
                         │
                         ▼
                EVIDENCE NORMALIZER (-100 to +100)
                         │
                         ▼
             CORRELATION GROUP CONTROLS
                         │
                         ▼
                 CROSS-CHECK ENGINE
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
           SUPPORT    CONFLICT    RISK
              │          │          │
              └──────────┼──────────┘
                         ▼
                 SIGNAL SCORER
                         │
                         ▼
               CONFIDENCE ENGINE
                         │
                         ▼
            DECISION RULES (BUY/HOLD/SELL/NO_SIGNAL)
                         │
                         ▼
              DETERMINISTIC REASONING ENGINE
                         │
                         ▼
              VERSIONED SIGNAL STORE (PostgreSQL V10)
```

---

## 2. Core Mandatory Rules

1. **NO BUY WITHOUT EVIDENCE:**
   - A `BUY` signal is issued **ONLY** if:
     - Final Signal Score $\ge +35.0$ (on scale $-100$ to $+100$).
     - Confidence $\ge 0.50$ ($50\%$).
     - Number of independent supporting categories $\ge 3$.
     - Conflict severity is **NOT HIGH**.
     - Data quality is **NOT INSUFFICIENT_DATA**.
2. **NO SELL WITHOUT EVIDENCE:**
   - A `SELL` signal is issued **ONLY** if:
     - Final Signal Score $\le -35.0$.
     - Confidence $\ge 0.50$.
     - Number of independent opposing categories $\ge 3$.
     - Conflict severity is **NOT HIGH**.
3. **NO SIGNAL ON MISSING DATA / QUALITY PENALTY:**
   - If critical evidence is missing, the system outputs `HOLD` or `NO_SIGNAL` with explicit reason `INSUFFICIENT_DATA`.
   - Missing data is **NEVER** treated as neutral signal support.
4. **CORRELATION DAMPENING & DOUBLE-COUNTING PREVENTION:**
   - Categories sharing common drivers (e.g. Trend Group: SMA50 + NIFTY Trend + Regime Direction) are dampened by `CorrelationGroupManager` so that correlated indicators cannot artificially dominate the score.
5. **DETERMINISTIC TRACEABLE REASONING:**
   - Every statement in `structured_reasoning` is mapped directly to a unique `evidence_id` in the database.

---

## 3. Signal Decision & Formulation

| Output Attribute | Formulation / Scale | Description |
| :--- | :--- | :--- |
| **Signal Decision** | `BUY`, `HOLD`, `SELL`, `NO_SIGNAL` | Rule-based deterministic decision. |
| **Signal Score** | $-100.0 \dots +100.0$ | Normalized weighted score penalized for conflict and data quality. |
| **Confidence** | $0.00 \dots 1.00$ ($0\% - 100\%$) | Multi-factor confidence based on completeness, quality, and consensus. |
| **Expected Return $E[R_{t+1}]$** | Continuous float | 1-Day forward expected return from Part 11 Model 1. |
| **Expected Volatility $\sigma_{5d}$** | Continuous float | 5-Day forward realized volatility from Part 11 Model 3. |
| **Return-to-Volatility Ratio** | $E[R] / \sigma$ | Risk-adjusted return metric. |
| **Conflict Severity** | `LOW`, `MEDIUM`, `HIGH` | Disagreement magnitude across independent evidence categories. |
| **Data Quality Status** | `HIGH_QUALITY`, `MEDIUM_QUALITY`, `LOW_QUALITY`, `INSUFFICIENT_DATA` | Input availability and validation status. |

---

## 4. REST API Endpoints (`/api/v1/signals/*`)

- `GET /api/v1/signals/current` — Returns latest active cross-checked signals for all tracked instruments.
- `GET /api/v1/signals/{symbol}` — Retrieves latest signal, score, confidence, return/volatility forecast, and components for a symbol.
- `GET /api/v1/signals/{symbol}/history` — Retrieves chronological historical signals for transition analysis.
- `GET /api/v1/signals/evidence/{signalId}` — Retrieves all atomic evidence items with source timestamps and publication lag.
- `GET /api/v1/signals/components/{signalId}` — Category-level scores, weights, and contributions.
- `GET /api/v1/signals/{symbol}/transitions` — Transition logs (e.g., BUY $\to$ HOLD).
- `GET /api/v1/signals/{symbol}/as-of/{timestamp}` — Point-in-Time signal reconstruction strictly using $T_{\text{avail}} \le T$.
- `GET /api/v1/signals/configuration` — Active weighting configuration and correlation groups.

---

## 5. PostgreSQL Schema (`V10__cross_check_signal_engine.sql`)

- **`signal_configurations`**: Weighting configurations, category caps, correlation groups, and threshold parameters.
- **`signal_generation_runs`**: Execution runs recording timestamp, instrument counts, duration, and status.
- **`signals`**: Point-in-time signals with scores, confidence, expected returns, conflict metrics, quality status, and version provenance.
- **`signal_components`**: Category-level scores (-100 to +100), weights, and contributions.
- **`signal_evidence`**: Atomic evidence entries with raw values, normalized scores, sources, and availability timestamps.
- **`signal_transitions`**: Chronological state transitions with transition reasons.
