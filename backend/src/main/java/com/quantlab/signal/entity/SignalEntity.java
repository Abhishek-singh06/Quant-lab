package com.quantlab.signal.entity;

import com.quantlab.signal.model.ConflictSeverity;
import com.quantlab.signal.model.SignalQualityStatus;
import com.quantlab.signal.model.SignalType;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signals", schema = "market_data")
public class SignalEntity {

    @Id
    private UUID id;

    @Column(name = "run_id")
    private UUID runId;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "signal_timestamp", nullable = false)
    private Instant signalTimestamp;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "signal", nullable = false, length = 20)
    private SignalType signal;

    @Column(name = "signal_score", nullable = false)
    private Double signalScore;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "expected_return")
    private Double expectedReturn;

    @Column(name = "expected_volatility")
    private Double expectedVolatility;

    @Column(name = "return_to_volatility_ratio")
    private Double returnToVolatilityRatio;

    @Column(name = "direction", nullable = false, length = 20)
    private String direction;

    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_severity", nullable = false, length = 20)
    private ConflictSeverity conflictSeverity;

    @Column(name = "conflict_score", nullable = false)
    private Double conflictScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_quality_status", nullable = false, length = 30)
    private SignalQualityStatus dataQualityStatus;

    @Column(name = "freshness_score", nullable = false)
    private Double freshnessScore;

    @Column(name = "reasoning", nullable = false, columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "structured_reasoning", columnDefinition = "JSONB")
    private String structuredReasoning;

    @Column(name = "supporting_categories", columnDefinition = "JSONB")
    private String supportingCategories;

    @Column(name = "opposing_categories", columnDefinition = "JSONB")
    private String opposingCategories;

    @Column(name = "signal_version", nullable = false, length = 32)
    private String signalVersion;

    @Column(name = "configuration_version", nullable = false, length = 32)
    private String configurationVersion;

    @Column(name = "feature_version", length = 32)
    private String featureVersion;

    @Column(name = "model_version", length = 32)
    private String modelVersion;

    @Column(name = "regime_version", length = 32)
    private String regimeVersion;

    @Column(name = "data_version", length = 32)
    private String dataVersion;

    @Column(name = "is_latest", nullable = false)
    private Boolean isLatest = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SignalEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }

    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Instant getSignalTimestamp() { return signalTimestamp; }
    public void setSignalTimestamp(Instant signalTimestamp) { this.signalTimestamp = signalTimestamp; }

    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }

    public SignalType getSignal() { return signal; }
    public void setSignal(SignalType signal) { this.signal = signal; }

    public Double getSignalScore() { return signalScore; }
    public void setSignalScore(Double signalScore) { this.signalScore = signalScore; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(Double expectedReturn) { this.expectedReturn = expectedReturn; }

    public Double getExpectedVolatility() { return expectedVolatility; }
    public void setExpectedVolatility(Double expectedVolatility) { this.expectedVolatility = expectedVolatility; }

    public Double getReturnToVolatilityRatio() { return returnToVolatilityRatio; }
    public void setReturnToVolatilityRatio(Double returnToVolatilityRatio) { this.returnToVolatilityRatio = returnToVolatilityRatio; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public ConflictSeverity getConflictSeverity() { return conflictSeverity; }
    public void setConflictSeverity(ConflictSeverity conflictSeverity) { this.conflictSeverity = conflictSeverity; }

    public Double getConflictScore() { return conflictScore; }
    public void setConflictScore(Double conflictScore) { this.conflictScore = conflictScore; }

    public SignalQualityStatus getDataQualityStatus() { return dataQualityStatus; }
    public void setDataQualityStatus(SignalQualityStatus dataQualityStatus) { this.dataQualityStatus = dataQualityStatus; }

    public Double getFreshnessScore() { return freshnessScore; }
    public void setFreshnessScore(Double freshnessScore) { this.freshnessScore = freshnessScore; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public String getStructuredReasoning() { return structuredReasoning; }
    public void setStructuredReasoning(String structuredReasoning) { this.structuredReasoning = structuredReasoning; }

    public String getSupportingCategories() { return supportingCategories; }
    public void setSupportingCategories(String supportingCategories) { this.supportingCategories = supportingCategories; }

    public String getOpposingCategories() { return opposingCategories; }
    public void setOpposingCategories(String opposingCategories) { this.opposingCategories = opposingCategories; }

    public String getSignalVersion() { return signalVersion; }
    public void setSignalVersion(String signalVersion) { this.signalVersion = signalVersion; }

    public String getConfigurationVersion() { return configurationVersion; }
    public void setConfigurationVersion(String configurationVersion) { this.configurationVersion = configurationVersion; }

    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getRegimeVersion() { return regimeVersion; }
    public void setRegimeVersion(String regimeVersion) { this.regimeVersion = regimeVersion; }

    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }

    public Boolean getIsLatest() { return isLatest; }
    public void setIsLatest(Boolean isLatest) { this.isLatest = isLatest; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
