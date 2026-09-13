package com.quantlab.news.intelligence;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * Event and News Signal Exponential Time Decay Service.
 * 
 * Computes time-decayed weights for historical news features:
 * Weight(t) = exp( - ln(2) * (ageHours / halfLifeHours) )
 */
@Service
public class EventDecayService {

    private static final double LN_2 = Math.log(2.0);

    /**
     * Compute exponential decay factor between [0.0, 1.0].
     * 
     * @param eventTime Timestamp when information became available.
     * @param asOfTime Current evaluation timestamp.
     * @param halfLifeHours Half-life in hours (e.g. 24.0 or 48.0 hours).
     */
    public BigDecimal calculateDecayFactor(Instant eventTime, Instant asOfTime, double halfLifeHours) {
        if (eventTime == null || asOfTime == null || eventTime.isAfter(asOfTime)) {
            return BigDecimal.ZERO;
        }

        double ageHours = Duration.between(eventTime, asOfTime).toMinutes() / 60.0;
        if (ageHours < 0) {
            return BigDecimal.ZERO;
        }

        double decay = Math.exp(-LN_2 * (ageHours / Math.max(1.0, halfLifeHours)));
        return BigDecimal.valueOf(Math.max(0.0, Math.min(1.0, decay))).setScale(6, RoundingMode.HALF_UP);
    }
}
