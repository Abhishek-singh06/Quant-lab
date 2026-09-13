package com.quantlab.paper.model;

public enum ExitReason {
    SIGNAL,
    STOP_LOSS,
    TAKE_PROFIT,
    MAX_HOLDING_TIME,
    REBALANCE,
    RISK_CIRCUIT,
    SESSION_CLOSE
}
