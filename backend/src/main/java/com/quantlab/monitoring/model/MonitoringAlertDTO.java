package com.quantlab.monitoring.model;

import java.time.Instant;
import java.util.UUID;

public record MonitoringAlertDTO(
        UUID id,
        String ruleId,
        String ruleName,
        String alertType,
        String component,
        String severity, // INFO, WARNING, ERROR, CRITICAL
        String status, // OPEN, ACKNOWLEDGED, RESOLVED, SUPPRESSED
        String observedValue,
        String thresholdValue,
        String message,
        String runbookRef,
        String acknowledgedBy,
        Instant acknowledgedAt,
        Instant resolvedAt,
        Instant createdAt
) {}
