package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperPredictionOutcomeDTO(
        UUID id,
        UUID decisionId,
        UUID predictionId,
        String modelVersion,
        String symbol,
        String horizon,
        Instant predictionTimestamp,
        Instant evaluationTimestamp,
        Double expectedReturn,
        Double realizedReturn,
        Double predictionError,
        Double absoluteError,
        Double squaredError,
        Double expectedVolatility,
        Double realizedVolatility,
        Boolean isDirectionCorrect,
        Instant createdAt
) {}
