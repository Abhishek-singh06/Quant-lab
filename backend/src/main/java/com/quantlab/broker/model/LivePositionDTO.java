package com.quantlab.broker.model;

import java.time.Instant;
import java.util.UUID;

public record LivePositionDTO(
        UUID id,
        UUID brokerAccountId,
        String symbol,
        String productType,
        int quantity,
        double averagePrice,
        double currentMarketPrice,
        double marketValue,
        double unrealizedPnl,
        double realizedPnl,
        Double stopPrice,
        Instant lastSyncedAt
) {}
