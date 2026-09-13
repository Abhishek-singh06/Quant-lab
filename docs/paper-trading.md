# QuantLab Part 17: Production Paper Trading Engine

## 1. Executive Summary & Safety Philosophy

QuantLab's **Production Paper Trading Engine (Part 17)** enables quantitative strategy execution against real-world, live Indian market conditions in a **100% isolated, non-real-money simulation environment**.

### Core Safety Invariants:
1. **Zero Real-Money Routing:** The system operates exclusively under `ExecutionMode.PAPER_TRADING`. No real broker APIs, orders, or funds are connected or accessible.
2. **True Live Data Truthfulness:** Market data feeds are continuously audited for freshness (`REAL_TIME`, `DELAYED`, `STALE`, `NOT_AVAILABLE`). If live market credentials or feeds are unavailable, the engine reports `LIVE_DATA_UNAVAILABLE` — it **never fabricates synthetic data as real**.
3. **Point-in-Time Availability Enforcement:** Every decision requires that input data timestamps satisfy $T_{\text{avail}} \le T_{\text{decision}}$. Future data mutations can never alter historical paper decisions.
4. **Expected vs. Realized Mathematical Calibration:** Model return forecasts ($E[R]$) and volatility forecasts ($\sigma$) are logged immutably at decision time and systematically compared with realized outcomes ($R_{\text{realized}}$, $\sigma_{\text{realized}}$) once the target horizon elapses.

---

## 2. End-to-End Architecture & Decision Loop

```
┌────────────────────────────────────────────────────────────────────────┐
│                        LIVE MARKET DATA FEED                           │
│     (NSE Authorized Feed / Delayed / Trading Calendar Gate)            │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        POINT-IN-TIME GATING                            │
│           Verify: T_information_available <= T_decision                │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                       MULTI-HORIZON MODELS                             │
│     (Short-Term 1-5D / Medium-Term 1-12W / Long-Term 6M-5Y)            │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 CROSS-CHECK SIGNAL ENGINE (PART 13)                    │
│     (8 Evidence Layers, Weighted Fusion, Contradiction Penalty)        │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 RISK ENGINE & POSITION SIZING (PART 14)                │
│     (Volatility Parity, Kelly Sizing, Liquidity / Sector Limits)       │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                 PAPER ORDER & EXECUTION SIMULATOR                      │
│     (Next-Bar / Live Quote, Slippage Models, Indian STT/GST Costs)     │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│                VIRTUAL PORTFOLIO & AUDITABLE LEDGER                    │
│     (Cash Reconciliation, Mark-to-Market P&L, Equity Curve)           │
└──────────────────────────────────┬─────────────────────────────────────┘
                                   │
                                   ▼
┌────────────────────────────────────────────────────────────────────────┐
│             EXPECTED VS REALIZED OUTCOME EVALUATION                    │
│     (Prediction Error, Directional Accuracy, Model Drift Monitor)      │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Core Modules & Implementation Structure

### A. Python Quant Service (`quant-service/app/paper/`)
- `schemas.py`: Pydantic domain models, strict enums (`ExecutionMode`, `PaperTradingStatus`, `DataFreshnessStatus`, `SignalOutcomeStatus`, etc.).
- `clock.py`: `PaperTradingClock` supporting both `LIVE_CLOCK` and `REPLAY_CLOCK`.
- `health.py`: `LiveDataHealthMonitor` measuring feed latency, age, rate limits, and provider coverage.
- `portfolio.py`: `PaperPortfolioManager` tracking cash balance, mark-to-market position values, drawdowns, and immutable ledger transactions.
- `execution.py`: `PaperExecutionSimulator` modeling execution prices, slippage (spread/volume-aware), and Indian transaction costs (STT, brokerage, exchange turnover, GST, stamp duty).
- `outcomes.py`: `ExpectedVsRealizedEngine` measuring realized performance, prediction errors, and directional accuracy after horizon expiration.
- `monitoring.py`: `ModelMonitoringEngine` tracking calibration, drift, and Information Coefficient (IC).
- `engine.py`: Master `PaperTradingEngine` managing session lifecycles, decision loops, and crash recovery.

### B. Spring Boot Java Backend (`backend/src/main/java/com/quantlab/paper/`)
- `model/`: Java 21 DTO records and Enums.
- `entity/`: JPA entities mapping to `paper_*` PostgreSQL tables.
- `repository/`: Spring Data JPA repositories with custom query finders.
- `service/`: `PaperTradingService.java` providing transactional session management, portfolio valuation, and health auditing.
- `controller/`: `PaperTradingController.java` exposing REST endpoints under `/api/v1/paper/*`.
- `PaperTradingControllerTest.java`: Spring WebMvc mock MVC unit test suite.

### C. Frontend Dashboard (`frontend/src/`)
- `PaperTradingTerminalCard.tsx`: Interactive dashboard component with:
  - Prominent "ISOLATED PAPER TRADING" banner.
  - Virtual portfolio capital, invested value, realized/unrealized P&L, drawdown.
  - Active positions table with stop loss, target price, and model attribution.
  - Live decisions stream with complete multi-layer evidence snapshots.
  - Expected vs. Realized scorecard with mathematical prediction error tracking.
  - Live Data Health and provider connectivity monitor.

---

## 4. Database Schema (`V14__paper_trading_engine.sql`)

| Table Name | Primary Purpose |
|:---|:---|
| `paper_trading_sessions` | Session lifecycle state (`RUNNING`, `PAUSED`, `STOPPED`), data provider info, and audit timestamps. |
| `paper_portfolios` | Virtual cash balances, invested values, gross/net exposures, and drawdown tracking. |
| `paper_positions` | Open/closed positions, average entry prices, cost basis, unrealized/realized P&L, and stops/targets. |
| `paper_decisions` | Immutable decision audit records preserving signals, predictions, risk parameters, and evidence snapshots. |
| `paper_orders` | Virtual orders (`MARKET`, `LIMIT`, `STOP`) with submitted/executed timestamps, slippage, and fees. |
| `paper_fills` | Detailed execution fill records including Indian tax/brokerage breakdowns. |
| `paper_ledger` | Append-only double-entry transaction ledger for cash credits/debits. |
| `paper_equity_curve` | Periodic portfolio valuations for equity curve plotting and drawdown analysis. |
| `paper_signal_outcomes` | Evaluation of signal direction after horizon completion. |
| `paper_prediction_outcomes` | Calibration and error calculation ($\text{realizedReturn} - \text{expectedReturn}$). |
| `paper_model_monitoring` | Live model drift tracking, directional accuracy, MAE, RMSE, and rank IC. |
| `paper_risk_monitoring` | Periodic portfolio risk limit evaluations and breach alerts. |
| `paper_data_health` | Provider connection status, data age, latency, and error counts. |

---

## 5. Indian Market Transaction Costs & Slippage Model

Paper execution applies the Indian regulatory cost schedule:
- **Brokerage:** Configured bps or flat fee (e.g. ₹20 per trade).
- **Securities Transaction Tax (STT):** 0.1% on equity delivery (buy & sell) or 0.025% on equity intraday (sell only).
- **Exchange Turnover Charges:** 0.00345% (NSE).
- **Goods and Services Tax (GST):** 18% on (Brokerage + Exchange Charges).
- **SEBI Turnover Charges:** ₹10 per crore (0.0001%).
- **Stamp Duty:** 0.015% on buy turnover.
- **Slippage Model:** Spread-aware + Volume participation model ($\text{bps} = \text{base\_bps} + k \times \frac{\text{Order Volume}}{\text{ADV}}$).

---

## 6. Verification & Automated Test Matrix

| Test Suite | Scope & Invariants Verified |
|:---|:---|
| `test_paper_engine.py` | Full session lifecycle, virtual cash debit/credit, order generation, execution fills, stops/targets, corporate actions, and outcome expiration. |
| `test_paper_pit_and_leakage.py` | Point-in-time invariant ($T_{\text{avail}} \le T_{\text{decision}}$), future data mutation rejection, stale-data trade blocking, and crash recovery idempotency. |
| `PaperTradingControllerTest.java` | WebMvc endpoint verification (`/api/v1/paper/sessions`, `/portfolios`, `/decisions`, `/health`). |
| `npm run build` | TypeScript type-safety, React component compilation, and zero-error bundle generation. |

---

## 7. Critical Limitations & Production Disclaimers

> [!WARNING]
> **Important Disclaimers:**
> 1. **Paper Trading Is Not Real Live Trading:** Paper trading execution does not guarantee identical live performance. It does not account for real queue positioning, market impact on large orders, exchange latency spikes, or partial broker fills unless modeled.
> 2. **No Data Fabrication:** If real-time market data is not supplied, the system explicitly reports `LIVE_DATA_UNAVAILABLE`.
> 3. **Statistical Sample Size Gating:** Predictive accuracy metrics are only reported as meaningful once sufficient statistical sample sizes ($N \ge 30$) are accumulated.
