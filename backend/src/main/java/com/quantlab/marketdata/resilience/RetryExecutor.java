package com.quantlab.marketdata.resilience;

import com.quantlab.marketdata.model.ErrorCategory;
import com.quantlab.marketdata.provider.AuthenticationException;
import com.quantlab.marketdata.provider.MalformedResponseException;
import com.quantlab.marketdata.provider.ProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resilient Retry Executor with exponential backoff and jitter.
 * Automatically fails fast on non-retryable errors (Authentication, Malformed data)
 * while safely retrying transient network timeouts, 429s, and 5xx errors.
 */
public class RetryExecutor {

    private static final Logger log = LoggerFactory.getLogger(RetryExecutor.class);

    private final int maxRetries;
    private final long initialBackoffMs;
    private final double backoffMultiplier;

    public RetryExecutor(int maxRetries, long initialBackoffMs, double backoffMultiplier) {
        this.maxRetries = Math.max(0, maxRetries);
        this.initialBackoffMs = Math.max(100, initialBackoffMs);
        this.backoffMultiplier = Math.max(1.0, backoffMultiplier);
    }

    public static RetryExecutor defaultExecutor() {
        return new RetryExecutor(3, 500, 2.0);
    }

    public <T> T execute(String operationName, Callable<T> task) throws Exception {
        int attempt = 0;
        long currentBackoff = initialBackoffMs;

        while (true) {
            try {
                attempt++;
                return task.call();
            } catch (Exception ex) {
                if (!isRetryable(ex) || attempt > maxRetries) {
                    log.error("[RetryExecutor] Operation '{}' failed permanently after {} attempt(s): {}",
                            operationName, attempt, ex.getMessage());
                    throw ex;
                }

                // Add random jitter between 0.8x and 1.2x
                double jitter = ThreadLocalRandom.current().nextDouble(0.8, 1.2);
                long sleepDuration = (long) (currentBackoff * jitter);

                log.warn("[RetryExecutor] Transient error in '{}' (attempt {}/{}). Retrying in {} ms: {}",
                        operationName, attempt, maxRetries, sleepDuration, ex.getMessage());

                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ProviderException("UNKNOWN", ErrorCategory.UNKNOWN, "Execution interrupted during retry backoff", ie);
                }

                currentBackoff = (long) (currentBackoff * backoffMultiplier);
            }
        }
    }

    private boolean isRetryable(Exception ex) {
        // Fast-fail non-retryable errors
        if (ex instanceof AuthenticationException || ex instanceof MalformedResponseException) {
            return false;
        }
        if (ex instanceof IllegalArgumentException || ex instanceof NullPointerException) {
            return false;
        }
        return true;
    }
}
