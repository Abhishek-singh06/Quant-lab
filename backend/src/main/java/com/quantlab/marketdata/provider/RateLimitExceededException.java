package com.quantlab.marketdata.provider;

import com.quantlab.marketdata.model.ErrorCategory;

public class RateLimitExceededException extends ProviderException {
    public RateLimitExceededException(String provider, String message) {
        super(provider, ErrorCategory.RATE_LIMITED, message);
    }
}
