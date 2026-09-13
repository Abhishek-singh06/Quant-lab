package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperSignalOutcomeDTO(
        UUID id,
        UUID decisionId,
        String symbol,
        String horizon,
        Instant signalTimestamp,
        Instant evaluationTimestamp,
        String expectedDirection,
        String realizedDirection,
        Double expectedReturn,
        Double realizedReturn,
        String outcomeStatus,
        String attribution,
        Instant createdAt
) {}
