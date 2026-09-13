package com.quantlab.marketdata.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "quantlab.market-data")
public class MarketDataProperties {

    /**
     * Active provider: "NSE", "MOCK", etc.
     */
    private String provider = "MOCK";

    /**
     * Generic top-level API Key (e.g. from MARKET_DATA_API_KEY).
     */
    private String apiKey;

    /**
     * Generic top-level API Secret (e.g. from MARKET_DATA_API_SECRET).
     */
    private String apiSecret;

    /**
     * Generic top-level Base URL (e.g. from MARKET_DATA_BASE_URL).
     */
    private String baseUrl;

    /**
     * Stale threshold in seconds (default: 900s / 15 mins for EOD/delayed, or 10s for real-time).
     */
    private long staleThresholdSeconds = 900;

    /**
     * Future tolerance in seconds to allow minor clock skew (default: 60s).
     */
    private long futureToleranceSeconds = 60;

    /**
     * Default tracked symbols for ingestion.
     */
    private List<String> defaultSymbols = List.of(
        "RELIANCE", "TCS", "HDFCBANK", "INFY", "ICICIBANK",
        "HINDUNILVR", "ITC", "SBIN", "BHARTIARTL", "KOTAKBANK",
        "LT", "BAJFINANCE", "AXISBANK", "ASIANPAINT", "MARUTI",
        "NIFTY 50", "NIFTY BANK"
    );

    /**
     * NSE Provider specific configuration.
     */
    private NseConfig nse = new NseConfig();

    public static class NseConfig {
        private String baseUrl = "https://api.nseindia.com";
        private String apiKey;
        private String apiSecret;
        private int connectTimeoutMs = 5000;
        private int readTimeoutMs = 10000;
        private int rateLimitPerMinute = 60;
        private int maxRetries = 3;
        private long backoffInitialMs = 500;
        private double backoffMultiplier = 2.0;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getApiSecret() { return apiSecret; }
        public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
        public int getConnectTimeoutMs() { return connectTimeoutMs; }
        public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
        public int getReadTimeoutMs() { return readTimeoutMs; }
        public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
        public int getRateLimitPerMinute() { return rateLimitPerMinute; }
        public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
        public long getBackoffInitialMs() { return backoffInitialMs; }
        public void setBackoffInitialMs(long backoffInitialMs) { this.backoffInitialMs = backoffInitialMs; }
        public double getBackoffMultiplier() { return backoffMultiplier; }
        public void setBackoffMultiplier(double backoffMultiplier) { this.backoffMultiplier = backoffMultiplier; }
    }

    public String getEffectiveApiKey() {
        if (nse != null && nse.getApiKey() != null && !nse.getApiKey().isBlank()) {
            return nse.getApiKey();
        }
        return apiKey;
    }

    public String getEffectiveApiSecret() {
        if (nse != null && nse.getApiSecret() != null && !nse.getApiSecret().isBlank()) {
            return nse.getApiSecret();
        }
        return apiSecret;
    }

    public String getEffectiveBaseUrl() {
        if (nse != null && nse.getBaseUrl() != null && !nse.getBaseUrl().isBlank() && !nse.getBaseUrl().equals("https://api.nseindia.com")) {
            return nse.getBaseUrl();
        }
        if (baseUrl != null && !baseUrl.isBlank()) {
            return baseUrl;
        }
        return nse != null && nse.getBaseUrl() != null ? nse.getBaseUrl() : "https://api.nseindia.com";
    }

    public boolean isConfigured() {
        String key = getEffectiveApiKey();
        return key != null && !key.isBlank() && !key.equalsIgnoreCase("changeme");
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public long getStaleThresholdSeconds() { return staleThresholdSeconds; }
    public void setStaleThresholdSeconds(long staleThresholdSeconds) { this.staleThresholdSeconds = staleThresholdSeconds; }
    public long getFutureToleranceSeconds() { return futureToleranceSeconds; }
    public void setFutureToleranceSeconds(long futureToleranceSeconds) { this.futureToleranceSeconds = futureToleranceSeconds; }
    public List<String> getDefaultSymbols() { return defaultSymbols; }
    public void setDefaultSymbols(List<String> defaultSymbols) { this.defaultSymbols = defaultSymbols; }
    public NseConfig getNse() { return nse; }
    public void setNse(NseConfig nse) { this.nse = nse; }
}
