package com.quantlab.broker.model;

import java.time.Instant;
import java.util.UUID;

public record BrokerAccountDTO(
        UUID id,
        String userId,
        BrokerProvider brokerProvider,
        String clientId,
        String accountName,
        String connectionStatus, // CONNECTED, AUTHENTICATION_REQUIRED, TOKEN_EXPIRED, DISCONNECTED, DEGRADED, ERROR
        String environment, // SANDBOX, LIVE
        boolean isLiveTradingEnabled,
        double availableCash,
        double usedMargin,
        Instant lastAuthenticatedAt,
        Instant tokenExpiresAt,
        Instant lastHealthCheckAt
) {}
