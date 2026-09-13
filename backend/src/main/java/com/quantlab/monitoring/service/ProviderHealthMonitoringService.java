package com.quantlab.monitoring.service;

import com.quantlab.monitoring.entity.ProviderHealthEntity;
import com.quantlab.monitoring.repository.ProviderHealthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProviderHealthMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(ProviderHealthMonitoringService.class);

    private final ProviderHealthRepository providerHealthRepository;
    private final AlertLifecycleService alertLifecycleService;
    private final MonitoringMetricsService metricsService;

    public ProviderHealthMonitoringService(
            ProviderHealthRepository providerHealthRepository,
            AlertLifecycleService alertLifecycleService,
            MonitoringMetricsService metricsService) {
        this.providerHealthRepository = providerHealthRepository;
        this.alertLifecycleService = alertLifecycleService;
        this.metricsService = metricsService;
    }

    public List<ProviderHealthEntity> getAllProviderHealth() {
        return providerHealthRepository.findAllByOrderByUpdatedAtDesc();
    }

    public Optional<ProviderHealthEntity> getProviderHealth(String provider) {
        return providerHealthRepository.findByProvider(provider);
    }

    @Transactional
    public ProviderHealthEntity recordProviderUpdate(
            String providerName,
            Instant marketTimestamp,
            int errorCount,
            int activeSymbols,
            int expectedSymbols,
            double latencyMs) {

        ProviderHealthEntity entity = providerHealthRepository.findByProvider(providerName)
                .orElseGet(() -> {
                    ProviderHealthEntity p = new ProviderHealthEntity();
                    p.setId(UUID.randomUUID());
                    p.setProvider(providerName);
                    return p;
                });

        Instant now = Instant.now();
        entity.setLastSuccessfulUpdate(errorCount == 0 ? now : entity.getLastSuccessfulUpdate());
        entity.setLastMarketTimestamp(marketTimestamp);
        entity.setLatencyMs(latencyMs);

        double dataAgeSeconds = entity.getLastSuccessfulUpdate() != null
                ? Duration.between(entity.getLastSuccessfulUpdate(), now).getSeconds()
                : 999999.0;
        entity.setDataAgeSeconds(dataAgeSeconds);

        double coveragePct = expectedSymbols > 0 ? ((double) activeSymbols / expectedSymbols) * 100.0 : 100.0;
        entity.setUniverseCoveragePct(coveragePct);

        if (errorCount > 0) {
            entity.setConsecutiveFailures(entity.getConsecutiveFailures() + errorCount);
            metricsService.recordProviderFailure(providerName);
        } else {
            entity.setConsecutiveFailures(0);
        }

        // Determine Connection Status and Freshness Status
        String connStatus;
        String freshnessStatus;
        double healthScore = 100.0;

        if (entity.getConsecutiveFailures() >= 5 || dataAgeSeconds > 3600) {
            connStatus = "UNAVAILABLE";
            freshnessStatus = "NOT_AVAILABLE";
            healthScore = 0.0;
        } else if (entity.getConsecutiveFailures() > 0 || dataAgeSeconds > 600) {
            connStatus = "CRITICAL";
            freshnessStatus = "STALE";
            healthScore = 25.0;
        } else if (dataAgeSeconds > 180 || coveragePct < 90.0) {
            connStatus = "DEGRADED";
            freshnessStatus = "DELAYED";
            healthScore = 65.0;
        } else {
            connStatus = "HEALTHY";
            freshnessStatus = "REAL_TIME";
            healthScore = 100.0;
        }

        entity.setConnectionStatus(connStatus);
        entity.setDataFreshnessStatus(freshnessStatus);
        entity.setHealthScore(healthScore);
        entity.setUpdatedAt(now);

        ProviderHealthEntity saved = providerHealthRepository.save(entity);

        if ("CRITICAL".equals(connStatus) || "UNAVAILABLE".equals(connStatus)) {
            alertLifecycleService.evaluateAndRaiseAlert(
                    "DATA_FEED_STOPPED",
                    "FEED_" + providerName,
                    dataAgeSeconds,
                    String.format("Provider %s is in %s state (data age: %.0fs, failures: %d)",
                            providerName, connStatus, dataAgeSeconds, entity.getConsecutiveFailures())
            );
        }

        return saved;
    }
}
