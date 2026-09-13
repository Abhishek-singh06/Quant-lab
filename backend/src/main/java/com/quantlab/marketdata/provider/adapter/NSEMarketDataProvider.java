package com.quantlab.marketdata.provider.adapter;

import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.model.*;
import com.quantlab.marketdata.provider.*;
import com.quantlab.marketdata.resilience.RateLimiter;
import com.quantlab.marketdata.resilience.RetryExecutor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authorized NSE Market Data Provider Adapter.
 * Integrates with official/licensed NSE market data feeds and gateways.
 * 
 * Safety & Reliability Guarantees:
 * - Credentials and endpoints are externalized via environment variables (MARKET_DATA_API_KEY, NSE_API_KEY).
 * - Reports NOT_CONFIGURED when credentials are not supplied (never pretends to be live or returns fake data).
 * - Enforces token-bucket rate limiting and exponential backoff retries.
 * - Provides non-trading connectivity smoke tests that never place orders or alter ledger state.
 * - Canonicalizes responses into standard MarketQuote / HistoricalCandle models.
 */
@Component("nseMarketDataProvider")
public class NSEMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(NSEMarketDataProvider.class);
    private static final String PROVIDER_NAME = "NSE";

    private final MarketDataProperties properties;
    private final RestClient restClient;
    private final RateLimiter rateLimiter;
    private final RetryExecutor retryExecutor;
    private final ObjectMapper objectMapper;
    private volatile ProviderHealthState lastKnownHealthState = null;

    @org.springframework.beans.factory.annotation.Autowired
    public NSEMarketDataProvider(MarketDataProperties properties) {
        this(properties, RestClient.builder()
            .baseUrl(properties.getEffectiveBaseUrl())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "QuantLab-MarketData-Client/1.0")
            .build());
    }

    NSEMarketDataProvider(MarketDataProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
        MarketDataProperties.NseConfig config = properties.getNse();

        this.rateLimiter = new RateLimiter(PROVIDER_NAME, config.getRateLimitPerMinute());
        this.retryExecutor = new RetryExecutor(
            config.getMaxRetries(),
            config.getBackoffInitialMs(),
            config.getBackoffMultiplier()
        );
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        if (!properties.isConfigured()) {
            return false;
        }
        try {
            rateLimiter.acquire();
            var response = restClient.get()
                .uri("/api/marketStatus")
                .headers(this::applyAuthHeaders)
                .retrieve()
                .toBodilessEntity();
            boolean ok = response.getStatusCode().is2xxSuccessful();
            lastKnownHealthState = ok ? ProviderHealthState.CONNECTIVITY_OK : ProviderHealthState.CONNECTIVITY_FAILED;
            return ok;
        } catch (Exception e) {
            log.warn("[NSEMarketDataProvider] Provider availability check failed: {}", e.getMessage());
            lastKnownHealthState = mapExceptionToHealthState(e);
            return false;
        }
    }

    @Override
    public ProviderHealthState getHealthState() {
        if (!properties.isConfigured()) {
            return ProviderHealthState.NOT_CONFIGURED;
        }
        return lastKnownHealthState != null ? lastKnownHealthState : ProviderHealthState.CONFIGURED;
    }

    @Override
    public ProviderSmokeTestResult runSmokeTest() {
        long startTime = System.currentTimeMillis();
        Instant now = Instant.now();

        if (!properties.isConfigured()) {
            return new ProviderSmokeTestResult(
                PROVIDER_NAME,
                ProviderHealthState.NOT_CONFIGURED,
                false,
                false,
                "API credentials are not configured. Set MARKET_DATA_API_KEY or NSE_API_KEY environment variable.",
                0L,
                now,
                Map.of("provider", PROVIDER_NAME, "configured", false)
            );
        }

        try {
            rateLimiter.acquire();
            String body = restClient.get()
                .uri("/api/marketStatus")
                .headers(this::applyAuthHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                    if (resp.getStatusCode().value() == 401 || resp.getStatusCode().value() == 403) {
                        throw new AuthenticationException(PROVIDER_NAME, "Authentication failed with provider (HTTP " + resp.getStatusCode().value() + ")");
                    }
                    if (resp.getStatusCode().value() == 429) {
                        throw new RateLimitExceededException(PROVIDER_NAME, "Rate limit exceeded (HTTP 429)");
                    }
                    throw new ProviderException(PROVIDER_NAME, ErrorCategory.PROVIDER_UNAVAILABLE, "Client error: " + resp.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    throw new ProviderUnavailableException(PROVIDER_NAME, "Server error: " + resp.getStatusCode());
                })
                .body(String.class);

            long latency = System.currentTimeMillis() - startTime;

            JsonNode root = objectMapper.readTree(body != null ? body : "{}");
            String state = root.path("marketState").path(0).path("marketStatus").asText("UNKNOWN");
            String tradeDate = root.path("marketState").path(0).path("tradeDate").asText("");

            ProviderHealthState health = ProviderHealthState.CONNECTIVITY_OK;
            String msg = "Non-trading connectivity smoke test passed. Market state: " + state;

            Map<String, Object> details = new HashMap<>();
            details.put("marketState", state);
            details.put("tradeDate", tradeDate);
            details.put("baseUrl", properties.getEffectiveBaseUrl());
            details.put("latencyMs", latency);

            lastKnownHealthState = health;
            return new ProviderSmokeTestResult(
                PROVIDER_NAME,
                health,
                true,
                true,
                msg,
                latency,
                now,
                details
            );

        } catch (AuthenticationException ae) {
            long latency = System.currentTimeMillis() - startTime;
            lastKnownHealthState = ProviderHealthState.AUTH_FAILED;
            return new ProviderSmokeTestResult(
                PROVIDER_NAME,
                ProviderHealthState.AUTH_FAILED,
                true,
                false,
                "Authentication failed: " + ae.getMessage(),
                latency,
                now,
                Map.of("error", ae.getMessage())
            );
        } catch (RateLimitExceededException rle) {
            long latency = System.currentTimeMillis() - startTime;
            lastKnownHealthState = ProviderHealthState.RATE_LIMITED;
            return new ProviderSmokeTestResult(
                PROVIDER_NAME,
                ProviderHealthState.RATE_LIMITED,
                true,
                false,
                "Rate limit reached: " + rle.getMessage(),
                latency,
                now,
                Map.of("error", rle.getMessage())
            );
        } catch (Exception ex) {
            long latency = System.currentTimeMillis() - startTime;
            ProviderHealthState health = mapExceptionToHealthState(ex);
            lastKnownHealthState = health;
            return new ProviderSmokeTestResult(
                PROVIDER_NAME,
                health,
                true,
                false,
                "Connectivity test failed: " + ex.getMessage(),
                latency,
                now,
                Map.of("error", ex.getMessage())
            );
        }
    }

    @Override
    public MarketQuote getQuote(String symbol, Exchange exchange) {
        validateCredentialsConfigured();

        return executeWithResilience("getQuote-" + symbol, () -> {
            rateLimiter.acquire();

            String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/quote-equity")
                    .queryParam("symbol", symbol)
                    .build())
                .headers(this::applyAuthHeaders)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, resp) -> {
                    if (resp.getStatusCode().value() == 401 || resp.getStatusCode().value() == 403) {
                        throw new AuthenticationException(PROVIDER_NAME, "Invalid or expired NSE API credentials / subscription");
                    }
                    if (resp.getStatusCode().value() == 429) {
                        throw new RateLimitExceededException(PROVIDER_NAME, "NSE rate limit exceeded (HTTP 429)");
                    }
                    throw new ProviderException(PROVIDER_NAME, ErrorCategory.PROVIDER_UNAVAILABLE, "Client error: " + resp.getStatusCode());
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, resp) -> {
                    throw new ProviderUnavailableException(PROVIDER_NAME, "NSE server error: " + resp.getStatusCode());
                })
                .body(String.class);

            return parseNseQuoteResponse(responseBody, symbol, exchange);
        });
    }

    @Override
    public List<MarketQuote> getQuotes(List<String> symbols, Exchange exchange) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }
        validateCredentialsConfigured();

        List<MarketQuote> quotes = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                quotes.add(getQuote(symbol, exchange));
            } catch (Exception e) {
                log.error("[NSEMarketDataProvider] Failed to fetch quote for symbol {}: {}", symbol, e.getMessage());
            }
        }
        return quotes;
    }

    @Override
    public List<HistoricalCandle> getHistoricalData(String symbol, Exchange exchange, Instant from, Instant to, String interval) {
        validateCredentialsConfigured();

        return executeWithResilience("getHistoricalData-" + symbol, () -> {
            rateLimiter.acquire();

            String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/historical/cm/equity")
                    .queryParam("symbol", symbol)
                    .queryParam("from", from != null ? from.toString() : Instant.now().minus(30, ChronoUnit.DAYS).toString())
                    .queryParam("to", to != null ? to.toString() : Instant.now().toString())
                    .queryParam("interval", interval != null ? interval : "1D")
                    .build())
                .headers(this::applyAuthHeaders)
                .retrieve()
                .body(String.class);

            return parseNseHistoricalResponse(responseBody, symbol, exchange);
        });
    }

    @Override
    public List<String> getSymbols(Exchange exchange) {
        return properties.getDefaultSymbols();
    }

    @Override
    public MarketStatusInfo getMarketStatus(Exchange exchange) {
        try {
            rateLimiter.acquire();
            String body = restClient.get()
                .uri("/api/marketStatus")
                .headers(this::applyAuthHeaders)
                .retrieve()
                .body(String.class);

            JsonNode root = objectMapper.readTree(body != null ? body : "{}");
            String state = root.path("marketState").path(0).path("marketStatus").asText("CLOSED");
            boolean isOpen = "Open".equalsIgnoreCase(state) || "Normal Trading".equalsIgnoreCase(state);
            String tradeDate = root.path("marketState").path(0).path("tradeDate").asText("");

            return new MarketStatusInfo(
                exchange,
                state,
                isOpen,
                tradeDate,
                Instant.now(),
                "Market status retrieved from official feed"
            );
        } catch (Exception e) {
            log.warn("[NSEMarketDataProvider] Could not fetch live market status: {}", e.getMessage());
            return new MarketStatusInfo(
                exchange,
                "UNKNOWN",
                false,
                "",
                Instant.now(),
                "Status check error: " + e.getMessage()
            );
        }
    }

    private void applyAuthHeaders(HttpHeaders headers) {
        String apiKey = properties.getEffectiveApiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            headers.set("X-NSE-API-KEY", apiKey);
        }
        String secret = properties.getEffectiveApiSecret();
        if (secret != null && !secret.isBlank()) {
            headers.set("X-NSE-API-SECRET", secret);
        }
    }

    private void validateCredentialsConfigured() {
        if (!properties.isConfigured()) {
            throw new AuthenticationException(PROVIDER_NAME,
                "NSE Market Data Provider is NOT_CONFIGURED. Please configure MARKET_DATA_API_KEY (or NSE_API_KEY) in environment variables.");
        }
    }

    public MarketQuote parseNseQuoteResponse(String json, String symbol, Exchange exchange) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode priceInfo = root.path("priceInfo");
            JsonNode metadata = root.path("metadata");
            JsonNode securityInfo = root.path("securityInfo");

            BigDecimal last = new BigDecimal(priceInfo.path("lastPrice").asText("0"));
            BigDecimal open = new BigDecimal(priceInfo.path("open").asText("0"));
            BigDecimal high = new BigDecimal(priceInfo.path("intraDayHighLow").path("max").asText("0"));
            BigDecimal low = new BigDecimal(priceInfo.path("intraDayHighLow").path("min").asText("0"));
            BigDecimal close = new BigDecimal(priceInfo.path("close").asText(last.toPlainString()));
            BigDecimal prevClose = new BigDecimal(priceInfo.path("previousClose").asText("0"));
            BigDecimal change = new BigDecimal(priceInfo.path("change").asText("0"));
            BigDecimal pChange = new BigDecimal(priceInfo.path("pChange").asText("0"));
            long volume = root.path("preOpenMarket").path("totalTradedVolume").asLong(0);

            String isin = securityInfo.path("isin").asText(null);

            return MarketQuote.builder()
                .symbol(symbol)
                .exchange(exchange)
                .isin(isin)
                .lastPrice(last)
                .openPrice(open)
                .highPrice(high)
                .lowPrice(low)
                .closePrice(close)
                .prevClosePrice(prevClose)
                .change(change)
                .changePercent(pChange)
                .volume(volume)
                .timestamp(Instant.now())
                .sourceTimestamp(Instant.now())
                .source(PROVIDER_NAME)
                .status(MarketDataStatus.VALID)
                .build();
        } catch (Exception e) {
            throw new MalformedResponseException(PROVIDER_NAME, "Failed to parse NSE quote response JSON: " + e.getMessage(), e);
        }
    }

    private List<HistoricalCandle> parseNseHistoricalResponse(String json, String symbol, Exchange exchange) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.path("data");
            List<HistoricalCandle> candles = new ArrayList<>();

            if (data.isArray()) {
                for (JsonNode node : data) {
                    Instant ts = Instant.parse(node.path("timestamp").asText());
                    BigDecimal o = new BigDecimal(node.path("open").asText());
                    BigDecimal h = new BigDecimal(node.path("high").asText());
                    BigDecimal l = new BigDecimal(node.path("low").asText());
                    BigDecimal c = new BigDecimal(node.path("close").asText());
                    long v = node.path("volume").asLong(0);
                    BigDecimal vwap = node.has("vwap") ? new BigDecimal(node.path("vwap").asText()) : null;

                    candles.add(new HistoricalCandle(symbol, exchange, ts, o, h, l, c, v, vwap, 1L));
                }
            }
            return candles;
        } catch (Exception e) {
            throw new MalformedResponseException(PROVIDER_NAME, "Failed to parse NSE historical JSON: " + e.getMessage(), e);
        }
    }

    private ProviderHealthState mapExceptionToHealthState(Exception ex) {
        if (ex instanceof AuthenticationException) return ProviderHealthState.AUTH_FAILED;
        if (ex instanceof RateLimitExceededException) return ProviderHealthState.RATE_LIMITED;
        return ProviderHealthState.CONNECTIVITY_FAILED;
    }

    private <T> T executeWithResilience(String op, java.util.concurrent.Callable<T> task) {
        try {
            return retryExecutor.execute(op, task);
        } catch (ProviderException pe) {
            throw pe;
        } catch (Exception ex) {
            throw new ProviderException(PROVIDER_NAME, ErrorCategory.UNKNOWN, "Failed executing operation: " + op, ex);
        }
    }
}
