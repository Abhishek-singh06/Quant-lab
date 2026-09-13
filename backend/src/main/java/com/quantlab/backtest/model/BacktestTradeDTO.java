package com.quantlab.backtest.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BacktestTradeDTO(
    UUID id,
    UUID runId,
    String symbol,
    String side,
    Integer quantity,
    UUID entryOrderId,
    UUID exitOrderId,
    OffsetDateTime entryTimestamp,
    OffsetDateTime exitTimestamp,
    Double entryPrice,
    Double exitPrice,
    Double grossPnl,
    Double netPnl,
    Double returnPct,
    Double totalFees,
    Double totalSlippage,
    Integer holdingPeriodDays,
    ExitReason exitReason,
    Double maxFavorableExcursion,
    Double maxAdverseExcursion,
    String regimeAtEntry,
    String regimeAtExit
) {}
