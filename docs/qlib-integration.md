# Microsoft Qlib Quantitative Research Engine Integration in QuantLab

## 1. Executive Summary

Microsoft Qlib has been integrated into **QuantLab** as an **OPTIONAL, modular quantitative research and modeling engine layer**.

The integration strictly respects QuantLab's architectural boundaries:
- **Canonical PostgreSQL Storage & Data Ingestion**: Qlib does not act as an independent data silo. All raw price and volume data flow from QuantLab's canonical data models.
- **Strict Point-in-Time (PIT) Integrity**: Every dataset and feature calculation strictly prevents lookahead bias. Future data querying in feature mode raises `LookaheadBiasError`.
- **Leak-Free Preprocessing**: All normalizers (Z-score, MinMax, Robust, Winsorize) are fitted strictly on training data and applied to validation/test partitions without refitting.
- **Microstructure & Execution Realism**: Vectorized alpha signals are validated against QuantLab's Next-Bar T+1 execution engine with Indian statutory costs (STT 0.1%, brokerage, GST, stamp duty) and slippage.
- **Non-Live Constraint**: Live trading remains disabled (`LIVE_TRADING_ENABLED=false`, `PAPER_TRADING=true`).

---

## 2. Architecture & Data Flow

```
QuantLab Canonical Warehouse / PostgreSQL
                  │
                  ▼
   [app/qlib/provider.py]
   QuantLabQlibDataProvider  ◄── Enforces PIT thresholds (information_available_at <= as_of)
                  │
                  ▼
   [app/qlib/loader.py]
   QuantLabDataLoader        ◄── Computes Alpha158, Alpha360, or Custom Expressions
                  │
                  ▼
   [app/qlib/handler.py]
   QuantLabDataHandler       ◄── Chronological Splits (Train / Valid / Test) + Leak-Free Scalers
                  │
                  ▼
   [app/qlib/dataset.py]
   QuantLabDatasetH          ◄── Segmented matrices with preserved PIT metadata
                  │
                  ▼
   [app/qlib/model_adapter.py]
   QlibModel (GBDT, Linear, RF, Ensemble) ──► Bridge to QuantPredictionModel & Model Registry
                  │
                  ▼
   [app/qlib/evaluator.py]
   QlibSignalEvaluator       ◄── Daily IC, Rank IC, ICIR, Long-Short Sharpe, Turnover, Deciles
                  │
                  ▼
   [app/qlib/backtest_comparison.py]
   QlibBacktestComparator    ◄── Side-by-side comparison: Ideal Vectorized vs Canonical Next-Bar T+1
```

---

## 3. Core Modules Implemented

| Module Path | Component | Responsibility |
|---|---|---|
| [`app/qlib/config.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/config.py) | `QlibConfig` | Configures cache directories, benchmark symbol (`NIFTY50`), and execution settings. |
| [`app/qlib/expressions.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/expressions.py) | `QlibExpressionEvaluator` | Vectorized time-series and cross-sectional operators (`Ref`, `Delta`, `Mean`, `Std`, `Slope`, `CSRank`, `CSZScore`) with PIT lookahead prevention. |
| [`app/qlib/alpha158.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/alpha158.py) | `Alpha158Builder`, `Alpha360Builder` | Implements Microsoft Qlib Alpha158 (KBar, rolling price, rolling volume factors across windows 5, 10, 20, 30, 60) and Alpha360 sequences. |
| [`app/qlib/provider.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/provider.py) | `QuantLabQlibDataProvider` | Normalizes canonical database and memory frames into MultiIndex `(datetime, instrument)` panel formats while tracking `source_timestamp` and `information_available_at`. |
| [`app/qlib/loader.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/loader.py) | `QuantLabDataLoader` | Coordinates raw data ingestion, feature generation, forward return target creation, and metadata detachment. |
| [`app/qlib/handler.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/handler.py) | `QuantLabDataHandler` | Manages non-overlapping chronological splits (Train, Valid, Test), purging periods, and fitted feature scalers. |
| [`app/qlib/dataset.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/dataset.py) | `QuantLabDatasetH` | Hierarchical dataset structure offering standard `.prepare(segment, col_set)` interface for models. |
| [`app/qlib/model_adapter.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/model_adapter.py) | `QlibGBDTModel`, `QlibLinearModel`, `QlibRandomForestModel`, `QuantLabQlibModelBridge` | Qlib-compatible model wrappers with seamless adapter bridge to QuantLab's `QuantPredictionModel`. |
| [`app/qlib/evaluator.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/evaluator.py) | `QlibSignalEvaluator` | Analyzes signal quality: daily IC, Rank IC, ICIR, Long-Short annualized return, Sharpe, drawdown, turnover, and quantile spreads. |
| [`app/qlib/backtest_comparison.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/backtest_comparison.py) | `QlibBacktestComparator` | Evaluates divergence between idealized vectorized backtesting and QuantLab Next-Bar T+1 execution with Indian statutory fees. |
| [`app/qlib/experiment_adapter.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/qlib/experiment_adapter.py) | `QlibExperimentManager` | Persists research run configurations, metrics, and models into `artifacts/qlib_experiments/` and `LocalModelRegistry`. |
| [`app/api/qlib.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/api/qlib.py) | FastAPI Router (`/api/v1/qlib`) | Exposes REST endpoints for alpha factor generation, model training, evaluation, and backtest comparison. |

---

## 4. REST API Endpoints

- `POST /api/v1/qlib/alpha/compute`: Computes Alpha158 or Alpha360 factors for a specified instrument list and date range.
- `POST /api/v1/qlib/model/train`: Trains a Qlib model (`GBDT`, `Ridge`, `RandomForest`, `Ensemble`), computes predictive metrics (IC/Rank IC), and performs backtest comparison.
- `GET /api/v1/qlib/experiments`: Lists all recorded quantitative research experiments and metadata.

---

## 5. Verification & Test Coverage

All automated test suites execute with 100% pass rates:
- **Backend (Java 21 / Gradle 8.12)**: 129 / 129 passed (`Exit code: 0`).
- **Quant-Service (Python 3.14 / Pytest)**: 121 / 121 passed (`Exit code: 0`).
  - `test_qlib_expressions.py`: Vectorized math, time-series ops, cross-sectional ops, and adversarial negative-lag rejection.
  - `test_qlib_alpha158.py`: Complete Alpha158 factor matrix (158 features), Alpha360 (360 features), and adversarial future-price invariance tests.
  - `test_qlib_data_and_pit.py`: PIT filtering, forward target separation, non-overlapping train/valid/test splits.
  - `test_qlib_models_and_eval.py`: GBDT, Ridge, RF, Ensemble training, bridge to `QuantPredictionModel`, IC/Rank IC evaluation, and backtest comparison.
  - `test_qlib_api.py`: FastAPI endpoints for alpha generation, training, and experiment audit.
- **Frontend (TypeScript / Vite)**: Production build passed with 0 errors (`Exit code: 0`).
