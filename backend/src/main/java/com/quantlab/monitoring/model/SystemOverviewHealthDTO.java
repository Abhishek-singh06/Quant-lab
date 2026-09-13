package com.quantlab.monitoring.model;

import java.time.Instant;
import java.util.Map;

public record SystemOverviewHealthDTO(
        String overallSystemStatus, // HEALTHY, DEGRADED, CRITICAL
        Map<String, String> subsystemStatus,
        int activeAlertsCount,
        int criticalAlertsCount,
        double averageApiLatencyMs,
        double databasePoolUsagePct,
        boolean liveTradingKillSwitchActive,
        Instant evaluatedAt
) {}
