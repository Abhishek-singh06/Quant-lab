package com.quantlab.marketdata.normalizer;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketDataStatus;
import com.quantlab.marketdata.model.MarketQuote;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Normalizer for raw market data.
 * Converts disparate provider representations into standardized internal canonical models:
 * - Timestamps normalized to UTC
 * - Symbols trimmed, uppercased, and cleaned
 * - Exchange canonicalized
 * - Numeric values scaled with proper precision and null-safe math
 */
@Component
public class MarketDataNormalizer {

    public MarketQuote normalize(MarketQuote raw, String runId) {
        if (raw == null) {
            throw new IllegalArgumentException("Cannot normalize null MarketQuote");
        }

        String normalizedSymbol = normalizeSymbol(raw.symbol());
        Exchange normalizedExchange = raw.exchange() != null ? raw.exchange() : Exchange.NSE;

        BigDecimal last = scalePrice(raw.lastPrice());
        BigDecimal open = scalePrice(raw.openPrice());
        BigDecimal high = scalePrice(raw.highPrice());
        BigDecimal low = scalePrice(raw.lowPrice());
        BigDecimal close = scalePrice(raw.closePrice());
        BigDecimal prevClose = scalePrice(raw.prevClosePrice());

        // Derive change and changePercent if missing but last & prevClose exist
        BigDecimal change = raw.change();
        BigDecimal changePercent = raw.changePercent();

        if (change == null && last != null && prevClose != null) {
            change = last.subtract(prevClose);
        }
        if (changePercent == null && change != null && prevClose != null && prevClose.compareTo(BigDecimal.ZERO) != 0) {
            changePercent = change.divide(prevClose, 6, RoundingMode.HALF_UP)
                                  .multiply(BigDecimal.valueOf(100))
                                  .setScale(4, RoundingMode.HALF_UP);
        }

        Instant timestamp = raw.timestamp() != null ? raw.timestamp() : Instant.now();
        Instant sourceTimestamp = raw.sourceTimestamp() != null ? raw.sourceTimestamp() : timestamp;
        Instant ingestionTimestamp = Instant.now();

        return MarketQuote.builder()
            .symbol(normalizedSymbol)
            .exchange(normalizedExchange)
            .isin(raw.isin() != null ? raw.isin().trim().toUpperCase() : null)
            .lastPrice(last)
            .openPrice(open)
            .highPrice(high)
            .lowPrice(low)
            .closePrice(close)
            .prevClosePrice(prevClose)
            .change(change != null ? change.setScale(4, RoundingMode.HALF_UP) : null)
            .changePercent(changePercent != null ? changePercent.setScale(4, RoundingMode.HALF_UP) : null)
            .volume(raw.volume() != null ? Math.max(0L, raw.volume()) : 0L)
            .totalTradedValue(raw.totalTradedValue())
            .openInterest(raw.openInterest())
            .timestamp(timestamp)
            .sourceTimestamp(sourceTimestamp)
            .ingestionTimestamp(ingestionTimestamp)
            .source(raw.source() != null ? raw.source().trim() : "UNKNOWN")
            .runId(runId)
            .status(raw.status() != null ? raw.status() : MarketDataStatus.VALID)
            .build();
    }

    public String normalizeSymbol(String rawSymbol) {
        if (rawSymbol == null || rawSymbol.trim().isEmpty()) {
            return "UNKNOWN";
        }
        String symbol = rawSymbol.trim().toUpperCase();
        // Standardize common NSE symbols
        if (symbol.equals("NIFTY50") || symbol.equals("NIFTY_50") || symbol.equals("NIFTY-50")) {
            return "NIFTY 50";
        }
        if (symbol.equals("BANKNIFTY") || symbol.equals("NIFTYBANK") || symbol.equals("NIFTY_BANK")) {
            return "NIFTY BANK";
        }
        return symbol;
    }

    private BigDecimal scalePrice(BigDecimal val) {
        if (val == null) return null;
        return val.setScale(4, RoundingMode.HALF_UP);
    }
}
