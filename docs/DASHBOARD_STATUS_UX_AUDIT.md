# QUANT-LAB — DASHBOARD STATUS & TRUTHFULNESS UX AUDIT

**Date**: 2026-09-13  
**Stage**: Dashboard Status & Truthfulness UX Pass  
**Frontend Route**: `/` (`OverviewPage.tsx`)  
**Backend Scope**: 100% Unchanged (Zero quantitative/model modifications)  
**Final Verdict**: **`DASHBOARD_STATUS_TRUTHFUL_AND_READY`**

---

## 1. Problems Found

During inspection of the main dashboard UI (`/`), three critical status representation defects were identified:

1. **Misleading "LIVE DATA / DISCONNECTED" Terminology**:
   - The UI showed `LIVE DATA` as `DISCONNECTED`. This was misleading because Quant-Lab's architecture intentionally uses **delayed market polling** via Yahoo Finance (~15m delay) rather than continuous websocket streaming.
2. **"PAPER ENGINE / UNAVAILABLE / No provider" Misrepresentation**:
   - The UI showed `PAPER ENGINE` as `UNAVAILABLE` with subtitle `No provider`, despite the FastAPI backend `/api/v1/paper/health` and `/api/v1/paper/session` endpoints returning `status: HEALTHY`, `is_ready_to_start: true`, `provider: YAHOO_FINANCE`, and `status: RUNNING`.
3. **Ambiguous "GLOBAL REGIME / UNAVAILABLE" and "COMPOSITE SCORE: N/A"**:
   - Without explanation, raw `UNAVAILABLE` and `N/A` gave the false impression of system failure rather than the truthful state of awaiting the first live market observation.

---

## 2. Root Causes Identified

1. **Schema Property Mismatch in `OverviewPage.tsx`**:
   - `OverviewPage.tsx` previously typed the paper health response using an older `PaperTradingHealthReport` interface expecting `overallStatus`, `liveDataProvider`, `liveDataAvailable`, and `evaluatedAt`.
   - The authoritative FastAPI endpoint `/api/v1/paper/health` returns `status: "HEALTHY"`, `start_gate: { ... }`, `emergency_stop_active: false`, etc.
   - Because `health.overallStatus` evaluated to `undefined`, the UI defaulted to `'UNAVAILABLE'`, and `health.liveDataProvider` defaulted to `'No provider'`.
2. **Architectural Vocabulary Conflict**:
   - The term "LIVE DATA" was previously hardcoded in the fourth stat card and evaluated against `liveDataAvailable`, which defaulted to `DISCONNECTED` because streaming feeds are not used in delayed paper trading.

---

## 3. Fixes Made (Frontend-Only)

1. **Truthful Market Data Terminology (`OverviewPage.tsx`)**:
   - Replaced "LIVE DATA / DISCONNECTED" with:
     - **Label**: `MARKET DATA`
     - **Value**: `DELAYED · YAHOO FINANCE`
     - **Subtitle**: `~15m delay · POLLING`
     - **Theme**: Amber/Warning badge highlighting delayed polling.
2. **Authoritative Paper Engine Health Binding**:
   - Aligned TypeScript interface `PaperHealthResponse` and `PaperSessionResponse` with actual FastAPI responses.
   - **Value**: Renders `READY` when `/api/v1/paper/health` returns `status: "HEALTHY"`.
   - **Subtitle**: Displays authoritative provider `YAHOO_FINANCE · 100% Virtual` and start gate status `Start Gate: PASSED (₹0 Risk)`.
   - **Color**: High-visibility Emerald (`text-emerald-400`).
3. **Truthful Global Regime & Composite Score Empty States**:
   - **Global Regime Value**: `NO CURRENT DATA` (when awaiting validated market ticks) with subtitle `Awaiting validated market observation`.
   - **Composite Score Value**: `N/A` with clear subtitle `No validated signal data` (Scale: -100 to +100).
4. **Active Session Telemetry Banner**:
   - Added persistent ribbon confirming:
     - Session ID: `a64b59db...`
     - Execution Mode: `PAPER TRADING (VIRTUAL)`
     - Real Money at Risk: `₹0.00 (Hardlocked)`
     - Live Broker Orders: `DISABLED`
     - Frozen Model: `PHASE_16_FROZEN_RIDGE_TOP8`

---

## 4. API & UI Consistency Matrix

| Dashboard Card | Previous Misleading UI | Corrected Truthful UI | Authoritative API Endpoint / Source |
| :--- | :--- | :--- | :--- |
| **Global Regime** | `UNAVAILABLE / No data` | `NO CURRENT DATA` <br>`Awaiting validated market observation` | `/api/v1/global-market/regime/latest` (404/Empty) |
| **Composite Score** | `N/A / -1 to +1` | `N/A` <br>`No validated signal data` | Derived from Regime (Empty) |
| **Paper Engine** | `UNAVAILABLE / No provider` | `READY` <br>`YAHOO_FINANCE · 100% Virtual` | `/api/v1/paper/health` (`status: HEALTHY`) |
| **Market Data** | `LIVE DATA / DISCONNECTED` | `DELAYED · YAHOO FINANCE` <br>`~15m delay · POLLING` | `/api/v1/paper/session` (`provider: YAHOO_FINANCE`) |

---

## 5. Legitimate Remaining Unavailable / No-Data States

- **Global Regime**: Will legitimately remain `NO CURRENT DATA` until the first live market observation cycle executes during open NSE hours.
- **Institutional / Mutual Fund Intelligence**: Legitimately displays data source requirements awaiting official SEBI/AMFI disclosures.

---

## 6. Build & Test Verification

- **Frontend Typecheck & Build (`tsc -b && vite build`)**: **`0 Errors` (Built in 1.45s)**.
- **Backend Test Suite (`pytest`)**: **`346 passed, 0 failed`** (in 96.65s).
- **Backend Isolation**: Zero backend, quantitative, model, or strategy files were modified.

---

## 7. Final Verdict

```
================================================================================
                    DASHBOARD STATUS & UX VERDICT
================================================================================
  VERDICT: DASHBOARD_STATUS_TRUTHFUL_AND_READY
  
  [✓] Truthful Delayed Polling Disclosures (No false "LIVE DATA" or "DISCONNECTED")
  [✓] Authoritative Paper Engine Health Connected (Reports READY / HEALTHY)
  [✓] Explanatory Empty States for Global Regime & Composite Score
  [✓] Real Money at Risk Confirmed: ₹0.00 | Live Broker Orders: DISABLED
  [✓] Frontend Build: 0 Errors | Backend Pytest: 346/346 Passed
  [✓] Frozen Model & Quantitative Parameters 100% Unchanged
================================================================================
```
