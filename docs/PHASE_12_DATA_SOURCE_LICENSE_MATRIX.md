# Phase 12 Data Source License & Terms Matrix

**Platform**: QuantLab (`E:/VS CODE MAIN/PROJECTS/Quant-lab`)  
**Date**: September 2026  
**Auditor**: QuantLab Legal & Quantitative Engineering Audit  

---

## 1. Provider Evaluation & Licensing Analysis

| Source / Provider | Data Domain | Access Method | Auth Required | Commercial Use | Redistribution | Rate Limits | Historical Depth | PIT Fields Available | QuantLab Status |
|---|---|---|---|---|---|---|---|---|---|
| **Yahoo Finance (Chart API)** | NSE/BSE Equity OHLCV, Splits, Dividends | HTTPS REST API (`query1.finance.yahoo.com`) | None / Cookie Session | Non-commercial / Research only | No | Reasonable (2000 req/hr) | 15–20+ Years (Daily) | Split Date, Ex-Div Date, Timestamps | **CONFIGURED (Research)** |
| **AMFI India Open Portal** | Indian Mutual Fund Historical NAVs & Scheme Master | Official HTTP Open Endpoints (`amfiindia.com`) | None (Public Domain) | Permitted for Analytics | Permitted with Attribution | High / Open Batch | 2006–Present | Explicit NAV Date, Scheme Code | **CONFIGURED (Production)** |
| **NSE Official Gateway** | NSE Live Quotes, Market Status, Bhavcopy | Authorized REST API (`api.nseindia.com`) | API Key / Secret (or Session Auth) | Requires Exchange License | Restricted by License | 60 req/min (Token Bucket) | 10+ Years (Daily/Intraday) | Trade Date, Announcement Date, As-Of | **CONFIGURED (Licensed Mode)** |
| **BSE Official Gateway** | BSE Scrip Master, Corporate Actions | Official REST / CSV Endpoints (`bseindia.com`) | API Key / Session | Requires Exchange License | Restricted by License | 60 req/min | 10+ Years | Ex-Date, Record Date | **CONFIGURED (Licensed Mode)** |
| **FRED / RBI (DBIE)** | Macro Yields, INR/USD, Repo Rate, Crude | Official REST API (`api.stlouisfed.org`) | API Key (Free Tier) | Permitted (Open Data) | Permitted with Citation | 120 req/min | 30+ Years | Release Date, Observation Date | **CONFIGURED (Open Data)** |
| **SEBI Regulatory Filings** | Insider Trading, Substantial Acquisitions, SAST | Official Regulatory Disclosures (`sebi.gov.in`) | None (Public Disclosures) | Permitted | Public Record | Respectful Crawl Rate | 10+ Years | Filing Date, Dissemination Date | **SCHEMA READY** |

---

## 2. IP & Clean-Room Compliance Declaration

1. **Zero Unauthorized Scraping**: QuantLab does NOT bypass CAPTCHAs, paywalls, or authentication barriers.
2. **Strict Credential Isolation**: All credentials, tokens, and API secrets are strictly loaded via `.env` / environment variables. Zero credentials are committed to version control.
3. **Fail-Closed Mode**: If a `REAL_DATA` provider is requested and credentials are missing or the service is unreachable, QuantLab raises an explicit `ProviderUnavailableException` or `AuthenticationException` rather than fabricating fake prices.
4. **Point-in-Time Tagging**: Every raw and normalized record stores `source`, `source_timestamp`, `retrieval_timestamp`, and `information_available_at`.
