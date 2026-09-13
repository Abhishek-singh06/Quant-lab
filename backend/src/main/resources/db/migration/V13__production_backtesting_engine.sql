-- V13__production_backtesting_engine.sql
-- Schema for QuantLab Part 16: Realistic, Point-in-Time-Safe Backtesting Engine

CREATE TABLE IF NOT EXISTS backtest_configs (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    horizon VARCHAR(32) NOT NULL DEFAULT 'SHORT_TERM', -- SHORT_TERM, MEDIUM_TERM, LONG_TERM, MULTI_HORIZON
    universe_type VARCHAR(32) NOT NULL DEFAULT 'NIFTY_50', -- NIFTY_50, NIFTY_500, CUSTOM
    symbols JSONB NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    initial_capital DOUBLE PRECISION NOT NULL DEFAULT 1000000.0, -- 10 Lakhs INR default
    cash_buffer_pct DOUBLE PRECISION NOT NULL DEFAULT 0.05, -- 5% cash buffer
    rebalance_frequency VARCHAR(32) NOT NULL DEFAULT 'DAILY', -- DAILY, WEEKLY, MONTHLY, SIGNAL_DRIVEN
    execution_timing VARCHAR(32) NOT NULL DEFAULT 'NEXT_BAR_OPEN', -- NEXT_BAR_OPEN, NEXT_BAR_VWAP, CLOSE_SAME_BAR_DISALLOWED
    cost_model_type VARCHAR(32) NOT NULL DEFAULT 'REALISTIC_INDIAN', -- ZERO, FIXED_BPS, REALISTIC_INDIAN
    slippage_model_type VARCHAR(32) NOT NULL DEFAULT 'FIXED_BPS', -- NONE, FIXED_BPS, SPREAD_AND_VOLUME
    brokerage_bps DOUBLE PRECISION NOT NULL DEFAULT 3.0, -- 3 bps
    stt_delivery_bps DOUBLE PRECISION NOT NULL DEFAULT 10.0, -- 10 bps on both buy/sell delivery
    stt_intraday_bps DOUBLE PRECISION NOT NULL DEFAULT 2.5, -- 2.5 bps on sell intraday
    exchange_charges_bps DOUBLE PRECISION NOT NULL DEFAULT 0.345, -- NSE charges
    gst_rate DOUBLE PRECISION NOT NULL DEFAULT 0.18, -- 18% GST on brokerage + exchange
    stamp_duty_bps DOUBLE PRECISION NOT NULL DEFAULT 1.5, -- 1.5 bps on buy
    slippage_bps DOUBLE PRECISION NOT NULL DEFAULT 5.0, -- 5 bps default slippage
    max_position_weight DOUBLE PRECISION NOT NULL DEFAULT 0.20, -- 20% max single stock
    max_sector_weight DOUBLE PRECISION NOT NULL DEFAULT 0.35, -- 35% max sector
    max_drawdown_limit DOUBLE PRECISION NOT NULL DEFAULT 0.15, -- 15% circuit breaker
    benchmark_symbol VARCHAR(32) NOT NULL DEFAULT 'NIFTY_50',
    version VARCHAR(32) NOT NULL DEFAULT 'v1.0.0',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS backtest_runs (
    id UUID PRIMARY KEY,
    config_id UUID REFERENCES backtest_configs(id),
    name VARCHAR(100) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CREATED', -- CREATED, RUNNING, COMPLETED, FAILED, CANCELLED
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
    data_quality_trust_level VARCHAR(32) NOT NULL DEFAULT 'PRODUCTION_READY', -- PRODUCTION_READY, DEGRADED, REJECTED
    data_quality_report JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS backtest_orders (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
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
    trade_ref_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS backtest_trades (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    side VARCHAR(10) NOT NULL, -- LONG, SHORT
    quantity INT NOT NULL,
    entry_order_id UUID REFERENCES backtest_orders(id),
    exit_order_id UUID REFERENCES backtest_orders(id),
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
    exit_reason VARCHAR(32) NOT NULL, -- SIGNAL, STOP_LOSS, TAKE_PROFIT, MAX_HOLDING_TIME, REBALANCE, RISK_CIRCUIT, FORCE_CLOSE_END
    max_favorable_excursion DOUBLE PRECISION, -- MFE
    max_adverse_excursion DOUBLE PRECISION, -- MAE
    regime_at_entry VARCHAR(32),
    regime_at_exit VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS backtest_positions (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS backtest_portfolio_snapshots (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS backtest_equity_curve (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
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

CREATE TABLE IF NOT EXISTS backtest_ledger (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
    transaction_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    transaction_type VARCHAR(32) NOT NULL, -- CAPITAL_INJECTION, BUY_EXECUTION, SELL_EXECUTION, DIVIDEND_CREDIT, FEE_DEBIT, SLIPPAGE_DEBIT, CORPORATE_ACTION_ADJUSTMENT
    symbol VARCHAR(32),
    amount DOUBLE PRECISION NOT NULL, -- positive for credit, negative for debit
    cash_balance_before DOUBLE PRECISION NOT NULL,
    cash_balance_after DOUBLE PRECISION NOT NULL,
    description TEXT NOT NULL,
    reference_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS backtest_metrics (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL UNIQUE REFERENCES backtest_runs(id) ON DELETE CASCADE,
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
    subperiod_metrics JSONB, -- yearly/monthly breakdown
    regime_breakdown_metrics JSONB, -- metrics per Bull/Bear/Sideways/HighVol
    sector_breakdown_metrics JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS backtest_rejected_signals (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES backtest_runs(id) ON DELETE CASCADE,
    symbol VARCHAR(32) NOT NULL,
    signal_timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    signal_type VARCHAR(20) NOT NULL, -- BUY, SELL
    signal_strength DOUBLE PRECISION NOT NULL,
    rejection_reason VARCHAR(64) NOT NULL, -- INSUFFICIENT_CASH, MAX_POSITION_LIMIT_EXCEEDED, SECTOR_LIMIT_EXCEEDED, RISK_CIRCUIT_BREAKER, ILLIQUID_MARKET, MODEL_AVAILABILITY_VIOLATION
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for performance and point-in-time auditing
CREATE INDEX IF NOT EXISTS idx_bt_runs_status ON backtest_runs(status);
CREATE INDEX IF NOT EXISTS idx_bt_orders_run ON backtest_orders(run_id, symbol);
CREATE INDEX IF NOT EXISTS idx_bt_trades_run ON backtest_trades(run_id, exit_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_bt_snap_run_date ON backtest_portfolio_snapshots(run_id, snapshot_date ASC);
CREATE INDEX IF NOT EXISTS idx_bt_equity_run_date ON backtest_equity_curve(run_id, point_date ASC);
CREATE INDEX IF NOT EXISTS idx_bt_ledger_run_ts ON backtest_ledger(run_id, transaction_timestamp ASC);
CREATE INDEX IF NOT EXISTS idx_bt_rejected_run ON backtest_rejected_signals(run_id, signal_timestamp);
