package com.quantlab.backtest.model;

public enum ExitReason {
    SIGNAL,
    STOP_LOSS,
    TAKE_PROFIT,
    MAX_HOLDING_TIME,
    REBALANCE,
    RISK_CIRCUIT,
    FORCE_CLOSE_END
}
