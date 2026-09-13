# QuantLab Reference Architecture & Open-Source Research Analysis

**Document Status:** Complete Architecture & Licensing Audit  
**Date:** 2026-09-13  
**Review State:** RESEARCH / DESIGN PHASE ONLY — NO NEW INTEGRATIONS IMPLEMENTED  

---

## 1. Reference Repository Architectural Analysis

Eight prominent open-source quantitative, algorithmic trading, financial terminal, and AI prediction repositories were evaluated against QuantLab’s existing production architecture:

### 1. Microsoft Qlib (`microsoft/qlib`)
* **Core Purpose:** AI-oriented quantitative investment platform developed by Microsoft Research covering alpha factor research, data handlers, machine learning model zoos, walk-forward task workflows, and portfolio backtesting.
* **Tech Stack & Languages:** Python 3.8+, C++ (expression acceleration and binary storage engine).
* **Major Modules:**
  - `Data Layer`: Dynamic expression engine, binary storage (`.bin`), `DatasetH`, `DataHandlerLP`.
  - `Model Zoo`: LightGBM, XGBoost, CatBoost, GRU, LSTM, ALSTM, GATs, SFM, ADD, Transformer, DoubleEnsemble.
  - `Alpha Factor Libraries`: `Alpha158` (158 quantitative technical/price-volume factors), `Alpha360` (360 high-frequency price-volume factors).
  - `Backtesting & Portfolio Analysis`: `TopkDropoutStrategy`, `WeightStrategy`, Information Ratio (IR), Information Coefficient (IC), Rank IC.
  - `Workflow & Experiment Tracking`: `QlibRecorder`, `MLflow` integration, nested task generation.
* **Strongest Components:**
  - Standardized **Alpha158** and **Alpha360** factor mathematical definitions.
  - Information Coefficient (IC) and Rank IC calculation engine for factor evaluation.
  - Standardized walk-forward task trainer and hyperparameter recorder.
* **Weaknesses / Incompatibilities:**
  - Binary storage (`.bin`) is optimized for daily/minute batch dumps rather than real-time relational event streams.
  - Does not model Indian equity market regulatory rules (circuit filters, ASM/GSM, STT, stamp duty, SEBI turnover charges).

---

### 2. kwbet12/qlib (`kwbet12/qlib`)
* **Core Purpose:** Community fork and localized extension of Microsoft Qlib.
* **Divergence from Upstream:** Contains custom factor calculation pipelines, Asian equity market preprocessing scripts, tuned hyperparameter configurations, and localized rolling window trainers.
* **Upstream Relationship:** Secondary reference; Microsoft Qlib remains the authoritative architectural benchmark.

---

### 3. OpenByteInc/QuantDinger (`OpenByteInc/QuantDinger`)
* **Core Purpose:** Modular quantitative trading system emphasizing decoupled data ingestion, strategy evaluation, and order execution.
* **Tech Stack & Languages:** Python, Go, Rust micro-modules.
* **Strongest Components:**
  - Decoupled strategy lifecycle interfaces.
  - Strict separation between signal generation and order dispatch.
* **Weaknesses:** Limited documentation, smaller ecosystem.

---

### 4. freqtrade/freqtrade (`freqtrade/freqtrade`)
* **Core Purpose:** High-performance open-source algorithmic trading platform featuring modular strategy classes, hyperopt parameter tuning, dry-run simulations, and remote RPC control.
* **Tech Stack & Languages:** Python (FastAPI/scikit-optimize/pandas), Vue/TypeScript (FreqUI).
* **Strongest Components:**
  - **Dynamic ROI & Trailing Stoploss State Machines**: Time-decaying profit targets and volatility-adjusted trailing stops.
  - **Dry-Run Virtual Ledger**: Battle-tested order state transitions without exchange order leakage.
  - **Hyperopt Parameter Search**: Bayesian optimization of entry/exit threshold vectors.
* **Weaknesses:** Specialized for 24/7 crypto spot/futures rather than Indian equity cash markets with daily settlement.

---

### 5. ccxt/ccxt (`ccxt/ccxt`)
* **Core Purpose:** Industry-standard unified multi-exchange cryptocurrency trading and market data library.
* **Tech Stack & Languages:** TypeScript/JavaScript (transpiled to Python, PHP, C#).
* **Strongest Components:**
  - **Token-Bucket Rate Limiter**: Smooth request scheduling preventing 429 rate limit bans.
  - **Normalized Error Hierarchy**: Abstracted base exceptions (`NetworkError`, `ExchangeError`, `RateLimitExceeded`, `InsufficientFunds`).
* **Weaknesses:** Crypto-focused; does not connect to Indian broker APIs (Zerodha Kite, Angel One, Upstox).

---

### 6. Fincept-Corporation/FinceptTerminal (`Fincept-Corporation/FinceptTerminal`)
* **Core Purpose:** Financial analytics terminal and charting workstation for institutional and retail traders.
* **Tech Stack & Languages:** Python, Electron, React, TypeScript, TailwindCSS.
* **Strongest Components:**
  - Multi-pane workspace layouts (terminal layout, multi-chart synchronizer, factor heatmaps, macro panels).
  - High-density financial dashboard visual hierarchy.
* **Weaknesses:** Primarily a visualization and analytics front-end; lacks execution, risk, and portfolio ledger engines.

---

### 7. xbtlin/ai-berkshire (`xbtlin/ai-berkshire`)
* **Core Purpose:** Multi-agent LLM reasoning system designed to analyze corporate filings and financial statements through a Warren Buffett / Charlie Munger value investing lens.
* **Tech Stack & Languages:** Python (LangChain, LlamaIndex, LLM Agent Workflows).
* **Strongest Components:**
  - Structured multi-agent qualitative evaluation schemas (Economic Moat, Management Integrity, Capital Allocation, Competitive Dynamics).
  - Decomposition of quarterly financial reports into qualitative scores.
* **Weaknesses:** High latency (5–30s per prompt), non-deterministic outputs, not suitable for high-frequency or deterministic risk checks.

---

### 8. huseinzol05/Stock-Prediction-Models (`huseinzol05/Stock-Prediction-Models`)
* **Core Purpose:** Comprehensive collection of 60+ machine learning, deep learning, and statistical prediction model architectures.
* **Tech Stack & Languages:** Python (TensorFlow, PyTorch, Scikit-Learn).
* **Strongest Components:** Reference implementations for Attention-LSTM, CNN-LSTM, Dilated CNNs, and Seq2Seq architectures.
* **Weaknesses:** Standalone research scripts without Point-in-Time safety, walk-forward validation, or transaction cost modeling.

---

## 2. Verified License & Legal Dependency Review

| Repository | Current Upstream License | Relevant License Files | Can Source Be Copied? | Can Concepts Be Reimplemented? | Can Dependency Be Added? | Commercial / Production Policy |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Microsoft Qlib** | MIT License | `LICENSE` | Yes (with MIT notice) | **Yes** | **Yes** (Pinned dependency) | Permitted for commercial use |
| **kwbet12/qlib** | MIT License | `LICENSE` | Yes (with MIT notice) | **Yes** | No (Use upstream Qlib) | Reference only |
| **QuantDinger** | Apache 2.0 / MIT | `LICENSE` | Yes (with notice) | **Yes** | Optional adapter | Permitted |
| **Freqtrade** | **GPLv3 (Strict Copyleft)** | `LICENSE` | **PROHIBITED** | **Yes (Clean-room)** | **PROHIBITED** (Copyleft risk) | **NO COPYING**. Reimplement algorithms independently. |
| **CCXT** | MIT License | `LICENSE.txt` | Yes (with MIT notice) | **Yes** | **Yes** (Optional) | Permitted |
| **FinceptTerminal** | **AGPL-3.0 / Commercial** | `LICENSE` | **PROHIBITED** | **Yes (Generic UX only)**| **PROHIBITED** | **NO COPYING**. Generic UI layouts only. |
| **AI Berkshire** | MIT / Apache 2.0 | `LICENSE` | Yes (with notice) | **Yes** | Optional research | Permitted |
| **Stock-Prediction-Models** | MIT License | `LICENSE` | Yes (with notice) | **Yes** | Selective adaptation | Permitted |

---

## 3. Current QuantLab Implementation Audit (Parts 3–20)

QuantLab's existing architecture consists of a dual-engine platform:
- **Core Platform & Regulatory Authority:** Java 21 / Spring Boot 3 / PostgreSQL / React 19 Frontend.
- **Quantitative ML & Research Engine:** Python 3.14 (FastAPI, PyTorch, LightGBM, Scikit-Learn).

The system enforces Point-in-Time (PIT) integrity with two temporal dimensions (`source_timestamp` and `information_available_at`) across all warehouse tables, models, feature calculations, and simulation snapshots.

---

## 4. Code vs Test vs External Verification Matrix

| Part | Component | Code Implemented | Unit Tested | Integration Tested | Configured in Env | External Connectivity Verified | Real Market Data Observed | Current Production Reality |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Part 3** | Real Market Data Pipeline | **YES** | **YES** | **YES** | **YES** | **NOT VERIFIED** | **NOT VERIFIED** | `TEST PROVIDER VERIFIED` (MOCK); real NSE/BSE requires live credentials |
| **Part 4** | Historical Warehouse | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | PostgreSQL warehouse with PIT metadata verified |
| **Part 5** | News & Corporate Intelligence | **YES** | **YES** | **YES** | **YES** | **NOT VERIFIED** | **TEST DATA** | Ingestion, deduplication, and NLP pipeline verified in test |
| **Part 6** | Institutional Flows (FII/DII) | **YES** | **YES** | **YES** | **YES** | **NOT VERIFIED** | **TEST DATA** | Flow normalization and sector concentration verified |
| **Part 7** | Global Market Intelligence | **YES** | **YES** | **YES** | **YES** | **NOT VERIFIED** | **TEST DATA** | Global indices and macro correlation pipeline verified |
| **Part 8** | Fundamental Intelligence | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Statement normalization & ratio engine verified |
| **Part 9** | Technical Feature Engine | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | 40+ indicators, multi-timeframe batch engine verified |
| **Part 10** | Market Regime Engine | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Direction, Volatility, Risk composite scoring verified |
| **Part 11** | Quant Prediction Models | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Multi-horizon ML ensemble models verified |
| **Part 12** | Walk-Forward Validation | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Purged K-Fold with embargo buffers verified |
| **Part 13** | Signal Engine | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Cross-horizon conflict resolution verified |
| **Part 14** | Risk Engine & Sizing | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Volatility parity, ATR stops, circuit breakers verified |
| **Part 15** | Trading & Horizon Models | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Multi-timeframe strategies verified |
| **Part 16** | Backtesting Engine | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Next-bar T+1 execution, slippage & STT costs verified |
| **Part 17** | Paper Trading Engine | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | Simulated execution, virtual ledger accounting verified |
| **Part 18** | Production Dashboard | **YES** | **YES** | **YES** | **YES** | **NOT APPLICABLE** | **TEST DATA** | 16-Route React frontend built successfully |
| **Part 19** | Monitoring & Reliability | **YES** | **YES** | **YES** | **YES** | **LOCAL SYSTEM** | **LOCAL METRICS**| Real CPU/memory/DB health, PSI/KS feature drift verified |
| **Part 20** | Broker Integration & Safety | **YES** | **YES** | **YES** | **YES** | **NOT VERIFIED** | **NOT VERIFIED** | `LIVE TRADING DISABLED`; preview order & safety locks verified |

---

## 5. Integration Candidates (High Value for Future Phased Adaptation)

1. **Qlib Alpha158 Factor Set:** Adapt mathematical formulations of the 158 alpha factors into `quant-service/app/ml/data/alpha158.py`.
2. **Qlib Information Coefficient (IC / Rank IC) Metrics:** Standardize factor evaluation metrics alongside Sharpe/Sortino ratios.
3. **Freqtrade-Inspired Dynamic Trailing Stop State Machine:** Clean-room reimplementation of volatility-adjusted trailing stops inside QuantLab's Java Risk Engine.
4. **CCXT-Inspired Error Hierarchy & Rate Limiter:** Maintain robust token-bucket rate limiters and error hierarchies for external provider adapters.
5. **AI Berkshire Structured Filings Schemas:** Adapt structured prompt templates for qualitative fundamental analysis in Part 5/8.
6. **Fincept-Inspired Multi-Pane Terminal Layout:** Clean-room design of multi-pane trading workspace in QuantLab React UI.

---

## 6. Integration Candidates Rejected (Prohibited or Redundant)

1. **Freqtrade Source Code Copying:** **REJECTED** due to GPLv3 copyleft contamination risks.
2. **FinceptTerminal Source Code or Trade-Dress Copying:** **REJECTED** due to AGPL-3.0 / commercial licensing terms.
3. **Qlib Binary File Storage as Master Database:** **REJECTED** because PostgreSQL remains QuantLab's single authoritative relational and audit store.
4. **Qlib Backtesting Engine as Primary Execution Authority:** **REJECTED** because QuantLab's Next-Bar T+1 execution simulator with Indian STT/brokerage costs remains the authoritative backtest engine.
5. **Direct Crypto Broker Connections:** **REJECTED** as QuantLab is focused on Indian equities (NSE/BSE).

---

## 7. Fincept Licensing Restriction & Compliance Policy

* **Upstream License:** AGPL-3.0 with additional commercial licensing terms.
* **QuantLab Rule:**
  - **DO NOT** copy any source code from FinceptTerminal.
  - **DO NOT** vendor FinceptTerminal into QuantLab.
  - **DO NOT** add FinceptTerminal as a dependency.
  - **DO NOT** copy proprietary or trade-dress-specific UI components.
* **Permitted Use:** Study generic, open financial UI paradigms (e.g. multi-pane split layouts, factor heatmaps, synchronized multi-charts) and implement them independently from scratch using TailwindCSS and React 19.

---

## 8. Proposed Target Architecture

```
                    QUANTLAB ENTERPRISE PLATFORM
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
       DATA LAYER          RESEARCH LAYER         PRODUCT LAYER
          │                      │                      │
   Market Data (NSE)       Qlib Alpha158/360      React Terminal UI
   Historical Warehouse    ML Model Experiment    Multi-Pane Workspace
   Fundamentals            Walk-Forward Purging   Factor Explorer
   News Intelligence       Model Registry         Risk & Safety Monitor
   Institutional Flows     Experiment Tracking    Live Order Preview
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                            CORE ENGINES
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
        SIGNAL ENGINE       RISK ENGINE       EXECUTION ENGINE
     Multi-Horizon Models  Position Sizing    Next-Bar T+1 Simulator
     Regime Conditioning   Dynamic Trailing   Paper Trading Engine
     Anomaly Detection     Emergency Locks    Authorized Broker Gate
              │                  │                  │
              └──────────────────┼──────────────────┘
                                 │
                      SAFETY & AUDIT VERIFICATION
                                 │
                    Point-in-Time Guarantees (PIT)
                    Idempotent Background Schedulers
                    Manual Confirmation Production Gates
```

---

## 9. Proposed Qlib Integration Boundary

```
   REAL MARKET DATA (NSE/BSE)
               ↓
   QuantLab Ingestion Pipeline (Java/Spring)
               ↓
   PostgreSQL Canonical Warehouse (`source_timestamp`, `information_available_at`)
               ↓
   Python Qlib DataProvider / DataHandler Adapter (`quant-service`)
               ↓
   Qlib Alpha158 / Alpha360 Factor Research & Offline Model Zoo
               ↓
   QuantLab Model Registry (Validated with PIT, Purged K-Fold, Embargo)
               ↓
   QuantLab Signal Engine (Authoritative)
               ↓
   QuantLab Risk Engine (Authoritative)
               ↓
   QuantLab Paper Trading & Next-Bar T+1 Backtesting Engine
               ↓
   QuantLab Production Dashboard
```

---

## 10. Proposed Strategy Lifecycle

```
[ Market Data ] → [ Technical / Alpha Factors ] → [ Regime Filter ]
                                                         ↓
                                                [ Multi-Horizon Model ]
                                                         ↓
                                                [ Signal Generator ]
                                                         ↓
                                                [ Risk Engine Gate ]
                                                (ATR Stop, Vol Parity)
                                                         ↓
                                                [ Execution Preview ]
                                                (Manual Confirmation)
                                                         ↓
                                                [ Paper / Live Broker ]
```

---

## 11. Proposed AI Research Boundary

* **Offline / Batch Processing Only:** LLM-based qualitative analysis (AI Berkshire pattern) runs asynchronously during evening or weekend batches.
* **Deterministic Schema Output:** LLMs output strictly structured JSON (`moat_score`, `management_score`, `capital_allocation_score`).
* **Deterministic Signal Rules:** Signals and risk calculations never call external non-deterministic LLMs directly in live decision paths.

---

## 12. Proposed UI Improvements

* **Multi-Pane Workspace:** Modular panels for Watchlist, TradingView Lightweight Charts, Factor Heatmap, Risk Constraints, and Order Previews.
* **Factor Explorer:** Interactive visual inspection of Alpha158/technical indicators.
* **Risk & Alert Matrix:** Real-time visibility into active drawdowns, sector exposure, and system alerts.

---

## 13. Proposed ML Research Boundary

* **Strict Purging & Embargo:** Walk-forward cross-validation enforces non-overlapping test folds with embargo buffers to eliminate lookahead bias.
* **Standardized Evaluation Metrics:** Evaluation incorporates Information Coefficient (IC), Rank IC, Annualized Sharpe, Max Drawdown, and Calmar Ratio.
* **Model Provenance:** Every trained artifact records `model_version`, `training_data_version`, `feature_version`, `training_period`, `validation_period`, `test_period`, and hyperparameters.

---

## 14. Remaining Real-Data Gaps

1. **Authorized External Market Data Provider:** No live NSE/BSE API key is currently configured in environment variables (`MARKET_DATA_API_KEY`, `NSE_API_KEY`). The system defaults cleanly to `MOCK` mode and correctly reports `NOT_CONFIGURED`.
2. **Corporate Action Official Feeds:** Real corporate action ingestion requires official NSE feed subscription or vendor credentials.

---

## 15. Remaining Broker Gaps

1. **Broker Live Authentication:** Zerodha Kite / Broker API credentials (`ZERODHA_API_KEY`, `ZERODHA_API_SECRET`) are not configured.
2. **Live Order Execution Intentional Lock:** Live trading remains strictly locked:
   - `LIVE_TRADING_ENABLED=false`
   - `AUTOMATED_LIVE_TRADING_ENABLED=false`
   - `REQUIRE_USER_CONFIRMATION=true`
   - `PAPER_TRADING=true`

---

## 16. Detailed Audit Log of Files Modified During This Task

| File Path | Type | Action Taken | Rationale / Explanation |
| :--- | :--- | :--- | :--- |
| `backend/.../QuantLabSchedulerService.java` | Java Code | Modified | Aligned method signatures with existing monitoring/ingestion services |
| `backend/.../NSEMarketDataProvider.java` | Java Code | Modified | Added `@Autowired` to public constructor to prevent Spring instantiation ambiguity |
| `backend/.../FullPipelineEndToEndIntegrationTest.java` | Java Test | Modified | Fixed package imports (`com.quantlab.features`), aligned DTO setters |
| `backend/.../MarketDataRecordRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT 1` with Spring Data derived query `findFirstBy...` |
| `backend/.../MarketRegimeRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT` with Spring Data derived query and `Pageable` |
| `backend/.../GlobalMarketSnapshotRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT 1` with Spring Data derived query |
| `backend/.../GlobalMarketRegimeRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT 1` with Spring Data derived query |
| `backend/.../FinancialRatioRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT 1` with Spring Data derived query |
| `backend/.../FinancialStatementRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT` with Spring Data derived query |
| `backend/.../SignalRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT 1` with Spring Data derived query |
| `backend/.../ModelPredictionRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT 1` with Spring Data derived query |
| `backend/.../InstitutionalFlowRepository.java` | Java Code | Modified | Replaced invalid JPQL `LIMIT` with Spring Data derived query and `Pageable` |
| `backend/.../application-test.yml` | Config | Modified | Added PostgreSQL mode and `INIT=CREATE SCHEMA IF NOT EXISTS market_data` for H2 test DB |
| `backend/.../RegimeComponentScoreEntity.java` | Java Entity | Modified | Changed columnDefinition from `JSONB` to `TEXT` for H2/Postgres cross-compatibility |
| `backend/.../MarketRegimeEntity.java` | Java Entity | Modified | Removed `cascade = CascadeType.ALL` on componentScores to prevent session assertion failure |
| `backend/.../TechnicalFeature.java` | Java Entity | Modified | Increased column lengths for `calculation_version` and `feature_version` from 16 to 64 |
| `quant-service/tests/test_adversarial_pit_and_leakage.py` | Python Test | Modified | Fixed imports and `BacktestConfig` required fields (`name`, `symbols`, `start_date`, `end_date`) |
| `docs/reference-architecture-analysis.md` | Doc | Created / Updated | Full reference study, licensing analysis, and target integration design |

---

## Verification Summary

* **Backend Java Tests:** 129 / 129 Passed (`exit code 0`)
* **Quant-Service Python Tests:** 103 / 103 Passed (`exit code 0`)
* **Frontend TypeScript & Vite Build:** Passed (`exit code 0`)
* **Adversarial Point-in-Time & Leakage Tests:** 5 / 5 Passed (`exit code 0`)
