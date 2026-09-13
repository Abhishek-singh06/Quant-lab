package com.quantlab.marketdata.validator;

import com.quantlab.marketdata.config.MarketDataProperties;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketQuote;
import com.quantlab.marketdata.model.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataValidatorTest {

    private MarketDataValidator validator;
    private MarketDataProperties properties;

    @BeforeEach
    void setUp() {
        properties = new MarketDataProperties();
        properties.setFutureToleranceSeconds(60);
        validator = new MarketDataValidator(properties);
    }

    @Test
    void validQuotePassesValidation() {
        MarketQuote quote = MarketQuote.builder()
            .symbol("RELIANCE")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("2450.50"))
            .openPrice(new BigDecimal("2440.00"))
            .highPrice(new BigDecimal("2460.00"))
            .lowPrice(new BigDecimal("2435.00"))
            .closePrice(new BigDecimal("2450.50"))
            .prevClosePrice(new BigDecimal("2445.00"))
            .volume(100_000L)
            .timestamp(Instant.now())
            .build();

        ValidationResult result = validator.validate(quote);
        assertTrue(result.isValid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void nullQuoteReturnsInvalid() {
        ValidationResult result = validator.validate(null);
        assertFalse(result.isValid());
        assertFalse(result.errors().isEmpty());
    }

    @Test
    void missingSymbolFails() {
        MarketQuote quote = MarketQuote.builder()
            .symbol("")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("100.00"))
            .timestamp(Instant.now())
            .build();

        ValidationResult result = validator.validate(quote);
        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Symbol is required")));
    }

    @Test
    void negativePriceFails() {
        MarketQuote quote = MarketQuote.builder()
            .symbol("TCS")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("-10.00"))
            .timestamp(Instant.now())
            .build();

        ValidationResult result = validator.validate(quote);
        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Last price must be positive")));
    }

    @Test
    void ohlcHighLowerThanLowFails() {
        MarketQuote quote = MarketQuote.builder()
            .symbol("INFY")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("1500.00"))
            .openPrice(new BigDecimal("1510.00"))
            .highPrice(new BigDecimal("1490.00")) // High lower than open/low
            .lowPrice(new BigDecimal("1520.00"))
            .closePrice(new BigDecimal("1500.00"))
            .timestamp(Instant.now())
            .build();

        ValidationResult result = validator.validate(quote);
        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("High") && e.contains("Low")));
    }

    @Test
    void negativeVolumeFails() {
        MarketQuote quote = MarketQuote.builder()
            .symbol("SBIN")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("750.00"))
            .volume(-50L)
            .timestamp(Instant.now())
            .build();

        ValidationResult result = validator.validate(quote);
        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Volume cannot be negative")));
    }

    @Test
    void futureTimestampExceedingToleranceFails() {
        MarketQuote quote = MarketQuote.builder()
            .symbol("HDFCBANK")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("1600.00"))
            .timestamp(Instant.now().plusSeconds(300)) // 5 mins in future (> 60s tolerance)
            .build();

        ValidationResult result = validator.validate(quote);
        assertFalse(result.isValid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Future timestamp detected")));
    }
}
