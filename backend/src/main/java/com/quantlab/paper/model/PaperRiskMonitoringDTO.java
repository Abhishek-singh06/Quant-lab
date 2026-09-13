package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperRiskMonitoringDTO(
        UUID id,
        UUID portfolioId,
        Instant evaluationTimestamp,
        Double portfolioVolatility,
        Double maxSectorConcentration,
        String highestConcentrationSector,
        Double currentDrawdownPct,
        Double leverage,
        Double cashBufferPct,
        Boolean isRiskBreached,
        String breachReason,
        String riskActionTaken,
        Instant createdAt
) {}
