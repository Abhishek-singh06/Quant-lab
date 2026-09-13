package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperPositionDTO(
        UUID id,
        UUID portfolioId,
        String symbol,
        Long instrumentId,
        String horizon,
        Integer quantity,
        Double averageEntryPrice,
        Double currentMarketPrice,
        Double costBasis,
        Double marketValue,
        Double unrealizedPnl,
        Double unrealizedReturnPct,
        Double realizedPnl,
        Double portfolioWeight,
        Double stopPrice,
        Double targetPrice,
        String stopMethod,
        Double highestPriceSeen,
        Double lowestPriceSeen,
        Instant entryTimestamp,
        Instant lastUpdatedAt,
        UUID signalId,
        UUID riskAssessmentId,
        String modelVersion,
        Boolean isActive,
        Instant createdAt
) {}
