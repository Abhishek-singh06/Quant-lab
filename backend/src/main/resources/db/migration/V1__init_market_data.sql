-- ==========================================================
-- QuantLab Migration V1: Market Data Pipeline Schema
-- ==========================================================

CREATE SCHEMA IF NOT EXISTS market_data;

-- 1. Data Sources Registry
CREATE TABLE IF NOT EXISTS market_data.market_data_sources (
    id BIGSERIAL PRIMARY KEY,
    source_name VARCHAR(64) NOT NULL UNIQUE,
    source_type VARCHAR(32) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    rate_limit_per_min INT DEFAULT 60,
    base_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

-- 2. Ingestion Runs Tracking
CREATE TABLE IF NOT EXISTS market_data.market_data_ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    provider VARCHAR(64) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL DEFAULT 'RUNNING',
    records_received INT NOT NULL DEFAULT 0,
    records_accepted INT NOT NULL DEFAULT 0,
    records_rejected INT NOT NULL DEFAULT 0,
    duplicates_count INT NOT NULL DEFAULT 0,
    missing_count INT NOT NULL DEFAULT 0,
    stale_count INT NOT NULL DEFAULT 0,
    error_count INT NOT NULL DEFAULT 0,
    retry_count INT NOT NULL DEFAULT 0,
    duration_ms BIGINT,
    error_message VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ingestion_provider ON market_data.market_data_ingestion_runs(provider);
CREATE INDEX IF NOT EXISTS idx_ingestion_status ON market_data.market_data_ingestion_runs(status);
CREATE INDEX IF NOT EXISTS idx_ingestion_start_time ON market_data.market_data_ingestion_runs(start_time);

-- 3. Market Quotes (Idempotent & Unique Key)
CREATE TABLE IF NOT EXISTS market_data.market_quotes (
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
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_quote_symbol_exchange_timestamp UNIQUE (symbol, exchange, timestamp, data_type)
);

CREATE INDEX IF NOT EXISTS idx_quotes_symbol_exchange ON market_data.market_quotes(symbol, exchange);
CREATE INDEX IF NOT EXISTS idx_quotes_timestamp ON market_data.market_quotes(timestamp);
CREATE INDEX IF NOT EXISTS idx_quotes_run_id ON market_data.market_quotes(run_id);
CREATE INDEX IF NOT EXISTS idx_quotes_status ON market_data.market_quotes(status);

-- 4. Market Data Errors / Quarantined Records
CREATE TABLE IF NOT EXISTS market_data.market_data_errors (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64),
    provider VARCHAR(64) NOT NULL,
    symbol VARCHAR(32),
    source_timestamp TIMESTAMP WITH TIME ZONE,
    error_category VARCHAR(32) NOT NULL,
    reason VARCHAR(1024) NOT NULL,
    raw_payload TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_errors_run_id ON market_data.market_data_errors(run_id);
CREATE INDEX IF NOT EXISTS idx_errors_symbol ON market_data.market_data_errors(symbol);
CREATE INDEX IF NOT EXISTS idx_errors_category ON market_data.market_data_errors(error_category);
CREATE INDEX IF NOT EXISTS idx_errors_created_at ON market_data.market_data_errors(created_at);

-- Seed initial data sources
INSERT INTO market_data.market_data_sources (source_name, source_type, enabled, is_primary, rate_limit_per_min, base_url)
VALUES 
    ('NSE', 'EXCHANGE', TRUE, TRUE, 60, 'https://api.nseindia.com'),
    ('MOCK', 'MOCK', TRUE, FALSE, 1000, 'http://localhost')
ON CONFLICT (source_name) DO NOTHING;
