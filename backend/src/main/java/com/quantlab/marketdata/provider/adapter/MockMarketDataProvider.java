package com.quantlab.marketdata.provider.adapter;

import com.quantlab.marketdata.model.*;
import com.quantlab.marketdata.provider.MarketDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Mock Market Data Provider.
 * 
 * STRICT WARNING:
 * This provider is ONLY for automated test suites and offline local development.
 * It is marked with source="MOCK" and must NEVER be used as a production data source.
 * It does NOT generate fake stock predictions or synthetic real-time data.
 */
@Component("mockMarketDataProvider")
public class MockMarketDataProvider implements MarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(MockMarketDataProvider.class);
    public static final String PROVIDER_NAME = "MOCK";

    private final Map<String, BigDecimal> basePrices = new HashMap<>();

    public MockMarketDataProvider() {
        // Deterministic baseline prices for Indian equities & indices
        basePrices.put("RELIANCE", new BigDecimal("2450.50"));
        basePrices.put("TCS", new BigDecimal("3920.00"));
        basePrices.put("HDFCBANK", new BigDecimal("1680.75"));
        basePrices.put("INFY", new BigDecimal("1540.20"));
        basePrices.put("ICICIBANK", new BigDecimal("1120.40"));
        basePrices.put("SBIN", new BigDecimal("780.60"));
        basePrices.put("NIFTY 50", new BigDecimal("24850.25"));
        basePrices.put("NIFTY BANK", new BigDecimal("52130.75"));
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
    public ProviderHealthState getHealthState() {
        return ProviderHealthState.CONFIGURED;
    }

    @Override
    public ProviderSmokeTestResult runSmokeTest() {
        return new ProviderSmokeTestResult(
            PROVIDER_NAME,
            ProviderHealthState.CONNECTIVITY_OK,
            true,
            true,
            "Mock Provider Smoke Test Passed (SANDBOX / TEST MODE ONLY)",
            1L,
            Instant.now(),
            Map.of("mode", "SANDBOX_SIMULATION", "provider", PROVIDER_NAME)
        );
    }

    @Override
    public MarketQuote getQuote(String symbol, Exchange exchange) {
        String sym = symbol != null ? symbol.trim().toUpperCase() : "UNKNOWN";
        BigDecimal base = basePrices.getOrDefault(sym, new BigDecimal("1000.00"));

        BigDecimal open = base.subtract(new BigDecimal("5.00"));
        BigDecimal high = base.add(new BigDecimal("15.00"));
        BigDecimal low = base.subtract(new BigDecimal("10.00"));
        BigDecimal close = base;
        BigDecimal prevClose = base.subtract(new BigDecimal("8.00"));
        BigDecimal change = close.subtract(prevClose);
        BigDecimal changePct = change.divide(prevClose, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        Instant now = Instant.now();

        return MarketQuote.builder()
            .symbol(sym)
            .exchange(exchange != null ? exchange : Exchange.NSE)
            .isin("INE" + sym.hashCode())
            .lastPrice(close)
            .openPrice(open)
            .highPrice(high)
            .lowPrice(low)
            .closePrice(close)
            .prevClosePrice(prevClose)
            .change(change)
            .changePercent(changePct)
            .volume(500_000L)
            .totalTradedValue(close.multiply(BigDecimal.valueOf(500_000L)))
            .timestamp(now)
            .sourceTimestamp(now)
            .source(PROVIDER_NAME)
            .status(MarketDataStatus.VALID)
            .build();
    }

    @Override
    public List<MarketQuote> getQuotes(List<String> symbols, Exchange exchange) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }
        List<MarketQuote> list = new ArrayList<>();
        for (String sym : symbols) {
            list.add(getQuote(sym, exchange));
        }
        return list;
    }

    @Override
    public List<HistoricalCandle> getHistoricalData(String symbol, Exchange exchange, Instant from, Instant to, String interval) {
        String sym = symbol != null ? symbol.trim().toUpperCase() : "UNKNOWN";
        BigDecimal base = basePrices.getOrDefault(sym, new BigDecimal("1000.00"));

        List<HistoricalCandle> candles = new ArrayList<>();
        Instant current = from != null ? from : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = to != null ? to : Instant.now();

        int stepDays = 1;
        while (!current.isAfter(end) && candles.size() < 100) {
            BigDecimal o = base.subtract(new BigDecimal("4.00"));
            BigDecimal h = base.add(new BigDecimal("12.00"));
            BigDecimal l = base.subtract(new BigDecimal("8.00"));
            BigDecimal c = base.add(new BigDecimal("3.00"));

            candles.add(new HistoricalCandle(sym, exchange, current, o, h, l, c, 250_000L, base, 1L));
            current = current.plus(stepDays, ChronoUnit.DAYS);
        }

        return candles;
    }

    @Override
    public List<String> getSymbols(Exchange exchange) {
        return new ArrayList<>(basePrices.keySet());
    }

    @Override
    public MarketStatusInfo getMarketStatus(Exchange exchange) {
        return new MarketStatusInfo(
            exchange != null ? exchange : Exchange.NSE,
            "Normal Trading",
            true,
            "2026-09-12",
            Instant.now(),
            "Mock Provider Test Status"
        );
    }
}
