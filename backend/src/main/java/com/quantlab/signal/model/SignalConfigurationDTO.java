package com.quantlab.signal.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SignalConfigurationDTO(
    UUID id,
    String version,
    Integer minSupportingCategories,
    Double buyThreshold,
    Double sellThreshold,
    Double minConfidence,
    Map<String, Double> weights,
    Map<String, Double> categoryCaps,
    Map<String, List<String>> correlationGroups,
    Boolean isActive,
    Instant createdAt
) {}
