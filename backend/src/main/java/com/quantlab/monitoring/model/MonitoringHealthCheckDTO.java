package com.quantlab.monitoring.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record MonitoringHealthCheckDTO(
        UUID id,
        String component,
        String status, // HEALTHY, DEGRADED, WARNING, CRITICAL, UNKNOWN, UNAVAILABLE
        Double latencyMs,
        String message,
        Instant checkedAt
) {}
