package com.quantlab.horizon.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class HorizonPredictionDTO {
    private UUID id;
    private UUID modelVersionId;
    private String symbol;
    private Long instrumentId;
    private Instant predictionTimestamp;
    private Instant informationAvailableAt;
    private Instant calculatedAt;
    private TradingHorizon horizon;
    private String horizonPeriod;
    private Double expectedReturn;
    private Double probabilityPositive;
    private Double probabilityNegative;
    private Integer predictedClass;
    private Double expectedVolatility;
    private Double expectedDrawdown;
    private Double relativeReturn;
    private double confidence;
    private HorizonOutlook outlook;
    private Map<String, Double> featureContributions;
    private String modelVersion;
    private String featureSetVersion;
    private String targetSetVersion;
    private String dataVersion;
    private Instant createdAt;

    public HorizonPredictionDTO() {}

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

    public Map<String, Double> getFeatureContributions() { return featureContributions; }
    public void setFeatureContributions(Map<String, Double> featureContributions) { this.featureContributions = featureContributions; }

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
