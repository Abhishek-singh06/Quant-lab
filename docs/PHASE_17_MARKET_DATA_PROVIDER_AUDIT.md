# Phase 17 — Market Data Provider Audit

**Audit Date:** September 13, 2026  
**Auditor:** Quantitative Market Data Engineering

---

## 1. Provider Capability & Discovery Matrix

| Capability | Yahoo Finance Indian Adapter | AMFI Mutual Fund Adapter | Zerodha Kite Adapter | Official Exchange Streaming |
| :--- | :--- | :--- | :--- | :--- |
| **HISTORICAL_OHLCV** | **VERIFIED** | NOT_SUPPORTED | CONFIGURATION_REQUIRED | CONFIGURATION_REQUIRED |
| **HISTORICAL_BARS** | **VERIFIED** | NOT_SUPPORTED | CONFIGURATION_REQUIRED | CONFIGURATION_REQUIRED |
| **REALTIME_QUOTES** | NOT_SUPPORTED | NOT_SUPPORTED | CONFIGURATION_REQUIRED | CONFIGURATION_REQUIRED |
| **STREAMING_QUOTES** | NOT_SUPPORTED | NOT_SUPPORTED | CONFIGURATION_REQUIRED | CONFIGURATION_REQUIRED |
| **DELAYED_QUOTES** | **VERIFIED** (~15 min delayed) | NOT_SUPPORTED | CONFIGURATION_REQUIRED | NOT_SUPPORTED |
| **EOD_QUOTES** | **VERIFIED** | **VERIFIED** (NAV) | CONFIGURATION_REQUIRED | CONFIGURATION_REQUIRED |
| **MARKET_STATUS** | **VERIFIED** (via Calendar) | NOT_SUPPORTED | CONFIGURATION_REQUIRED | CONFIGURATION_REQUIRED |
| **CORPORATE_ACTIONS** | **VERIFIED** | NOT_SUPPORTED | CONFIGURATION_REQUIRED | CONFIGURATION_REQUIRED |
| **INSTRUMENT_MASTER** | NOT_SUPPORTED | NOT_SUPPORTED | CONFIGURATION_REQUIRED | CONFIGURATION_REQUIRED |

---

## 2. Distinction: Real-Time vs Delayed vs Replay

- **REALTIME_STREAMING**: `NOT_VERIFIED`  
  QuantLab does not currently possess an active, authenticated direct exchange market-data socket connection.
- **DELAYED_MARKET_DATA**: `VERIFIED`  
  Public Yahoo Finance endpoints provide ~15-minute delayed quotes and daily historical bars for NSE equities.
- **POLLING_QUOTES**: `VERIFIED`  
  Periodic polling adapter is available with rate-limiting and backoff controls.
- **HISTORICAL_REPLAY**: `VERIFIED`  
  Replay engine deterministically plays through historical ticks and bars using `REPLAY_CLOCK`.

> [!WARNING]
> Polling or replay feeds are never labeled as "Real-Time Streaming" in QuantLab. All delayed paper sessions are labeled `DELAYED_PAPER_TRADING`.

---

## 3. Compliance & Terms of Service Assurance

1. **No Scraping**: No HTML scrapers, DOM parsers, or headless browsers are used to bypass rate limits or access controls.
2. **No CAPTCHA Bypass**: System relies exclusively on structured API interfaces.
3. **Fail-Safe Gating**: Any provider response outside schema bounds triggers fail-closed quarantine.
