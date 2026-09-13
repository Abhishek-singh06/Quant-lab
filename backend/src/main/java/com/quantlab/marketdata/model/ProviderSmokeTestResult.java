package com.quantlab.marketdata.model;

import java.time.Instant;
import java.util.Map;

/**
 * Result of a non-trading, read-only connectivity smoke test on a Market Data Provider.
 */
public record ProviderSmokeTestResult(
    String providerName,
    ProviderHealthState healthState,
    boolean isConfigured,
    boolean connectivityOk,
    String message,
    Long latencyMs,
    Instant timestamp,
    Map<String, Object> diagnosticDetails
) {}
