package com.quantlab.paper.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaperModelMonitoringDTO(
        UUID id,
        String modelVersion,
        String horizon,
        LocalDate evaluationWindowStart,
        LocalDate evaluationWindowEnd,
        Integer sampleSize,
        Double directionalAccuracy,
        Double mae,
        Double rmse,
        Double ic,
        Double rankIc,
        ModelMonitoringStatus driftStatus,
        PaperModelStatus modelStatus,
        Instant evaluatedAt,
        Instant createdAt
) {}
