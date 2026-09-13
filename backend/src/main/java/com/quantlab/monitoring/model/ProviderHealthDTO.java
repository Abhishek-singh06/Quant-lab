package com.quantlab.monitoring.model;

import java.time.Instant;
import java.util.UUID;

public record ProviderHealthDTO(
        UUID id,
        String provider,
        String connectionStatus,
        String dataFreshnessStatus,
        Instant lastSuccessfulUpdate,
        Instant lastMarketTimestamp,
        Double latencyMs,
        Double dataAgeSeconds,
        int consecutiveFailures,
        Double healthScore,
        Double universeCoveragePct,
        boolean isFailingOver,
        String fallbackProvider,
        Instant updatedAt
) {}
