package com.quantlab.marketdata.model;

import java.time.Instant;
import java.util.Map;

public record AlertEvent(
    String alertId,
    String alertType,
    AlertSeverity severity,
    String title,
    String message,
    String provider,
    String runId,
    Instant timestamp,
    Map<String, Object> metadata
) {
    public static AlertEvent of(String alertType, AlertSeverity severity, String title, String message, String provider, String runId) {
        return new AlertEvent(
            java.util.UUID.randomUUID().toString(),
            alertType,
            severity,
            title,
            message,
            provider,
            runId,
            Instant.now(),
            Map.of()
        );
    }
}
