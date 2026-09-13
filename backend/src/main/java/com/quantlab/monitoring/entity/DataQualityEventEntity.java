package com.quantlab.monitoring.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "data_quality_events")
public class DataQualityEventEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(length = 32)
    private String symbol;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType; // PRICE_ANOMALY, FUTURE_DATA_DETECTED, MISSING_OHLC, NULL_SPIKE, STALE_FEED, DUPLICATE_DATA, INVALID_OHLC, TIMESTAMP_ANOMALY

    @Column(nullable = false, length = 32)
    private String severity; // INFO, WARNING, ERROR, CRITICAL

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "affected_records_count", nullable = false)
    private int affectedRecordsCount = 1;

    @Column(name = "source_timestamp")
    private Instant sourceTimestamp;

    @Column(name = "available_timestamp")
    private Instant availableTimestamp;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getAffectedRecordsCount() { return affectedRecordsCount; }
    public void setAffectedRecordsCount(int affectedRecordsCount) { this.affectedRecordsCount = affectedRecordsCount; }

    public Instant getSourceTimestamp() { return sourceTimestamp; }
    public void setSourceTimestamp(Instant sourceTimestamp) { this.sourceTimestamp = sourceTimestamp; }

    public Instant getAvailableTimestamp() { return availableTimestamp; }
    public void setAvailableTimestamp(Instant availableTimestamp) { this.availableTimestamp = availableTimestamp; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
