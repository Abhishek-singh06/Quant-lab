package com.quantlab.backtest.model;

import java.util.Map;

public record PerformanceMetricsDTO(
    Double totalReturnPct,
    Double cagr,
    Double annualizedVolatility,
    Double sharpeRatio,
    Double sortinoRatio,
    Double maxDrawdownPct,
    Integer maxDrawdownDurationDays,
    Double calmarRatio,
    Double winRatePct,
    Double profitFactor,
    Double averageTradeReturnPct,
    Double averageWinReturnPct,
    Double averageLossReturnPct,
    Double winLossRatio,
    Integer totalTradesCount,
    Integer winningTradesCount,
    Integer losingTradesCount,
    Double annualizedTurnover,
    Double betaToBenchmark,
    Double alphaToBenchmark,
    Double informationRatio,
    Map<String, Object> subperiodMetrics,
    Map<String, Object> regimeBreakdownMetrics,
    Map<String, Object> sectorBreakdownMetrics
) {}
