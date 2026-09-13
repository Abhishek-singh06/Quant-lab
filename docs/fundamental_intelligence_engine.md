# QuantLab Part 8 — Fundamental Intelligence Engine

## 1. Overview
The **Fundamental Intelligence Engine** provides high-precision, point-in-time safe corporate financial reporting, statement normalization, ratio derivation, and valuation analytics for Indian equities listed on the NSE/BSE.

Under strict financial econometric requirements, fundamental disclosures are never treated as instantaneous. Financial statements released for period ending $T_{\text{period}}$ (e.g. Q4 ended March 31) are published at $T_{\text{published}}$ (e.g. May 15). All features and data points are timestamped with `available_at` and indexed so that backtesting and ML models cannot peek into future earnings.

---

## 2. Architecture & Data Flow

```
Corporate Filings (NSE/BSE / Licensed Feed / XBRL)
                   ↓
         FundamentalDataProvider
  (NSEFundamentalDataProvider / MockProvider)
                   ↓
         FundamentalFiling
                   ↓
   FinancialStatementNormalizerService
   (Validates Balance Sheet & Quality)
                   ↓
    FinancialRatioCalculatorService
 (EBITDA Margin, ROE, ROCE, P/E, P/B, EV/EBITDA)
                   ↓
           PostgreSQL (V6)
 (fundamental_sources, fundamental_filings,
  financial_statements, financial_ratios,
  fundamental_ingestion_runs)
                   ↓
     REST API (/api/v1/fundamentals/*)
                   ↓
  Python Point-in-Time Feature Store
(FundamentalFeatureGenerator with zero look-ahead bias)
```

---

## 3. Database Schema (Migration `V6`)

### `fundamental_filings`
- `id` (BIGSERIAL PRIMARY KEY)
- `instrument_id` (BIGINT NOT NULL REFERENCES instruments(id))
- `symbol` (VARCHAR(30) NOT NULL)
- `filing_type` (VARCHAR(30) - FINANCIAL_RESULT, ANNUAL_REPORT, SHAREHOLDING_PATTERN)
- `fiscal_year` (VARCHAR(10))
- `fiscal_quarter` (VARCHAR(10))
- `period_start` (DATE NOT NULL)
- `period_end` (DATE NOT NULL)
- `period_type` (VARCHAR(20) - QUARTERLY, ANNUAL, HALF_YEARLY)
- `reporting_basis` (VARCHAR(20) - CONSOLIDATED, STANDALONE)
- `audit_status` (VARCHAR(20) - AUDITED, UNAUDITED, LIMITED_REVIEW)
- `published_at` (TIMESTAMPTZ NOT NULL)
- `available_at` (TIMESTAMPTZ NOT NULL)
- `version` (INT DEFAULT 1)
- `is_restatement` (BOOLEAN DEFAULT FALSE)
- `restatement_reason` (TEXT)
- `data_quality_score` (VARCHAR(20))

### `financial_statements`
- `id` (BIGSERIAL PRIMARY KEY)
- `filing_id` (BIGINT REFERENCES fundamental_filings(id))
- **Income Statement**: `revenue`, `operating_profit`, `ebitda`, `ebit`, `interest_expense`, `depreciation_amortization`, `profit_before_tax`, `tax_expense`, `net_profit`, `basic_eps`, `diluted_eps`
- **Balance Sheet**: `total_assets`, `current_assets`, `cash_and_equivalents`, `inventory`, `trade_receivables`, `total_liabilities`, `current_liabilities`, `total_debt`, `short_term_debt`, `long_term_debt`, `total_equity`, `retained_earnings`
- **Cash Flow**: `operating_cash_flow`, `investing_cash_flow`, `financing_cash_flow`, `capital_expenditure`, `free_cash_flow`
- **Sector Specific**: `sector_specific_metrics` (JSONB)

### `financial_ratios`
- Margins: `ebitda_margin`, `ebit_margin`, `net_profit_margin`
- Return Ratios: `roe`, `roce`, `roa`, `asset_turnover`
- Solvency & Coverage: `debt_to_equity`, `net_debt_to_ebitda`, `interest_coverage`, `current_ratio`
- Growth: `revenue_growth_yoy`, `ebitda_growth_yoy`, `profit_growth_yoy`, `eps_growth_yoy`
- Valuation: `market_cap_crores`, `enterprise_value_crores`, `pe_ratio`, `pb_ratio`, `ev_ebitda`, `ev_sales`

---

## 4. Point-in-Time Look-Ahead Invariants

1. **Publication Lag Barrier**: Statements are only included where `available_at <= feature_timestamp`.
2. **Historical Feature Invariance**: Queries evaluated at $T_1$ will always return identical features even after subsequent earnings are published at $T_2 > T_1$.
3. **Restatement Versioning**: Restatements published at $T_{\text{restatement}}$ do not overwrite or alter historical feature values evaluated prior to $T_{\text{restatement}}$.
4. **TTM Look-Ahead Invariance**: Trailing-twelve-months calculations strictly sum the 4 most recent distinct quarters available as of the evaluation timestamp.
5. **Reporting Basis Isolation**: Standalone and Consolidated metrics are partitioned without cross-contamination.

---

## 5. REST Endpoints

- `GET /api/v1/fundamentals/company/{symbol}?asOf=...` — Returns full company fundamental snapshot, provenance tags, statement history, and key ratios.
- `GET /api/v1/fundamentals/statements/{symbol}?periodType=QUARTERLY&reportingBasis=CONSOLIDATED&asOf=...` — Returns financial statement time series.
- `GET /api/v1/fundamentals/ratios/{symbol}?asOf=...` — Returns financial ratios time series.
- `GET /api/v1/fundamentals/filings/{symbol}?asOf=...` — Returns raw filing history.
- `POST /api/v1/fundamentals/ingest/{symbol}` — Triggers on-demand ingestion and ratio recalculation.
- `POST /api/v1/fundamentals/ingest-all` — Triggers batch ingestion for active universe.
