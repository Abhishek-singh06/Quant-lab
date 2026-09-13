package com.quantlab.news.provider;

import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.resilience.RateLimiter;
import com.quantlab.marketdata.resilience.RetryExecutor;
import com.quantlab.news.entity.CorporateEvent;
import com.quantlab.news.model.EventType;
import com.quantlab.news.model.SentimentLabel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Official NSE Corporate Filings and Disclosures Adapter.
 * Communicates with authorized corporate announcement endpoints.
 */
@org.springframework.context.annotation.Primary
@Component("nseCorporateFilingsAdapter")
public class NSECorporateFilingsAdapter implements CorporateFilingsProvider {

    private static final Logger log = LoggerFactory.getLogger(NSECorporateFilingsAdapter.class);
    private static final String PROVIDER_NAME = "NSE_FILINGS";

    private final MarketDataProperties properties;
    private final RestClient restClient;
    private final RateLimiter rateLimiter;
    private final RetryExecutor retryExecutor;
    private final ObjectMapper objectMapper;

    public NSECorporateFilingsAdapter(MarketDataProperties properties) {
        this.properties = properties;
        MarketDataProperties.NseConfig config = properties.getNse();

        this.rateLimiter = new RateLimiter(PROVIDER_NAME, config.getRateLimitPerMinute());
        this.retryExecutor = new RetryExecutor(config.getMaxRetries(), config.getBackoffInitialMs(), config.getBackoffMultiplier());
        this.objectMapper = new ObjectMapper();

        this.restClient = RestClient.builder()
            .baseUrl(config.getBaseUrl())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "QuantLab-CorporateIntelligence-Client/1.0")
            .build();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<CorporateEvent> fetchLatestFilings(Instant since, int limit) {
        try {
            rateLimiter.acquire();
            String response = restClient.get()
                .uri("/api/corporate-announcements?index=equities")
                .headers(this::applyAuthHeaders)
                .retrieve()
                .body(String.class);

            return parseFilingsJson(response);
        } catch (Exception e) {
            log.warn("[NSECorporateFilingsAdapter] Could not fetch live filings (requires active enterprise credentials): {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<CorporateEvent> fetchFilingsForSymbol(String symbol, Instant from, Instant to) {
        try {
            rateLimiter.acquire();
            String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/corporate-announcements")
                    .queryParam("symbol", symbol)
                    .build())
                .headers(this::applyAuthHeaders)
                .retrieve()
                .body(String.class);

            return parseFilingsJson(response);
        } catch (Exception e) {
            log.warn("[NSECorporateFilingsAdapter] Failed to fetch filings for symbol {}: {}", symbol, e.getMessage());
            return Collections.emptyList();
        }
    }

    public List<CorporateEvent> parseFilingsJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            List<CorporateEvent> events = new ArrayList<>();
            JsonNode data = root.isArray() ? root : root.path("data");

            if (data.isArray()) {
                for (JsonNode node : data) {
                    String symbol = node.path("symbol").asText("UNKNOWN");
                    String desc = node.path("desc").asText(node.path("sm_name").asText("Announcement"));
                    String dt = node.path("an_dt").asText("");
                    Instant announcedAt = !dt.isEmpty() ? Instant.parse(dt) : Instant.now();

                    CorporateEvent event = new CorporateEvent(
                        0L, // will be resolved by entity resolver
                        symbol,
                        EventType.OTHER_MATERIAL,
                        desc,
                        desc,
                        LocalDate.now(),
                        announcedAt,
                        announcedAt,
                        PROVIDER_NAME,
                        BigDecimal.valueOf(0.7),
                        BigDecimal.ZERO,
                        SentimentLabel.NEUTRAL
                    );
                    events.add(event);
                }
            }
            return events;
        } catch (Exception e) {
            log.error("[NSECorporateFilingsAdapter] JSON parse error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private void applyAuthHeaders(HttpHeaders headers) {
        String apiKey = properties.getNse().getApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("X-NSE-API-KEY", apiKey);
        }
    }
}
