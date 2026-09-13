# QuantLab — Global Market Intelligence Architecture

## 1. Overview
The Global Market Intelligence subsystem ingests, normalizes, and models international financial market data across 15 canonical assets (US equities, volatility, yields, currencies, commodities, European & Asian benchmarks). It derives point-in-time **GlobalMarketSnapshot** and **GlobalMarketRegime** datasets for feature engineering and quantitative decision support.

---

## 2. Global Asset Coverage Matrix

| Canonical Symbol | Provider Symbol | Asset Class | Market / Exchange | Timezone | Observation Format |
|---|---|---|---|---|---|
| **`SPX`** | `^GSPC` | `EQUITY_INDEX` | US / CBOE/NYSE | `America/New_York` | Index Level / OHLC |
| **`NASDAQ`** | `^IXIC` | `EQUITY_INDEX` | US / NASDAQ | `America/New_York` | Index Level / OHLC |
| **`DJI`** | `^DJI` | `EQUITY_INDEX` | US / NYSE | `America/New_York` | Index Level / OHLC |
| **`RUT`** | `^RUT` | `EQUITY_INDEX` | US / FTSE Russell | `America/New_York` | Index Level / OHLC |
| **`VIX`** | `^VIX` | `VOLATILITY_INDEX`| US / CBOE | `America/New_York` | Volatility Level |
| **`DXY`** | `DX-Y.NYB` | `OTHER_MACRO` | US / ICE | `America/New_York` | Currency Index Level |
| **`US10Y`** | `^TNX` | `BOND_YIELD` | US / US Treasury | `America/New_York` | Yield Rate (%) |
| **`USDINR`** | `USDINR=X`| `FX` | INDIA / RBI/Forex | `Asia/Kolkata` | Spot Exchange Rate |
| **`N225`** | `^N225` | `EQUITY_INDEX` | JAPAN / TSE | `Asia/Tokyo` | Index Level / OHLC |
| **`HSI`** | `^HSI` | `EQUITY_INDEX` | HONG KONG / HKEX | `Asia/Hong_Kong` | Index Level / OHLC |
| **`SSEC`** | `000001.SS`| `EQUITY_INDEX` | CHINA / SSE | `Asia/Shanghai` | Index Level / OHLC |
| **`FTSE`** | `^FTSE` | `EQUITY_INDEX` | UK / LSE | `Europe/London` | Index Level / OHLC |
| **`DAX`** | `^GDAXI` | `EQUITY_INDEX` | GERMANY / XETRA | `Europe/Berlin` | Index Level / OHLC |
| **`CRUDE_WTI`**| `CL=F` | `COMMODITY` | GLOBAL / NYMEX | `America/New_York` | Futures Price ($/bbl) |
| **`GOLD`** | `GC=F` | `COMMODITY` | GLOBAL / COMEX | `America/New_York` | Futures Price ($/oz) |

---

## 3. Critical Financial Integrity & Timezone Invariants

### 3.1 Timezone-Aware Session Alignment
- Observations maintain UTC timestamps alongside their source local time zones.
- Trading hours, holidays, and weekends are evaluated per financial center (`GlobalMarketCalendarService`).
- **Indian Market Open Invariant (09:15 IST / 03:45 UTC):** Features calculated at 09:15 IST use the prior day's US close (~21:00 UTC) and the live Asian morning session. Under no circumstances can a future US or European session close leak into an Indian morning snapshot.

### 3.2 Provenance & Freshness Transparency
Every observation exposes:
- **`source_timestamp`**: Timestamp of observation in the source market.
- **`ingestion_timestamp`**: When our system recorded the data.
- **`data_freshness`**: Explicitly labeled `REAL_TIME`, `DELAYED` (e.g. 15-min delay), `END_OF_DAY`, or `PERIODIC`.
- **`age_minutes`**: Elapsed time since source observation.

---

## 4. Deterministic Global Market Regime Engine

The **GlobalMarketRegime** is a derived, transparent analytical metric calculated using factor attribution:

$$\text{Composite Score} = 0.25 \cdot \text{Eq} + 0.20 \cdot \text{Vol} + 0.15 \cdot \text{Dollar} + 0.10 \cdot \text{Rates} + 0.10 \cdot \text{Comm} + 0.10 \cdot \text{Asia} + 0.10 \cdot \text{Europe}$$

- **Scale:** Normalized $[-100.0, +100.0]$.
- **Regimes:** `RISK_ON` ($> +25$), `RISK_OFF` ($< -25$), `HIGH_VOLATILITY` ($\text{VIX} > 24$), `LOW_VOLATILITY` ($\text{VIX} < 14$), `NEUTRAL`, `TRANSITION`.
- **Confidence:** `HIGH` ($\ge 12$ assets), `MEDIUM` ($7-11$ assets), `LOW` ($< 7$ assets).
- **Explainability:** Transparent factor breakdown and natural language rationale stored with every regime record.

---

## 5. API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/global/ingest` | Trigger global market ingestion run |
| `GET` | `/api/v1/global/instruments` | List global instrument master |
| `GET` | `/api/v1/global/snapshots/latest` | Latest cross-asset global market snapshot |
| `GET` | `/api/v1/global/snapshots/history/{symbol}` | Historical snapshot series with date filtering |
| `GET` | `/api/v1/global/regime/latest` | Latest point-in-time global market regime |
| `GET` | `/api/v1/global/regime/history` | Historical regime series |
| `GET` | `/api/v1/global/status` | Real-time global market session & holiday statuses |

---

## 6. Verification & Automated Test Suite

- **Python Quant Service:** 27/27 unit & look-ahead bias tests passing in [`tests/test_global_lookahead_bias.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/tests/test_global_lookahead_bias.py).
- **Java Unit Tests:** [`GlobalMarketCalendarTest.java`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/backend/src/test/java/com/quantlab/global/GlobalMarketCalendarTest.java), [`GlobalMarketRegimeEngineTest.java`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/backend/src/test/java/com/quantlab/global/GlobalMarketRegimeEngineTest.java), [`GlobalMarketIntelligenceControllerTest.java`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/backend/src/test/java/com/quantlab/global/GlobalMarketIntelligenceControllerTest.java).
- **Frontend Web App:** TypeScript compilation and production packaging verified via `npm run build`.
