# QuantLab Part 17: Paper Trading Engine — Implementation Plan

## 1. Objectives & Safety Invariants
- **Isolated Paper Trading Environment:** Strict `ExecutionMode.PAPER_TRADING`. Zero real-money order routing, zero broker order placement.
- **True Live Market Data Ingestion & Health:** Track live data status (`REAL_TIME`, `DELAYED`, `STALE`, `NOT_AVAILABLE`). If live data is unavailable, report `LIVE_DATA_UNAVAILABLE` — never fabricate synthetic data as live.
- **Point-in-Time & Model Availability Protection:** Strict gating ($T_{\text{avail}} \le T_{\text{decision}}$) on all features, predictions, signals, and risk assessments.
- **Expected vs Realized Analytics:** Continuous evaluation of `expectedReturn` vs `realizedReturn`, `expectedVolatility` vs `realizedVolatility`, directional accuracy, and prediction errors over horizon periods.
- **Full Life-Cycle Execution Simulation:** Next-bar/live quote execution, Indian transaction costs (STT, brokerage, exchange charges, GST, stamp duty), slippage, liquidity constraints, stops, targets, time-based horizon exits, and corporate actions.
- **Deterministic Replay & Crash Recovery:** Idempotent event processing and state restoration from database.

---

## 2. Architecture & Step-by-Step Implementation

### Step 1: Database Migration (`V14__paper_trading_engine.sql` & `init-db.sql`)
- `paper_trading_sessions`: Session lifecycle (`CREATED`, `STARTING`, `RUNNING`, `PAUSED`, `DATA_DEGRADED`, `DISCONNECTED`, `STOPPED`, `FAILED`), data provider info, timings.
- `paper_portfolios`: Virtual capital, cash, invested value, current portfolio value, horizon (`SHORT_TERM`, `MEDIUM_TERM`, `LONG_TERM`), risk profile.
- `paper_positions`: Live open/closed paper positions, entry prices, quantities, unrealized/realized PnL, stops, targets, horizon attribution.
- `paper_decisions`: Immutable decision audit records with complete signal, prediction, risk, and evidence snapshot.
- `paper_orders`: Virtual orders (`MARKET`, `LIMIT`, `STOP`) with requested and fill prices, status (`PENDING`, `FILLED`, `REJECTED`, `CANCELLED`), rejection reasons.
- `paper_fills`: Execution fill records with slippage and fee breakdowns.
- `paper_ledger`: Append-only transaction ledger.
- `paper_equity_curve`: Time-series equity, daily returns, and drawdown points.
- `paper_signal_outcomes`: Evaluation of signal direction (`CORRECT_DIRECTION`, `WRONG_DIRECTION`, `PARTIAL`, `EXPIRED`, `NO_OUTCOME_YET`).
- `paper_prediction_outcomes`: Mathematical calibration and prediction errors ($\text{realizedReturn} - \text{expectedReturn}$).
- `paper_model_monitoring`: Drift, MAE, RMSE, IC, rank IC, calibration tracking per model version.
- `paper_risk_monitoring`: Continuous portfolio risk limits and breach tracking.
- `paper_data_health`: Live feed status, latency, freshness, error counts.

### Step 2: Python Quantitative Paper Engine (`quant-service/app/paper/`)
- `schemas.py`: Pydantic data schemas, status enums (`ExecutionMode`, `PaperTradingStatus`, `DataFreshnessStatus`, `SignalOutcomeStatus`, etc.).
- `clock.py`: `PaperTradingClock` supporting `LIVE_CLOCK` and `REPLAY_CLOCK`.
- `health.py`: `LiveDataHealthMonitor` checking provider connectivity, data age, and staleness.
- `portfolio.py`: `PaperPortfolioManager` tracking cash, positions, mark-to-market valuations, cash reconciliation, stops/targets, and ledger logging.
- `execution.py`: `PaperExecutionSimulator` simulating execution prices, slippage, Indian costs, and liquidity constraints.
- `outcomes.py`: `ExpectedVsRealizedEngine` measuring realized performance, prediction error, and signal accuracy after horizon expiration.
- `monitoring.py`: `ModelMonitoringEngine` tracking calibration, drift, and IC.
- `engine.py`: Master `PaperTradingEngine` managing session loops, data health gating, decision logging, order creation, fills, and crash recovery.

### Step 3: Python Test Suites (`quant-service/tests/`)
- `test_paper_engine.py`: Unit and end-to-end tests for session lifecycle, order generation, execution, stops/targets, corporate actions, cash reconciliation, and expected vs realized outcomes.
- `test_paper_pit_and_leakage.py`: Tests proving future price/news/fundamental/model mutations do not alter past paper decisions, stale-data trade blocking, and crash recovery idempotency.

### Step 4: Java Backend Layer (`backend/src/main/java/com/quantlab/paper/`)
- `model/`: DTOs and Enums (`PaperTradingSessionDTO`, `PaperPortfolioDTO`, `PaperPositionDTO`, `PaperDecisionDTO`, `PaperOrderDTO`, `PaperOutcomeDTO`, `LiveDataHealthDTO`, etc.).
- `entity/`: JPA entities for all Part 17 database tables.
- `repository/`: Spring Data JPA repositories.
- `service/`: `PaperTradingService.java` managing sessions, portfolios, decisions, and analytics.
- `controller/`: `PaperTradingController.java` (`/api/v1/paper/*`).
- `PaperControllerTest.java`: WebMvc controller unit tests.

### Step 5: Frontend Terminal Dashboard (`frontend/src/`)
- Types in `frontend/src/types/market.ts`.
- Component `frontend/src/components/dashboard/PaperTradingTerminalCard.tsx` featuring:
  - Prominent "PAPER TRADING / NO REAL MONEY" isolation badge.
  - Virtual portfolio overview (Cash, Invested Value, Daily & Total P&L, Drawdown).
  - Live decisions and signals feed with evidence breakdown.
  - Expected vs Realized Prediction Scorecard with error tracking.
  - Open Paper Positions and Orders table.
  - Live Data Health monitor (freshness, latency, provider status).
- Integrated into `Dashboard.tsx`.
- Verified with `npm run build`.

### Step 6: Documentation & Final Verification Report
- `docs/paper-trading.md`.
- Section 104/105 Final Verification Report.
