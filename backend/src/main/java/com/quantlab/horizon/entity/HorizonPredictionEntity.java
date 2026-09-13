package com.quantlab.horizon.entity;

import com.quantlab.horizon.model.HorizonOutlook;
import com.quantlab.horizon.model.TradingHorizon;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "horizon_predictions", schema = "market_data")
public class HorizonPredictionEntity {

    @Id
    private UUID id;

    @Column(name = "model_version_id")
    private UUID modelVersionId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "instrument_id")
    private Long instrumentId;

    @Column(name = "prediction_timestamp", nullable = false)
    private Instant predictionTimestamp;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "horizon", nullable = false, length = 32)
    private TradingHorizon horizon;

    @Column(name = "horizon_period", nullable = false, length = 32)
    private String horizonPeriod;

    @Column(name = "expected_return")
    private Double expectedReturn;

    @Column(name = "probability_positive")
    private Double probabilityPositive;

    @Column(name = "probability_negative")
    private Double probabilityNegative;

    @Column(name = "predicted_class")
    private Integer predictedClass;

    @Column(name = "expected_volatility")
    private Double expectedVolatility;

    @Column(name = "expected_drawdown")
    private Double expectedDrawdown;

    @Column(name = "relative_return")
    private Double relativeReturn;

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "outlook", nullable = false, length = 20)
    private HorizonOutlook outlook;

    @Column(name = "feature_contributions", columnDefinition = "jsonb")
    private String featureContributions;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @Column(name = "feature_set_version", nullable = false, length = 32)
    private String featureSetVersion;

    @Column(name = "target_set_version", nullable = false, length = 32)
    private String targetSetVersion;

    @Column(name = "data_version", nullable = false, length = 32)
    private String dataVersion = "1";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public HorizonPredictionEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (calculatedAt == null) calculatedAt = Instant.now();
        if (predictionTimestamp == null) predictionTimestamp = Instant.now();
        if (informationAvailableAt == null) informationAvailableAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getModelVersionId() { return modelVersionId; }
    public void setModelVersionId(UUID modelVersionId) { this.modelVersionId = modelVersionId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }

    public Instant getPredictionTimestamp() { return predictionTimestamp; }
    public void setPredictionTimestamp(Instant predictionTimestamp) { this.predictionTimestamp = predictionTimestamp; }

    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }

    public TradingHorizon getHorizon() { return horizon; }
    public void setHorizon(TradingHorizon horizon) { this.horizon = horizon; }

    public String getHorizonPeriod() { return horizonPeriod; }
    public void setHorizonPeriod(String horizonPeriod) { this.horizonPeriod = horizonPeriod; }

    public Double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(Double expectedReturn) { this.expectedReturn = expectedReturn; }

    public Double getProbabilityPositive() { return probabilityPositive; }
    public void setProbabilityPositive(Double probabilityPositive) { this.probabilityPositive = probabilityPositive; }

    public Double getProbabilityNegative() { return probabilityNegative; }
    public void setProbabilityNegative(Double probabilityNegative) { this.probabilityNegative = probabilityNegative; }

    public Integer getPredictedClass() { return predictedClass; }
    public void setPredictedClass(Integer predictedClass) { this.predictedClass = predictedClass; }

    public Double getExpectedVolatility() { return expectedVolatility; }
    public void setExpectedVolatility(Double expectedVolatility) { this.expectedVolatility = expectedVolatility; }

    public Double getExpectedDrawdown() { return expectedDrawdown; }
    public void setExpectedDrawdown(Double expectedDrawdown) { this.expectedDrawdown = expectedDrawdown; }

    public Double getRelativeReturn() { return relativeReturn; }
    public void setRelativeReturn(Double relativeReturn) { this.relativeReturn = relativeReturn; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public HorizonOutlook getOutlook() { return outlook; }
    public void setOutlook(HorizonOutlook outlook) { this.outlook = outlook; }

    public String getFeatureContributions() { return featureContributions; }
    public void setFeatureContributions(String featureContributions) { this.featureContributions = featureContributions; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getFeatureSetVersion() { return featureSetVersion; }
    public void setFeatureSetVersion(String featureSetVersion) { this.featureSetVersion = featureSetVersion; }

    public String getTargetSetVersion() { return targetSetVersion; }
    public void setTargetSetVersion(String targetSetVersion) { this.targetSetVersion = targetSetVersion; }

    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
