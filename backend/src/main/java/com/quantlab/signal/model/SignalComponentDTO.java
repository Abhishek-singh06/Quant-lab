package com.quantlab.signal.model;

import java.util.UUID;

public record SignalComponentDTO(
    UUID id,
    UUID signalId,
    EvidenceCategory category,
    Double categoryScore,
    Double weight,
    Double weightedContribution,
    String direction,
    Double strength,
    Double quality,
    Double freshness,
    Boolean isPresent,
    String missingReason
) {}
