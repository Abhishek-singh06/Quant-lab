package com.quantlab.signal.model;

import java.time.Instant;
import java.util.UUID;

public record SignalGenerationRunDTO(
    UUID id,
    Instant runTimestamp,
    Instant asOfTimestamp,
    Integer instrumentCount,
    Integer signalCount,
    String configurationVersion,
    String signalVersion,
    String modelVersion,
    String regimeVersion,
    String status,
    Long durationMs,
    String errorSummary,
    Instant createdAt
) {}
