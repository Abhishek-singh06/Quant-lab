package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record PaperEquityCurveDTO(
        UUID id,
        UUID portfolioId,
        Instant snapshotTimestamp,
        Double portfolioValue,
        Double cashBalance,
        Double investedValue,
        Double dailyReturnPct,
        Double cumulativeReturnPct,
        Double drawdownPct,
        Instant createdAt
) {}
