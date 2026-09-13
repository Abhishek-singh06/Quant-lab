-- V16__broker_integration_and_live_execution.sql
-- Schema for QuantLab Part 20: Broker Integration, Manual Live Orders, Reconciliation & Safety Controls

CREATE TABLE IF NOT EXISTS broker_accounts (
    id UUID PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL DEFAULT 'SYSTEM_DEFAULT_USER',
    broker_provider VARCHAR(64) NOT NULL, -- ZERODHA_KITE, ANGEL_ONE_SMARTAPI, UPSTOX, MOCK_SANDBOX
    client_id VARCHAR(64) NOT NULL,
    account_name VARCHAR(128) NOT NULL,
    connection_status VARCHAR(32) NOT NULL DEFAULT 'DISCONNECTED', -- CONNECTED, AUTHENTICATION_REQUIRED, TOKEN_EXPIRED, DISCONNECTED, DEGRADED, ERROR
    environment VARCHAR(32) NOT NULL DEFAULT 'SANDBOX', -- SANDBOX, LIVE
    is_live_trading_enabled BOOLEAN NOT NULL DEFAULT false,
    available_cash DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    used_margin DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_authenticated_at TIMESTAMP WITH TIME ZONE,
    token_expires_at TIMESTAMP WITH TIME ZONE,
    last_health_check_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_broker_user_client UNIQUE (user_id, broker_provider, client_id)
);

CREATE TABLE IF NOT EXISTS live_orders (
    id UUID PRIMARY KEY,
    broker_account_id UUID NOT NULL REFERENCES broker_accounts(id) ON DELETE CASCADE,
    broker_order_id VARCHAR(64),
    order_intent_id UUID NOT NULL UNIQUE,
    symbol VARCHAR(32) NOT NULL,
    exchange VARCHAR(16) NOT NULL DEFAULT 'NSE',
    side VARCHAR(10) NOT NULL, -- BUY, SELL
    order_type VARCHAR(20) NOT NULL DEFAULT 'LIMIT', -- MARKET, LIMIT, STOP_LOSS, STOP_LIMIT
    product_type VARCHAR(20) NOT NULL DEFAULT 'CASH', -- CASH, INTRADAY
    quantity INT NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    trigger_price DOUBLE PRECISION,
    stop_loss_price DOUBLE PRECISION,
    target_price DOUBLE PRECISION,
    status VARCHAR(32) NOT NULL DEFAULT 'AWAITING_CONFIRMATION', -- CREATED, VALIDATING, AWAITING_CONFIRMATION, CONFIRMED, SUBMITTING, SUBMITTED, OPEN, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED, UNKNOWN_EXECUTION_STATE
    filled_quantity INT NOT NULL DEFAULT 0,
    average_fill_price DOUBLE PRECISION,
    rejection_reason TEXT,
    signal_id UUID,
    signal_score DOUBLE PRECISION,
    signal_confidence DOUBLE PRECISION,
    expected_return DOUBLE PRECISION,
    risk_assessment_id UUID,
    risk_level VARCHAR(32),
    suggested_allocation DOUBLE PRECISION,
    is_manually_confirmed BOOLEAN NOT NULL DEFAULT false,
    confirmed_by_user VARCHAR(64),
    confirmed_at TIMESTAMP WITH TIME ZONE,
    order_submitted_at TIMESTAMP WITH TIME ZONE,
    order_executed_at TIMESTAMP WITH TIME ZONE,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS live_trades (
    id UUID PRIMARY KEY,
    live_order_id UUID NOT NULL REFERENCES live_orders(id) ON DELETE CASCADE,
    broker_trade_id VARCHAR(64) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    side VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    execution_price DOUBLE PRECISION NOT NULL,
    brokerage DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    stt DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    exchange_charges DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    gst DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    stamp_duty DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    total_fees DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    execution_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS live_positions (
    id UUID PRIMARY KEY,
    broker_account_id UUID NOT NULL REFERENCES broker_accounts(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    product_type VARCHAR(20) NOT NULL DEFAULT 'CASH',
    quantity INT NOT NULL,
    average_price DOUBLE PRECISION NOT NULL,
    current_market_price DOUBLE PRECISION NOT NULL,
    market_value DOUBLE PRECISION NOT NULL,
    unrealized_pnl DOUBLE PRECISION NOT NULL,
    realized_pnl DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    stop_price DOUBLE PRECISION,
    last_synced_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT uk_live_pos_acc_sym UNIQUE (broker_account_id, symbol, product_type)
);

CREATE TABLE IF NOT EXISTS live_portfolio_reconciliations (
    id UUID PRIMARY KEY,
    broker_account_id UUID NOT NULL REFERENCES broker_accounts(id) ON DELETE CASCADE,
    reconciliation_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    status VARCHAR(32) NOT NULL, -- MATCHED, MISMATCH, UNKNOWN
    total_positions_matched INT NOT NULL,
    total_discrepancies_count INT NOT NULL DEFAULT 0,
    discrepancy_details JSONB,
    checked_by VARCHAR(64) NOT NULL DEFAULT 'RECONCILIATION_ENGINE'
);

CREATE TABLE IF NOT EXISTS trading_safety_locks (
    id VARCHAR(64) PRIMARY KEY, -- GLOBAL_SAFETY_LOCK, EMERGENCY_STOP, DAILY_LOSS_GUARD
    is_active BOOLEAN NOT NULL DEFAULT false,
    lock_reason TEXT,
    activated_by VARCHAR(64),
    activated_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS broker_compliance_reviews (
    id UUID PRIMARY KEY,
    broker_provider VARCHAR(64) NOT NULL,
    review_status VARCHAR(64) NOT NULL DEFAULT 'APPROVED_FOR_MANUAL_LIVE_ORDERS',
    regulatory_framework VARCHAR(128) NOT NULL DEFAULT 'SEBI Retail Algo & API Mandate 2024/2025',
    is_manual_confirmation_enforced BOOLEAN NOT NULL DEFAULT true,
    is_automated_trading_disabled BOOLEAN NOT NULL DEFAULT true,
    reviewed_by VARCHAR(64) NOT NULL,
    reviewed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    review_notes TEXT
);

CREATE INDEX IF NOT EXISTS idx_live_ord_status ON live_orders(status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_live_ord_sym ON live_orders(symbol, created_at DESC);
