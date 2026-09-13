package com.quantlab.monitoring.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feature_drift_events")
public class FeatureDriftEventEntity {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "feature_name", nullable = false, length = 64)
    private String featureName;

    @Column(name = "metric_type", nullable = false, length = 32)
    private String metricType; // PSI, KS_TEST, WASSERSTEIN, MEAN_DIFF

    @Column(name = "observed_value", nullable = false)
    private Double observedValue;

    @Column(nullable = false)
    private Double threshold;

    @Column(name = "drift_status", nullable = false, length = 32)
    private String driftStatus; // NORMAL, WATCH, WARNING, CRITICAL

    @Column(name = "sample_size", nullable = false)
    private int sampleSize;

    @Column(name = "reference_window", length = 64)
    private String referenceWindow;

    @Column(name = "evaluation_window", length = 64)
    private String evaluationWindow;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }

    public String getMetricType() { return metricType; }
    public void setMetricType(String metricType) { this.metricType = metricType; }

    public Double getObservedValue() { return observedValue; }
    public void setObservedValue(Double observedValue) { this.observedValue = observedValue; }

    public Double getThreshold() { return threshold; }
    public void setThreshold(Double threshold) { this.threshold = threshold; }

    public String getDriftStatus() { return driftStatus; }
    public void setDriftStatus(String driftStatus) { this.driftStatus = driftStatus; }

    public int getSampleSize() { return sampleSize; }
    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }

    public String getReferenceWindow() { return referenceWindow; }
    public void setReferenceWindow(String referenceWindow) { this.referenceWindow = referenceWindow; }

    public String getEvaluationWindow() { return evaluationWindow; }
    public void setEvaluationWindow(String evaluationWindow) { this.evaluationWindow = evaluationWindow; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
