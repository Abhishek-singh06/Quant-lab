package com.quantlab.monitoring.model;

import java.time.Instant;
import java.util.UUID;

public record SignalAnomalyEventDTO(
        UUID id,
        String signalType,
        String anomalyCategory,
        Double observedRate,
        Double expectedRate,
        String affectedSector,
        String severity,
        String description,
        Instant detectedAt
) {}
