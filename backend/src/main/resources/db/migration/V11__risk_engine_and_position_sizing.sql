-- V11__risk_engine_and_position_sizing.sql
-- Schema for QuantLab Part 14: Production Risk Engine & Position Sizing

CREATE TABLE IF NOT EXISTS risk_profiles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    profile_type VARCHAR(32) NOT NULL DEFAULT 'MODERATE', -- CONSERVATIVE, MODERATE, AGGRESSIVE, CUSTOM
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

CREATE TABLE IF NOT EXISTS portfolios (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    risk_profile_id UUID REFERENCES risk_profiles(id),
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

CREATE TABLE IF NOT EXISTS portfolio_positions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    instrument_id BIGINT,
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

CREATE TABLE IF NOT EXISTS portfolio_snapshots (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS risk_assessments (
    id UUID PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    portfolio_id UUID REFERENCES portfolios(id),
    risk_profile_id UUID REFERENCES risk_profiles(id),
    signal_id UUID,
    symbol VARCHAR(32) NOT NULL,
    signal_type VARCHAR(20) NOT NULL,
    signal_score DOUBLE PRECISION NOT NULL,
    signal_confidence DOUBLE PRECISION NOT NULL,
    
    -- Sizing outputs
    suggested_allocation DOUBLE PRECISION NOT NULL,
    maximum_allocation DOUBLE PRECISION NOT NULL,
    recommended_quantity DOUBLE PRECISION NOT NULL,
    entry_price DOUBLE PRECISION NOT NULL,
    stop_price DOUBLE PRECISION NOT NULL,
    target_price DOUBLE PRECISION,
    stop_distance DOUBLE PRECISION NOT NULL,
    stop_distance_pct DOUBLE PRECISION NOT NULL,
    stop_method VARCHAR(32) NOT NULL,
    
    -- Risk metrics
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
    
    -- Decision
    risk_decision VARCHAR(40) NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    
    -- Machine-readable traces and explanations
    risk_trace JSONB NOT NULL,
    limiting_constraints JSONB,
    risk_warnings JSONB,
    reasoning TEXT NOT NULL,
    data_quality_status VARCHAR(30) NOT NULL,
    
    -- Version provenance
    risk_engine_version VARCHAR(32) NOT NULL DEFAULT 'RISK_v1.0.0',
    risk_profile_version VARCHAR(32) NOT NULL DEFAULT 'RP_v1.0.0',
    signal_version VARCHAR(32),
    data_version VARCHAR(32) NOT NULL DEFAULT '1',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS risk_adjustments (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES risk_assessments(id) ON DELETE CASCADE,
    adjustment_type VARCHAR(50) NOT NULL,
    multiplier DOUBLE PRECISION NOT NULL,
    base_allocation DOUBLE PRECISION NOT NULL,
    adjusted_allocation DOUBLE PRECISION NOT NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS risk_warnings (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES risk_assessments(id) ON DELETE CASCADE,
    warning_code VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL, -- LOW, MODERATE, HIGH, CRITICAL
    message TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS correlation_snapshots (
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

CREATE TABLE IF NOT EXISTS drawdown_snapshots (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES portfolios(id) ON DELETE CASCADE,
    snapshot_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    peak_value DOUBLE PRECISION NOT NULL,
    current_value DOUBLE PRECISION NOT NULL,
    drawdown_pct DOUBLE PRECISION NOT NULL,
    drawdown_duration_days INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for point-in-time querying and rapid portfolio evaluation
CREATE INDEX IF NOT EXISTS idx_portfolios_active ON portfolios(is_active);
CREATE INDEX IF NOT EXISTS idx_portfolio_pos_port ON portfolio_positions(portfolio_id, is_active);
CREATE INDEX IF NOT EXISTS idx_portfolio_pos_sym ON portfolio_positions(symbol);
CREATE INDEX IF NOT EXISTS idx_port_snaps_port_ts ON portfolio_snapshots(portfolio_id, snapshot_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_risk_assess_sym_ts ON risk_assessments(symbol, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_risk_assess_port_ts ON risk_assessments(portfolio_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_risk_assess_info_avail ON risk_assessments(information_available_at);
CREATE INDEX IF NOT EXISTS idx_correl_snaps_syms ON correlation_snapshots(symbol_a, symbol_b, snapshot_timestamp DESC);
