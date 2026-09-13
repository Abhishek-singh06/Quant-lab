-- ==========================================================
-- QuantLab Migration V4: Mutual Fund & Institutional Intelligence
-- ==========================================================

-- 1. AMC Masters
CREATE TABLE IF NOT EXISTS market_data.amc_masters (
    id BIGSERIAL PRIMARY KEY,
    amc_code VARCHAR(32) NOT NULL UNIQUE,
    amc_name VARCHAR(255) NOT NULL,
    sebi_reg_no VARCHAR(64),
    amfi_code VARCHAR(64),
    website VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_amc_code ON market_data.amc_masters(amc_code);

-- 2. Mutual Fund Schemes
CREATE TABLE IF NOT EXISTS market_data.mutual_fund_schemes (
    id BIGSERIAL PRIMARY KEY,
    amc_id BIGINT REFERENCES market_data.amc_masters(id),
    scheme_code VARCHAR(64) NOT NULL UNIQUE,
    scheme_name VARCHAR(255) NOT NULL,
    isin VARCHAR(12),
    amfi_scheme_code VARCHAR(32),
    category VARCHAR(64) NOT NULL,
    scheme_type VARCHAR(32) NOT NULL,
    benchmark_name VARCHAR(128),
    fund_manager VARCHAR(255),
    aum_crores NUMERIC(15, 2),
    expense_ratio NUMERIC(5, 4),
    inception_date DATE,
    asset_class VARCHAR(32) NOT NULL DEFAULT 'EQUITY',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_mfs_scheme_code ON market_data.mutual_fund_schemes(scheme_code);
CREATE INDEX IF NOT EXISTS idx_mfs_isin ON market_data.mutual_fund_schemes(isin);
CREATE INDEX IF NOT EXISTS idx_mfs_amc_id ON market_data.mutual_fund_schemes(amc_id);
CREATE INDEX IF NOT EXISTS idx_mfs_category ON market_data.mutual_fund_schemes(category);

-- 3. Fund Portfolio Disclosures
CREATE TABLE IF NOT EXISTS market_data.fund_portfolio_disclosures (
    id BIGSERIAL PRIMARY KEY,
    scheme_id BIGINT NOT NULL REFERENCES market_data.mutual_fund_schemes(id),
    data_as_of DATE NOT NULL,
    published_at DATE NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    portfolio_scope VARCHAR(32) NOT NULL DEFAULT 'COMPLETE',
    total_aum NUMERIC(20, 4),
    equity_holding_percent NUMERIC(8, 4),
    debt_holding_percent NUMERIC(8, 4),
    cash_holding_percent NUMERIC(8, 4),
    derivatives_holding_percent NUMERIC(8, 4),
    foreign_holding_percent NUMERIC(8, 4),
    source VARCHAR(64) NOT NULL,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_disclosure_scheme_date UNIQUE (scheme_id, data_as_of)
);

CREATE INDEX IF NOT EXISTS idx_fpd_scheme_id ON market_data.fund_portfolio_disclosures(scheme_id);
CREATE INDEX IF NOT EXISTS idx_fpd_data_as_of ON market_data.fund_portfolio_disclosures(data_as_of);
CREATE INDEX IF NOT EXISTS idx_fpd_available_at ON market_data.fund_portfolio_disclosures(available_at);

-- 4. Fund Holdings
CREATE TABLE IF NOT EXISTS market_data.fund_holdings (
    id BIGSERIAL PRIMARY KEY,
    disclosure_id BIGINT NOT NULL REFERENCES market_data.fund_portfolio_disclosures(id) ON DELETE CASCADE,
    scheme_id BIGINT NOT NULL REFERENCES market_data.mutual_fund_schemes(id),
    instrument_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    company_name VARCHAR(255),
    holding_date DATE NOT NULL,
    data_as_of DATE NOT NULL,
    published_at DATE NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    quantity BIGINT,
    market_value NUMERIC(20, 4),
    portfolio_weight NUMERIC(8, 4) NOT NULL,
    weight_change_pp NUMERIC(8, 4),
    relative_weight_change_pct NUMERIC(8, 4),
    change_type VARCHAR(32) DEFAULT 'UNCHANGED',
    asset_class VARCHAR(32) DEFAULT 'EQUITY',
    sector VARCHAR(64),
    source VARCHAR(64) NOT NULL,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_holding_disc_inst UNIQUE (disclosure_id, instrument_id)
);

CREATE INDEX IF NOT EXISTS idx_fh_scheme_id ON market_data.fund_holdings(scheme_id);
CREATE INDEX IF NOT EXISTS idx_fh_instrument_id ON market_data.fund_holdings(instrument_id);
CREATE INDEX IF NOT EXISTS idx_fh_symbol ON market_data.fund_holdings(symbol);
CREATE INDEX IF NOT EXISTS idx_fh_available_at ON market_data.fund_holdings(available_at);
CREATE INDEX IF NOT EXISTS idx_fh_data_as_of ON market_data.fund_holdings(data_as_of);

-- 5. Institutional Flows (FII & DII)
CREATE TABLE IF NOT EXISTS market_data.institutional_flows (
    id BIGSERIAL PRIMARY KEY,
    trade_date DATE NOT NULL,
    market VARCHAR(32) NOT NULL DEFAULT 'NSE',
    institution_type VARCHAR(32) NOT NULL,
    flow_frequency VARCHAR(32) NOT NULL DEFAULT 'DAILY',
    buy_value NUMERIC(20, 4) NOT NULL,
    sell_value NUMERIC(20, 4) NOT NULL,
    net_value NUMERIC(20, 4) NOT NULL,
    data_as_of DATE NOT NULL,
    published_at DATE NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    source VARCHAR(64) NOT NULL,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_flow_date_type_freq UNIQUE (trade_date, institution_type, flow_frequency)
);

CREATE INDEX IF NOT EXISTS idx_if_trade_date ON market_data.institutional_flows(trade_date);
CREATE INDEX IF NOT EXISTS idx_if_inst_type ON market_data.institutional_flows(institution_type);
CREATE INDEX IF NOT EXISTS idx_if_available_at ON market_data.institutional_flows(available_at);
CREATE INDEX IF NOT EXISTS idx_if_data_as_of ON market_data.institutional_flows(data_as_of);

-- 6. Institutional Ownership (Shareholding Pattern)
CREATE TABLE IF NOT EXISTS market_data.institutional_ownership (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    period_end DATE NOT NULL,
    data_as_of DATE NOT NULL,
    published_at DATE NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    institution_type VARCHAR(32) NOT NULL,
    category VARCHAR(64),
    ownership_percentage NUMERIC(8, 4) NOT NULL,
    share_quantity BIGINT,
    market_value NUMERIC(20, 4),
    change_in_ownership_pp NUMERIC(8, 4),
    source VARCHAR(64) NOT NULL,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_inst_ownership_inst_date_type UNIQUE (instrument_id, period_end, institution_type)
);

CREATE INDEX IF NOT EXISTS idx_io_instrument_id ON market_data.institutional_ownership(instrument_id);
CREATE INDEX IF NOT EXISTS idx_io_symbol ON market_data.institutional_ownership(symbol);
CREATE INDEX IF NOT EXISTS idx_io_period_end ON market_data.institutional_ownership(period_end);
CREATE INDEX IF NOT EXISTS idx_io_available_at ON market_data.institutional_ownership(available_at);
CREATE INDEX IF NOT EXISTS idx_io_data_as_of ON market_data.institutional_ownership(data_as_of);

-- 7. Institutional Ingestion Runs
CREATE TABLE IF NOT EXISTS market_data.institutional_ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    run_uuid VARCHAR(64) NOT NULL UNIQUE,
    provider VARCHAR(64) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL,
    records_received INT NOT NULL DEFAULT 0,
    records_inserted INT NOT NULL DEFAULT 0,
    duplicates_count INT NOT NULL DEFAULT 0,
    duration_ms BIGINT,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed initial AMCs and Schemes
INSERT INTO market_data.amc_masters (amc_code, amc_name, sebi_reg_no, amfi_code, website)
VALUES
    ('HDFC_MF', 'HDFC Asset Management Company Ltd', 'MF/044/00/6', 'HDFC_AMC', 'https://www.hdfcfund.com'),
    ('SBI_MF', 'SBI Funds Management Ltd', 'MF/009/93/3', 'SBI_AMC', 'https://www.sbimf.com'),
    ('PPFAS_MF', 'PPFAS Asset Management Pvt Ltd', 'MF/069/13/1', 'PPFAS_AMC', 'https://amc.ppfas.com'),
    ('ICICI_PRU_MF', 'ICICI Prudential Asset Management Company Ltd', 'MF/003/93/1', 'ICICI_AMC', 'https://www.icicipruamc.com'),
    ('NIPPON_MF', 'Nippon Life India Asset Management Ltd', 'MF/022/95/1', 'NIPPON_AMC', 'https://mf.nipponindiaim.com')
ON CONFLICT (amc_code) DO NOTHING;
