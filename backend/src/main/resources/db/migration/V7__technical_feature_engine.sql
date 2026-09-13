-- ==========================================================
-- QuantLab Migration V7: Production Technical Feature Engine Schema
-- ==========================================================

-- 1. Feature Definitions Registry
CREATE TABLE IF NOT EXISTS market_data.feature_definitions (
    id BIGSERIAL PRIMARY KEY,
    feature_name VARCHAR(64) NOT NULL UNIQUE,
    category VARCHAR(32) NOT NULL, -- MOMENTUM, TREND, VOLATILITY, VOLUME, STATISTICAL, RELATIVE_STRENGTH
    description VARCHAR(255) NOT NULL,
    default_lookback INT NOT NULL,
    timeframe VARCHAR(16) NOT NULL DEFAULT '1D',
    feature_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    formula_version VARCHAR(16) NOT NULL DEFAULT '1.0.0',
    price_series_type VARCHAR(32) NOT NULL DEFAULT 'SPLIT_ADJUSTED', -- RAW, SPLIT_ADJUSTED, TOTAL_RETURN
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_feat_def_category ON market_data.feature_definitions(category);
CREATE INDEX IF NOT EXISTS idx_feat_def_enabled ON market_data.feature_definitions(is_enabled);

-- 2. Technical Features Store (Normalized Time Series)
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

-- 3. Feature Calculation Runs Audit
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

CREATE INDEX IF NOT EXISTS idx_feat_run_time ON market_data.feature_calculation_runs(start_time DESC);
CREATE INDEX IF NOT EXISTS idx_feat_run_status ON market_data.feature_calculation_runs(status);

-- 4. Feature Data Quality & Anomaly Log
CREATE TABLE IF NOT EXISTS market_data.feature_data_quality (
    id BIGSERIAL PRIMARY KEY,
    run_id VARCHAR(64),
    instrument_id BIGINT NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    feature_name VARCHAR(64) NOT NULL,
    feature_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    feature_value NUMERIC(24, 8),
    validation_status VARCHAR(32) NOT NULL, -- VALID, OUT_OF_BOUNDS, NULL_VALUE, INSUFFICIENT_HISTORY, CALCULATION_ERROR
    anomaly_reason VARCHAR(255),
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_feat_dq_inst ON market_data.feature_data_quality(instrument_id);
CREATE INDEX IF NOT EXISTS idx_feat_dq_status ON market_data.feature_data_quality(validation_status);

-- Seed Initial Feature Definitions Registry
INSERT INTO market_data.feature_definitions (feature_name, category, description, default_lookback, timeframe, feature_version, price_series_type)
VALUES
    -- Returns
    ('RETURN_1D', 'MOMENTUM', '1-Day Simple Price Return', 1, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('RETURN_5D', 'MOMENTUM', '5-Day Simple Price Return', 5, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('RETURN_10D', 'MOMENTUM', '10-Day Simple Price Return', 10, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('RETURN_20D', 'MOMENTUM', '20-Day Simple Price Return', 20, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('RETURN_21D', 'MOMENTUM', '21-Day (1-Month) Simple Price Return', 21, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('RETURN_63D', 'MOMENTUM', '63-Day (3-Month) Simple Price Return', 63, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('RETURN_126D', 'MOMENTUM', '126-Day (6-Month) Simple Price Return', 126, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('RETURN_252D', 'MOMENTUM', '252-Day (1-Year) Simple Price Return', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('LOG_RETURN_1D', 'MOMENTUM', '1-Day Logarithmic Return', 1, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    -- Trend Moving Averages
    ('SMA_5', 'TREND', '5-Day Simple Moving Average', 5, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('SMA_10', 'TREND', '10-Day Simple Moving Average', 10, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('SMA_20', 'TREND', '20-Day Simple Moving Average', 20, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('SMA_50', 'TREND', '50-Day Simple Moving Average', 50, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('SMA_100', 'TREND', '100-Day Simple Moving Average', 100, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('SMA_200', 'TREND', '200-Day Simple Moving Average', 200, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('EMA_5', 'TREND', '5-Day Exponential Moving Average', 5, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('EMA_10', 'TREND', '10-Day Exponential Moving Average', 10, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('EMA_20', 'TREND', '20-Day Exponential Moving Average', 20, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('EMA_50', 'TREND', '50-Day Exponential Moving Average', 50, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('EMA_100', 'TREND', '100-Day Exponential Moving Average', 100, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('EMA_200', 'TREND', '200-Day Exponential Moving Average', 200, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    -- Oscillators & Momentum
    ('RSI_14', 'MOMENTUM', '14-Day Relative Strength Index (Wilder smoothed)', 14, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MACD_LINE_12_26', 'TREND', 'MACD Line (EMA 12 - EMA 26)', 26, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MACD_SIGNAL_9', 'TREND', 'MACD Signal Line (9-day EMA of MACD Line)', 35, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MACD_HISTOGRAM_12_26_9', 'TREND', 'MACD Histogram (MACD Line - Signal)', 35, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MOMENTUM_5', 'MOMENTUM', '5-Day Price Momentum Ratio', 5, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MOMENTUM_10', 'MOMENTUM', '10-Day Price Momentum Ratio', 10, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MOMENTUM_20', 'MOMENTUM', '20-Day Price Momentum Ratio', 20, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MOMENTUM_63', 'MOMENTUM', '63-Day Price Momentum Ratio', 63, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MOMENTUM_126', 'MOMENTUM', '126-Day Price Momentum Ratio', 126, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MOMENTUM_252', 'MOMENTUM', '252-Day Price Momentum Ratio', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    -- Volatility & Risk
    ('ATR_14', 'VOLATILITY', '14-Day Average True Range', 14, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('ATR_PERCENT_14', 'VOLATILITY', '14-Day ATR as Percentage of Price', 14, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('VOLATILITY_10D', 'VOLATILITY', '10-Day Realized Annualized Volatility', 10, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('VOLATILITY_20D', 'VOLATILITY', '20-Day Realized Annualized Volatility', 20, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('VOLATILITY_21D', 'VOLATILITY', '21-Day Realized Annualized Volatility', 21, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('VOLATILITY_63D', 'VOLATILITY', '63-Day Realized Annualized Volatility', 63, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('VOLATILITY_252D', 'VOLATILITY', '252-Day Realized Annualized Volatility', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    -- Volume Ratios
    ('VOLUME_RATIO_5', 'VOLUME', 'Current Volume / 5-Day SMA Volume', 5, '1D', '1.0.0', 'RAW'),
    ('VOLUME_RATIO_10', 'VOLUME', 'Current Volume / 10-Day SMA Volume', 10, '1D', '1.0.0', 'RAW'),
    ('VOLUME_RATIO_20', 'VOLUME', 'Current Volume / 20-Day SMA Volume', 20, '1D', '1.0.0', 'RAW'),
    ('VOLUME_RATIO_50', 'VOLUME', 'Current Volume / 50-Day SMA Volume', 50, '1D', '1.0.0', 'RAW'),
    -- 52-Week Range & Drawdowns
    ('WEEK_52_HIGH', 'STATISTICAL', 'Trailing 52-Week (252-Day) High Price', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('WEEK_52_LOW', 'STATISTICAL', 'Trailing 52-Week (252-Day) Low Price', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('WEEK_52_POSITION', 'STATISTICAL', 'Normalized 52-Week Position (0 to 1)', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('DISTANCE_FROM_52W_HIGH', 'STATISTICAL', 'Percentage Distance from 52-Week High', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('DISTANCE_FROM_52W_LOW', 'STATISTICAL', 'Percentage Distance from 52-Week Low', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('DRAWDOWN', 'STATISTICAL', 'Current Drawdown from Trailing Peak', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MAX_DRAWDOWN_20', 'STATISTICAL', 'Maximum Drawdown over 20 Days', 20, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MAX_DRAWDOWN_63', 'STATISTICAL', 'Maximum Drawdown over 63 Days', 63, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MAX_DRAWDOWN_126', 'STATISTICAL', 'Maximum Drawdown over 126 Days', 126, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('MAX_DRAWDOWN_252', 'STATISTICAL', 'Maximum Drawdown over 252 Days', 252, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    -- Relative Strength (vs Benchmark)
    ('RS_NIFTY_20', 'RELATIVE_STRENGTH', '20-Day Return Relative to NIFTY 50 Benchmark', 20, '1D', '1.0.0', 'SPLIT_ADJUSTED'),
    ('RS_NIFTY_63', 'RELATIVE_STRENGTH', '63-Day Return Relative to NIFTY 50 Benchmark', 63, '1D', '1.0.0', 'SPLIT_ADJUSTED')
ON CONFLICT (feature_name) DO NOTHING;
