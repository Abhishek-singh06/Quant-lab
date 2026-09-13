package com.quantlab.marketdata.provider.adapter;

import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketQuote;
import com.quantlab.marketdata.model.ProviderHealthState;
import com.quantlab.marketdata.model.ProviderSmokeTestResult;
import com.quantlab.marketdata.provider.AuthenticationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class NSEMarketDataProviderTest {

    @Test
    void unconfiguredProviderReportsNotConfiguredHealthState() {
        MarketDataProperties properties = new MarketDataProperties();
        properties.getNse().setApiKey(null);
        properties.setApiKey(null);

        NSEMarketDataProvider provider = new NSEMarketDataProvider(properties);

        assertEquals(ProviderHealthState.NOT_CONFIGURED, provider.getHealthState());
        assertFalse(provider.isAvailable());

        ProviderSmokeTestResult smokeResult = provider.runSmokeTest();
        assertNotNull(smokeResult);
        assertEquals(ProviderHealthState.NOT_CONFIGURED, smokeResult.healthState());
        assertFalse(smokeResult.isConfigured());
        assertFalse(smokeResult.connectivityOk());
        assertTrue(smokeResult.message().contains("not configured"));
    }

    @Test
    void unconfiguredProviderThrowsAuthenticationExceptionOnGetQuote() {
        MarketDataProperties properties = new MarketDataProperties();
        properties.getNse().setApiKey(null);
        properties.setApiKey(null);

        NSEMarketDataProvider provider = new NSEMarketDataProvider(properties);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
            provider.getQuote("RELIANCE", Exchange.NSE)
        );
        assertTrue(ex.getMessage().contains("NOT_CONFIGURED"));
    }

    @Test
    void configuredProviderWithValidResponseReturnsConnectivityOk() {
        MarketDataProperties properties = new MarketDataProperties();
        properties.getNse().setApiKey("real-authorized-key-123");
        properties.getNse().setBaseUrl("https://api.nseindia.com");

        RestClient.Builder clientBuilder = RestClient.builder().baseUrl("https://api.nseindia.com");
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(clientBuilder).build();
        RestClient restClient = clientBuilder.build();

        String statusJson = """
        {
            "marketState": [
                {
                    "market": "Capital Market",
                    "marketStatus": "Normal Trading",
                    "tradeDate": "2026-09-12"
                }
            ]
        }
        """;

        mockServer.expect(requestTo("https://api.nseindia.com/api/marketStatus"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-NSE-API-KEY", "real-authorized-key-123"))
                .andRespond(withSuccess(statusJson, MediaType.APPLICATION_JSON));

        NSEMarketDataProvider provider = new NSEMarketDataProvider(properties, restClient);

        ProviderSmokeTestResult smokeResult = provider.runSmokeTest();

        assertNotNull(smokeResult);
        assertEquals(ProviderHealthState.CONNECTIVITY_OK, smokeResult.healthState());
        assertTrue(smokeResult.isConfigured());
        assertTrue(smokeResult.connectivityOk());
        assertTrue(smokeResult.message().contains("smoke test passed"));
        assertEquals("Normal Trading", smokeResult.diagnosticDetails().get("marketState"));
        assertEquals("2026-09-12", smokeResult.diagnosticDetails().get("tradeDate"));

        mockServer.verify();
    }

    @Test
    void smokeTestReturnsAuthFailedOn401Unauthorized() {
        MarketDataProperties properties = new MarketDataProperties();
        properties.getNse().setApiKey("invalid-expired-key");
        properties.getNse().setBaseUrl("https://api.nseindia.com");

        RestClient.Builder clientBuilder = RestClient.builder().baseUrl("https://api.nseindia.com");
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(clientBuilder).build();
        RestClient restClient = clientBuilder.build();

        mockServer.expect(requestTo("https://api.nseindia.com/api/marketStatus"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).body("{\"error\":\"Invalid API credentials\"}"));

        NSEMarketDataProvider provider = new NSEMarketDataProvider(properties, restClient);

        ProviderSmokeTestResult smokeResult = provider.runSmokeTest();

        assertNotNull(smokeResult);
        assertEquals(ProviderHealthState.AUTH_FAILED, smokeResult.healthState());
        assertTrue(smokeResult.isConfigured());
        assertFalse(smokeResult.connectivityOk());
        assertTrue(smokeResult.message().contains("Authentication failed"));

        mockServer.verify();
    }

    @Test
    void smokeTestReturnsRateLimitedOn429TooManyRequests() {
        MarketDataProperties properties = new MarketDataProperties();
        properties.getNse().setApiKey("valid-key");
        properties.getNse().setBaseUrl("https://api.nseindia.com");

        RestClient.Builder clientBuilder = RestClient.builder().baseUrl("https://api.nseindia.com");
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(clientBuilder).build();
        RestClient restClient = clientBuilder.build();

        mockServer.expect(requestTo("https://api.nseindia.com/api/marketStatus"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).body("{\"error\":\"Rate limit exceeded\"}"));

        NSEMarketDataProvider provider = new NSEMarketDataProvider(properties, restClient);

        ProviderSmokeTestResult smokeResult = provider.runSmokeTest();

        assertNotNull(smokeResult);
        assertEquals(ProviderHealthState.RATE_LIMITED, smokeResult.healthState());
        assertTrue(smokeResult.isConfigured());
        assertFalse(smokeResult.connectivityOk());

        mockServer.verify();
    }

    @Test
    void parseNseQuoteResponseParsesValidJsonCorrectly() {
        MarketDataProperties properties = new MarketDataProperties();
        NSEMarketDataProvider provider = new NSEMarketDataProvider(properties);

        String mockNseJson = """
        {
            "priceInfo": {
                "lastPrice": 2450.50,
                "open": 2440.00,
                "previousClose": 2445.00,
                "change": 5.50,
                "pChange": 0.22,
                "intraDayHighLow": {
                    "max": 2460.00,
                    "min": 2435.00
                },
                "close": 2450.50
            },
            "securityInfo": {
                "isin": "INE002A01018"
            },
            "preOpenMarket": {
                "totalTradedVolume": 5234000
            }
        }
        """;

        MarketQuote quote = provider.parseNseQuoteResponse(mockNseJson, "RELIANCE", Exchange.NSE);

        assertNotNull(quote);
        assertEquals("RELIANCE", quote.symbol());
        assertEquals(Exchange.NSE, quote.exchange());
        assertEquals("INE002A01018", quote.isin());
        assertEquals(0, new BigDecimal("2450.50").compareTo(quote.lastPrice()));
        assertEquals(0, new BigDecimal("2440.00").compareTo(quote.openPrice()));
        assertEquals(0, new BigDecimal("2460.00").compareTo(quote.highPrice()));
        assertEquals(0, new BigDecimal("2435.00").compareTo(quote.lowPrice()));
        assertEquals(5234000L, quote.volume());
        assertEquals("NSE", quote.source());
    }
}
