# QuantLab Historical Data Warehouse (Part 4)

## 1. Overview & Core Philosophy

The QuantLab Historical Data Warehouse is engineered specifically to prevent **Look-Ahead Bias** and **Survivorship Bias** in quantitative backtesting, model training, and investment research on Indian markets.

### Golden Rules

1. **Zero Look-Ahead Bias:** For any historical timestamp $T$, the quantitative feature generation layer may ONLY use observations where $\text{information\_available\_at} \le T$.
2. **Raw Prices Never Overwritten:** Raw OHLCV market prices are stored immutably in `market_data.historical_prices`. Split and dividend adjustments are computed deterministically and stored separately in `market_data.historical_prices_adjusted`.
3. **Point-in-Time Universes (Survivorship Bias Safe):** Index constituents (e.g. NIFTY 50, NIFTY BANK) are queried with `effective_from <= T <= effective_to`. Delisted or historical members are preserved.
4. **Chronological Machine Learning Splitting:** Financial time series are NEVER randomly shuffled. Train, validation, and test splits are strictly chronological. Transformers/scalers fit strictly on training splits.

---

## 2. Architecture & Data Flow

```
Provider (NSE Official / Authorized Vendor / Mock)
                    ↓
  Historical Ingestion & Backfill Engine (Checkpointing & Upserts)
                    ↓
  Raw Historical Warehouse (`market_data.historical_prices`)
                    ↓
  Corporate Action Adjustment Engine (Splits, Bonus, Dividends)
                    ↓
  Adjusted Historical Warehouse (`market_data.historical_prices_adjusted`)
                    ↓
  Point-in-Time Universe Engine (`market_data.index_constituents`)
                    ↓
  Point-in-Time Feature Store (Strict Rolling Windows: T-N+1 .. T)
                    ↓
  Chronological Dataset Splitter & Target Generator
                    ↓
  Model Training & Backtesting Engine
```

---

## 3. Database Schema

### Raw vs. Adjusted Tables

```sql
-- Raw Unadjusted Prices
CREATE TABLE market_data.historical_prices (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL,
    trading_date DATE NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    open_price NUMERIC(18, 4) NOT NULL,
    high_price NUMERIC(18, 4) NOT NULL,
    low_price NUMERIC(18, 4) NOT NULL,
    close_price NUMERIC(18, 4) NOT NULL,
    volume BIGINT NOT NULL,
    granularity VARCHAR(16) NOT NULL DEFAULT 'DAILY',
    source VARCHAR(64) NOT NULL,
    CONSTRAINT uk_hist_price_raw UNIQUE (instrument_id, exchange, trading_date, granularity)
);

-- Adjusted Prices (Separate Table)
CREATE TABLE market_data.historical_prices_adjusted (
    id BIGSERIAL PRIMARY KEY,
    raw_price_id BIGINT NOT NULL REFERENCES market_data.historical_prices(id),
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL,
    trading_date DATE NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    adj_open NUMERIC(18, 4) NOT NULL,
    adj_high NUMERIC(18, 4) NOT NULL,
    adj_low NUMERIC(18, 4) NOT NULL,
    adj_close NUMERIC(18, 4) NOT NULL,
    total_return_close NUMERIC(18, 4),
    cumulative_split_factor NUMERIC(18, 8) NOT NULL DEFAULT 1.0,
    cumulative_dividend_factor NUMERIC(18, 8) DEFAULT 1.0,
    methodology VARCHAR(32) NOT NULL DEFAULT 'SPLIT_ADJUSTED',
    CONSTRAINT uk_hist_price_adj UNIQUE (instrument_id, exchange, trading_date, granularity, methodology)
);
```

### Corporate Actions & Point-in-Time Information Availability

```sql
CREATE TABLE market_data.corporate_actions (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    announcement_date DATE,
    ex_date DATE NOT NULL,
    record_date DATE,
    effective_date DATE,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    adjustment_factor NUMERIC(18, 8),
    dividend_amount NUMERIC(18, 4),
    source VARCHAR(64) NOT NULL
);
```

---

## 4. Corporate Action Adjustment Methodology

For any trading date $t$ and corporate actions with ex-dates $t_{\text{ex}} > t$:

- **Cumulative Split Factor:**
  $$\text{SplitFactor}(t) = \prod_{t_{\text{ex}} > t} \text{Factor}(t_{\text{ex}})$$
  $$\text{AdjClose}(t) = \text{RawClose}(t) \times \text{SplitFactor}(t)$$

- **Total Return Factor (Dividend Reinvestment):**
  $$\text{DivFactor}(t_{\text{ex}}) = 1 - \frac{\text{DividendAmount}}{\text{RawClose}(t_{\text{ex}}-1)}$$
  $$\text{TotalReturnClose}(t) = \text{AdjClose}(t) \times \prod_{t_{\text{ex}} > t} \text{DivFactor}(t_{\text{ex}})$$

---

## 5. Survivorship Bias Prevention

To avoid survivorship bias, researchers query `PointInTimeUniverseService`:

```java
PointInTimeUniverse universe = universeService.getUniverseAsOf("NIFTY 50", LocalDate.of(2018, 6, 1));
```

This returns only the exact 50 stocks that were constituents on **June 1, 2018** (including historical constituents like YES Bank or Zee Entertainment), rather than back-propagating today's constituents to the past.

---

## 6. Look-Ahead Bias Prevention & Automated Leakage Verification

Automated test [`test_lookahead_bias.py`](file:///E:/VS%20CODE%20MAIN/PROJECTS/Quant-lab/quant-service/tests/test_lookahead_bias.py) verifies:

1. **Future Invariance:** Calculates feature vector $X(T)$ at historical timestamp $T = 50$. Appends 50 subsequent future days ($T = 51 \dots 100$). Re-computes $X(T)$ and verifies that all indicators (SMA, EMA, RSI, Volatility, Momentum) remain **100% identical**.
2. **Scaler Isolation:** Feature normalization scalers (`StandardScaler`) are fit strictly on the training partition without test set leakage.
3. **Target Separation:** Forward returns $y(T) = \frac{\text{Close}(T+h) - \text{Close}(T)}{\text{Close}(T)}$ are strictly isolated as target labels and never present in input feature matrices $X$.

---

## 7. REST API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/warehouse/prices/{symbol}/raw?from=...&to=...` | Query immutable raw historical OHLCV prices |
| `GET` | `/api/v1/warehouse/prices/{symbol}/adjusted?from=...&to=...&methodology=SPLIT_ADJUSTED` | Query split-adjusted and total-return series |
| `GET` | `/api/v1/warehouse/universe/{indexSymbol}?asOfDate=...` | Survivorship-bias safe constituent membership on date $T$ |
| `GET` | `/api/v1/warehouse/quality-reports` | Data quality and missing-day gap reports |
| `POST` | `/api/v1/warehouse/backfill` | Execute historical backfill with checkpointing |
