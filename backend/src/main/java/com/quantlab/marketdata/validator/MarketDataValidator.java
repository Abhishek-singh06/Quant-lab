package com.quantlab.marketdata.validator;

import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.model.MarketDataStatus;
import com.quantlab.marketdata.model.MarketQuote;
import com.quantlab.marketdata.model.ValidationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Validates financial market data before persistence.
 * Rejects invalid records and flags quarantined anomalies.
 * Never silently "fixes" suspicious prices.
 */
@Component
public class MarketDataValidator {

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("^[A-Z0-9 &._-]{1,32}$");

    private final MarketDataProperties properties;

    public MarketDataValidator(MarketDataProperties properties) {
        this.properties = properties;
    }

    public ValidationResult validate(MarketQuote quote) {
        if (quote == null) {
            return ValidationResult.invalid(java.util.List.of("MarketQuote record is null"));
        }

        ValidationResult.Builder builder = ValidationResult.builder();

        // 1. Symbol validation
        if (quote.symbol() == null || quote.symbol().trim().isEmpty() || quote.symbol().equals("UNKNOWN")) {
            builder.addError("Symbol is required and cannot be empty or UNKNOWN");
        } else if (!SYMBOL_PATTERN.matcher(quote.symbol()).matches()) {
            builder.addError("Symbol '" + quote.symbol() + "' contains invalid characters or exceeds 32 chars");
        }

        // 2. Exchange validation
        if (quote.exchange() == null) {
            builder.addError("Exchange is required");
        }

        // 3. Timestamp validation
        if (quote.timestamp() == null) {
            builder.addError("Timestamp is required");
        } else {
            Instant now = Instant.now();
            long futureToleranceSeconds = properties.getFutureToleranceSeconds();
            if (quote.timestamp().isAfter(now.plusSeconds(futureToleranceSeconds))) {
                builder.addError(String.format("Future timestamp detected: %s is ahead of current server time %s by > %ds",
                        quote.timestamp(), now, futureToleranceSeconds));
            }
        }

        // 4. Last price validation
        if (quote.lastPrice() == null) {
            builder.addError("Last price (LTP) is required");
        } else if (quote.lastPrice().compareTo(BigDecimal.ZERO) <= 0) {
            builder.addError("Last price must be positive, received: " + quote.lastPrice());
        }

        // 5. OHLC consistency checks
        BigDecimal open = quote.openPrice();
        BigDecimal high = quote.highPrice();
        BigDecimal low = quote.lowPrice();
        BigDecimal close = quote.closePrice();

        if (high != null && low != null && high.compareTo(low) < 0) {
            builder.addError(String.format("OHLC inconsistency: High (%s) is less than Low (%s)", high, low));
        }

        if (high != null && open != null && high.compareTo(open) < 0) {
            builder.addError(String.format("OHLC inconsistency: High (%s) is less than Open (%s)", high, open));
        }

        if (high != null && close != null && high.compareTo(close) < 0) {
            builder.addError(String.format("OHLC inconsistency: High (%s) is less than Close (%s)", high, close));
        }

        if (low != null && open != null && low.compareTo(open) > 0) {
            builder.addError(String.format("OHLC inconsistency: Low (%s) is greater than Open (%s)", low, open));
        }

        if (low != null && close != null && low.compareTo(close) > 0) {
            builder.addError(String.format("OHLC inconsistency: Low (%s) is greater than Close (%s)", low, close));
        }

        // 6. Volume validation
        if (quote.volume() != null && quote.volume() < 0) {
            builder.addError("Volume cannot be negative, received: " + quote.volume());
        }

        // 7. Extreme price warnings / sanity checks
        if (quote.lastPrice() != null && quote.prevClosePrice() != null && quote.prevClosePrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal ratio = quote.lastPrice().divide(quote.prevClosePrice(), 4, java.math.RoundingMode.HALF_UP);
            if (ratio.compareTo(BigDecimal.valueOf(3.0)) > 0 || ratio.compareTo(BigDecimal.valueOf(0.2)) < 0) {
                builder.addWarning(String.format("Unusual price variation (>300%% or <20%% of prevClose): LTP=%s vs prevClose=%s",
                        quote.lastPrice(), quote.prevClosePrice()));
            }
        }

        return builder.build();
    }
}
