package com.quantlab.marketdata.provider;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.HistoricalCandle;
import com.quantlab.marketdata.model.MarketQuote;
import com.quantlab.marketdata.model.MarketStatusInfo;

import java.time.Instant;
import java.util.List;

/**
 * Generic MarketDataProvider interface.
 * Exposes provider-independent operations.
 * The application layer and analytics layer interact ONLY with this interface.
 * Specific providers (NSE, Thomson Reuters, Bloomberg, etc.) are implemented via adapters.
 */
public interface MarketDataProvider {

    /**
     * Provider identification name (e.g. "NSE", "BLOOMBERG", "MOCK").
     */
    String getProviderName();

    /**
     * Check if the provider service is reachable and healthy.
     */
    boolean isAvailable();

    /**
     * Fetch the real-time or latest quote for a single symbol.
     */
    MarketQuote getQuote(String symbol, Exchange exchange);

    /**
     * Fetch bulk quotes for multiple symbols.
     */
    List<MarketQuote> getQuotes(List<String> symbols, Exchange exchange);

    /**
     * Fetch historical OHLCV candles for a symbol.
     */
    List<HistoricalCandle> getHistoricalData(String symbol, Exchange exchange, Instant from, Instant to, String interval);

    /**
     * List all active symbols/instruments traded on the exchange.
     */
    List<String> getSymbols(Exchange exchange);

    /**
     * Get current market status (OPEN, CLOSED, HALTED).
     */
    MarketStatusInfo getMarketStatus(Exchange exchange);

    /**
     * Get the current operational health state of the provider.
     */
    com.quantlab.marketdata.model.ProviderHealthState getHealthState();

    /**
     * Run a non-trading, read-only connectivity smoke test verifying authentication,
     * connectivity, response structure, and timestamp freshness.
     * NEVER places orders or alters portfolio state.
     */
    com.quantlab.marketdata.model.ProviderSmokeTestResult runSmokeTest();
}
