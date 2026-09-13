package com.quantlab.monitoring.model;

import java.time.Instant;
import java.util.UUID;

public record DataQualityEventDTO(
        UUID id,
        String provider,
        String symbol,
        String eventType,
        String severity,
        String description,
        int affectedRecordsCount,
        Instant sourceTimestamp,
        Instant availableTimestamp,
        Instant detectedAt
) {}
