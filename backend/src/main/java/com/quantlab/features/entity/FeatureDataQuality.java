package com.quantlab.features.entity;

import com.quantlab.features.model.ValidationStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "feature_data_quality", schema = "market_data")
public class FeatureDataQuality {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", length = 64)
    private String runId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "feature_name", nullable = false, length = 64)
    private String featureName;

    @Column(name = "feature_timestamp", nullable = false)
    private Instant featureTimestamp;

    @Column(name = "feature_value", precision = 24, scale = 8)
    private BigDecimal featureValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false, length = 32)
    private ValidationStatus validationStatus;

    @Column(name = "anomaly_reason", length = 255)
    private String anomalyReason;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt = Instant.now();

    public FeatureDataQuality() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    public Instant getFeatureTimestamp() { return featureTimestamp; }
    public void setFeatureTimestamp(Instant featureTimestamp) { this.featureTimestamp = featureTimestamp; }
    public BigDecimal getFeatureValue() { return featureValue; }
    public void setFeatureValue(BigDecimal featureValue) { this.featureValue = featureValue; }
    public ValidationStatus getValidationStatus() { return validationStatus; }
    public void setValidationStatus(ValidationStatus validationStatus) { this.validationStatus = validationStatus; }
    public String getAnomalyReason() { return anomalyReason; }
    public void setAnomalyReason(String anomalyReason) { this.anomalyReason = anomalyReason; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
