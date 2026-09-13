package com.quantlab.marketdata.provider.exception;

import com.quantlab.marketdata.model.ErrorCategory;
import com.quantlab.marketdata.model.ProviderCapability;
import com.quantlab.marketdata.provider.ProviderException;

public class UnsupportedCapabilityException extends ProviderException {

    private final ProviderCapability capability;

    public UnsupportedCapabilityException(String provider, ProviderCapability capability) {
        super(provider, ErrorCategory.CONFIGURATION_ERROR,
                String.format("Provider '%s' does not support declared capability: %s", provider, capability));
        this.capability = capability;
    }

    public ProviderCapability getCapability() {
        return capability;
    }
}
