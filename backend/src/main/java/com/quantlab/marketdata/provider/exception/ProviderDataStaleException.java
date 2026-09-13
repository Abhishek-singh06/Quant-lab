package com.quantlab.marketdata.provider.exception;

import com.quantlab.marketdata.model.ErrorCategory;
import com.quantlab.marketdata.provider.ProviderException;

public class ProviderDataStaleException extends ProviderException {

    public ProviderDataStaleException(String provider, String message) {
        super(provider, ErrorCategory.STALE_DATA, message);
    }
}
