package com.quantlab.monitoring.model;

import java.time.Instant;
import java.util.UUID;

public record MonitoringIncidentDTO(
        UUID id,
        String incidentNumber,
        String title,
        String severity,
        String status, // DETECTED, INVESTIGATING, MITIGATED, RESOLVED, POSTMORTEM
        String affectedComponents,
        String rootCause,
        Instant createdAt,
        Instant resolvedAt
) {}
