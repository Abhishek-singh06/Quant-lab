package com.quantlab.backtest.model;

import java.time.LocalDate;
import java.util.UUID;

public record BacktestPositionDTO(
    UUID id,
    UUID runId,
    String symbol,
    Integer quantity,
    Double averageEntryPrice,
    Double currentMarketPrice,
    Double costBasis,
    Double marketValue,
    Double unrealizedPnl,
    Double unrealizedReturnPct,
    Double weightInPortfolio,
    LocalDate asOfDate
) {}
