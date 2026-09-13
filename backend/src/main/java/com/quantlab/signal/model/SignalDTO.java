package com.quantlab.signal.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record SignalDTO(
    UUID id,
    UUID runId,
    Long instrumentId,
    String symbol,
    Instant signalTimestamp,
    Instant informationAvailableAt,
    Instant calculatedAt,
    SignalType signal,
    Double signalScore,
    Double confidence,
    Double expectedReturn,
    Double expectedVolatility,
    Double returnToVolatilityRatio,
    String direction,
    ConflictSeverity conflictSeverity,
    Double conflictScore,
    SignalQualityStatus dataQualityStatus,
    Double freshnessScore,
    String reasoning,
    List<Map<String, Object>> structuredReasoning,
    List<Map<String, Object>> supportingEvidence,
    List<Map<String, Object>> opposingEvidence,
    Map<String, SignalComponentDTO> components,
    String signalVersion,
    String configurationVersion,
    String featureVersion,
    String modelVersion,
    String regimeVersion,
    String dataVersion,
    Boolean isLatest
) {}
