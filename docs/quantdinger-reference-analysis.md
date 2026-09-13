# QuantDinger Reference Architecture & Lifecycle Analysis

**Document Status:** Complete Architecture & Research Analysis  
**Repository Reference:** `OpenByteInc/QuantDinger` (v5.0+)  
**Review State:** RESEARCH & DESIGN AUDIT — BEFORE IMPLEMENTATION  

---

## 1. Executive Summary & Repository Overview

QuantDinger (`OpenByteInc/QuantDinger`) is an open-source AI Trading OS and multi-tenant algorithmic trading framework designed for strategy research, backtesting, paper trading, live execution, and monitoring.

### Core Tech Stack:
- **Backend**: Python (Flask, Gunicorn, Celery Worker, Celery Beat, PostgreSQL, Redis cache, Redis durable jobs).
- **Frontend / Client**: Vue 3 / TypeScript desktop and mobile clients (separate repositories: `QuantDinger-Vue`, `QuantDinger-Mobile`).
- **AI Agent / MCP**: Agent Gateway (`/api/agent/v1`) with Model Context Protocol (MCP) server for tool integration with Cursor/Claude/Codex.
- **Process Topology**:
  1. `backend` (HTTP API — thin facade)
  2. `trading-worker` (long-running strategy execution loops, leases, heartbeats)
  3. `scheduler-worker` (periodic scans and monitoring jobs)
  4. `celery-worker` (finite, retryable background tasks)
  5. `celery-beat` (cron-like job trigger)
  6. `mcp-server` (agent tool dispatch)

---

## 2. Verified License & Intellectual Property Audit

- **Backend Source License:** **Apache License 2.0** (`LICENSE`).
- **Web & Mobile Frontend Licenses:** **Source-Available / Proprietary Commercial** (`QuantDinger-Vue`, `QuantDinger-Mobile`).
- **Trademarks & Branding:** Governed by `TRADEMARKS.md`. Apache 2.0 does not grant trademark or branding rights.
- **IP & Implementation Policy for QuantLab:**
  - **NO SOURCE CODE COPYING**.
  - **NO COPYING OF PROPRIETARY/TRADEMARKED UI**.
  - QuantLab retains its own Java 21 / Spring Boot 3 core authority, Python FastAPI quantitative engine, and React 19 dashboard.
  - All concepts adapted from QuantDinger must be implemented **clean-room**, natively adhering to QuantLab's strict Point-in-Time (PIT) architecture, Walk-Forward validation, and Indian equity regulatory safety boundaries.

---

## 3. Subsystem-by-Subsystem Architecture Breakdown

### 3.1 Overall Architecture & Process Roles
- **QuantDinger Approach:** Deconstructs trading operations into independent process containers: HTTP API does not own long-running trading loops; `trading-worker` acquires leases via PostgreSQL to execute strategy state machines; Celery handles finite async jobs.
- **QuantLab Mapping:** QuantLab already enforces separation between Java Spring Boot backend (persistence, audit, order guards) and Python `quant-service` (ML, alpha, backtest, paper engine).

### 3.2 Strategy Lifecycle & Strategy Registry (Strategy API V2)
- **QuantDinger Approach:** Strategies are versioned entities with explicit lifecycle hooks (`initialize`, `on_bar`, `on_order_status`, `on_stop`). State transitions are managed explicitly with fencing tokens and lease heartbeats.
- **QuantLab Mapping:** QuantLab has Horizon Models (Part 15) and ML Models (Part 11), but lacks a **Unified Strategy Registry & Lifecycle Manager** that packages Feature Sets + ML Models + Horizon Rules + Risk Config + Backtest Results into an immutable, versioned strategy entity.

### 3.3 Research, Experiment & Provenance Lifecycle
- **QuantDinger Approach:** Tracks strategy versions, parameters, data time-ranges, and backtest run results in PostgreSQL tables.
- **QuantLab Mapping:** QuantLab has Walk-Forward and Qlib Model Registries, but needs an **End-to-End Experiment Provenance Manager** connecting Dataset Version → Feature Version → Model Version → Walk-Forward Split → Backtest ID → Strategy Version.

### 3.4 Backtest → Paper Trading Continuity & Configuration Drift
- **QuantDinger Approach:** Strategies run identically in backtest and live/paper runtimes through standard Strategy API interfaces.
- **QuantLab Mapping:** QuantLab has separate backtest (Part 16) and paper trading (Part 17) engines. **Key Missing Gap:** Automated **Configuration Drift Validation** that compares Backtest Config vs Paper Config (Universe, Slippage, Fees, Stop Loss, Sizing, Feature Version) and **BLOCKS deployment** if drift is detected.

### 3.5 Deployment Promotion Gates
- **QuantDinger Approach:** Promotion state machine with permissions, API keys, and agent scopes (`paper_only=true`, `AGENT_LIVE_TRADING_ENABLED=false`).
- **QuantLab Mapping:** QuantLab requires an authoritative Strategy Promotion Gate:
  `RESEARCH` → `VALIDATED` → `BACKTESTED` → `PAPER_ELIGIBLE` → `PAPER_RUNNING` → `PAPER_VALIDATED` → `MANUAL_REVIEW` → `LIVE_ELIGIBLE`.
  `LIVE_ELIGIBLE` remains subject to `LIVE_TRADING_ENABLED=false`, `REQUIRE_USER_CONFIRMATION=true`.

### 3.6 Workflow / Job Orchestration
- **QuantDinger Approach:** Celery tasks and database job state tracking with retry policies.
- **QuantLab Mapping:** QuantLab scheduled background jobs (Part 5Scheduler) can be augmented with a structured **Workflow Job Engine** (`job_id`, `job_type`, `status`, `correlation_id`, `started_at`, `completed_at`, `idempotency_key`, `error_details`).

### 3.7 Monitoring Integration
- **QuantDinger Approach:** Prometheus exporter + Grafana dashboards + Alertmanager.
- **QuantLab Mapping:** QuantLab already has a comprehensive Part 19 Monitoring & Alerting Engine (PSI/KS drift, system metrics, alert cooldowns, incident lifecycle). Workflow jobs and strategy state changes must feed directly into Part 19 alerts.

---

## 4. Concept Classification Matrix (A / B / C / D / E)

- **A** = Useful architectural concept to adapt
- **B** = Concept already implemented in QuantLab
- **C** = Missing capability in QuantLab
- **D** = Concept unsuitable for QuantLab
- **E** = Concept requiring clean-room implementation

| QuantDinger Concept | Category | QuantLab Status & Integration Strategy |
|---|---|---|
| **Immutable Strategy Registry** (`strategy_id`, version, features, models, risk, execution) | **A, C, E** | **To implement (clean-room)**: Unifies model, features, risk, and regime dependencies into versioned strategy definitions. |
| **Experiment Provenance Tracker** (Data ver → Feature ver → Model ver → Backtest) | **A, C, E** | **To implement (clean-room)**: Stores full lineage and hash reproducibility metadata for strategy research runs. |
| **Backtest-to-Paper Configuration Drift Guard** | **A, C, E** | **To implement (clean-room)**: Verifies exact parity of risk, sizing, models, and universe before paper trading activation; blocks on mismatch. |
| **Strategy Promotion State Machine Gates** (`RESEARCH` → ... → `MANUAL_REVIEW`) | **A, C, E** | **To implement (clean-room)**: Enforces rigorous validation criteria before a strategy can transition between lifecycle states. |
| **Workflow Job & Orchestration Lifecycle** (`job_id`, idempotency, audit) | **A, C, E** | **To implement (clean-room)**: Tracks execution runs for data ingestion, feature generation, model training, and backtesting. |
| **Broker Safety Guard & Manual Confirmation** | **B** | **Already implemented in QuantLab** (Part 20 `TradingSafetyGuard`, preview order, double confirmation, circuit limits). Keep authoritative. |
| **Next-Bar T+1 Realistic Backtest Engine** | **B** | **Already implemented in QuantLab** (Part 16 with Indian STT, stamp duty, GST, brokerage). |
| **Point-in-Time (PIT) Warehouse & Feature Store** | **B** | **Already implemented in QuantLab** (Parts 4, 9, 11 with `source_timestamp` and `information_available_at`). |
| **Part 19 System Health & Drift Monitoring** | **B** | **Already implemented in QuantLab** (PSI, KS, Wasserstein, CPU/Memory/DB/Redis health). |
| **Multi-Tenant SaaS / Billing / Stripe / User Subscriptions** | **D** | **Unsuitable**: QuantLab is a private, self-hosted institutional quant lab and pair-programming research environment, not a multi-tenant commercial SaaS. |
| **Crypto-Only Perpetuals / 24-7 Perpetual Futures Funding** | **D** | **Unsuitable**: QuantLab focuses on Indian Equities (NSE/BSE) cash and derivatives with daily trading sessions and statutory clearing rules. |

---

## 5. Proposed QuantLab Clean-Room Strategy Lifecycle Architecture

```
                       ┌─────────────────────────────────────┐
                       │          1. RESEARCH PHASE          │
                       │  - Alpha Factor Exploration (Qlib)  │
                       │  - Dataset & PIT Feature Matrix     │
                       └──────────────────┬──────────────────┘
                                          │
                                          ▼
                       ┌─────────────────────────────────────┐
                       │       2. MODEL TRAINING & WF        │
                       │  - Purged Walk-Forward Cross-Val    │
                       │  - Out-of-Sample Metric Evaluation  │
                       └──────────────────┬──────────────────┘
                                          │
                                          ▼
                       ┌─────────────────────────────────────┐
                       │       3. STRATEGY REGISTRATION      │
                       │  - Strategy Registry (Immutable vX) │
                       │  - Provenance & Checksum Record     │
                       └──────────────────┬──────────────────┘
                                          │
                                          ▼
                       ┌─────────────────────────────────────┐
                       │       4. REALISTIC BACKTESTING      │
                       │  - Next-Bar T+1 Execution           │
                       │  - Indian Statutory Friction / STT  │
                       └──────────────────┬──────────────────┘
                                          │
                                          ▼
                       ┌─────────────────────────────────────┐
                       │      5. CONFIG DRIFT VALIDATION     │
                       │  - Check Backtest vs Paper Config   │
                       │  - Block on parameter mismatch      │
                       └──────────────────┬──────────────────┘
                                          │
                                          ▼
                       ┌─────────────────────────────────────┐
                       │        6. PAPER TRADING GATE        │
                       │  - Virtual Ledger Simulation        │
                       │  - Live Signal Anomaly Monitoring   │
                       └──────────────────┬──────────────────┘
                                          │
                                          ▼
                       ┌─────────────────────────────────────┐
                       │     7. MANUAL OPERATOR APPROVAL     │
                       │  - Strict Human Confirmation Gate   │
                       │  - LIVE_TRADING_ENABLED=false (Safe)│
                       └─────────────────────────────────────┘
```

---

## 6. Implementation Scope for Phase 6

To realize the valuable architectural concepts identified above without bloating or compromising QuantLab:

1. **`app/strategy_lifecycle/models.py`**: Pydantic and domain schemas for `StrategyDefinition`, `StrategyStatus`, `PromotionGate`, `ExperimentProvenance`, `WorkflowJob`, `ConfigDriftReport`.
2. **`app/strategy_lifecycle/registry.py`**: `StrategyRegistry` managing versioned, immutable strategy definitions and serialized artifact references.
3. **`app/strategy_lifecycle/provenance.py`**: `ExperimentProvenanceManager` linking Data Version → Feature Version → Model Version → Backtest ID → Strategy Version.
4. **`app/strategy_lifecycle/drift_detector.py`**: `ConfigDriftDetector` comparing Backtest Config against Paper Config and enforcing deployment blocking rules.
5. **`app/strategy_lifecycle/gates.py`**: `StrategyPromotionGate` state machine verifying gate transition requirements.
6. **`app/strategy_lifecycle/orchestrator.py`**: `WorkflowOrchestrator` managing job execution, run IDs, idempotency, and status tracking.
7. **`app/api/strategy_lifecycle.py`**: REST endpoints for Strategy Registry, Promotion Gates, Drift Detection, and Workflow Jobs.
8. **Test Suite**: `tests/test_strategy_lifecycle.py` covering provenance, version uniqueness, drift detection, gate enforcement, and safety blocks.
