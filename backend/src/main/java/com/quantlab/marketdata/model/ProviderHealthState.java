package com.quantlab.marketdata.model;

/**
 * Standardized Health and Connectivity States for Market Data Providers.
 */
public enum ProviderHealthState {
    /**
     * Required API credentials or base URLs are not set in environment variables.
     */
    NOT_CONFIGURED,

    /**
     * Credentials are present, but active connectivity has not yet been verified.
     */
    CONFIGURED,

    /**
     * Active non-trading verification request succeeded with valid structured data.
     */
    CONNECTIVITY_OK,

    /**
     * Network error, connection timeout, DNS resolution failure, or connection refused.
     */
    CONNECTIVITY_FAILED,

    /**
     * Provider returned data, but data timestamps exceed the configured staleness threshold.
     */
    STALE,

    /**
     * Provider returned HTTP 429 Too Many Requests or token bucket exhausted.
     */
    RATE_LIMITED,

    /**
     * Provider returned HTTP 401/403 or rejected authentication credentials / license.
     */
    AUTH_FAILED
}
