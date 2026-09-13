package com.quantlab.monitoring.model;

import java.time.Instant;

public record MonitoringRuleDTO(
        String id,
        String ruleVersion,
        String name,
        String targetComponent,
        String metricName,
        String conditionOperator,
        Double thresholdValue,
        int windowSeconds,
        int cooldownSeconds,
        String severity,
        boolean isEnabled,
        String runbookRef,
        Instant updatedAt
) {}
