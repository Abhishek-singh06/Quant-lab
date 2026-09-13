package com.quantlab.marketdata.resilience;

import com.quantlab.marketdata.provider.RateLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token-bucket provider-aware rate limiter.
 * Protects market data providers from request storms and enforces strict quota compliance.
 */
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    private final String providerName;
    private final int maxPermitsPerMinute;
    private final AtomicInteger currentTokens;
    private final AtomicLong lastRefillTimestamp;

    public RateLimiter(String providerName, int maxPermitsPerMinute) {
        this.providerName = providerName;
        this.maxPermitsPerMinute = Math.max(1, maxPermitsPerMinute);
        this.currentTokens = new AtomicInteger(this.maxPermitsPerMinute);
        this.lastRefillTimestamp = new AtomicLong(Instant.now().toEpochMilli());
    }

    public synchronized void acquire() {
        refill();
        if (currentTokens.get() <= 0) {
            long waitMs = 60_000 - (Instant.now().toEpochMilli() - lastRefillTimestamp.get());
            if (waitMs > 0) {
                log.warn("[RateLimiter] Quota exhausted for provider {}. Throttling for {} ms", providerName, waitMs);
                try {
                    Thread.sleep(Math.min(waitMs, 5000));
                    refill();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RateLimitExceededException(providerName, "Rate limit acquisition interrupted");
                }
            }
            if (currentTokens.get() <= 0) {
                throw new RateLimitExceededException(providerName, "Rate limit exceeded (" + maxPermitsPerMinute + " req/min)");
            }
        }
        currentTokens.decrementAndGet();
    }

    public synchronized boolean tryAcquire() {
        refill();
        if (currentTokens.get() > 0) {
            currentTokens.decrementAndGet();
            return true;
        }
        return false;
    }

    private void refill() {
        long now = Instant.now().toEpochMilli();
        long elapsed = now - lastRefillTimestamp.get();
        if (elapsed >= 60_000) {
            currentTokens.set(maxPermitsPerMinute);
            lastRefillTimestamp.set(now);
        }
    }

    public int getAvailableTokens() {
        refill();
        return currentTokens.get();
    }

    public int getMaxPermitsPerMinute() {
        return maxPermitsPerMinute;
    }
}
