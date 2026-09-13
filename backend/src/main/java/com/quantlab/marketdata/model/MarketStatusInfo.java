package com.quantlab.marketdata.model;

import java.time.Instant;

public record MarketStatusInfo(
    Exchange exchange,
    String marketState,
    boolean isOpen,
    String tradeDate,
    Instant serverTime,
    String message
) {}
