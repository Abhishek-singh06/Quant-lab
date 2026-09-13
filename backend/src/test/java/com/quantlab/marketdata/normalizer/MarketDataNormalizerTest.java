package com.quantlab.marketdata.normalizer;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketQuote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataNormalizerTest {

    private MarketDataNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new MarketDataNormalizer();
    }

    @Test
    void normalizeTrimsAndUppercasesSymbols() {
        assertEquals("RELIANCE", normalizer.normalizeSymbol("  reliance  "));
        assertEquals("NIFTY 50", normalizer.normalizeSymbol("nifty50"));
        assertEquals("NIFTY 50", normalizer.normalizeSymbol("nifty-50"));
        assertEquals("NIFTY BANK", normalizer.normalizeSymbol("banknifty"));
    }

    @Test
    void normalizeDerivesPriceChangeAndPercentageWhenMissing() {
        MarketQuote raw = MarketQuote.builder()
            .symbol("infy")
            .exchange(Exchange.NSE)
            .lastPrice(new BigDecimal("1500.00"))
            .prevClosePrice(new BigDecimal("1450.00"))
            .timestamp(Instant.now())
            .build();

        MarketQuote normalized = normalizer.normalize(raw, "test-run-1");

        assertEquals("INFY", normalized.symbol());
        assertEquals("test-run-1", normalized.runId());
        assertNotNull(normalized.change());
        assertEquals(0, new BigDecimal("50.0000").compareTo(normalized.change()));
        assertNotNull(normalized.changePercent());
        assertTrue(normalized.changePercent().compareTo(BigDecimal.ZERO) > 0);
    }
}
