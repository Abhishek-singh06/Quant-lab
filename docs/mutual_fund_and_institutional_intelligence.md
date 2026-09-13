# QuantLab — Mutual Fund & Institutional Intelligence Architecture

## 1. Overview
The Mutual Fund & Institutional Intelligence subsystem ingests, models, and exposes institutional holding data, FII/DII daily cash flows, and quarterly shareholding patterns for Indian capital markets.

---

## 2. Critical Financial Integrity Rules

### 2.1 Periodic vs Real-Time Data Segregation
Institutional and mutual fund disclosures are **periodic** (monthly/quarterly), never tick-by-tick real-time. The system mandates that every record and UI representation explicitly communicates:
- **`data_as_of`**: The balance sheet / portfolio snapshot date (e.g., `2026-07-31`).
- **`published_at`**: The calendar date on which the disclosure was officially released by AMFI/NSE (e.g., `2026-08-15`).
- **`available_at`**: The UTC timestamp at which the data entered the public domain.
- **`source`**: The authoritative filing registry (`AMFI_INDIA`, `NSE_INDIA`).
- **`age_days`**: Elapsed days from `data_as_of` to present day (e.g., `28 days`).

### 2.2 Point-in-Time Zero Look-Ahead Bias
All quantitative feature generators and analytics queries require an `asOf` timestamp filter:
$$\text{available\_at} \le t_{\text{asOf}}$$
- A July 31 portfolio published August 15 is **invisible** to any backtest, model feature, or query evaluated prior to August 15.
- Re-running feature extraction on historical dates produces immutable, reproducible vectors invariant to subsequent data insertions.

### 2.3 Safe Position Change Classification
- **Percentage Point Change ($\Delta \text{pp}$):** $\text{weight}_{t} - \text{weight}_{t-1}$
- **Relative % Change:** $\frac{\text{weight}_{t} - \text{weight}_{t-1}}{\text{weight}_{t-1}} \times 100\%$
- **Exits:** A security present in disclosure $t-1$ and absent in disclosure $t$ is classified as `EXITED_POSITION` **only if** disclosure $t$ has `portfolio_scope = COMPLETE`. If disclosure $t$ is partial (e.g., `TOP_10_ONLY`), absent securities are never falsely recorded as exits.

---

## 3. Database Schema (market_data)

```
+-----------------------------------------------------------------------------------+
| amc_masters (id, amc_code, amc_name, sebi_reg_no, amfi_code, website)             |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| mutual_fund_schemes (id, amc_id, scheme_code, scheme_name, isin, aum_crores, ...)  |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| fund_portfolio_disclosures (id, scheme_id, data_as_of, published_at, available_at)|
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| fund_holdings (id, disclosure_id, scheme_id, symbol, weight, weight_change_pp)    |
+-----------------------------------------------------------------------------------+

+-----------------------------------------------------------------------------------+
| institutional_flows (id, trade_date, institution_type, buy_value, sell_value, ...) |
+-----------------------------------------------------------------------------------+

+-----------------------------------------------------------------------------------+
| institutional_ownership (id, instrument_id, symbol, period_end, ownership_pct)    |
+-----------------------------------------------------------------------------------+
```

---

## 4. API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/institutional/ingest` | Trigger institutional data ingestion pipeline |
| `GET` | `/api/v1/institutional/ownership/{symbol}` | Point-in-time institutional shareholding & top MF holders |
| `GET` | `/api/v1/institutional/portfolio/{schemeCode}` | Point-in-time mutual fund portfolio disclosure & holdings |
| `GET` | `/api/v1/institutional/flows` | Daily FII and DII net cash flows with date filtering |
| `GET` | `/api/v1/institutional/schemes` | Master registry of mutual fund schemes |

---

## 5. Quantitative Feature Generator (`quant-service`)

The [`InstitutionalFeatureGenerator`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/app/warehouse/institutional_feature_generator.py) produces point-in-time quantitative signals:
1. `total_funds_holding`: Active mutual fund count holding the asset.
2. `funds_increasing_stake` / `funds_decreasing_stake`: Net accumulation/distribution breadth.
3. `fii_net_flow_5d_cr` / `fii_net_flow_20d_cr`: Short and medium-term FII liquidity impulses.
4. `dii_net_flow_5d_cr` / `dii_net_flow_20d_cr`: Domestic institutional liquidity absorption.
5. `flow_divergence_20d_cr`: FII vs DII institutional tug-of-war spread.
6. `fii_ownership_pct` / `dii_ownership_pct` / `total_institutional_pct`: Quarterly institutional ownership base.
7. `mf_disclosure_age_days` / `ownership_disclosure_age_days`: Explicit disclosure staleness signals.

---

## 6. Verification & Automated Test Suite

- **Python Quant Service:** 22/22 unit and look-ahead bias tests passing in [`tests/test_institutional_lookahead_bias.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/tests/test_institutional_lookahead_bias.py).
- **Backend Service Unit Tests:** [`HoldingChangeDetectionTest.java`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/backend/src/test/java/com/quantlab/institutional/HoldingChangeDetectionTest.java) and [`InstitutionalIntelligenceControllerTest.java`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/backend/src/test/java/com/quantlab/institutional/InstitutionalIntelligenceControllerTest.java).
- **Frontend App:** TypeScript compilation and production packaging verified via `npm run build`.
