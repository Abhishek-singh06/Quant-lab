package com.quantlab.marketdata.provider;

import com.quantlab.marketdata.model.ErrorCategory;

public class MalformedResponseException extends ProviderException {
    public MalformedResponseException(String provider, String message) {
        super(provider, ErrorCategory.MALFORMED_RESPONSE, message);
    }

    public MalformedResponseException(String provider, String message, Throwable cause) {
        super(provider, ErrorCategory.MALFORMED_RESPONSE, message, cause);
    }
}
