package com.quantlab.marketdata.resilience;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterTest {

    @Test
    void rateLimiterAcquiresWithinLimit() {
        RateLimiter limiter = new RateLimiter("TEST_PROVIDER", 10);

        assertEquals(10, limiter.getMaxPermitsPerMinute());
        assertTrue(limiter.tryAcquire());
        assertEquals(9, limiter.getAvailableTokens());
    }

    @Test
    void rateLimiterRejectsWhenDepleted() {
        RateLimiter limiter = new RateLimiter("TEST_PROVIDER", 2);

        assertTrue(limiter.tryAcquire());
        assertTrue(limiter.tryAcquire());
        assertFalse(limiter.tryAcquire());
    }
}
