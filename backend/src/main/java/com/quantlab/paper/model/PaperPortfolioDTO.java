package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperPortfolioDTO(
        UUID id,
        UUID sessionId,
        String name,
        String horizon,
        UUID riskProfileId,
        String currency,
        Double initialVirtualCapital,
        Double cashBalance,
        Double availableCash,
        Double reservedCash,
        Double investedValue,
        Double totalPortfolioValue,
        Double peakPortfolioValue,
        Double currentDrawdownPct,
        Double maxDrawdownPct,
        Double grossExposure,
        Double netExposure,
        Double leverage,
        Double totalRealizedPnl,
        Double totalUnrealizedPnl,
        Double totalFeesPaid,
        Double totalSlippagePaid,
        Double totalDividendsReceived,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
