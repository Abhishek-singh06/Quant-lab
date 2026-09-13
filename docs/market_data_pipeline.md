# QuantLab Real Indian Market Data Pipeline (Part 3)

## 1. Overview & Architecture

The QuantLab Real Market Data Ingestion Pipeline is designed for production-grade ingestion of Indian equity and index market data (NSE/BSE).

### Core Pipeline Flow

```
Real Market Data Provider (e.g. Authorized NSE Feed)
            ↓
  MarketDataProvider (Interface)
            ↓
  Provider Adapter (e.g. NSEMarketDataProvider)
            ↓
  MarketDataNormalizer (UTC, Clean Symbols, Scaled Numerics)
            ↓
  MarketDataValidator (OHLC Consistency, Positive Prices/Volumes)
            ↓
  StaleDataDetector (Session-aware delay threshold)
            ↓
  DuplicateDetector (Composite Key & In-memory/DB Idempotency)
            ↓
  MissingDataDetector (Trading Calendar & Session-aware Gaps)
            ↓
  PostgreSQL (market_data.market_quotes & Ingestion Runs)
            ↓
  AlertService (Warning/Error Notifications for Anomalies)
            ↓
  MarketDataQueryService & REST APIs (/api/v1/market-data/*)
```

---

## 2. MarketDataProvider Abstraction

The entire application communicates solely with the generic `MarketDataProvider` interface:

```java
public interface MarketDataProvider {
    String getProviderName();
    boolean isAvailable();
    MarketQuote getQuote(String symbol, Exchange exchange);
    List<MarketQuote> getQuotes(List<String> symbols, Exchange exchange);
    List<HistoricalCandle> getHistoricalData(String symbol, Exchange exchange, Instant from, Instant to, String interval);
    List<String> getSymbols(Exchange exchange);
    MarketStatusInfo getMarketStatus(Exchange exchange);
}
```

No NSE-specific formats or API logic leak outside of the adapter layer.

---

## 3. Supported Providers & Adapters

| Provider | Class | Purpose | Production Ready |
|----------|-------|---------|------------------|
| **NSE** | `NSEMarketDataProvider` | Connects to authorized NSE APIs/feeds using token-bucket rate limits and exponential backoff retries. | Yes (requires licensed credentials) |
| **MOCK** | `MockMarketDataProvider` | Isolated test/dev provider generating deterministic quotes with `source="MOCK"`. | Test/Dev Only |

---

## 4. How to Add a New Market Data Provider

To add a new data vendor (e.g. Refinitiv, Bloomberg, Interactive Brokers):

1. **Implement `MarketDataProvider`:**
   ```java
   @Component("bloombergMarketDataProvider")
   public class BloombergMarketDataProvider implements MarketDataProvider {
       // Implement getQuote, getQuotes, getHistoricalData, etc.
   }
   ```
2. **Register configuration in `MarketDataProperties`:**
   ```java
   private BloombergConfig bloomberg = new BloombergConfig();
   ```
3. **Add provider key in `MarketDataProviderFactory`:**
   ```java
   case "BLOOMBERG":
       return bloombergProvider;
   ```
4. **Configure in `.env`:**
   ```env
   MARKET_DATA_PROVIDER=BLOOMBERG
   ```

**Zero changes are required to the normalizer, validator, deduplication, storage, analytics, or UI layers.**

---

## 5. Normalization, Validation & Quality Rules

### Normalization
- **Timestamps:** Canonicalized to UTC (`Instant`), while source timestamp is preserved.
- **Symbols:** Stripped, uppercase, index aliases mapped (`NIFTY50` -> `NIFTY 50`).
- **Math:** Missing price change and percent change automatically derived if last and previous close are present.

### Validation
- **Symbol & Exchange:** Non-empty, alphanumeric pattern, valid exchange enum.
- **Prices:** Must be strictly positive (> 0).
- **Volume:** Must be non-negative (>= 0).
- **OHLC Consistency:**
  - `High >= Low`
  - `High >= Open`
  - `High >= Close`
  - `Low <= Open`
  - `Low <= Close`
- **Future Timestamp:** Rejects ticks with timestamps ahead of server time + tolerance window.
- **Quarantine:** Invalid records are stored in `market_data.market_data_errors` with exact failure reasons.

---

## 6. Duplicate & Missing Data Detection

- **Idempotency Key:** Composite unique key `(symbol, exchange, timestamp, data_type)`.
- **Trading Calendar:** `IndianTradingCalendar` models NSE trading hours (09:15 to 15:30 IST), weekends, and official Indian national holidays. Missing data is never flagged during closed sessions.
- **Stale Data:** Compares source timestamp age against `staleThresholdSeconds` during open market hours.

---

## 7. PostgreSQL Database Schema

```sql
-- Main Quotes Table
CREATE TABLE market_data.market_quotes (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL,
    isin VARCHAR(16),
    data_type VARCHAR(16) NOT NULL DEFAULT 'QUOTE',
    last_price NUMERIC(18, 4),
    open_price NUMERIC(18, 4),
    high_price NUMERIC(18, 4),
    low_price NUMERIC(18, 4),
    close_price NUMERIC(18, 4),
    prev_close_price NUMERIC(18, 4),
    price_change NUMERIC(18, 4),
    change_percent NUMERIC(10, 4),
    volume BIGINT,
    total_traded_value NUMERIC(24, 4),
    open_interest BIGINT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    source_timestamp TIMESTAMP WITH TIME ZONE,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    source VARCHAR(64) NOT NULL,
    run_id VARCHAR(64),
    status VARCHAR(20) NOT NULL DEFAULT 'VALID',
    CONSTRAINT uk_quote_symbol_exchange_timestamp UNIQUE (symbol, exchange, timestamp, data_type)
);

-- Ingestion Runs Audit Table
CREATE TABLE market_data.market_data_ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    provider VARCHAR(64) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL,
    records_received INT,
    records_accepted INT,
    records_rejected INT,
    duplicates_count INT,
    missing_count INT,
    stale_count INT,
    error_count INT,
    duration_ms BIGINT
);
```

---

## 8. REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/market-data/quotes/{symbol}` | Get latest stored quote for a symbol |
| `GET` | `/api/v1/market-data/quotes?symbols=RELIANCE,TCS` | Bulk quote lookup |
| `GET` | `/api/v1/market-data/history/{symbol}?from=...&to=...` | Historical candles from provider |
| `GET` | `/api/v1/market-data/status` | Current Indian market trading status |
| `POST` | `/api/v1/market-data/ingest` | Trigger manual or scheduled ingestion run |
| `GET` | `/api/v1/market-data/runs` | List recent ingestion audit runs |
| `GET` | `/api/v1/market-data/runs/{runId}` | Ingestion run detail and metrics |
