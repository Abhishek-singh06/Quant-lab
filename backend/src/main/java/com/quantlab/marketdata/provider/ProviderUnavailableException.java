package com.quantlab.marketdata.provider;

import com.quantlab.marketdata.model.ErrorCategory;

public class ProviderUnavailableException extends ProviderException {
    public ProviderUnavailableException(String provider, String message) {
        super(provider, ErrorCategory.PROVIDER_UNAVAILABLE, message);
    }

    public ProviderUnavailableException(String provider, String message, Throwable cause) {
        super(provider, ErrorCategory.PROVIDER_UNAVAILABLE, message, cause);
    }
}
