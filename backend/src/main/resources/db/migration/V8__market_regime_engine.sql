-- ==========================================================
-- QuantLab Migration V8: Production Market Regime Engine Schema
-- ==========================================================

-- 1. Model Versions Master
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

-- 2. Market Regime Master Store
CREATE TABLE IF NOT EXISTS market_data.market_regimes (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL DEFAULT 'NIFTY 50',
    regime_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    trading_date DATE NOT NULL,
    direction_regime VARCHAR(32) NOT NULL, -- BULL, BEAR, SIDEWAYS, TRANSITION
    volatility_regime VARCHAR(32) NOT NULL, -- LOW_VOL, NORMAL_VOL, HIGH_VOL, EXTREME_VOL
    risk_regime VARCHAR(32) NOT NULL, -- RISK_ON, NEUTRAL, RISK_OFF
    direction_score NUMERIC(10, 4) NOT NULL, -- -100 to +100
    volatility_score NUMERIC(10, 4) NOT NULL, -- -100 to +100
    risk_score NUMERIC(10, 4) NOT NULL, -- -100 to +100
    confidence NUMERIC(6, 4) NOT NULL, -- 0.0 to 1.0
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

-- 3. Component Level Signals & Scores (Audit & Explainability)
CREATE TABLE IF NOT EXISTS market_data.regime_component_scores (
    id BIGSERIAL PRIMARY KEY,
    regime_id BIGINT NOT NULL REFERENCES market_data.market_regimes(id) ON DELETE CASCADE,
    component_name VARCHAR(64) NOT NULL, -- NIFTY_TREND, MARKET_BREADTH, VOLATILITY_VIX, MOMENTUM, GLOBAL_RISK, FII_FLOWS, DII_FLOWS, DOMESTIC_MACRO, INTEREST_RATES, USD_INR, SECTOR_PARTICIPATION
    raw_value NUMERIC(18, 4),
    normalized_value NUMERIC(10, 4),
    component_score NUMERIC(10, 4) NOT NULL, -- -100 to +100
    configured_weight NUMERIC(6, 4) NOT NULL,
    effective_weight NUMERIC(6, 4) NOT NULL,
    confidence VARCHAR(16) NOT NULL DEFAULT 'HIGH', -- HIGH, MEDIUM, LOW, MISSING
    source VARCHAR(64) NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    details JSONB
);

CREATE INDEX IF NOT EXISTS idx_reg_comp_reg_id ON market_data.regime_component_scores(regime_id);
CREATE INDEX IF NOT EXISTS idx_reg_comp_name ON market_data.regime_component_scores(component_name);

-- 4. Historical Evaluation Runs & Metrics
CREATE TABLE IF NOT EXISTS market_data.regime_evaluation_runs (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    model_version VARCHAR(32) NOT NULL,
    evaluation_type VARCHAR(32) NOT NULL DEFAULT 'WALK_FORWARD', -- IN_SAMPLE, OUT_OF_SAMPLE, WALK_FORWARD
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

-- Seed Default Model Version
INSERT INTO market_data.regime_model_versions (model_version, model_type, feature_version, normalization_version, description, weights_json, is_active)
VALUES (
    'REGIME_v1.0.0',
    'WEIGHTED_COMPOSITE_PROBABILISTIC',
    '1.0.0',
    '1.0.0',
    'Multi-dimensional composite regime model combining NIFTY trend, breadth, volatility, momentum, global risk, institutional flows, and macro rates.',
    '{"trendWeight": 0.25, "breadthWeight": 0.15, "volatilityWeight": 0.15, "momentumWeight": 0.15, "globalRiskWeight": 0.10, "fiiFlowWeight": 0.08, "diiFlowWeight": 0.04, "ratesWeight": 0.04, "sectorWeight": 0.04}',
    TRUE
) ON CONFLICT (model_version) DO NOTHING;
