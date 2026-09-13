package com.quantlab.monitoring.model;

import java.time.Instant;
import java.util.UUID;

public record FeatureDriftEventDTO(
        UUID id,
        String featureName,
        String metricType,
        Double observedValue,
        Double threshold,
        String driftStatus,
        int sampleSize,
        String referenceWindow,
        String evaluationWindow,
        Instant detectedAt
) {}
