-- =========================================
-- QuantLab Database Initialization
-- =========================================

-- Enable useful extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS market_data;
CREATE SCHEMA IF NOT EXISTS analytics;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA market_data TO quantlab;
GRANT ALL PRIVILEGES ON SCHEMA analytics TO quantlab;

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

-- 5. Mutual Fund and Institutional Intelligence
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

-- 6. Global Market Intelligence
CREATE TABLE IF NOT EXISTS market_data.global_market_sources (
    id BIGSERIAL PRIMARY KEY,
    source_name VARCHAR(64) NOT NULL UNIQUE,
    source_type VARCHAR(32) NOT NULL,
    default_freshness VARCHAR(32) NOT NULL DEFAULT 'DELAYED',
    delay_minutes INT DEFAULT 15,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    base_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_data.global_instruments (
    id BIGSERIAL PRIMARY KEY,
    canonical_symbol VARCHAR(32) NOT NULL UNIQUE,
    provider_symbol VARCHAR(64) NOT NULL,
    instrument_name VARCHAR(255) NOT NULL,
    asset_class VARCHAR(32) NOT NULL,
    market VARCHAR(64) NOT NULL,
    country VARCHAR(32) NOT NULL,
    exchange_source VARCHAR(64) NOT NULL,
    currency VARCHAR(16) NOT NULL DEFAULT 'USD',
    timezone VARCHAR(64) NOT NULL,
    instrument_type VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS market_data.global_market_snapshots (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.global_instruments(id),
    canonical_symbol VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    trading_date DATE NOT NULL,
    open_val NUMERIC(20, 6),
    high_val NUMERIC(20, 6),
    low_val NUMERIC(20, 6),
    close_val NUMERIC(20, 6) NOT NULL,
    previous_close NUMERIC(20, 6),
    price_change NUMERIC(20, 6),
    change_percent NUMERIC(10, 4),
    volume BIGINT,
    yield_rate NUMERIC(10, 4),
    currency VARCHAR(16),
    source VARCHAR(64) NOT NULL,
    source_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    ingestion_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    data_freshness VARCHAR(32) NOT NULL DEFAULT 'DELAYED',
    session_status VARCHAR(32) NOT NULL DEFAULT 'CLOSED',
    data_status VARCHAR(32) NOT NULL DEFAULT 'VALID',
    CONSTRAINT uk_gms_inst_timestamp_src UNIQUE (instrument_id, timestamp, source)
);

CREATE TABLE IF NOT EXISTS market_data.global_market_regimes (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    regime_label VARCHAR(32) NOT NULL,
    composite_score NUMERIC(8, 4) NOT NULL,
    equity_score NUMERIC(8, 4) NOT NULL,
    volatility_score NUMERIC(8, 4) NOT NULL,
    rates_score NUMERIC(8, 4) NOT NULL,
    dollar_score NUMERIC(8, 4) NOT NULL,
    commodity_score NUMERIC(8, 4) NOT NULL,
    asia_score NUMERIC(8, 4) NOT NULL,
    europe_score NUMERIC(8, 4) NOT NULL,
    confidence VARCHAR(32) NOT NULL DEFAULT 'HIGH',
    explanation TEXT NOT NULL,
    methodology_version VARCHAR(32) NOT NULL DEFAULT '1.0.0',
    source_snapshot_count INT NOT NULL DEFAULT 0,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_gmr_timestamp UNIQUE (timestamp)
);

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

-- 7. Fundamental Intelligence Engine
CREATE TABLE IF NOT EXISTS market_data.fundamental_sources (
    id BIGSERIAL PRIMARY KEY,
    source_name VARCHAR(64) NOT NULL UNIQUE,
    source_type VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    base_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_data.fundamental_filings (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    filing_type VARCHAR(32) NOT NULL,
    fiscal_year VARCHAR(16) NOT NULL,
    fiscal_quarter VARCHAR(8),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    period_type VARCHAR(32) NOT NULL DEFAULT 'QUARTERLY',
    reporting_basis VARCHAR(32) NOT NULL DEFAULT 'CONSOLIDATED',
    audit_status VARCHAR(32) NOT NULL DEFAULT 'UNAUDITED',
    announced_at TIMESTAMP WITH TIME ZONE,
    published_at TIMESTAMP WITH TIME ZONE NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ingested_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    source VARCHAR(64) NOT NULL,
    source_document_url VARCHAR(1024),
    version INT NOT NULL DEFAULT 1,
    superseded_by_id BIGINT,
    is_restatement BOOLEAN NOT NULL DEFAULT FALSE,
    restatement_reason VARCHAR(512),
    data_quality_score VARCHAR(16) NOT NULL DEFAULT 'HIGH',
    CONSTRAINT uk_filing_inst_period_basis_ver UNIQUE (instrument_id, period_end, period_type, reporting_basis, version)
);

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
    unit VARCHAR(16) NOT NULL DEFAULT 'CRORES',
    revenue NUMERIC(20, 4),
    operating_profit NUMERIC(20, 4),
    ebitda NUMERIC(20, 4),
    ebit NUMERIC(20, 4),
    interest_expense NUMERIC(20, 4),
    depreciation_amortization NUMERIC(20, 4),
    profit_before_tax NUMERIC(20, 4),
    tax_expense NUMERIC(20, 4),
    net_profit NUMERIC(20, 4),
    profit_attributable_to_owners NUMERIC(20, 4),
    basic_eps NUMERIC(10, 4),
    diluted_eps NUMERIC(10, 4),
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
    operating_cash_flow NUMERIC(20, 4),
    investing_cash_flow NUMERIC(20, 4),
    financing_cash_flow NUMERIC(20, 4),
    capital_expenditure NUMERIC(20, 4),
    free_cash_flow NUMERIC(20, 4),
    sector_specific_metrics JSONB,
    source VARCHAR(64) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_data.financial_ratios (
    id BIGSERIAL PRIMARY KEY,
    filing_id BIGINT REFERENCES market_data.fundamental_filings(id) ON DELETE CASCADE,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    period_end DATE NOT NULL,
    period_type VARCHAR(32) NOT NULL,
    reporting_basis VARCHAR(32) NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    gross_margin NUMERIC(8, 4),
    ebitda_margin NUMERIC(8, 4),
    ebit_margin NUMERIC(8, 4),
    net_profit_margin NUMERIC(8, 4),
    roe NUMERIC(8, 4),
    roce NUMERIC(8, 4),
    roa NUMERIC(8, 4),
    asset_turnover NUMERIC(8, 4),
    debt_to_equity NUMERIC(8, 4),
    net_debt_to_ebitda NUMERIC(8, 4),
    interest_coverage NUMERIC(8, 4),
    current_ratio NUMERIC(8, 4),
    revenue_growth_yoy NUMERIC(8, 4),
    ebitda_growth_yoy NUMERIC(8, 4),
    profit_growth_yoy NUMERIC(8, 4),
    eps_growth_yoy NUMERIC(8, 4),
    fcf_growth_yoy NUMERIC(8, 4),
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

-- ==========================================================
-- QuantLab V7: Production Technical Feature Engine
-- ==========================================================

CREATE TABLE IF NOT EXISTS market_data.feature_definitions (
    id BIGSERIAL PRIMARY KEY,
    feature_name VARCHAR(64) NOT NULL UNIQUE,
    category VARCHAR(32) NOT NULL,
    description VARCHAR(255) NOT NULL,
    default_lookback INT NOT NULL,
    timeframe VARCHAR(16) NOT NULL DEFAULT '1D',
    feature_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    formula_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    price_series_type VARCHAR(32) NOT NULL DEFAULT 'SPLIT_ADJUSTED',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS market_data.technical_features (
    id BIGSERIAL PRIMARY KEY,
    instrument_id BIGINT NOT NULL REFERENCES market_data.instruments(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    feature_name VARCHAR(64) NOT NULL,
    feature_value NUMERIC(24, 8),
    feature_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    trading_date DATE NOT NULL,
    timeframe VARCHAR(16) NOT NULL DEFAULT '1D',
    frequency VARCHAR(16) NOT NULL DEFAULT 'DAILY',
    feature_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    calculation_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    data_version INT NOT NULL DEFAULT 1,
    source_data_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    source VARCHAR(64) NOT NULL DEFAULT 'TECHNICAL_FEATURE_ENGINE',
    ingestion_run_id VARCHAR(64),
    CONSTRAINT uk_technical_feature UNIQUE (instrument_id, feature_name, feature_timestamp, timeframe, feature_version)
);

CREATE INDEX IF NOT EXISTS idx_tech_feat_inst_ts ON market_data.technical_features(instrument_id, feature_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_tech_feat_inst_name_ts ON market_data.technical_features(instrument_id, feature_name, feature_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_tech_feat_symbol_ts ON market_data.technical_features(symbol, feature_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_tech_feat_name_ts ON market_data.technical_features(feature_name, feature_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_tech_feat_info_avail ON market_data.technical_features(information_available_at);

CREATE TABLE IF NOT EXISTS market_data.feature_calculation_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    instruments_requested INT NOT NULL DEFAULT 0,
    instruments_processed INT NOT NULL DEFAULT 0,
    features_calculated BIGINT NOT NULL DEFAULT 0,
    features_failed INT NOT NULL DEFAULT 0,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE,
    duration_ms BIGINT,
    status VARCHAR(32) NOT NULL DEFAULT 'RUNNING',
    feature_set VARCHAR(64) NOT NULL DEFAULT 'ALL_TECHNICAL',
    timeframe VARCHAR(16) NOT NULL DEFAULT '1D',
    from_date DATE,
    to_date DATE,
    error_message VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_data.feature_data_quality (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64),
    instrument_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    feature_name VARCHAR(64) NOT NULL,
    feature_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    feature_value NUMERIC(24, 8),
    validation_status VARCHAR(32) NOT NULL,
    anomaly_reason VARCHAR(255),
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ==========================================================
-- QuantLab V8: Production Market Regime Engine
-- ==========================================================

CREATE TABLE IF NOT EXISTS market_data.regime_model_versions (
    id BIGSERIAL PRIMARY KEY,
    model_version VARCHAR(32) NOT NULL UNIQUE,
    model_type VARCHAR(32) NOT NULL DEFAULT 'WEIGHTED_COMPOSITE_PROBABILISTIC',
    feature_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    normalization_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    description VARCHAR(255) NOT NULL,
    weights_json JSONB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS market_data.market_regimes (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL DEFAULT 'NIFTY 50',
    regime_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    trading_date DATE NOT NULL,
    direction_regime VARCHAR(32) NOT NULL,
    volatility_regime VARCHAR(32) NOT NULL,
    risk_regime VARCHAR(32) NOT NULL,
    direction_score NUMERIC(10, 4) NOT NULL,
    volatility_score NUMERIC(10, 4) NOT NULL,
    risk_score NUMERIC(10, 4) NOT NULL,
    confidence NUMERIC(6, 4) NOT NULL,
    prob_bull NUMERIC(6, 4) NOT NULL,
    prob_bear NUMERIC(6, 4) NOT NULL,
    prob_sideways NUMERIC(6, 4) NOT NULL,
    prob_risk_on NUMERIC(6, 4) NOT NULL,
    prob_risk_off NUMERIC(6, 4) NOT NULL,
    previous_direction_regime VARCHAR(32),
    days_in_regime INT NOT NULL DEFAULT 1,
    is_transition BOOLEAN NOT NULL DEFAULT FALSE,
    explanation TEXT NOT NULL,
    model_version VARCHAR(32) NOT NULL DEFAULT 'REGIME_v1.0.0',
    feature_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    data_version INT NOT NULL DEFAULT 1,
    source_data_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_market_regime UNIQUE (symbol, regime_timestamp, model_version)
);

CREATE INDEX IF NOT EXISTS idx_mkt_reg_sym_ts ON market_data.market_regimes(symbol, regime_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_mkt_reg_date ON market_data.market_regimes(trading_date DESC);
CREATE INDEX IF NOT EXISTS idx_mkt_reg_direction ON market_data.market_regimes(direction_regime);
CREATE INDEX IF NOT EXISTS idx_mkt_reg_risk ON market_data.market_regimes(risk_regime);
CREATE INDEX IF NOT EXISTS idx_mkt_reg_info_avail ON market_data.market_regimes(information_available_at);

CREATE TABLE IF NOT EXISTS market_data.regime_component_scores (
    id BIGSERIAL PRIMARY KEY,
    regime_id BIGINT NOT NULL REFERENCES market_data.market_regimes(id) ON DELETE CASCADE,
    component_name VARCHAR(64) NOT NULL,
    raw_value NUMERIC(18, 4),
    normalized_value NUMERIC(10, 4),
    component_score NUMERIC(10, 4) NOT NULL,
    configured_weight NUMERIC(6, 4) NOT NULL,
    effective_weight NUMERIC(6, 4) NOT NULL,
    confidence VARCHAR(16) NOT NULL DEFAULT 'HIGH',
    source VARCHAR(64) NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    details JSONB
);

CREATE INDEX IF NOT EXISTS idx_reg_comp_reg_id ON market_data.regime_component_scores(regime_id);
CREATE INDEX IF NOT EXISTS idx_reg_comp_name ON market_data.regime_component_scores(component_name);

CREATE TABLE IF NOT EXISTS market_data.regime_evaluation_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    model_version VARCHAR(32) NOT NULL,
    evaluation_type VARCHAR(32) NOT NULL DEFAULT 'WALK_FORWARD',
    train_start DATE NOT NULL,
    train_end DATE NOT NULL,
    test_start DATE NOT NULL,
    test_end DATE NOT NULL,
    bull_forward_return_20d NUMERIC(10, 4),
    bear_forward_return_20d NUMERIC(10, 4),
    sideways_forward_return_20d NUMERIC(10, 4),
    bull_sharpe NUMERIC(8, 4),
    bear_sharpe NUMERIC(8, 4),
    regime_persistence NUMERIC(6, 4),
    baseline1_sma50_sharpe NUMERIC(8, 4),
    baseline2_sma200_sharpe NUMERIC(8, 4),
    baseline3_momentum_sharpe NUMERIC(8, 4),
    baseline4_vix_sharpe NUMERIC(8, 4),
    summary_report JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ==========================================================
-- QuantLab V9: Production Quant Predictions & Walk-Forward Engine
-- ==========================================================

CREATE TABLE IF NOT EXISTS market_data.dataset_snapshots (
    id BIGSERIAL PRIMARY KEY,
    dataset_id VARCHAR(64) NOT NULL UNIQUE,
    dataset_version VARCHAR(32) NOT NULL,
    feature_version VARCHAR(16) NOT NULL,
    target_version VARCHAR(16) NOT NULL,
    universe_version VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    row_count INTEGER NOT NULL,
    instrument_count INTEGER NOT NULL,
    feature_count INTEGER NOT NULL,
    feature_columns JSONB NOT NULL,
    missingness_stats JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_dataset_snapshots_version ON market_data.dataset_snapshots(dataset_version);

CREATE TABLE IF NOT EXISTS market_data.model_registry (
    id BIGSERIAL PRIMARY KEY,
    model_id VARCHAR(64) NOT NULL UNIQUE,
    model_name VARCHAR(128) NOT NULL,
    model_type VARCHAR(32) NOT NULL,
    algorithm VARCHAR(64) NOT NULL,
    model_version VARCHAR(32) NOT NULL UNIQUE,
    feature_version VARCHAR(16) NOT NULL,
    dataset_version VARCHAR(32) NOT NULL,
    target_definition VARCHAR(64) NOT NULL,
    target_horizon VARCHAR(16) NOT NULL,
    training_start DATE NOT NULL,
    training_end DATE NOT NULL,
    validation_start DATE NOT NULL,
    validation_end DATE NOT NULL,
    test_start DATE,
    test_end DATE,
    hyperparameters JSONB NOT NULL,
    metrics JSONB NOT NULL,
    feature_importance JSONB,
    artifact_path VARCHAR(256),
    artifact_checksum VARCHAR(64),
    status VARCHAR(32) NOT NULL DEFAULT 'VALIDATED',
    random_seed INTEGER NOT NULL DEFAULT 42,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_model_registry_type ON market_data.model_registry(model_type, status);
CREATE INDEX IF NOT EXISTS idx_model_registry_version ON market_data.model_registry(model_version);

CREATE TABLE IF NOT EXISTS market_data.model_predictions (
    id BIGSERIAL PRIMARY KEY,
    prediction_id VARCHAR(64) NOT NULL UNIQUE,
    model_id VARCHAR(64) NOT NULL,
    model_version VARCHAR(32) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    prediction_timestamp TIMESTAMPTZ NOT NULL,
    trading_date DATE NOT NULL,
    target_horizon VARCHAR(16) NOT NULL,
    prediction_type VARCHAR(32) NOT NULL,
    predicted_return NUMERIC(10, 6),
    probability_positive NUMERIC(6, 4),
    probability_negative NUMERIC(6, 4),
    predicted_class INTEGER,
    predicted_volatility NUMERIC(10, 6),
    actual_return NUMERIC(10, 6),
    actual_volatility NUMERIC(10, 6),
    feature_contributions JSONB,
    regime_at_prediction VARCHAR(32),
    information_available_at TIMESTAMPTZ NOT NULL,
    calculated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_model_pred_symbol_date ON market_data.model_predictions(symbol, trading_date);
CREATE INDEX IF NOT EXISTS idx_model_pred_model_ver ON market_data.model_predictions(model_version, trading_date);

CREATE TABLE IF NOT EXISTS market_data.walk_forward_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    run_name VARCHAR(128) NOT NULL,
    mode VARCHAR(32) NOT NULL,
    initial_train_start DATE NOT NULL,
    initial_train_end DATE NOT NULL,
    step_size VARCHAR(16) NOT NULL,
    model_type VARCHAR(32) NOT NULL,
    target_definition VARCHAR(64) NOT NULL,
    purge_window_days INTEGER NOT NULL DEFAULT 5,
    embargo_window_days INTEGER NOT NULL DEFAULT 2,
    total_folds INTEGER NOT NULL,
    completed_folds INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    aggregate_metrics JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS market_data.walk_forward_folds (
    id BIGSERIAL PRIMARY KEY,
    fold_id VARCHAR(64) NOT NULL UNIQUE,
    run_id VARCHAR(64) NOT NULL REFERENCES market_data.walk_forward_runs(run_id) ON DELETE CASCADE,
    fold_number INTEGER NOT NULL,
    train_start DATE NOT NULL,
    train_end DATE NOT NULL,
    validation_start DATE NOT NULL,
    validation_end DATE NOT NULL,
    test_start DATE NOT NULL,
    test_end DATE NOT NULL,
    winning_model_id VARCHAR(64) NOT NULL,
    winning_algorithm VARCHAR(64) NOT NULL,
    model_version VARCHAR(32) NOT NULL,
    test_observations INTEGER NOT NULL,
    test_ic NUMERIC(8, 4),
    test_rank_ic NUMERIC(8, 4),
    test_mae NUMERIC(10, 6),
    test_rmse NUMERIC(10, 6),
    test_roc_auc NUMERIC(6, 4),
    test_directional_accuracy NUMERIC(6, 4),
    baseline_comparison JSONB NOT NULL,
    regime_breakdown JSONB,
    sector_breakdown JSONB,
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wf_folds_run ON market_data.walk_forward_folds(run_id, fold_number);

-- ==========================================================
-- QuantLab V10: Production Cross-Check / Signal Engine
-- ==========================================================

CREATE TABLE IF NOT EXISTS market_data.signal_configurations (
    id UUID PRIMARY KEY,
    version VARCHAR(50) NOT NULL UNIQUE,
    min_supporting_categories INT NOT NULL DEFAULT 3,
    buy_threshold DOUBLE PRECISION NOT NULL DEFAULT 40.0,
    sell_threshold DOUBLE PRECISION NOT NULL DEFAULT -40.0,
    min_confidence DOUBLE PRECISION NOT NULL DEFAULT 0.50,
    weights JSONB NOT NULL,
    category_caps JSONB NOT NULL,
    correlation_groups JSONB NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.signal_generation_runs (
    id UUID PRIMARY KEY,
    run_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    as_of_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    instrument_count INT NOT NULL DEFAULT 0,
    signal_count INT NOT NULL DEFAULT 0,
    configuration_version VARCHAR(50) NOT NULL,
    signal_version VARCHAR(50) NOT NULL,
    model_version VARCHAR(50),
    regime_version VARCHAR(50),
    status VARCHAR(32) NOT NULL,
    duration_ms BIGINT DEFAULT 0,
    error_summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.signals (
    id UUID PRIMARY KEY,
    run_id UUID REFERENCES market_data.signal_generation_runs(id) ON DELETE SET NULL,
    instrument_id BIGINT REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    signal VARCHAR(20) NOT NULL,
    signal_score DOUBLE PRECISION NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    expected_return DOUBLE PRECISION,
    expected_volatility DOUBLE PRECISION,
    return_to_volatility_ratio DOUBLE PRECISION,
    direction VARCHAR(20) NOT NULL,
    conflict_severity VARCHAR(20) NOT NULL,
    conflict_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    data_quality_status VARCHAR(30) NOT NULL,
    freshness_score DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    reasoning TEXT NOT NULL,
    structured_reasoning JSONB,
    supporting_categories JSONB,
    opposing_categories JSONB,
    signal_version VARCHAR(32) NOT NULL,
    configuration_version VARCHAR(32) NOT NULL,
    feature_version VARCHAR(32),
    model_version VARCHAR(32),
    regime_version VARCHAR(32),
    data_version VARCHAR(32),
    is_latest BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.signal_components (
    id UUID PRIMARY KEY,
    signal_id UUID NOT NULL REFERENCES market_data.signals(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    category_score DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    weighted_contribution DOUBLE PRECISION NOT NULL,
    direction VARCHAR(20) NOT NULL,
    strength DOUBLE PRECISION NOT NULL,
    quality DOUBLE PRECISION NOT NULL,
    freshness DOUBLE PRECISION NOT NULL,
    is_present BOOLEAN NOT NULL DEFAULT true,
    missing_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.signal_evidence (
    id UUID PRIMARY KEY,
    signal_id UUID NOT NULL REFERENCES market_data.signals(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    feature_name VARCHAR(100) NOT NULL,
    raw_value DOUBLE PRECISION,
    raw_value_str TEXT,
    normalized_score DOUBLE PRECISION NOT NULL,
    direction VARCHAR(20) NOT NULL,
    strength DOUBLE PRECISION NOT NULL,
    quality DOUBLE PRECISION NOT NULL,
    freshness DOUBLE PRECISION NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    contribution DOUBLE PRECISION NOT NULL,
    source VARCHAR(100) NOT NULL,
    source_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version VARCHAR(50),
    reason TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.signal_transitions (
    id UUID PRIMARY KEY,
    instrument_id BIGINT REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    previous_signal VARCHAR(20),
    new_signal VARCHAR(20) NOT NULL,
    previous_score DOUBLE PRECISION,
    new_score DOUBLE PRECISION NOT NULL,
    transition_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    transition_reason TEXT NOT NULL,
    signal_id UUID REFERENCES market_data.signals(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_signals_sym_ts ON market_data.signals(symbol, signal_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_signals_sym_latest ON market_data.signals(symbol, is_latest);
CREATE INDEX IF NOT EXISTS idx_signals_info_avail ON market_data.signals(information_available_at);
CREATE INDEX IF NOT EXISTS idx_signal_components_sig ON market_data.signal_components(signal_id);
CREATE INDEX IF NOT EXISTS idx_signal_evidence_sig ON market_data.signal_evidence(signal_id);
CREATE INDEX IF NOT EXISTS idx_signal_trans_sym ON market_data.signal_transitions(symbol, transition_timestamp DESC);

-- ==========================================================
-- QuantLab V11: Production Risk Engine & Position Sizing
-- ==========================================================

CREATE TABLE IF NOT EXISTS market_data.risk_profiles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    profile_type VARCHAR(32) NOT NULL DEFAULT 'MODERATE',
    max_portfolio_risk DOUBLE PRECISION NOT NULL DEFAULT 0.05,
    max_position_risk DOUBLE PRECISION NOT NULL DEFAULT 0.01,
    max_position_allocation DOUBLE PRECISION NOT NULL DEFAULT 0.10,
    max_sector_allocation DOUBLE PRECISION NOT NULL DEFAULT 0.25,
    max_industry_allocation DOUBLE PRECISION NOT NULL DEFAULT 0.15,
    max_single_security_allocation DOUBLE PRECISION NOT NULL DEFAULT 0.10,
    max_correlation_exposure DOUBLE PRECISION NOT NULL DEFAULT 0.70,
    max_drawdown_tolerance DOUBLE PRECISION NOT NULL DEFAULT 0.15,
    max_portfolio_volatility DOUBLE PRECISION NOT NULL DEFAULT 0.20,
    minimum_liquidity_requirement DOUBLE PRECISION NOT NULL DEFAULT 1000000.0,
    default_stop_method VARCHAR(32) NOT NULL DEFAULT 'ATR_MULTIPLE',
    default_position_sizing_method VARCHAR(32) NOT NULL DEFAULT 'FIXED_RISK',
    allow_short_selling BOOLEAN NOT NULL DEFAULT false,
    allow_leverage BOOLEAN NOT NULL DEFAULT false,
    max_leverage DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    cash_buffer DOUBLE PRECISION NOT NULL DEFAULT 0.05,
    minimum_confidence DOUBLE PRECISION NOT NULL DEFAULT 0.50,
    minimum_signal_score DOUBLE PRECISION NOT NULL DEFAULT 35.0,
    risk_budget_method VARCHAR(32) NOT NULL DEFAULT 'VOLATILITY_ADJUSTED',
    is_active BOOLEAN NOT NULL DEFAULT true,
    version VARCHAR(32) NOT NULL DEFAULT 'RP_v1.0.0',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.portfolios (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    risk_profile_id UUID REFERENCES market_data.risk_profiles(id),
    current_cash DOUBLE PRECISION NOT NULL DEFAULT 1000000.0,
    current_portfolio_value DOUBLE PRECISION NOT NULL DEFAULT 1000000.0,
    peak_portfolio_value DOUBLE PRECISION NOT NULL DEFAULT 1000000.0,
    peak_timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    current_drawdown DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    max_drawdown DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.portfolio_positions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES market_data.portfolios(id) ON DELETE CASCADE,
    instrument_id BIGINT REFERENCES market_data.instruments(id),
    symbol VARCHAR(32) NOT NULL,
    sector VARCHAR(64),
    industry VARCHAR(64),
    quantity DOUBLE PRECISION NOT NULL,
    average_entry_price DOUBLE PRECISION NOT NULL,
    current_price DOUBLE PRECISION NOT NULL,
    market_value DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    current_stop_price DOUBLE PRECISION,
    position_risk_amount DOUBLE PRECISION,
    position_risk_percent DOUBLE PRECISION,
    entry_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    last_updated_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.portfolio_snapshots (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES market_data.portfolios(id) ON DELETE CASCADE,
    snapshot_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    portfolio_value DOUBLE PRECISION NOT NULL,
    cash DOUBLE PRECISION NOT NULL,
    total_positions_value DOUBLE PRECISION NOT NULL,
    position_count INT NOT NULL,
    positions_json JSONB NOT NULL,
    sector_exposure_json JSONB NOT NULL,
    consumed_risk_budget DOUBLE PRECISION NOT NULL,
    remaining_risk_budget DOUBLE PRECISION NOT NULL,
    portfolio_volatility DOUBLE PRECISION,
    current_drawdown DOUBLE PRECISION NOT NULL,
    data_version VARCHAR(32) NOT NULL DEFAULT '1',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.risk_assessments (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    portfolio_id UUID REFERENCES market_data.portfolios(id),
    risk_profile_id UUID REFERENCES market_data.risk_profiles(id),
    signal_id UUID REFERENCES market_data.signals(id),
    symbol VARCHAR(32) NOT NULL,
    signal_type VARCHAR(20) NOT NULL,
    signal_score DOUBLE PRECISION NOT NULL,
    signal_confidence DOUBLE PRECISION NOT NULL,
    suggested_allocation DOUBLE PRECISION NOT NULL,
    maximum_allocation DOUBLE PRECISION NOT NULL,
    recommended_quantity DOUBLE PRECISION NOT NULL,
    entry_price DOUBLE PRECISION NOT NULL,
    stop_price DOUBLE PRECISION NOT NULL,
    target_price DOUBLE PRECISION,
    stop_distance DOUBLE PRECISION NOT NULL,
    stop_distance_pct DOUBLE PRECISION NOT NULL,
    stop_method VARCHAR(32) NOT NULL,
    position_risk_amount DOUBLE PRECISION NOT NULL,
    position_risk_percent DOUBLE PRECISION NOT NULL,
    estimated_downside DOUBLE PRECISION NOT NULL,
    portfolio_value DOUBLE PRECISION NOT NULL,
    remaining_risk_budget DOUBLE PRECISION NOT NULL,
    portfolio_volatility DOUBLE PRECISION,
    security_volatility DOUBLE PRECISION NOT NULL,
    expected_volatility DOUBLE PRECISION,
    max_correlation DOUBLE PRECISION,
    sector_exposure_after_trade DOUBLE PRECISION,
    current_drawdown DOUBLE PRECISION NOT NULL,
    risk_reward_ratio DOUBLE PRECISION,
    risk_decision VARCHAR(40) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    risk_trace JSONB NOT NULL,
    limiting_constraints JSONB,
    risk_warnings JSONB,
    reasoning TEXT NOT NULL,
    data_quality_status VARCHAR(30) NOT NULL,
    risk_engine_version VARCHAR(32) NOT NULL DEFAULT 'RISK_v1.0.0',
    risk_profile_version VARCHAR(32) NOT NULL DEFAULT 'RP_v1.0.0',
    signal_version VARCHAR(32),
    data_version VARCHAR(32) NOT NULL DEFAULT '1',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.risk_adjustments (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES market_data.risk_assessments(id) ON DELETE CASCADE,
    adjustment_type VARCHAR(50) NOT NULL,
    multiplier DOUBLE PRECISION NOT NULL,
    base_allocation DOUBLE PRECISION NOT NULL,
    adjusted_allocation DOUBLE PRECISION NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.risk_warnings (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES market_data.risk_assessments(id) ON DELETE CASCADE,
    warning_code VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.correlation_snapshots (
    id UUID PRIMARY KEY,
    snapshot_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    symbol_a VARCHAR(32) NOT NULL,
    symbol_b VARCHAR(32) NOT NULL,
    correlation_window_days INT NOT NULL DEFAULT 60,
    pearson_correlation DOUBLE PRECISION NOT NULL,
    sample_size INT NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_correlation_pair UNIQUE (snapshot_timestamp, symbol_a, symbol_b, correlation_window_days)
);

CREATE TABLE IF NOT EXISTS market_data.drawdown_snapshots (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES market_data.portfolios(id) ON DELETE CASCADE,
    snapshot_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    peak_value DOUBLE PRECISION NOT NULL,
    current_value DOUBLE PRECISION NOT NULL,
    drawdown_pct DOUBLE PRECISION NOT NULL,
    drawdown_duration_days INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_portfolios_active ON market_data.portfolios(is_active);
CREATE INDEX IF NOT EXISTS idx_portfolio_pos_port ON market_data.portfolio_positions(portfolio_id, is_active);
CREATE INDEX IF NOT EXISTS idx_portfolio_pos_sym ON market_data.portfolio_positions(symbol);
CREATE INDEX IF NOT EXISTS idx_port_snaps_port_ts ON market_data.portfolio_snapshots(portfolio_id, snapshot_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_risk_assess_sym_ts ON market_data.risk_assessments(symbol, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_risk_assess_port_ts ON market_data.risk_assessments(portfolio_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_risk_assess_info_avail ON market_data.risk_assessments(information_available_at);
CREATE INDEX IF NOT EXISTS idx_correl_snaps_syms ON market_data.correlation_snapshots(symbol_a, symbol_b, snapshot_timestamp DESC);

CREATE TABLE IF NOT EXISTS market_data.horizon_feature_sets (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    horizon VARCHAR(32) NOT NULL,
    feature_names JSONB NOT NULL,
    feature_definitions JSONB NOT NULL,
    lookback_days INT NOT NULL,
    sources JSONB NOT NULL,
    version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.horizon_target_sets (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    horizon VARCHAR(32) NOT NULL,
    target_period VARCHAR(32) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    formula TEXT NOT NULL,
    threshold DOUBLE PRECISION,
    benchmark_symbol VARCHAR(32),
    version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.horizon_model_configs (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    horizon VARCHAR(32) NOT NULL,
    target_period VARCHAR(32) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    feature_set_id UUID REFERENCES market_data.horizon_feature_sets(id),
    target_set_id UUID REFERENCES market_data.horizon_target_sets(id),
    algorithm VARCHAR(64) NOT NULL,
    hyperparameters JSONB NOT NULL,
    retrain_frequency VARCHAR(32) NOT NULL DEFAULT 'WEEKLY',
    version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.horizon_datasets (
    id UUID PRIMARY KEY,
    dataset_name VARCHAR(100) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    feature_set_version VARCHAR(32) NOT NULL,
    target_set_version VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    row_count INT NOT NULL,
    column_count INT NOT NULL,
    universe_version VARCHAR(32) NOT NULL DEFAULT 'UNIVERSE_v1.0.0',
    data_version VARCHAR(32) NOT NULL DEFAULT '1',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.horizon_model_versions (
    id UUID PRIMARY KEY,
    config_id UUID REFERENCES market_data.horizon_model_configs(id),
    model_id VARCHAR(100) NOT NULL,
    model_version VARCHAR(32) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    target_period VARCHAR(32) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    feature_set_version VARCHAR(32) NOT NULL,
    target_set_version VARCHAR(32) NOT NULL,
    algorithm VARCHAR(64) NOT NULL,
    hyperparameters JSONB NOT NULL,
    training_start DATE NOT NULL,
    training_end DATE NOT NULL,
    validation_start DATE,
    validation_end DATE,
    test_start DATE,
    test_end DATE,
    model_status VARCHAR(32) NOT NULL DEFAULT 'CANDIDATE',
    model_availability_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    metrics JSONB NOT NULL,
    feature_importances JSONB,
    model_artifacts_path TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_horizon_model_ver UNIQUE (model_id, model_version)
);

CREATE TABLE IF NOT EXISTS market_data.horizon_predictions (
    id UUID PRIMARY KEY,
    model_version_id UUID REFERENCES market_data.horizon_model_versions(id),
    symbol VARCHAR(32) NOT NULL,
    instrument_id BIGINT,
    prediction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    horizon_period VARCHAR(32) NOT NULL,
    expected_return DOUBLE PRECISION,
    probability_positive DOUBLE PRECISION,
    probability_negative DOUBLE PRECISION,
    predicted_class INT,
    expected_volatility DOUBLE PRECISION,
    expected_drawdown DOUBLE PRECISION,
    relative_return DOUBLE PRECISION,
    confidence DOUBLE PRECISION NOT NULL,
    outlook VARCHAR(20) NOT NULL,
    feature_contributions JSONB,
    model_version VARCHAR(32) NOT NULL,
    feature_set_version VARCHAR(32) NOT NULL,
    target_set_version VARCHAR(32) NOT NULL,
    data_version VARCHAR(32) NOT NULL DEFAULT '1',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.horizon_evaluations (
    id UUID PRIMARY KEY,
    model_version_id UUID NOT NULL REFERENCES market_data.horizon_model_versions(id) ON DELETE CASCADE,
    horizon VARCHAR(32) NOT NULL,
    target_period VARCHAR(32) NOT NULL,
    evaluation_type VARCHAR(32) NOT NULL DEFAULT 'WALK_FORWARD',
    sample_size INT NOT NULL,
    mae DOUBLE PRECISION,
    rmse DOUBLE PRECISION,
    r2 DOUBLE PRECISION,
    directional_accuracy DOUBLE PRECISION,
    ic DOUBLE PRECISION,
    rank_ic DOUBLE PRECISION,
    roc_auc DOUBLE PRECISION,
    brier_score DOUBLE PRECISION,
    log_loss DOUBLE PRECISION,
    hit_rate DOUBLE PRECISION,
    max_drawdown DOUBLE PRECISION,
    baseline_metrics JSONB,
    regime_metrics JSONB,
    sector_metrics JSONB,
    evaluation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.horizon_conflicts (
    id UUID PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    short_term_outlook VARCHAR(20) NOT NULL,
    medium_term_outlook VARCHAR(20) NOT NULL,
    long_term_outlook VARCHAR(20) NOT NULL,
    conflict_detected BOOLEAN NOT NULL DEFAULT false,
    conflict_severity VARCHAR(20) NOT NULL DEFAULT 'NONE',
    explanation TEXT NOT NULL,
    short_term_pred_id UUID REFERENCES market_data.horizon_predictions(id),
    medium_term_pred_id UUID REFERENCES market_data.horizon_predictions(id),
    long_term_pred_id UUID REFERENCES market_data.horizon_predictions(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.horizon_monitoring (
    id UUID PRIMARY KEY,
    model_version_id UUID NOT NULL REFERENCES market_data.horizon_model_versions(id) ON DELETE CASCADE,
    horizon VARCHAR(32) NOT NULL,
    window_start DATE NOT NULL,
    window_end DATE NOT NULL,
    rolling_ic DOUBLE PRECISION,
    rolling_directional_accuracy DOUBLE PRECISION,
    rolling_brier_score DOUBLE PRECISION,
    psi_score DOUBLE PRECISION,
    ks_statistic DOUBLE PRECISION,
    drift_status VARCHAR(32) NOT NULL DEFAULT 'STABLE',
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_horizon_pred_sym_ts ON market_data.horizon_predictions(symbol, prediction_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_horizon_pred_hor_ts ON market_data.horizon_predictions(horizon, prediction_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_horizon_pred_info_avail ON market_data.horizon_predictions(information_available_at);
CREATE INDEX IF NOT EXISTS idx_horizon_model_ver_status ON market_data.horizon_model_versions(horizon, model_status);
CREATE INDEX IF NOT EXISTS idx_horizon_conflicts_sym_ts ON market_data.horizon_conflicts(symbol, timestamp DESC);
CREATE TABLE IF NOT EXISTS market_data.backtest_configs (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    horizon VARCHAR(32) NOT NULL DEFAULT 'SHORT_TERM',
    universe_type VARCHAR(32) NOT NULL DEFAULT 'NIFTY_50',
    symbols JSONB NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    initial_capital DOUBLE PRECISION NOT NULL DEFAULT 1000000.0,
    cash_buffer_pct DOUBLE PRECISION NOT NULL DEFAULT 0.05,
    rebalance_frequency VARCHAR(32) NOT NULL DEFAULT 'DAILY',
    execution_timing VARCHAR(32) NOT NULL DEFAULT 'NEXT_BAR_OPEN',
    cost_model_type VARCHAR(32) NOT NULL DEFAULT 'REALISTIC_INDIAN',
    slippage_model_type VARCHAR(32) NOT NULL DEFAULT 'FIXED_BPS',
    brokerage_bps DOUBLE PRECISION NOT NULL DEFAULT 3.0,
    stt_delivery_bps DOUBLE PRECISION NOT NULL DEFAULT 10.0,
    stt_intraday_bps DOUBLE PRECISION NOT NULL DEFAULT 2.5,
    exchange_charges_bps DOUBLE PRECISION NOT NULL DEFAULT 0.345,
    gst_rate DOUBLE PRECISION NOT NULL DEFAULT 0.18,
    stamp_duty_bps DOUBLE PRECISION NOT NULL DEFAULT 1.5,
    slippage_bps DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    max_position_weight DOUBLE PRECISION NOT NULL DEFAULT 0.20,
    max_sector_weight DOUBLE PRECISION NOT NULL DEFAULT 0.35,
    max_drawdown_limit DOUBLE PRECISION NOT NULL DEFAULT 0.15,
    benchmark_symbol VARCHAR(32) NOT NULL DEFAULT 'NIFTY_50',
    version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.backtest_runs (
    id UUID PRIMARY KEY,
    config_id UUID REFERENCES market_data.backtest_configs(id),
    name VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    engine_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_bars_processed INT NOT NULL DEFAULT 0,
    total_trades_count INT NOT NULL DEFAULT 0,
    initial_capital DOUBLE PRECISION NOT NULL,
    final_equity DOUBLE PRECISION,
    total_net_pnl DOUBLE PRECISION,
    total_fees_paid DOUBLE PRECISION,
    total_slippage_paid DOUBLE PRECISION,
    total_dividends_received DOUBLE PRECISION,
    error_message TEXT,
    execution_duration_ms BIGINT,
    data_quality_trust_level VARCHAR(32) NOT NULL DEFAULT 'PRODUCTION_READY',
    data_quality_report JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS market_data.backtest_orders (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES market_data.backtest_runs(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    side VARCHAR(10) NOT NULL,
    order_type VARCHAR(20) NOT NULL DEFAULT 'MARKET',
    quantity INT NOT NULL,
    requested_price DOUBLE PRECISION NOT NULL,
    executed_price DOUBLE PRECISION,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    order_submitted_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    order_executed_timestamp TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    slippage_bps DOUBLE PRECISION DEFAULT 0.0,
    slippage_amount DOUBLE PRECISION DEFAULT 0.0,
    fees_amount DOUBLE PRECISION DEFAULT 0.0,
    trade_ref_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.backtest_trades (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES market_data.backtest_runs(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    side VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    entry_order_id UUID REFERENCES market_data.backtest_orders(id),
    exit_order_id UUID REFERENCES market_data.backtest_orders(id),
    entry_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    exit_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    entry_price DOUBLE PRECISION NOT NULL,
    exit_price DOUBLE PRECISION NOT NULL,
    gross_pnl DOUBLE PRECISION NOT NULL,
    net_pnl DOUBLE PRECISION NOT NULL,
    return_pct DOUBLE PRECISION NOT NULL,
    total_fees DOUBLE PRECISION NOT NULL,
    total_slippage DOUBLE PRECISION NOT NULL,
    holding_period_days INT NOT NULL,
    exit_reason VARCHAR(32) NOT NULL,
    max_favorable_excursion DOUBLE PRECISION,
    max_adverse_excursion DOUBLE PRECISION,
    regime_at_entry VARCHAR(32),
    regime_at_exit VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.backtest_positions (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES market_data.backtest_runs(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    quantity INT NOT NULL,
    average_entry_price DOUBLE PRECISION NOT NULL,
    current_market_price DOUBLE PRECISION NOT NULL,
    cost_basis DOUBLE PRECISION NOT NULL,
    market_value DOUBLE PRECISION NOT NULL,
    unrealized_pnl DOUBLE PRECISION NOT NULL,
    unrealized_return_pct DOUBLE PRECISION NOT NULL,
    weight_in_portfolio DOUBLE PRECISION NOT NULL,
    as_of_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_backtest_pos_run_sym_date UNIQUE (run_id, symbol, as_of_date)
);

CREATE TABLE IF NOT EXISTS market_data.backtest_portfolio_snapshots (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES market_data.backtest_runs(id) ON DELETE CASCADE,
    snapshot_date DATE NOT NULL,
    cash_balance DOUBLE PRECISION NOT NULL,
    positions_market_value DOUBLE PRECISION NOT NULL,
    total_equity DOUBLE PRECISION NOT NULL,
    gross_exposure DOUBLE PRECISION NOT NULL,
    net_exposure DOUBLE PRECISION NOT NULL,
    leverage DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    daily_pnl DOUBLE PRECISION NOT NULL,
    daily_return DOUBLE PRECISION NOT NULL,
    cumulative_return DOUBLE PRECISION NOT NULL,
    drawdown_pct DOUBLE PRECISION NOT NULL,
    open_positions_count INT NOT NULL,
    trades_executed_today INT NOT NULL,
    dividends_credited_today DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_backtest_snap_run_date UNIQUE (run_id, snapshot_date)
);

CREATE TABLE IF NOT EXISTS market_data.backtest_equity_curve (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES market_data.backtest_runs(id) ON DELETE CASCADE,
    point_date DATE NOT NULL,
    strategy_equity DOUBLE PRECISION NOT NULL,
    strategy_return_pct DOUBLE PRECISION NOT NULL,
    strategy_drawdown_pct DOUBLE PRECISION NOT NULL,
    buy_and_hold_equity DOUBLE PRECISION NOT NULL,
    buy_and_hold_return_pct DOUBLE PRECISION NOT NULL,
    benchmark_equity DOUBLE PRECISION NOT NULL,
    benchmark_return_pct DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_backtest_equity_run_date UNIQUE (run_id, point_date)
);

CREATE TABLE IF NOT EXISTS market_data.backtest_ledger (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES market_data.backtest_runs(id) ON DELETE CASCADE,
    transaction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    transaction_type VARCHAR(32) NOT NULL,
    symbol VARCHAR(32),
    amount DOUBLE PRECISION NOT NULL,
    cash_balance_before DOUBLE PRECISION NOT NULL,
    cash_balance_after DOUBLE PRECISION NOT NULL,
    description TEXT NOT NULL,
    reference_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.backtest_metrics (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL UNIQUE REFERENCES market_data.backtest_runs(id) ON DELETE CASCADE,
    total_return_pct DOUBLE PRECISION NOT NULL,
    cagr DOUBLE PRECISION NOT NULL,
    annualized_volatility DOUBLE PRECISION NOT NULL,
    sharpe_ratio DOUBLE PRECISION NOT NULL,
    sortino_ratio DOUBLE PRECISION NOT NULL,
    max_drawdown_pct DOUBLE PRECISION NOT NULL,
    max_drawdown_duration_days INT NOT NULL,
    calmar_ratio DOUBLE PRECISION NOT NULL,
    win_rate_pct DOUBLE PRECISION NOT NULL,
    profit_factor DOUBLE PRECISION NOT NULL,
    average_trade_return_pct DOUBLE PRECISION NOT NULL,
    average_win_return_pct DOUBLE PRECISION NOT NULL,
    average_loss_return_pct DOUBLE PRECISION NOT NULL,
    win_loss_ratio DOUBLE PRECISION NOT NULL,
    total_trades_count INT NOT NULL,
    winning_trades_count INT NOT NULL,
    losing_trades_count INT NOT NULL,
    annualized_turnover DOUBLE PRECISION NOT NULL,
    beta_to_benchmark DOUBLE PRECISION,
    alpha_to_benchmark DOUBLE PRECISION,
    information_ratio DOUBLE PRECISION,
    subperiod_metrics JSONB,
    regime_breakdown_metrics JSONB,
    sector_breakdown_metrics JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.backtest_rejected_signals (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES market_data.backtest_runs(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    signal_type VARCHAR(20) NOT NULL,
    signal_strength DOUBLE PRECISION NOT NULL,
    rejection_reason VARCHAR(64) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_bt_runs_status ON market_data.backtest_runs(status);
CREATE INDEX IF NOT EXISTS idx_bt_orders_run ON market_data.backtest_orders(run_id, symbol);
CREATE INDEX IF NOT EXISTS idx_bt_trades_run ON market_data.backtest_trades(run_id, exit_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_bt_snap_run_date ON market_data.backtest_portfolio_snapshots(run_id, snapshot_date ASC);
CREATE INDEX IF NOT EXISTS idx_bt_equity_run_date ON market_data.backtest_equity_curve(run_id, point_date ASC);
CREATE INDEX IF NOT EXISTS idx_bt_ledger_run_ts ON market_data.backtest_ledger(run_id, transaction_timestamp ASC);
CREATE INDEX IF NOT EXISTS idx_bt_rejected_run ON market_data.backtest_rejected_signals(run_id, signal_timestamp);

CREATE TABLE IF NOT EXISTS market_data.paper_trading_sessions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    execution_mode VARCHAR(32) NOT NULL DEFAULT 'PAPER_TRADING',
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED',
    clock_type VARCHAR(32) NOT NULL DEFAULT 'LIVE_CLOCK',
    data_provider VARCHAR(64) NOT NULL DEFAULT 'AUTHORIZED_FEED',
    data_freshness_status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN',
    start_time TIMESTAMP WITH TIME ZONE,
    end_time TIMESTAMP WITH TIME ZONE,
    configuration_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    engine_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    total_decisions_count INT NOT NULL DEFAULT 0,
    total_orders_count INT NOT NULL DEFAULT 0,
    total_fills_count INT NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_portfolios (
    id UUID PRIMARY KEY,
    session_id UUID REFERENCES market_data.paper_trading_sessions(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    horizon VARCHAR(32) NOT NULL DEFAULT 'SHORT_TERM',
    risk_profile_id UUID,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    initial_virtual_capital DOUBLE PRECISION NOT NULL DEFAULT 1000000.0,
    cash_balance DOUBLE PRECISION NOT NULL,
    available_cash DOUBLE PRECISION NOT NULL,
    reserved_cash DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    invested_value DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_portfolio_value DOUBLE PRECISION NOT NULL,
    peak_portfolio_value DOUBLE PRECISION NOT NULL,
    current_drawdown_pct DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    max_drawdown_pct DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    gross_exposure DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    net_exposure DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    leverage DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    total_realized_pnl DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_unrealized_pnl DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_fees_paid DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_slippage_paid DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_dividends_received DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_positions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES market_data.paper_portfolios(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    instrument_id BIGINT,
    horizon VARCHAR(32) NOT NULL,
    quantity INT NOT NULL,
    average_entry_price DOUBLE PRECISION NOT NULL,
    current_market_price DOUBLE PRECISION NOT NULL,
    cost_basis DOUBLE PRECISION NOT NULL,
    market_value DOUBLE PRECISION NOT NULL,
    unrealized_pnl DOUBLE PRECISION NOT NULL,
    unrealized_return_pct DOUBLE PRECISION NOT NULL,
    realized_pnl DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    portfolio_weight DOUBLE PRECISION NOT NULL,
    stop_price DOUBLE PRECISION,
    target_price DOUBLE PRECISION,
    stop_method VARCHAR(32),
    highest_price_seen DOUBLE PRECISION NOT NULL,
    lowest_price_seen DOUBLE PRECISION NOT NULL,
    entry_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    last_updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    signal_id UUID,
    risk_assessment_id UUID,
    model_version VARCHAR(32),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_paper_pos_port_sym UNIQUE (portfolio_id, symbol)
);

CREATE TABLE IF NOT EXISTS market_data.paper_decisions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES market_data.paper_portfolios(id) ON DELETE CASCADE,
    session_id UUID REFERENCES market_data.paper_trading_sessions(id) ON DELETE SET NULL,
    symbol VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    decision VARCHAR(20) NOT NULL,
    decision_reason TEXT NOT NULL,
    signal_id UUID,
    signal_version VARCHAR(32),
    signal_score DOUBLE PRECISION NOT NULL,
    signal_confidence DOUBLE PRECISION NOT NULL,
    expected_return DOUBLE PRECISION,
    expected_volatility DOUBLE PRECISION,
    predicted_direction VARCHAR(20),
    predicted_probability DOUBLE PRECISION,
    prediction_id UUID,
    model_version VARCHAR(32),
    risk_assessment_id UUID,
    risk_engine_version VARCHAR(32),
    suggested_allocation DOUBLE PRECISION NOT NULL,
    maximum_allocation DOUBLE PRECISION NOT NULL,
    recommended_quantity INT NOT NULL,
    entry_price DOUBLE PRECISION NOT NULL,
    stop_price DOUBLE PRECISION,
    target_price DOUBLE PRECISION,
    risk_level VARCHAR(20) NOT NULL,
    supporting_evidence JSONB,
    opposing_evidence JSONB,
    data_quality_status VARCHAR(32) NOT NULL DEFAULT 'HIGH_QUALITY',
    data_version VARCHAR(32) NOT NULL DEFAULT '1',
    feature_version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'RECORDED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_orders (
    id UUID PRIMARY KEY,
    decision_id UUID REFERENCES market_data.paper_decisions(id) ON DELETE SET NULL,
    portfolio_id UUID NOT NULL REFERENCES market_data.paper_portfolios(id) ON DELETE CASCADE,
    session_id UUID REFERENCES market_data.paper_trading_sessions(id) ON DELETE SET NULL,
    symbol VARCHAR(32) NOT NULL,
    side VARCHAR(10) NOT NULL,
    order_type VARCHAR(20) NOT NULL DEFAULT 'MARKET',
    quantity INT NOT NULL,
    requested_price DOUBLE PRECISION NOT NULL,
    executed_price DOUBLE PRECISION,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    order_submitted_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    order_executed_timestamp TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    slippage_bps DOUBLE PRECISION DEFAULT 0.0,
    slippage_amount DOUBLE PRECISION DEFAULT 0.0,
    fees_amount DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_fills (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES market_data.paper_orders(id) ON DELETE CASCADE,
    portfolio_id UUID NOT NULL REFERENCES market_data.paper_portfolios(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    side VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    requested_price DOUBLE PRECISION NOT NULL,
    fill_price DOUBLE PRECISION NOT NULL,
    slippage_bps DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    slippage_amount DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    brokerage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    stt DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    exchange_charges DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    gst DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    stamp_duty DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_fees DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    execution_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_ledger (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES market_data.paper_portfolios(id) ON DELETE CASCADE,
    session_id UUID REFERENCES market_data.paper_trading_sessions(id) ON DELETE SET NULL,
    transaction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    symbol VARCHAR(32),
    amount DOUBLE PRECISION NOT NULL,
    cash_balance_before DOUBLE PRECISION NOT NULL,
    cash_balance_after DOUBLE PRECISION NOT NULL,
    description TEXT NOT NULL,
    reference_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_equity_curve (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES market_data.paper_portfolios(id) ON DELETE CASCADE,
    snapshot_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    portfolio_value DOUBLE PRECISION NOT NULL,
    cash_balance DOUBLE PRECISION NOT NULL,
    invested_value DOUBLE PRECISION NOT NULL,
    daily_return_pct DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    cumulative_return_pct DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    drawdown_pct DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_paper_eq_port_ts UNIQUE (portfolio_id, snapshot_timestamp)
);

CREATE TABLE IF NOT EXISTS market_data.paper_signal_outcomes (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL REFERENCES market_data.paper_decisions(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    evaluation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    expected_direction VARCHAR(20) NOT NULL,
    realized_direction VARCHAR(20) NOT NULL,
    expected_return DOUBLE PRECISION NOT NULL,
    realized_return DOUBLE PRECISION NOT NULL,
    outcome_status VARCHAR(32) NOT NULL,
    attribution JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_prediction_outcomes (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL REFERENCES market_data.paper_decisions(id) ON DELETE CASCADE,
    prediction_id UUID,
    model_version VARCHAR(32) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    prediction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    evaluation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    expected_return DOUBLE PRECISION NOT NULL,
    realized_return DOUBLE PRECISION NOT NULL,
    prediction_error DOUBLE PRECISION NOT NULL,
    absolute_error DOUBLE PRECISION NOT NULL,
    squared_error DOUBLE PRECISION NOT NULL,
    expected_volatility DOUBLE PRECISION,
    realized_volatility DOUBLE PRECISION,
    is_direction_correct BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_model_monitoring (
    id UUID PRIMARY KEY,
    model_version VARCHAR(32) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    evaluation_window_start DATE NOT NULL,
    evaluation_window_end DATE NOT NULL,
    sample_size INT NOT NULL,
    directional_accuracy DOUBLE PRECISION NOT NULL,
    mae DOUBLE PRECISION NOT NULL,
    rmse DOUBLE PRECISION NOT NULL,
    ic DOUBLE PRECISION,
    rank_ic DOUBLE PRECISION,
    drift_status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
    model_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_risk_monitoring (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES market_data.paper_portfolios(id) ON DELETE CASCADE,
    evaluation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    current_drawdown_pct DOUBLE PRECISION NOT NULL,
    max_position_weight DOUBLE PRECISION NOT NULL,
    max_sector_weight DOUBLE PRECISION NOT NULL,
    cash_buffer_pct DOUBLE PRECISION NOT NULL,
    limit_breached BOOLEAN NOT NULL DEFAULT false,
    breach_type VARCHAR(64),
    action_taken VARCHAR(32) NOT NULL DEFAULT 'NONE',
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS market_data.paper_data_health (
    id UUID PRIMARY KEY,
    provider_name VARCHAR(64) NOT NULL,
    connection_status VARCHAR(32) NOT NULL,
    last_successful_update TIMESTAMP WITH TIME ZONE,
    last_market_timestamp TIMESTAMP WITH TIME ZONE,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    data_age_seconds INT NOT NULL DEFAULT 0,
    freshness_status VARCHAR(32) NOT NULL,
    error_count INT NOT NULL DEFAULT 0,
    coverage_ratio DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_paper_sess_status ON market_data.paper_trading_sessions(status);
CREATE INDEX IF NOT EXISTS idx_paper_port_sess ON market_data.paper_portfolios(session_id);
CREATE INDEX IF NOT EXISTS idx_paper_pos_port ON market_data.paper_positions(portfolio_id, is_active);
CREATE INDEX IF NOT EXISTS idx_paper_dec_port_ts ON market_data.paper_decisions(portfolio_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_paper_dec_sym_ts ON market_data.paper_decisions(symbol, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_paper_ord_port_ts ON market_data.paper_orders(portfolio_id, order_submitted_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_paper_fill_ord ON market_data.paper_fills(order_id);
CREATE INDEX IF NOT EXISTS idx_paper_led_port_ts ON market_data.paper_ledger(portfolio_id, transaction_timestamp ASC);
CREATE INDEX IF NOT EXISTS idx_paper_eq_port_ts ON market_data.paper_equity_curve(portfolio_id, snapshot_timestamp ASC);
CREATE INDEX IF NOT EXISTS idx_paper_sig_out_dec ON market_data.paper_signal_outcomes(decision_id);
CREATE INDEX IF NOT EXISTS idx_paper_pred_out_dec ON market_data.paper_prediction_outcomes(decision_id);
CREATE INDEX IF NOT EXISTS idx_paper_mod_mon_ver ON market_data.paper_model_monitoring(model_version, horizon);
CREATE INDEX IF NOT EXISTS idx_paper_dh_prov ON market_data.paper_data_health(provider_name, checked_at DESC);

DO $$
BEGIN
  RAISE NOTICE 'QuantLab market data database initialized successfully';
END $$;
