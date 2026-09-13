package com.quantlab.marketdata.detector;

import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.model.MarketQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Detects if incoming market data is stale compared to current time.
 * Prevents old ticks/prices from being treated as real-time market data.
 */
@Component
public class StaleDataDetector {

    private static final Logger log = LoggerFactory.getLogger(StaleDataDetector.class);

    private final MarketDataProperties properties;
    private final IndianTradingCalendar calendar;

    public StaleDataDetector(MarketDataProperties properties, IndianTradingCalendar calendar) {
        this.properties = properties;
        this.calendar = calendar;
    }

    public boolean isStale(MarketQuote quote) {
        if (quote == null || quote.sourceTimestamp() == null) {
            return false;
        }

        Instant now = Instant.now();
        Duration age = Duration.between(quote.sourceTimestamp(), now);
        long threshold = properties.getStaleThresholdSeconds();

        // If market is currently open in IST and data age exceeds threshold
        if (calendar.isMarketOpen(now) && age.getSeconds() > threshold) {
            log.warn("[StaleDataDetector] Stale data detected for symbol {}. Source age is {} seconds (threshold: {}s)",
                    quote.symbol(), age.getSeconds(), threshold);
            return true;
        }

        return false;
    }

    public long getDataAgeSeconds(MarketQuote quote) {
        if (quote == null || quote.sourceTimestamp() == null) {
            return 0;
        }
        return Math.max(0, Duration.between(quote.sourceTimestamp(), Instant.now()).getSeconds());
    }
}
