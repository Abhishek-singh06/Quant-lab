-- ==========================================================
-- QuantLab Migration V6: Fundamental Intelligence Schema
-- ==========================================================

-- 1. Fundamental Sources Registry
CREATE TABLE IF NOT EXISTS market_data.fundamental_sources (
    id BIGSERIAL PRIMARY KEY,
    source_name VARCHAR(64) NOT NULL UNIQUE,
    source_type VARCHAR(32) NOT NULL, -- EXCHANGE, REGULATORY, COMPANY_IR, DATA_VENDOR, MOCK
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    base_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 2. Fundamental Filings Registry (Provenance & Versioning)
CREATE TABLE IF NOT EXISTS market_data.fundamental_filings (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    filing_type VARCHAR(32) NOT NULL, -- FINANCIAL_RESULT, ANNUAL_REPORT, XBRL_INSTANCE, SHAREHOLDING
    fiscal_year VARCHAR(16) NOT NULL, -- FY2025, FY2026
    fiscal_quarter VARCHAR(8), -- Q1, Q2, Q3, Q4
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    period_type VARCHAR(32) NOT NULL DEFAULT 'QUARTERLY', -- QUARTERLY, HALF_YEAR, NINE_MONTH, ANNUAL, TTM
    reporting_basis VARCHAR(32) NOT NULL DEFAULT 'CONSOLIDATED', -- STANDALONE, CONSOLIDATED
    audit_status VARCHAR(32) NOT NULL DEFAULT 'UNAUDITED', -- AUDITED, UNAUDITED, LIMITED_REVIEW
    announced_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL, -- Point-in-time cutoff
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    source VARCHAR(64) NOT NULL,
    source_document_url VARCHAR(1024),
    version INT NOT NULL DEFAULT 1,
    superseded_by_id BIGINT,
    is_restatement BOOLEAN NOT NULL DEFAULT FALSE,
    restatement_reason VARCHAR(512),
    data_quality_score VARCHAR(16) NOT NULL DEFAULT 'HIGH', -- HIGH, MEDIUM, LOW
    CONSTRAINT uk_filing_inst_period_basis_ver UNIQUE (instrument_id, period_end, period_type, reporting_basis, version)
);

CREATE INDEX IF NOT EXISTS idx_ff_inst_id ON market_data.fundamental_filings(instrument_id);
CREATE INDEX IF NOT EXISTS idx_ff_symbol ON market_data.fundamental_filings(symbol);
CREATE INDEX IF NOT EXISTS idx_ff_period_end ON market_data.fundamental_filings(period_end);
CREATE INDEX IF NOT EXISTS idx_ff_available_at ON market_data.fundamental_filings(available_at);

-- 3. Financial Statements (Income Statement, Balance Sheet, Cash Flow)
CREATE TABLE IF NOT EXISTS market_data.financial_statements (
    id BIGSERIAL PRIMARY KEY,
    filing_id BIGINT NOT NULL REFERENCES market_data.fundamental_filings(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    period_end DATE NOT NULL,
    period_type VARCHAR(32) NOT NULL,
    reporting_basis VARCHAR(32) NOT NULL,
    audit_status VARCHAR(32) NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    currency VARCHAR(8) NOT NULL DEFAULT 'INR',
    unit VARCHAR(16) NOT NULL DEFAULT 'CRORES', -- CRORES, LAKHS, MILLIONS

    -- Income Statement (Values in Unit Scale)
    revenue NUMERIC(20, 4),
    operating_profit NUMERIC(20, 4),
    ebitda NUMERIC(20, 4),
    ebit NUMERIC(20, 4),
    interest_expense NUMERIC(20, 4),
    depreciation_amortization NUMERIC(20, 4),
    profit_before_tax NUMERIC(20, 4),
    tax_expense NUMERIC(20, 4),
    net_profit NUMERIC(20, 4), -- PAT
    profit_attributable_to_owners NUMERIC(20, 4),
    basic_eps NUMERIC(10, 4),
    diluted_eps NUMERIC(10, 4),

    -- Balance Sheet
    total_assets NUMERIC(20, 4),
    current_assets NUMERIC(20, 4),
    non_current_assets NUMERIC(20, 4),
    cash_and_equivalents NUMERIC(20, 4),
    inventory NUMERIC(20, 4),
    trade_receivables NUMERIC(20, 4),
    total_liabilities NUMERIC(20, 4),
    current_liabilities NUMERIC(20, 4),
    non_current_liabilities NUMERIC(20, 4),
    total_debt NUMERIC(20, 4),
    short_term_debt NUMERIC(20, 4),
    long_term_debt NUMERIC(20, 4),
    total_equity NUMERIC(20, 4),
    retained_earnings NUMERIC(20, 4),

    -- Cash Flow
    operating_cash_flow NUMERIC(20, 4),
    investing_cash_flow NUMERIC(20, 4),
    financing_cash_flow NUMERIC(20, 4),
    capital_expenditure NUMERIC(20, 4),
    free_cash_flow NUMERIC(20, 4),

    -- Sector Specific Metrics (JSON payload for Banking, IT, Auto, Energy)
    sector_specific_metrics JSONB,

    source VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fs_inst_period ON market_data.financial_statements(instrument_id, period_end);
CREATE INDEX IF NOT EXISTS idx_fs_symbol_available ON market_data.financial_statements(symbol, available_at);

-- 4. Derived Financial Ratios & Valuation Metrics
CREATE TABLE IF NOT EXISTS market_data.financial_ratios (
    id BIGSERIAL PRIMARY KEY,
    filing_id BIGINT REFERENCES market_data.fundamental_filings(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    period_end DATE NOT NULL,
    period_type VARCHAR(32) NOT NULL,
    reporting_basis VARCHAR(32) NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,

    -- Profitability & Quality
    gross_margin NUMERIC(8, 4),
    ebitda_margin NUMERIC(8, 4),
    ebit_margin NUMERIC(8, 4),
    net_profit_margin NUMERIC(8, 4),
    roe NUMERIC(8, 4), -- Net Income / Avg Equity
    roce NUMERIC(8, 4), -- EBIT / Capital Employed
    roa NUMERIC(8, 4),
    asset_turnover NUMERIC(8, 4),

    -- Solvency & Coverage
    debt_to_equity NUMERIC(8, 4),
    net_debt_to_ebitda NUMERIC(8, 4),
    interest_coverage NUMERIC(8, 4),
    current_ratio NUMERIC(8, 4),

    -- Growth Metrics (YoY and QoQ)
    revenue_growth_yoy NUMERIC(8, 4),
    ebitda_growth_yoy NUMERIC(8, 4),
    profit_growth_yoy NUMERIC(8, 4),
    eps_growth_yoy NUMERIC(8, 4),
    fcf_growth_yoy NUMERIC(8, 4),

    -- Valuation Ratios (Point-in-Time as of publication/evaluation)
    market_cap_crores NUMERIC(20, 4),
    enterprise_value_crores NUMERIC(20, 4),
    pe_ratio NUMERIC(10, 4),
    pb_ratio NUMERIC(10, 4),
    ev_ebitda NUMERIC(10, 4),
    ev_sales NUMERIC(10, 4),
    dividend_yield NUMERIC(8, 4),
    payout_ratio NUMERIC(8, 4),

    methodology_version VARCHAR(32) NOT NULL DEFAULT '1.0.0',
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fr_inst_available ON market_data.financial_ratios(instrument_id, available_at);
CREATE INDEX IF NOT EXISTS idx_fr_symbol_period ON market_data.financial_ratios(symbol, period_end);

-- 5. Fundamental Ingestion Runs
CREATE TABLE IF NOT EXISTS market_data.fundamental_ingestion_runs (
    id BIGSERIAL PRIMARY KEY,
    run_uuid VARCHAR(64) NOT NULL UNIQUE,
    provider VARCHAR(64) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    status VARCHAR(32) NOT NULL,
    companies_requested INT NOT NULL DEFAULT 0,
    filings_received INT NOT NULL DEFAULT 0,
    filings_inserted INT NOT NULL DEFAULT 0,
    duplicates_count INT NOT NULL DEFAULT 0,
    rejected_count INT NOT NULL DEFAULT 0,
    duration_ms BIGINT,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed Fundamental Sources
INSERT INTO market_data.fundamental_sources (source_name, source_type, is_active, base_url)
VALUES
    ('NSE_XBRL', 'EXCHANGE', TRUE, 'https://www.nseindia.com/companies-listing/corporate-filings-financial-results'),
    ('BSE_CORPORATE', 'EXCHANGE', TRUE, 'https://www.bseindia.com/corporates'),
    ('AUTHORIZED_FUNDAMENTAL_FEED', 'DATA_VENDOR', TRUE, 'https://api.fundamentaldata.com'),
    ('MOCK_FUNDAMENTAL_SOURCE', 'MOCK', TRUE, 'http://localhost')
ON CONFLICT (source_name) DO NOTHING;
