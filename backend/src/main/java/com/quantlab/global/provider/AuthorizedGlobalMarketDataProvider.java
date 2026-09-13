package com.quantlab.global.provider;

import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.entity.GlobalMarketSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for licensed enterprise/institutional global market data feeds.
 * Requires authorized API keys / subscription endpoints.
 */
@Component("authorizedGlobalMarketDataProvider")
public class AuthorizedGlobalMarketDataProvider implements GlobalMarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(AuthorizedGlobalMarketDataProvider.class);
    private static final String PROVIDER_NAME = "AUTHORIZED_GLOBAL_FEED";

    private final RestClient restClient;
    private final boolean liveEnabled;
    private final String apiKey;

    public AuthorizedGlobalMarketDataProvider(
            @Value("${quantlab.global.authorized.base-url:https://api.marketdata.com}") String baseUrl,
            @Value("${quantlab.global.authorized.api-key:}") String apiKey,
            @Value("${quantlab.global.authorized.live-enabled:false}") boolean liveEnabled) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.liveEnabled = liveEnabled;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return liveEnabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public GlobalMarketSnapshot fetchLatestSnapshot(GlobalInstrument instrument) {
        if (!isAvailable()) {
            log.info("Authorized global feed disabled or unconfigured, skipping remote call for {}", instrument.getCanonicalSymbol());
            return null;
        }
        return null;
    }

    @Override
    public List<GlobalMarketSnapshot> fetchHistoricalSnapshots(GlobalInstrument instrument, LocalDate fromDate, LocalDate toDate) {
        if (!isAvailable()) {
            return List.of();
        }
        return new ArrayList<>();
    }

    @Override
    public List<GlobalMarketSnapshot> fetchLatestSnapshots(List<GlobalInstrument> instruments) {
        if (!isAvailable()) {
            return List.of();
        }
        return new ArrayList<>();
    }
}
