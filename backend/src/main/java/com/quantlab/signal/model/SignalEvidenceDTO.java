package com.quantlab.signal.model;

import java.time.Instant;
import java.util.UUID;

public record SignalEvidenceDTO(
    UUID id,
    UUID signalId,
    EvidenceCategory category,
    String featureName,
    Double rawValue,
    String rawValueStr,
    Double normalizedScore,
    String direction,
    Double strength,
    Double quality,
    Double freshness,
    Double confidence,
    Double weight,
    Double contribution,
    String source,
    Instant sourceTimestamp,
    Instant availableAt,
    String version,
    String reason
) {}
