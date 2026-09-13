package com.quantlab.marketdata.provider;

import com.quantlab.marketdata.model.ErrorCategory;

public class ProviderException extends RuntimeException {

    private final String provider;
    private final ErrorCategory errorCategory;

    public ProviderException(String provider, ErrorCategory errorCategory, String message) {
        super(String.format("[%s] %s: %s", provider, errorCategory, message));
        this.provider = provider;
        this.errorCategory = errorCategory;
    }

    public ProviderException(String provider, ErrorCategory errorCategory, String message, Throwable cause) {
        super(String.format("[%s] %s: %s", provider, errorCategory, message), cause);
        this.provider = provider;
        this.errorCategory = errorCategory;
    }

    public String getProvider() { return provider; }
    public ErrorCategory getErrorCategory() { return errorCategory; }
}
