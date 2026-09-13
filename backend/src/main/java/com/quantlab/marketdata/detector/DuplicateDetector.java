package com.quantlab.marketdata.detector;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketQuote;
import com.quantlab.marketdata.repository.MarketDataRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Duplicate and Idempotency Detector.
 * Checks idempotency key: (symbol, exchange, timestamp, data_type).
 * Prevents redundant database insertion and tracks duplicate metrics.
 */
@Component
public class DuplicateDetector {

    private static final Logger log = LoggerFactory.getLogger(DuplicateDetector.class);

    private final MarketDataRecordRepository repository;
    private final Set<String> recentKeysCache = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public DuplicateDetector(MarketDataRecordRepository repository) {
        this.repository = repository;
    }

    public boolean isDuplicate(MarketQuote quote, String dataType) {
        if (quote == null || quote.symbol() == null || quote.timestamp() == null) {
            return false;
        }

        Exchange exchange = quote.exchange() != null ? quote.exchange() : Exchange.NSE;
        String key = buildIdempotencyKey(quote.symbol(), exchange, quote.timestamp(), dataType);

        // 1. Fast in-memory cache check
        if (recentKeysCache.contains(key)) {
            log.debug("[DuplicateDetector] In-memory duplicate detected: {}", key);
            return true;
        }

        // 2. Database existence check
        boolean exists = repository.existsBySymbolAndExchangeAndTimestampAndDataType(
            quote.symbol(), exchange, quote.timestamp(), dataType
        );

        if (exists) {
            recentKeysCache.add(key);
            log.debug("[DuplicateDetector] Database duplicate detected: {}", key);
            return true;
        }

        return false;
    }

    public void registerKey(MarketQuote quote, String dataType) {
        if (quote != null && quote.symbol() != null && quote.timestamp() != null) {
            Exchange exchange = quote.exchange() != null ? quote.exchange() : Exchange.NSE;
            String key = buildIdempotencyKey(quote.symbol(), exchange, quote.timestamp(), dataType);
            recentKeysCache.add(key);
            if (recentKeysCache.size() > 50_000) {
                recentKeysCache.clear();
            }
        }
    }

    public String buildIdempotencyKey(String symbol, Exchange exchange, Instant timestamp, String dataType) {
        return String.format("%s:%s:%d:%s", symbol, exchange, timestamp.toEpochMilli(), dataType);
    }
}
