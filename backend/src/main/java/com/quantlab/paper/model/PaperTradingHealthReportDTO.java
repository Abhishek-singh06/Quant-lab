package com.quantlab.paper.model;

import java.time.Instant;
import java.util.Map;

public record PaperTradingHealthReportDTO(
        String overallStatus, // HEALTHY, DEGRADED, BLOCKED
        ExecutionMode executionMode,
        boolean liveDataAvailable,
        String liveDataProvider,
        Map<String, String> subsystemStatus, // MARKET_DATA, FEATURE_ENGINE, MODEL, SIGNAL, RISK, PORTFOLIO, DATABASE, EXECUTION
        String message,
        Instant evaluatedAt
) {}
