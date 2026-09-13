package com.quantlab.broker.provider;

import com.quantlab.broker.model.BrokerProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BrokerProviderFactory {

    private final Map<BrokerProvider, BrokerTradingProvider> providers;

    public BrokerProviderFactory(List<BrokerTradingProvider> providerList) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(BrokerTradingProvider::getProviderType, p -> p));
    }

    public BrokerTradingProvider getProvider(BrokerProvider provider) {
        BrokerTradingProvider p = providers.get(provider);
        if (p == null) {
            // Default to sandbox mock provider for safety
            return providers.getOrDefault(BrokerProvider.MOCK_SANDBOX, providers.values().iterator().next());
        }
        return p;
    }
}
