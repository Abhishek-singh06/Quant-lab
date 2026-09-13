-- V14__paper_trading_engine.sql
-- Schema for QuantLab Part 17: Production Paper Trading Engine

CREATE TABLE IF NOT EXISTS paper_trading_sessions (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    execution_mode VARCHAR(32) NOT NULL DEFAULT 'PAPER_TRADING', -- Explicit PAPER_TRADING (strictly no real broker routing)
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED', -- CREATED, STARTING, RUNNING, PAUSED, DATA_DEGRADED, DISCONNECTED, STOPPED, FAILED
    clock_type VARCHAR(32) NOT NULL DEFAULT 'LIVE_CLOCK', -- LIVE_CLOCK, REPLAY_CLOCK
    data_provider VARCHAR(64) NOT NULL DEFAULT 'AUTHORIZED_FEED',
    data_freshness_status VARCHAR(32) NOT NULL DEFAULT 'UNKNOWN', -- REAL_TIME, DELAYED, STALE, NOT_AVAILABLE
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

CREATE TABLE IF NOT EXISTS paper_portfolios (
    id UUID PRIMARY KEY,
    session_id UUID REFERENCES paper_trading_sessions(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL UNIQUE,
    horizon VARCHAR(32) NOT NULL DEFAULT 'SHORT_TERM', -- SHORT_TERM, MEDIUM_TERM, LONG_TERM
    risk_profile_id UUID,
    currency VARCHAR(10) NOT NULL DEFAULT 'INR',
    initial_virtual_capital DOUBLE PRECISION NOT NULL DEFAULT 1000000.0, -- 10 Lakhs INR default virtual capital
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
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, CLOSED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS paper_positions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES paper_portfolios(id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS paper_decisions (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES paper_portfolios(id) ON DELETE CASCADE,
    session_id UUID REFERENCES paper_trading_sessions(id) ON DELETE SET NULL,
    symbol VARCHAR(32) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    decision VARCHAR(20) NOT NULL, -- BUY, HOLD, SELL, NO_TRADE
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
    status VARCHAR(32) NOT NULL DEFAULT 'RECORDED', -- RECORDED, EXECUTED, REJECTED_RISK, REJECTED_CASH, REJECTED_DATA
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS paper_orders (
    id UUID PRIMARY KEY,
    decision_id UUID REFERENCES paper_decisions(id) ON DELETE SET NULL,
    portfolio_id UUID NOT NULL REFERENCES paper_portfolios(id) ON DELETE CASCADE,
    session_id UUID REFERENCES paper_trading_sessions(id) ON DELETE SET NULL,
    symbol VARCHAR(32) NOT NULL,
    side VARCHAR(10) NOT NULL, -- BUY, SELL
    order_type VARCHAR(20) NOT NULL DEFAULT 'MARKET', -- MARKET, LIMIT, STOP
    quantity INT NOT NULL,
    requested_price DOUBLE PRECISION NOT NULL,
    executed_price DOUBLE PRECISION,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    order_submitted_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    order_executed_timestamp TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, FILLED, REJECTED, CANCELLED
    rejection_reason TEXT,
    slippage_bps DOUBLE PRECISION DEFAULT 0.0,
    slippage_amount DOUBLE PRECISION DEFAULT 0.0,
    fees_amount DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS paper_fills (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES paper_orders(id) ON DELETE CASCADE,
    portfolio_id UUID NOT NULL REFERENCES paper_portfolios(id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS paper_ledger (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES paper_portfolios(id) ON DELETE CASCADE,
    session_id UUID REFERENCES paper_trading_sessions(id) ON DELETE SET NULL,
    transaction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    event_type VARCHAR(32) NOT NULL, -- ORDER_CREATED, ORDER_FILLED, ORDER_REJECTED, POSITION_OPENED, POSITION_REDUCED, POSITION_CLOSED, DIVIDEND_RECEIVED, CORPORATE_ACTION, FEE_CHARGED, SLIPPAGE, RISK_BLOCK, DATA_OUTAGE
    symbol VARCHAR(32),
    amount DOUBLE PRECISION NOT NULL, -- positive for cash credit, negative for cash debit
    cash_balance_before DOUBLE PRECISION NOT NULL,
    cash_balance_after DOUBLE PRECISION NOT NULL,
    description TEXT NOT NULL,
    reference_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS paper_equity_curve (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES paper_portfolios(id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS paper_signal_outcomes (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL REFERENCES paper_decisions(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    evaluation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    expected_direction VARCHAR(20) NOT NULL,
    realized_direction VARCHAR(20) NOT NULL,
    expected_return DOUBLE PRECISION NOT NULL,
    realized_return DOUBLE PRECISION NOT NULL,
    outcome_status VARCHAR(32) NOT NULL, -- CORRECT_DIRECTION, WRONG_DIRECTION, PARTIAL, EXPIRED, NO_OUTCOME_YET
    attribution JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS paper_prediction_outcomes (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL REFERENCES paper_decisions(id) ON DELETE CASCADE,
    prediction_id UUID,
    model_version VARCHAR(32) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    horizon VARCHAR(32) NOT NULL,
    prediction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    evaluation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    expected_return DOUBLE PRECISION NOT NULL,
    realized_return DOUBLE PRECISION NOT NULL,
    prediction_error DOUBLE PRECISION NOT NULL, -- realized - expected
    absolute_error DOUBLE PRECISION NOT NULL,
    squared_error DOUBLE PRECISION NOT NULL,
    expected_volatility DOUBLE PRECISION,
    realized_volatility DOUBLE PRECISION,
    is_direction_correct BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS paper_model_monitoring (
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
    drift_status VARCHAR(32) NOT NULL DEFAULT 'NORMAL', -- NORMAL, WARNING, DRIFT, INSUFFICIENT_DATA
    model_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, WARNING, DEGRADED, PAUSED, RETIRED
    evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS paper_risk_monitoring (
    id UUID PRIMARY KEY,
    portfolio_id UUID NOT NULL REFERENCES paper_portfolios(id) ON DELETE CASCADE,
    evaluation_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    current_drawdown_pct DOUBLE PRECISION NOT NULL,
    max_position_weight DOUBLE PRECISION NOT NULL,
    max_sector_weight DOUBLE PRECISION NOT NULL,
    cash_buffer_pct DOUBLE PRECISION NOT NULL,
    limit_breached BOOLEAN NOT NULL DEFAULT false,
    breach_type VARCHAR(64),
    action_taken VARCHAR(32) NOT NULL DEFAULT 'NONE', -- NONE, WARN, REDUCE, BLOCK_NEW_POSITION
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS paper_data_health (
    id UUID PRIMARY KEY,
    provider_name VARCHAR(64) NOT NULL,
    connection_status VARCHAR(32) NOT NULL, -- HEALTHY, DEGRADED, STALE, DISCONNECTED, UNAVAILABLE
    last_successful_update TIMESTAMP WITH TIME ZONE,
    last_market_timestamp TIMESTAMP WITH TIME ZONE,
    latency_ms BIGINT NOT NULL DEFAULT 0,
    data_age_seconds INT NOT NULL DEFAULT 0,
    freshness_status VARCHAR(32) NOT NULL, -- REAL_TIME, DELAYED, STALE, NOT_AVAILABLE
    error_count INT NOT NULL DEFAULT 0,
    coverage_ratio DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    checked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for rapid point-in-time auditing, state queries, and dashboard loading
CREATE INDEX IF NOT EXISTS idx_paper_sess_status ON paper_trading_sessions(status);
CREATE INDEX IF NOT EXISTS idx_paper_port_sess ON paper_portfolios(session_id);
CREATE INDEX IF NOT EXISTS idx_paper_pos_port ON paper_positions(portfolio_id, is_active);
CREATE INDEX IF NOT EXISTS idx_paper_dec_port_ts ON paper_decisions(portfolio_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_paper_dec_sym_ts ON paper_decisions(symbol, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_paper_ord_port_ts ON paper_orders(portfolio_id, order_submitted_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_paper_fill_ord ON paper_fills(order_id);
CREATE INDEX IF NOT EXISTS idx_paper_led_port_ts ON paper_ledger(portfolio_id, transaction_timestamp ASC);
CREATE INDEX IF NOT EXISTS idx_paper_eq_port_ts ON paper_equity_curve(portfolio_id, snapshot_timestamp ASC);
CREATE INDEX IF NOT EXISTS idx_paper_sig_out_dec ON paper_signal_outcomes(decision_id);
CREATE INDEX IF NOT EXISTS idx_paper_pred_out_dec ON paper_prediction_outcomes(decision_id);
CREATE INDEX IF NOT EXISTS idx_paper_mod_mon_ver ON paper_model_monitoring(model_version, horizon);
CREATE INDEX IF NOT EXISTS idx_paper_dh_prov ON paper_data_health(provider_name, checked_at DESC);
