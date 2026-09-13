-- V10__cross_check_signal_engine.sql
-- Schema for QuantLab Part 13: Production Cross-Check / Signal Engine

CREATE TABLE IF NOT EXISTS signal_configurations (
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

CREATE TABLE IF NOT EXISTS signal_generation_runs (
    id UUID PRIMARY KEY,
    run_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    as_of_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    instrument_count INT NOT NULL DEFAULT 0,
    signal_count INT NOT NULL DEFAULT 0,
    configuration_version VARCHAR(50) NOT NULL,
    signal_version VARCHAR(50) NOT NULL,
    model_version VARCHAR(50),
    regime_version VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    duration_ms BIGINT DEFAULT 0,
    error_summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS signals (
    id UUID PRIMARY KEY,
    run_id UUID REFERENCES signal_generation_runs(id) ON DELETE SET NULL,
    instrument_id UUID,
    symbol VARCHAR(30) NOT NULL,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    signal VARCHAR(20) NOT NULL, -- BUY, HOLD, SELL, NO_SIGNAL
    signal_score DOUBLE PRECISION NOT NULL, -- -100 to +100
    confidence DOUBLE PRECISION NOT NULL, -- 0.0 to 1.0
    expected_return DOUBLE PRECISION,
    expected_volatility DOUBLE PRECISION,
    return_to_volatility_ratio DOUBLE PRECISION,
    direction VARCHAR(20) NOT NULL, -- BULLISH, BEARISH, NEUTRAL
    conflict_severity VARCHAR(20) NOT NULL, -- LOW, MEDIUM, HIGH
    conflict_score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    data_quality_status VARCHAR(30) NOT NULL, -- HIGH_QUALITY, MEDIUM_QUALITY, LOW_QUALITY, INSUFFICIENT_DATA
    freshness_score DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    reasoning TEXT NOT NULL,
    structured_reasoning JSONB,
    supporting_categories JSONB,
    opposing_categories JSONB,
    signal_version VARCHAR(30) NOT NULL,
    configuration_version VARCHAR(30) NOT NULL,
    feature_version VARCHAR(30),
    model_version VARCHAR(30),
    regime_version VARCHAR(30),
    data_version VARCHAR(30),
    is_latest BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS signal_components (
    id UUID PRIMARY KEY,
    signal_id UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    category_score DOUBLE PRECISION NOT NULL, -- -100 to +100
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

CREATE TABLE IF NOT EXISTS signal_evidence (
    id UUID PRIMARY KEY,
    signal_id UUID NOT NULL REFERENCES signals(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    feature_name VARCHAR(100) NOT NULL,
    raw_value DOUBLE PRECISION,
    raw_value_str TEXT,
    normalized_score DOUBLE PRECISION NOT NULL, -- -100 to +100
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

CREATE TABLE IF NOT EXISTS signal_transitions (
    id UUID PRIMARY KEY,
    instrument_id UUID,
    symbol VARCHAR(30) NOT NULL,
    previous_signal VARCHAR(20),
    new_signal VARCHAR(20) NOT NULL,
    previous_score DOUBLE PRECISION,
    new_score DOUBLE PRECISION NOT NULL,
    transition_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    transition_reason TEXT NOT NULL,
    signal_id UUID REFERENCES signals(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for point-in-time querying and symbol lookups
CREATE INDEX IF NOT EXISTS idx_signals_symbol_timestamp ON signals(symbol, signal_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_signals_symbol_latest ON signals(symbol, is_latest);
CREATE INDEX IF NOT EXISTS idx_signals_available_at ON signals(information_available_at);
CREATE INDEX IF NOT EXISTS idx_signal_components_signal ON signal_components(signal_id);
CREATE INDEX IF NOT EXISTS idx_signal_evidence_signal ON signal_evidence(signal_id);
CREATE INDEX IF NOT EXISTS idx_signal_transitions_symbol ON signal_transitions(symbol, transition_timestamp DESC);
