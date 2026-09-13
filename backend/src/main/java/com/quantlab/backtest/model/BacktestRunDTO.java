package com.quantlab.backtest.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record BacktestRunDTO(
    UUID id,
    UUID configId,
    String name,
    BacktestStatus status,
    String engineVersion,
    LocalDate startDate,
    LocalDate endDate,
    Integer totalBarsProcessed,
    Integer totalTradesCount,
    Double initialCapital,
    Double finalEquity,
    Double totalNetPnl,
    Double totalFeesPaid,
    Double totalSlippagePaid,
    Double totalDividendsReceived,
    String errorMessage,
    Long executionDurationMs,
    String dataQualityTrustLevel,
    Map<String, Object> dataQualityReport,
    OffsetDateTime createdAt,
    OffsetDateTime completedAt
) {}
