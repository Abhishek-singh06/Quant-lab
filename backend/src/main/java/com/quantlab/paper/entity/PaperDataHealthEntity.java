package com.quantlab.paper.entity;

import com.quantlab.paper.model.ConnectionStatus;
import com.quantlab.paper.model.DataFreshnessStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_data_health")
public class PaperDataHealthEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String provider = "AUTHORIZED_FEED";

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false, length = 32)
    private ConnectionStatus connectionStatus = ConnectionStatus.HEALTHY;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_freshness_status", nullable = false, length = 32)
    private DataFreshnessStatus dataFreshnessStatus = DataFreshnessStatus.REAL_TIME;

    @Column(name = "last_successful_update")
    private Instant lastSuccessfulUpdate;

    @Column(name = "last_market_timestamp")
    private Instant lastMarketTimestamp;

    @Column(name = "latency_ms")
    private Double latencyMs = 0.0;

    @Column(name = "data_age_seconds")
    private Double dataAgeSeconds = 0.0;

    @Column(name = "error_count", nullable = false)
    private Integer errorCount = 0;

    @Column(name = "rate_limit_status", length = 32)
    private String rateLimitStatus = "NORMAL";

    @Column(name = "universe_coverage_pct")
    private Double universeCoveragePct = 100.0;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public ConnectionStatus getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(ConnectionStatus connectionStatus) { this.connectionStatus = connectionStatus; }

    public DataFreshnessStatus getDataFreshnessStatus() { return dataFreshnessStatus; }
    public void setDataFreshnessStatus(DataFreshnessStatus dataFreshnessStatus) { this.dataFreshnessStatus = dataFreshnessStatus; }

    public Instant getLastSuccessfulUpdate() { return lastSuccessfulUpdate; }
    public void setLastSuccessfulUpdate(Instant lastSuccessfulUpdate) { this.lastSuccessfulUpdate = lastSuccessfulUpdate; }

    public Instant getLastMarketTimestamp() { return lastMarketTimestamp; }
    public void setLastMarketTimestamp(Instant lastMarketTimestamp) { this.lastMarketTimestamp = lastMarketTimestamp; }

    public Double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Double latencyMs) { this.latencyMs = latencyMs; }

    public Double getDataAgeSeconds() { return dataAgeSeconds; }
    public void setDataAgeSeconds(Double dataAgeSeconds) { this.dataAgeSeconds = dataAgeSeconds; }

    public Integer getErrorCount() { return errorCount; }
    public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }

    public String getRateLimitStatus() { return rateLimitStatus; }
    public void setRateLimitStatus(String rateLimitStatus) { this.rateLimitStatus = rateLimitStatus; }

    public Double getUniverseCoveragePct() { return universeCoveragePct; }
    public void setUniverseCoveragePct(Double universeCoveragePct) { this.universeCoveragePct = universeCoveragePct; }

    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
