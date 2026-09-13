package com.quantlab.monitoring.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "provider_health")
public class ProviderHealthEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(name = "connection_status", nullable = false, length = 32)
    private String connectionStatus; // HEALTHY, DEGRADED, WARNING, CRITICAL, UNKNOWN, UNAVAILABLE

    @Column(name = "data_freshness_status", nullable = false, length = 32)
    private String dataFreshnessStatus; // REAL_TIME, DELAYED, STALE, NOT_AVAILABLE

    @Column(name = "last_successful_update")
    private Instant lastSuccessfulUpdate;

    @Column(name = "last_market_timestamp")
    private Instant lastMarketTimestamp;

    @Column(name = "latency_ms")
    private Double latencyMs;

    @Column(name = "data_age_seconds")
    private Double dataAgeSeconds;

    @Column(name = "consecutive_failures", nullable = false)
    private int consecutiveFailures = 0;

    @Column(name = "health_score", nullable = false)
    private Double healthScore = 100.0;

    @Column(name = "universe_coverage_pct", nullable = false)
    private Double universeCoveragePct = 100.0;

    @Column(name = "is_failing_over", nullable = false)
    private boolean isFailingOver = false;

    @Column(name = "fallback_provider", length = 64)
    private String fallbackProvider;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }

    public String getDataFreshnessStatus() { return dataFreshnessStatus; }
    public void setDataFreshnessStatus(String dataFreshnessStatus) { this.dataFreshnessStatus = dataFreshnessStatus; }

    public Instant getLastSuccessfulUpdate() { return lastSuccessfulUpdate; }
    public void setLastSuccessfulUpdate(Instant lastSuccessfulUpdate) { this.lastSuccessfulUpdate = lastSuccessfulUpdate; }

    public Instant getLastMarketTimestamp() { return lastMarketTimestamp; }
    public void setLastMarketTimestamp(Instant lastMarketTimestamp) { this.lastMarketTimestamp = lastMarketTimestamp; }

    public Double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Double latencyMs) { this.latencyMs = latencyMs; }

    public Double getDataAgeSeconds() { return dataAgeSeconds; }
    public void setDataAgeSeconds(Double dataAgeSeconds) { this.dataAgeSeconds = dataAgeSeconds; }

    public int getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }

    public Double getHealthScore() { return healthScore; }
    public void setHealthScore(Double healthScore) { this.healthScore = healthScore; }

    public Double getUniverseCoveragePct() { return universeCoveragePct; }
    public void setUniverseCoveragePct(Double universeCoveragePct) { this.universeCoveragePct = universeCoveragePct; }

    public boolean isFailingOver() { return isFailingOver; }
    public void setFailingOver(boolean failingOver) { isFailingOver = failingOver; }

    public String getFallbackProvider() { return fallbackProvider; }
    public void setFallbackProvider(String fallbackProvider) { this.fallbackProvider = fallbackProvider; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
