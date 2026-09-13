package com.quantlab.paper.model;

import java.time.Instant;
import java.util.UUID;

public record LiveDataHealthDTO(
        UUID id,
        String provider,
        ConnectionStatus connectionStatus,
        DataFreshnessStatus dataFreshnessStatus,
        Instant lastSuccessfulUpdate,
        Instant lastMarketTimestamp,
        Double latencyMs,
        Double dataAgeSeconds,
        Integer errorCount,
        String rateLimitStatus,
        Double universeCoveragePct,
        Instant checkedAt
) {}
