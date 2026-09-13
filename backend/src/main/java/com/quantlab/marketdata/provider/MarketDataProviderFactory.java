package com.quantlab.marketdata.provider;

import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.provider.adapter.MockMarketDataProvider;
import com.quantlab.marketdata.provider.adapter.NSEMarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Factory and configuration that dynamically wires the active MarketDataProvider.
 * Ensures the application layer interacts solely with the generic interface.
 */
@Configuration
public class MarketDataProviderFactory {

    private static final Logger log = LoggerFactory.getLogger(MarketDataProviderFactory.class);

    private final MarketDataProperties properties;
    private final NSEMarketDataProvider nseProvider;
    private final MockMarketDataProvider mockProvider;

    public MarketDataProviderFactory(
            MarketDataProperties properties,
            NSEMarketDataProvider nseProvider,
            MockMarketDataProvider mockProvider) {
        this.properties = properties;
        this.nseProvider = nseProvider;
        this.mockProvider = mockProvider;
    }

    @Bean
    @Primary
    public MarketDataProvider activeMarketDataProvider() {
        String providerName = properties.getProvider();
        if (providerName == null || providerName.trim().isEmpty()) {
            providerName = "MOCK";
        }

        switch (providerName.trim().toUpperCase()) {
            case "NSE":
                log.info("[MarketDataProviderFactory] Initializing primary provider: NSEMarketDataProvider");
                return nseProvider;
            case "MOCK":
                log.warn("[MarketDataProviderFactory] Initializing MockMarketDataProvider. (FOR TEST/DEVELOPMENT ONLY)");
                return mockProvider;
            default:
                log.warn("[MarketDataProviderFactory] Unknown provider '{}'. Defaulting to MockMarketDataProvider.", providerName);
                return mockProvider;
        }
    }
}
