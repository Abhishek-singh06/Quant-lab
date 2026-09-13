package com.quantlab.marketdata.detector;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects missing data during active trading sessions.
 * Never treats weekends or Indian stock market holidays as missing data.
 */
@Component
public class MissingDataDetector {

    private static final Logger log = LoggerFactory.getLogger(MissingDataDetector.class);

    private final IndianTradingCalendar calendar;

    public MissingDataDetector(IndianTradingCalendar calendar) {
        this.calendar = calendar;
    }

    /**
     * Identifies which expected symbols were missing from a batch ingestion run.
     */
    public List<String> detectMissingSymbols(List<String> expectedSymbols, List<MarketQuote> receivedQuotes, Instant checkTime) {
        if (expectedSymbols == null || expectedSymbols.isEmpty()) {
            return Collections.emptyList();
        }

        // If market is not open (holiday, weekend, or outside trading hours), missing data is not anomalous
        if (!calendar.isMarketOpen(checkTime)) {
            log.debug("[MissingDataDetector] Market is closed at {}. Skipping missing-data anomaly check.", checkTime);
            return Collections.emptyList();
        }

        Set<String> receivedSymbols = receivedQuotes != null
            ? receivedQuotes.stream()
                .filter(Objects::nonNull)
                .map(MarketQuote::symbol)
                .collect(Collectors.toSet())
            : Collections.emptySet();

        List<String> missing = new ArrayList<>();
        for (String expected : expectedSymbols) {
            if (!receivedSymbols.contains(expected)) {
                missing.add(expected);
            }
        }

        if (!missing.isEmpty()) {
            log.warn("[MissingDataDetector] {} expected symbol(s) missing during open market session: {}",
                    missing.size(), missing);
        }

        return missing;
    }

    /**
     * Checks if a symbol has had no tick updates for an extended period during trading hours.
     */
    public boolean hasUpdateGap(String symbol, Instant lastSeenTimestamp, Instant currentTime, long maxGapSeconds) {
        if (!calendar.isMarketOpen(currentTime)) {
            return false;
        }
        if (lastSeenTimestamp == null) {
            return true;
        }
        long gap = java.time.Duration.between(lastSeenTimestamp, currentTime).getSeconds();
        return gap > maxGapSeconds;
    }
}
