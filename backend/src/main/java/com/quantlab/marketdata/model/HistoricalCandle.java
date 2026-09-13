package com.quantlab.marketdata.model;

import java.math.BigDecimal;
import java.time.Instant;

public record HistoricalCandle(
    String symbol,
    Exchange exchange,
    Instant timestamp,
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    Long volume,
    BigDecimal vwap,
    Long count
) {}
