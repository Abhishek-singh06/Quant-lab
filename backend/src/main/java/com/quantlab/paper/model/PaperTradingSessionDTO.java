package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperTradingSessionDTO(
        UUID id,
        String name,
        ExecutionMode executionMode,
        PaperTradingStatus status,
        ClockType clockType,
        String dataProvider,
        DataFreshnessStatus dataFreshnessStatus,
        Instant startTime,
        Instant endTime,
        String configurationVersion,
        String engineVersion,
        Integer totalDecisionsCount,
        Integer totalOrdersCount,
        Integer totalFillsCount,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {}
