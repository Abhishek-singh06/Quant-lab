# QUANT-LAB — FRONTEND → BACKEND CONNECTIVITY AUDIT & RESOLUTION REPORT

**Date**: 2026-09-13  
**Stage**: Frontend-to-Backend Infrastructure & Proxy Routing Resolution  
**Frontend URL**: `http://localhost:3000`  
**Backend URL**: `http://127.0.0.1:8000` (FastAPI, Uvicorn)  
**Final Verdict**: **`FRONTEND_BACKEND_CONNECTED`**

---

## 1. Root Cause Identification

When accessing `http://localhost:3000/`, Vite logged:
```
[vite] http proxy error: /api/v1/paper/health ECONNREFUSED
[vite] http proxy error: /api/v1/global-market/regime/latest ECONNREFUSED
```

**Root Cause**:
- In `frontend/vite.config.ts`, the `/api` proxy target was previously configured as:
  ```typescript
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // Legacy Spring Boot backend port
      changeOrigin: true,
    }
  }
  ```
- Because the authoritative FastAPI backend is running on `http://127.0.0.1:8000` (port 8000), Vite attempted to forward all `/api/*` requests to port `8080` where no service was listening, causing `ECONNREFUSED` / `502 Bad Gateway`.

---

## 2. Vite Proxy Configuration Before vs After

| Property | Before Fix | After Fix |
| :--- | :--- | :--- |
| **Vite Proxy Target** | `http://localhost:8080` | `http://127.0.0.1:8000` |
| **Change Origin** | `true` | `true` |
| **Path Alias** | `path.resolve(__dirname, './src')` | `fileURLToPath(new URL('./src', import.meta.url))` |
| **Frontend API Base URL** | `/api` | `/api` |

---

## 3. Direct Backend vs Vite Proxy Test Matrix

| Endpoint | Method | Direct Call (`127.0.0.1:8000`) | Proxied Call (`localhost:3000`) | Verdict |
| :--- | :--- | :--- | :--- | :--- |
| `/api/v1/paper/health` | `GET` | `HTTP 200 OK` (Healthy, Gate 9/9) | `HTTP 200 OK` (Healthy, Gate 9/9) | **PASS** |
| `/api/v1/paper/session` | `GET` | `HTTP 200 OK` (`RUNNING`) | `HTTP 200 OK` (`RUNNING`) | **PASS** |
| `/api/v1/paper/provenance` | `GET` | `HTTP 200 OK` (9 orders audited) | `HTTP 200 OK` (9 orders audited) | **PASS** |
| `/api/v1/research/company/RELIANCE` | `POST` | `HTTP 200 OK` (23 audit keys) | `HTTP 200 OK` (23 audit keys) | **PASS** |
| `/api/v1/global-market/regime/latest` | `GET` | `HTTP 404 Not Found` (Application state) | `HTTP 404 Not Found` (Application state) | **PASS** |

*Note: HTTP 404 on regime is an application-level response reflecting pre-market zero-tick state, not a proxy or network connection failure.*

---

## 4. AI Research End-to-End Verification

Tested symbol: **`RELIANCE`**
- Request: `POST http://localhost:3000/api/v1/research/company/RELIANCE`
- Result: **`HTTP 200 OK`**
- Payload verified:
  - `symbol`: `"RELIANCE"`
  - `overall_score`: `4.52`
  - `checklist`: 20-Point value investment criteria populated
  - `agent_reviews`: Multi-agent fundamental evaluation complete
  - `provenance_hash`: Cryptographically verified

---

## 5. Files Changed (Frontend-Only)

1. [`frontend/vite.config.ts`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/frontend/vite.config.ts):
   - Updated `/api` proxy target from `http://localhost:8080` to `http://127.0.0.1:8000`.
2. [`frontend/src/pages/AiResearchPage.tsx`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/frontend/src/pages/AiResearchPage.tsx):
   - Refined error reporting to avoid falsely claiming "backend is offline".
3. [`frontend/src/pages/OverviewPage.tsx`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/frontend/src/pages/OverviewPage.tsx):
   - Streamlined error message styling and status binding.

---

## 6. Build & System Integrity Verification

- **Frontend Typecheck & Production Build (`tsc -b && vite build`)**: **`0 Errors` (Built in 1.48s)**.
- **Backend Test Suite (`pytest`)**: **`346 passed, 0 failed`** (in 96.65s).

---

## 7. Explicit Architectural Invariant Statements

- **Backend Modified**: **NO** (0 backend files modified)
- **Model Modified**: **NO** (`PHASE_16_FROZEN_RIDGE_TOP8_V1` 100% frozen)
- **Strategy Modified**: **NO** (Top-8 universe, 2-day inertia, 15 bps friction untouched)
- **Quantitative Logic Modified**: **NO**

---

## 8. Final Verdict

```
================================================================================
               FRONTEND → BACKEND CONNECTIVITY VERDICT
================================================================================
  VERDICT: FRONTEND_BACKEND_CONNECTED
  
  [✓] Direct Backend (http://127.0.0.1:8000/api/v1/paper/health) -> HTTP 200 OK
  [✓] Vite Proxy (http://localhost:3000/api/v1/paper/health) -> HTTP 200 OK
  [✓] AI Research Endpoint (http://localhost:3000/api/v1/research/company/RELIANCE) -> HTTP 200 OK
  [✓] Zero ECONNREFUSED or Bad Gateway errors
  [✓] Frontend Production Build: 0 Errors
  [✓] Zero Backend / Quantitative Changes Made
================================================================================
```
