-- V12__trading_and_investing_models.sql
-- Schema for QuantLab Part 15: Trading + Investing Models (Short-Term, Medium-Term, Long-Term)

CREATE TABLE IF NOT EXISTS horizon_feature_sets (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    horizon VARCHAR(32) NOT NULL, -- SHORT_TERM, MEDIUM_TERM, LONG_TERM
    feature_names JSONB NOT NULL,
    feature_definitions JSONB NOT NULL,
    lookback_days INT NOT NULL,
    sources JSONB NOT NULL,
    version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS horizon_target_sets (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    horizon VARCHAR(32) NOT NULL, -- SHORT_TERM, MEDIUM_TERM, LONG_TERM
    target_period VARCHAR(32) NOT NULL, -- ONE_DAY, FIVE_DAYS, FOUR_WEEKS, ONE_YEAR, etc.
    target_type VARCHAR(32) NOT NULL, -- REGRESSION, CLASSIFICATION, VOLATILITY, RELATIVE_RETURN
    formula TEXT NOT NULL,
    threshold DOUBLE PRECISION,
    benchmark_symbol VARCHAR(32),
    version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS horizon_model_configs (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    horizon VARCHAR(32) NOT NULL, -- SHORT_TERM, MEDIUM_TERM, LONG_TERM
    target_period VARCHAR(32) NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    feature_set_id UUID REFERENCES horizon_feature_sets(id),
    target_set_id UUID REFERENCES horizon_target_sets(id),
    algorithm VARCHAR(64) NOT NULL, -- RIDGE, ELASTIC_NET, RANDOM_FOREST, GRADIENT_BOOSTING, LOGISTIC_REGRESSION
    hyperparameters JSONB NOT NULL,
    retrain_frequency VARCHAR(32) NOT NULL DEFAULT 'WEEKLY', -- DAILY, WEEKLY, MONTHLY, QUARTERLY
    version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS horizon_datasets (
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

CREATE TABLE IF NOT EXISTS horizon_model_versions (
    id UUID PRIMARY KEY,
    config_id UUID REFERENCES horizon_model_configs(id),
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
    model_status VARCHAR(32) NOT NULL DEFAULT 'CANDIDATE', -- CANDIDATE, VALIDATED, CHAMPION, RETIRED
    model_availability_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    metrics JSONB NOT NULL,
    feature_importances JSONB,
    model_artifacts_path TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_horizon_model_ver UNIQUE (model_id, model_version)
);

CREATE TABLE IF NOT EXISTS horizon_predictions (
    id UUID PRIMARY KEY,
    model_version_id UUID REFERENCES horizon_model_versions(id),
    symbol VARCHAR(32) NOT NULL,
    instrument_id BIGINT,
    prediction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    information_available_at TIMESTAMP WITH TIME ZONE NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    horizon VARCHAR(32) NOT NULL, -- SHORT_TERM, MEDIUM_TERM, LONG_TERM
    horizon_period VARCHAR(32) NOT NULL, -- ONE_DAY, FIVE_DAYS, FOUR_WEEKS, ONE_YEAR, etc.
    expected_return DOUBLE PRECISION,
    probability_positive DOUBLE PRECISION,
    probability_negative DOUBLE PRECISION,
    predicted_class INT,
    expected_volatility DOUBLE PRECISION,
    expected_drawdown DOUBLE PRECISION,
    relative_return DOUBLE PRECISION,
    confidence DOUBLE PRECISION NOT NULL,
    outlook VARCHAR(20) NOT NULL, -- BULLISH, BEARISH, NEUTRAL
    feature_contributions JSONB,
    model_version VARCHAR(32) NOT NULL,
    feature_set_version VARCHAR(32) NOT NULL,
    target_set_version VARCHAR(32) NOT NULL,
    data_version VARCHAR(32) NOT NULL DEFAULT '1',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS horizon_evaluations (
    id UUID PRIMARY KEY,
    model_version_id UUID NOT NULL REFERENCES horizon_model_versions(id) ON DELETE CASCADE,
    horizon VARCHAR(32) NOT NULL,
    target_period VARCHAR(32) NOT NULL,
    evaluation_type VARCHAR(32) NOT NULL DEFAULT 'WALK_FORWARD', -- IN_SAMPLE, VALIDATION, TEST, WALK_FORWARD
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

CREATE TABLE IF NOT EXISTS horizon_conflicts (
    id UUID PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    short_term_outlook VARCHAR(20) NOT NULL,
    medium_term_outlook VARCHAR(20) NOT NULL,
    long_term_outlook VARCHAR(20) NOT NULL,
    conflict_detected BOOLEAN NOT NULL DEFAULT false,
    conflict_severity VARCHAR(20) NOT NULL DEFAULT 'NONE', -- NONE, LOW, MEDIUM, HIGH
    explanation TEXT NOT NULL,
    short_term_pred_id UUID REFERENCES horizon_predictions(id),
    medium_term_pred_id UUID REFERENCES horizon_predictions(id),
    long_term_pred_id UUID REFERENCES horizon_predictions(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS horizon_monitoring (
    id UUID PRIMARY KEY,
    model_version_id UUID NOT NULL REFERENCES horizon_model_versions(id) ON DELETE CASCADE,
    horizon VARCHAR(32) NOT NULL,
    window_start DATE NOT NULL,
    window_end DATE NOT NULL,
    rolling_ic DOUBLE PRECISION,
    rolling_directional_accuracy DOUBLE PRECISION,
    rolling_brier_score DOUBLE PRECISION,
    psi_score DOUBLE PRECISION,
    ks_statistic DOUBLE PRECISION,
    drift_status VARCHAR(32) NOT NULL DEFAULT 'STABLE', -- STABLE, WARNING, DECAYED
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Point-in-time and rapid querying indexes
CREATE INDEX IF NOT EXISTS idx_horizon_pred_sym_ts ON horizon_predictions(symbol, prediction_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_horizon_pred_hor_ts ON horizon_predictions(horizon, prediction_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_horizon_pred_info_avail ON horizon_predictions(information_available_at);
CREATE INDEX IF NOT EXISTS idx_horizon_model_ver_status ON horizon_model_versions(horizon, model_status);
CREATE INDEX IF NOT EXISTS idx_horizon_conflicts_sym_ts ON horizon_conflicts(symbol, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_horizon_eval_ver_type ON horizon_evaluations(model_version_id, evaluation_type);
