package com.quantlab.marketdata.provider.exception;

import com.quantlab.marketdata.model.ErrorCategory;
import com.quantlab.marketdata.provider.ProviderException;

public class ProviderRateLimitException extends ProviderException {

    private final long retryAfterMs;

    public ProviderRateLimitException(String provider, String message, long retryAfterMs) {
        super(provider, ErrorCategory.RATE_LIMITED, message);
        this.retryAfterMs = retryAfterMs;
    }

    public long getRetryAfterMs() {
        return retryAfterMs;
    }
}
