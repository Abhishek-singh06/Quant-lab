-- =============================================================================
-- V9__quant_prediction_and_walk_forward.sql
-- Schema Migration for Parts 11 & 12: Production Quant Prediction Models & Walk-Forward Engine
-- =============================================================================

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
    model_type VARCHAR(32) NOT NULL, -- REGRESSION, CLASSIFICATION, VOLATILITY
    algorithm VARCHAR(64) NOT NULL,  -- RIDGE, RANDOM_FOREST, GRADIENT_BOOSTING, LOGISTIC
    model_version VARCHAR(32) NOT NULL UNIQUE,
    feature_version VARCHAR(16) NOT NULL,
    dataset_version VARCHAR(32) NOT NULL,
    target_definition VARCHAR(64) NOT NULL,
    target_horizon VARCHAR(16) NOT NULL, -- 1D, 5D, 10D, 20D, 63D
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
    status VARCHAR(32) NOT NULL DEFAULT 'VALIDATED', -- TRAINING, VALIDATED, CANDIDATE, PRODUCTION, RETIRED, FAILED
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
    prediction_type VARCHAR(32) NOT NULL, -- REGRESSION, CLASSIFICATION, VOLATILITY
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
    mode VARCHAR(32) NOT NULL, -- EXPANDING, ROLLING
    initial_train_start DATE NOT NULL,
    initial_train_end DATE NOT NULL,
    step_size VARCHAR(16) NOT NULL, -- QUARTERLY, ANNUAL, MONTHLY
    model_type VARCHAR(32) NOT NULL,
    target_definition VARCHAR(64) NOT NULL,
    purge_window_days INTEGER NOT NULL DEFAULT 5,
    embargo_window_days INTEGER NOT NULL DEFAULT 2,
    total_folds INTEGER NOT NULL,
    completed_folds INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'COMPLETED', -- RUNNING, COMPLETED, FAILED
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
