-- ==========================================================
-- QuantLab Migration V2: Historical Data Warehouse Schema
-- ==========================================================

-- 1. Master Instruments
CREATE TABLE IF NOT EXISTS market_data.instruments (
    id BIGSERIAL PRIMARY KEY,
    isin VARCHAR(16) UNIQUE,
    exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
    current_symbol VARCHAR(32) NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    listing_date DATE,
    delisting_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    sector VARCHAR(64),
    industry VARCHAR(64),
    is_index BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_inst_current_symbol ON market_data.instruments(current_symbol);
CREATE INDEX IF NOT EXISTS idx_inst_isin ON market_data.instruments(isin);
CREATE INDEX IF NOT EXISTS idx_inst_exchange ON market_data.instruments(exchange);
CREATE INDEX IF NOT EXISTS idx_inst_status ON market_data.instruments(status);

-- 2. Time-Aware Instrument History (Symbol renames & changes)
CREATE TABLE IF NOT EXISTS market_data.instrument_history (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    company_name VARCHAR(255),
    valid_from DATE NOT NULL,
    valid_to DATE,
    change_reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_inst_hist_inst_id ON market_data.instrument_history(instrument_id);
CREATE INDEX IF NOT EXISTS idx_inst_hist_symbol ON market_data.instrument_history(symbol);
CREATE INDEX IF NOT EXISTS idx_inst_hist_dates ON market_data.instrument_history(valid_from, valid_to);

-- 3. Raw Historical Prices (Unadjusted)
CREATE TABLE IF NOT EXISTS market_data.historical_prices (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
    trading_date DATE NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    open_price NUMERIC(18, 4) NOT NULL,
    high_price NUMERIC(18, 4) NOT NULL,
    low_price NUMERIC(18, 4) NOT NULL,
    close_price NUMERIC(18, 4) NOT NULL,
    volume BIGINT NOT NULL,
    total_traded_value NUMERIC(24, 4),
    granularity VARCHAR(16) NOT NULL DEFAULT 'DAILY',
    source VARCHAR(64) NOT NULL,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    data_version INT NOT NULL DEFAULT 1,
    CONSTRAINT uk_hist_price_raw UNIQUE (instrument_id, exchange, trading_date, granularity)
);

CREATE INDEX IF NOT EXISTS idx_hist_raw_inst_date ON market_data.historical_prices(instrument_id, trading_date);
CREATE INDEX IF NOT EXISTS idx_hist_raw_symbol_date ON market_data.historical_prices(symbol, trading_date);
CREATE INDEX IF NOT EXISTS idx_hist_raw_date ON market_data.historical_prices(trading_date);

-- 4. Adjusted Historical Prices (Separate from Raw)
CREATE TABLE IF NOT EXISTS market_data.historical_prices_adjusted (
    id BIGSERIAL PRIMARY KEY,
    raw_price_id BIGINT NOT NULL REFERENCES market_data.historical_prices(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
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
    granularity VARCHAR(16) NOT NULL DEFAULT 'DAILY',
    adjustment_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    data_version INT NOT NULL DEFAULT 1,
    CONSTRAINT uk_hist_price_adj UNIQUE (instrument_id, exchange, trading_date, granularity, methodology)
);

CREATE INDEX IF NOT EXISTS idx_hist_adj_inst_date ON market_data.historical_prices_adjusted(instrument_id, trading_date);
CREATE INDEX IF NOT EXISTS idx_hist_adj_symbol_date ON market_data.historical_prices_adjusted(symbol, trading_date);

-- 5. Corporate Actions
CREATE TABLE IF NOT EXISTS market_data.corporate_actions (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    announcement_date DATE,
    ex_date DATE NOT NULL,
    record_date DATE,
    effective_date DATE,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ratio_numerator NUMERIC(10, 4),
    ratio_denominator NUMERIC(10, 4),
    adjustment_factor NUMERIC(18, 8),
    dividend_amount NUMERIC(18, 4),
    old_symbol VARCHAR(32),
    new_symbol VARCHAR(32),
    description VARCHAR(255),
    source VARCHAR(64) NOT NULL,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_corp_act_inst ON market_data.corporate_actions(instrument_id);
CREATE INDEX IF NOT EXISTS idx_corp_act_ex_date ON market_data.corporate_actions(ex_date);
CREATE INDEX IF NOT EXISTS idx_corp_act_info_avail ON market_data.corporate_actions(information_available_at);

-- 6. Index Master & Point-in-Time Constituents (Survivorship-bias safe)
CREATE TABLE IF NOT EXISTS market_data.index_master (
    id BIGSERIAL PRIMARY KEY,
    index_symbol VARCHAR(32) NOT NULL UNIQUE,
    index_name VARCHAR(128) NOT NULL,
    exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
    sector VARCHAR(64),
    description VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_data.index_constituents (
    id BIGSERIAL PRIMARY KEY,
    index_id BIGINT NOT NULL REFERENCES market_data.index_master(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    weight NUMERIC(8, 4),
    effective_from DATE NOT NULL,
    effective_to DATE, -- NULL = currently active member
    source VARCHAR(64),
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_idx_const_idx_id ON market_data.index_constituents(index_id);
CREATE INDEX IF NOT EXISTS idx_idx_const_inst_id ON market_data.index_constituents(instrument_id);
CREATE INDEX IF NOT EXISTS idx_idx_const_dates ON market_data.index_constituents(effective_from, effective_to);

-- 7. Historical Ingestion Runs & Data Quality
CREATE TABLE IF NOT EXISTS market_data.historical_ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    provider VARCHAR(64) NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL DEFAULT 'RUNNING',
    symbols_count INT NOT NULL DEFAULT 0,
    records_inserted INT NOT NULL DEFAULT 0,
    records_updated INT NOT NULL DEFAULT 0,
    duplicates_count INT NOT NULL DEFAULT 0,
    gaps_count INT NOT NULL DEFAULT 0,
    duration_ms BIGINT,
    checkpoint_symbol VARCHAR(32),
    error_message VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_data.historical_data_quality (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64),
    instrument_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    expected_days BIGINT NOT NULL,
    actual_days BIGINT NOT NULL,
    missing_days BIGINT NOT NULL,
    duplicate_records BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed Index Master
INSERT INTO market_data.index_master (index_symbol, index_name, exchange, sector, description)
VALUES 
    ('NIFTY 50', 'Nifty 50 Index', 'NSE', 'Broad Market', 'Flagship Indian benchmark index representing top 50 companies'),
    ('NIFTY BANK', 'Nifty Bank Index', 'NSE', 'Banking', 'Sectoral index tracking top Indian private and PSU banks'),
    ('NIFTY IT', 'Nifty IT Index', 'NSE', 'Technology', 'Sectoral index tracking top Indian IT and software companies')
ON CONFLICT (index_symbol) DO NOTHING;
