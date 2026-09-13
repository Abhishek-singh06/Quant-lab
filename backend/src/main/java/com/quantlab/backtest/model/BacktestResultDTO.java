package com.quantlab.backtest.model;

import java.util.List;
import java.util.UUID;

public record BacktestResultDTO(
    UUID runId,
    BacktestConfigDTO config,
    BacktestRunDTO run,
    PerformanceMetricsDTO metrics,
    BenchmarkComparisonDTO benchmarkComparison,
    List<EquityCurvePointDTO> equityCurve,
    List<BacktestTradeDTO> trades,
    List<PortfolioSnapshotDTO> snapshots,
    List<BacktestRejectedSignalDTO> rejectedSignals
) {}
