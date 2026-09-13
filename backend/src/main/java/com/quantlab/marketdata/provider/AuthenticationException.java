package com.quantlab.marketdata.provider;

import com.quantlab.marketdata.model.ErrorCategory;

public class AuthenticationException extends ProviderException {
    public AuthenticationException(String provider, String message) {
        super(provider, ErrorCategory.AUTHENTICATION_FAILURE, message);
    }
}
