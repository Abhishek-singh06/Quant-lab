-- ==========================================================
-- QuantLab Migration V5: Global Market Intelligence Schema
-- ==========================================================

-- 1. Global Market Sources Registry
CREATE TABLE IF NOT EXISTS market_data.global_market_sources (
    id BIGSERIAL PRIMARY KEY,
    source_name VARCHAR(64) NOT NULL UNIQUE,
    source_type VARCHAR(32) NOT NULL, -- EXCHANGE, CENTRAL_BANK, DATA_VENDOR, MOCK
    default_freshness VARCHAR(32) NOT NULL DEFAULT 'DELAYED', -- REAL_TIME, DELAYED, END_OF_DAY, PERIODIC
    delay_minutes INT DEFAULT 15,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    base_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. Global Instrument Master
CREATE TABLE IF NOT EXISTS market_data.global_instruments (
    id BIGSERIAL PRIMARY KEY,
    canonical_symbol VARCHAR(32) NOT NULL UNIQUE,
    provider_symbol VARCHAR(64) NOT NULL,
    instrument_name VARCHAR(255) NOT NULL,
    asset_class VARCHAR(32) NOT NULL, -- EQUITY_INDEX, VOLATILITY_INDEX, FX, BOND_YIELD, COMMODITY, OTHER_MACRO
    market VARCHAR(64) NOT NULL, -- US, JAPAN, HONG_KONG, CHINA, UK, GERMANY, INDIA, GLOBAL
    country VARCHAR(32) NOT NULL,
    exchange_source VARCHAR(64) NOT NULL,
    currency VARCHAR(16) NOT NULL DEFAULT 'USD',
    timezone VARCHAR(64) NOT NULL, -- America/New_York, Asia/Tokyo, Europe/London, etc.
    instrument_type VARCHAR(32) NOT NULL, -- INDEX, VOLATILITY, YIELD, SPOT_FX, COMMODITY_FUTURE
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_gi_canonical ON market_data.global_instruments(canonical_symbol);
CREATE INDEX IF NOT EXISTS idx_gi_asset_class ON market_data.global_instruments(asset_class);
CREATE INDEX IF NOT EXISTS idx_gi_market ON market_data.global_instruments(market);

-- 3. Global Market Snapshots
CREATE TABLE IF NOT EXISTS market_data.global_market_snapshots (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.global_instruments(id),
    canonical_symbol VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL, -- Actual observation / market close time in UTC
    trading_date DATE NOT NULL,
    open_val NUMERIC(20, 6),
    high_val NUMERIC(20, 6),
    low_val NUMERIC(20, 6),
    close_val NUMERIC(20, 6) NOT NULL, -- Price, Index level, Yield, or FX rate
    previous_close NUMERIC(20, 6),
    price_change NUMERIC(20, 6),
    change_percent NUMERIC(10, 4),
    volume BIGINT,
    yield_rate NUMERIC(10, 4),
    currency VARCHAR(16),
    source VARCHAR(64) NOT NULL,
    source_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    data_freshness VARCHAR(32) NOT NULL DEFAULT 'DELAYED', -- REAL_TIME, DELAYED, END_OF_DAY, PERIODIC
    session_status VARCHAR(32) NOT NULL DEFAULT 'CLOSED', -- OPEN, CLOSED, HOLIDAY, WEEKEND, STALE, NOT_AVAILABLE
    data_status VARCHAR(32) NOT NULL DEFAULT 'VALID',
    CONSTRAINT uk_gms_inst_timestamp_src UNIQUE (instrument_id, timestamp, source)
);

CREATE INDEX IF NOT EXISTS idx_gms_inst_id ON market_data.global_market_snapshots(instrument_id);
CREATE INDEX IF NOT EXISTS idx_gms_symbol ON market_data.global_market_snapshots(canonical_symbol);
CREATE INDEX IF NOT EXISTS idx_gms_timestamp ON market_data.global_market_snapshots(timestamp);
CREATE INDEX IF NOT EXISTS idx_gms_trading_date ON market_data.global_market_snapshots(trading_date);
CREATE INDEX IF NOT EXISTS idx_gms_source_time ON market_data.global_market_snapshots(source_timestamp);

-- 4. Global Market Regimes
CREATE TABLE IF NOT EXISTS market_data.global_market_regimes (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL, -- Regime evaluation timestamp (UTC)
    regime_label VARCHAR(32) NOT NULL, -- RISK_ON, RISK_OFF, NEUTRAL, HIGH_VOLATILITY, LOW_VOLATILITY, TRANSITION
    composite_score NUMERIC(8, 4) NOT NULL, -- Normalized -100 to +100
    equity_score NUMERIC(8, 4) NOT NULL,
    volatility_score NUMERIC(8, 4) NOT NULL,
    rates_score NUMERIC(8, 4) NOT NULL,
    dollar_score NUMERIC(8, 4) NOT NULL,
    commodity_score NUMERIC(8, 4) NOT NULL,
    asia_score NUMERIC(8, 4) NOT NULL,
    europe_score NUMERIC(8, 4) NOT NULL,
    confidence VARCHAR(32) NOT NULL DEFAULT 'HIGH', -- HIGH, MEDIUM, LOW
    explanation TEXT NOT NULL,
    methodology_version VARCHAR(32) NOT NULL DEFAULT '1.0.0',
    source_snapshot_count INT NOT NULL DEFAULT 0,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_gmr_timestamp UNIQUE (timestamp)
);

CREATE INDEX IF NOT EXISTS idx_gmr_timestamp ON market_data.global_market_regimes(timestamp);
CREATE INDEX IF NOT EXISTS idx_gmr_regime_label ON market_data.global_market_regimes(regime_label);

-- 5. Global Market Ingestion Runs
CREATE TABLE IF NOT EXISTS market_data.global_market_ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    run_uuid VARCHAR(64) NOT NULL UNIQUE,
    provider VARCHAR(64) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL,
    instruments_requested INT NOT NULL DEFAULT 0,
    observations_received INT NOT NULL DEFAULT 0,
    observations_inserted INT NOT NULL DEFAULT 0,
    duplicates_count INT NOT NULL DEFAULT 0,
    rejected_count INT NOT NULL DEFAULT 0,
    duration_ms BIGINT,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed Core Global Instruments
INSERT INTO market_data.global_instruments (
    canonical_symbol, provider_symbol, instrument_name, asset_class, market, country, exchange_source, currency, timezone, instrument_type
) VALUES
    ('SPX', '^GSPC', 'S&P 500 Index', 'EQUITY_INDEX', 'US', 'USA', 'CBOE/NYSE', 'USD', 'America/New_York', 'INDEX'),
    ('NASDAQ', '^IXIC', 'NASDAQ Composite', 'EQUITY_INDEX', 'US', 'USA', 'NASDAQ', 'USD', 'America/New_York', 'INDEX'),
    ('DJI', '^DJI', 'Dow Jones Industrial Average', 'EQUITY_INDEX', 'US', 'USA', 'NYSE', 'USD', 'America/New_York', 'INDEX'),
    ('RUT', '^RUT', 'Russell 2000 Index', 'EQUITY_INDEX', 'US', 'USA', 'FTSE_RUSSELL', 'USD', 'America/New_York', 'INDEX'),
    ('VIX', '^VIX', 'Cboe Volatility Index', 'VOLATILITY_INDEX', 'US', 'USA', 'CBOE', 'USD', 'America/New_York', 'VOLATILITY'),
    ('DXY', 'DX-Y.NYB', 'US Dollar Index', 'OTHER_MACRO', 'US', 'USA', 'ICE', 'USD', 'America/New_York', 'INDEX'),
    ('US10Y', '^TNX', 'US 10-Year Treasury Yield', 'BOND_YIELD', 'US', 'USA', 'US_TREASURY/CBOE', 'USD', 'America/New_York', 'YIELD'),
    ('USDINR', 'USDINR=X', 'USD/INR Currency Pair', 'FX', 'INDIA', 'IND', 'RBI/FOREX', 'INR', 'Asia/Kolkata', 'SPOT_FX'),
    ('N225', '^N225', 'Nikkei 225', 'EQUITY_INDEX', 'JAPAN', 'JPN', 'TSE', 'JPY', 'Asia/Tokyo', 'INDEX'),
    ('HSI', '^HSI', 'Hang Seng Index', 'EQUITY_INDEX', 'HONG_KONG', 'HKG', 'HKEX', 'HKD', 'Asia/Hong_Kong', 'INDEX'),
    ('SSEC', '000001.SS', 'Shanghai Composite Index', 'EQUITY_INDEX', 'CHINA', 'CHN', 'SSE', 'CNY', 'Asia/Shanghai', 'INDEX'),
    ('FTSE', '^FTSE', 'FTSE 100 Index', 'EQUITY_INDEX', 'UK', 'GBR', 'LSE', 'GBP', 'Europe/London', 'INDEX'),
    ('DAX', '^GDAXI', 'DAX Performance Index', 'EQUITY_INDEX', 'GERMANY', 'DEU', 'XETRA', 'EUR', 'Europe/Berlin', 'INDEX'),
    ('CRUDE_WTI', 'CL=F', 'Crude Oil WTI Futures', 'COMMODITY', 'GLOBAL', 'USA', 'NYMEX', 'USD', 'America/New_York', 'COMMODITY_FUTURE'),
    ('GOLD', 'GC=F', 'Gold Continuous Futures', 'COMMODITY', 'GLOBAL', 'USA', 'COMEX', 'USD', 'America/New_York', 'COMMODITY_FUTURE')
ON CONFLICT (canonical_symbol) DO NOTHING;

-- Seed Sources
INSERT INTO market_data.global_market_sources (source_name, source_type, default_freshness, delay_minutes, is_active, base_url)
VALUES
    ('AUTHORIZED_GLOBAL_FEED', 'DATA_VENDOR', 'DELAYED', 15, TRUE, 'https://api.marketdata.com'),
    ('YAHOO_FINANCE', 'DATA_VENDOR', 'DELAYED', 15, TRUE, 'https://query1.finance.yahoo.com'),
    ('FRED_STLOUIS', 'CENTRAL_BANK', 'END_OF_DAY', 0, TRUE, 'https://api.stlouisfed.org'),
    ('MOCK_GLOBAL_SOURCE', 'MOCK', 'DELAYED', 0, TRUE, 'http://localhost')
ON CONFLICT (source_name) DO NOTHING;
