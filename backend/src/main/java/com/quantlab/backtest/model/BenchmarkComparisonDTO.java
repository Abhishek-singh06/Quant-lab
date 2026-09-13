package com.quantlab.backtest.model;

public record BenchmarkComparisonDTO(
    String benchmarkSymbol,
    Double strategyTotalReturn,
    Double benchmarkTotalReturn,
    Double strategyCagr,
    Double benchmarkCagr,
    Double strategySharpe,
    Double benchmarkSharpe,
    Double strategyMaxDd,
    Double benchmarkMaxDd,
    Double alpha,
    Double beta,
    Double trackingError,
    Double informationRatio
) {}
