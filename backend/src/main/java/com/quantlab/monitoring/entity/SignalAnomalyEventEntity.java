package com.quantlab.monitoring.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signal_anomaly_events")
public class SignalAnomalyEventEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "signal_type", nullable = false, length = 32)
    private String signalType;

    @Column(name = "anomaly_category", nullable = false, length = 64)
    private String anomalyCategory; // BUY_SPIKE, SELL_SPIKE, CONFIDENCE_COLLAPSE, CONCENTRATION_SPIKE, SCORE_DISTRIBUTION_ANOMALY

    @Column(name = "observed_rate", nullable = false)
    private Double observedRate;

    @Column(name = "expected_rate", nullable = false)
    private Double expectedRate;

    @Column(name = "affected_sector", length = 64)
    private String affectedSector;

    @Column(nullable = false, length = 32)
    private String severity; // INFO, WARNING, ERROR, CRITICAL

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }

    public String getAnomalyCategory() { return anomalyCategory; }
    public void setAnomalyCategory(String anomalyCategory) { this.anomalyCategory = anomalyCategory; }

    public Double getObservedRate() { return observedRate; }
    public void setObservedRate(Double observedRate) { this.observedRate = observedRate; }

    public Double getExpectedRate() { return expectedRate; }
    public void setExpectedRate(Double expectedRate) { this.expectedRate = expectedRate; }

    public String getAffectedSector() { return affectedSector; }
    public void setAffectedSector(String affectedSector) { this.affectedSector = affectedSector; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
