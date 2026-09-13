package com.quantlab.backtest.model;

import java.time.LocalDate;
import java.util.UUID;

public record PortfolioSnapshotDTO(
    UUID id,
    UUID runId,
    LocalDate snapshotDate,
    Double cashBalance,
    Double positionsMarketValue,
    Double totalEquity,
    Double grossExposure,
    Double netExposure,
    Double leverage,
    Double dailyPnl,
    Double dailyReturn,
    Double cumulativeReturn,
    Double drawdownPct,
    Integer openPositionsCount,
    Integer tradesExecutedToday,
    Double dividendsCreditedToday
) {}
