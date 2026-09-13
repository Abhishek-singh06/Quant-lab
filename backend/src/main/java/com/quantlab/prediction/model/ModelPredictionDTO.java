package com.quantlab.prediction.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public class ModelPredictionDTO {

    private String predictionId;
    private String modelId;
    private String modelVersion;
    private String symbol;
    private Instant predictionTimestamp;
    private LocalDate tradingDate;
    private String targetHorizon;
    private ModelType predictionType;
    private BigDecimal predictedReturn;
    private BigDecimal probabilityPositive;
    private BigDecimal probabilityNegative;
    private Integer predictedClass;
    private BigDecimal predictedVolatility;
    private BigDecimal actualReturn;
    private BigDecimal actualVolatility;
    private Map<String, Object> featureContributions;
    private String regimeAtPrediction;
    private Instant informationAvailableAt;
    private Instant calculatedAt;

    public ModelPredictionDTO() {}

    public String getPredictionId() { return predictionId; }
    public void setPredictionId(String predictionId) { this.predictionId = predictionId; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Instant getPredictionTimestamp() { return predictionTimestamp; }
    public void setPredictionTimestamp(Instant predictionTimestamp) { this.predictionTimestamp = predictionTimestamp; }
    public LocalDate getTradingDate() { return tradingDate; }
    public void setTradingDate(LocalDate tradingDate) { this.tradingDate = tradingDate; }
    public String getTargetHorizon() { return targetHorizon; }
    public void setTargetHorizon(String targetHorizon) { this.targetHorizon = targetHorizon; }
    public ModelType getPredictionType() { return predictionType; }
    public void setPredictionType(ModelType predictionType) { this.predictionType = predictionType; }
    public BigDecimal getPredictedReturn() { return predictedReturn; }
    public void setPredictedReturn(BigDecimal predictedReturn) { this.predictedReturn = predictedReturn; }
    public BigDecimal getProbabilityPositive() { return probabilityPositive; }
    public void setProbabilityPositive(BigDecimal probabilityPositive) { this.probabilityPositive = probabilityPositive; }
    public BigDecimal getProbabilityNegative() { return probabilityNegative; }
    public void setProbabilityNegative(BigDecimal probabilityNegative) { this.probabilityNegative = probabilityNegative; }
    public Integer getPredictedClass() { return predictedClass; }
    public void setPredictedClass(Integer predictedClass) { this.predictedClass = predictedClass; }
    public BigDecimal getPredictedVolatility() { return predictedVolatility; }
    public void setPredictedVolatility(BigDecimal predictedVolatility) { this.predictedVolatility = predictedVolatility; }
    public BigDecimal getActualReturn() { return actualReturn; }
    public void setActualReturn(BigDecimal actualReturn) { this.actualReturn = actualReturn; }
    public BigDecimal getActualVolatility() { return actualVolatility; }
    public void setActualVolatility(BigDecimal actualVolatility) { this.actualVolatility = actualVolatility; }
    public Map<String, Object> getFeatureContributions() { return featureContributions; }
    public void setFeatureContributions(Map<String, Object> featureContributions) { this.featureContributions = featureContributions; }
    public String getRegimeAtPrediction() { return regimeAtPrediction; }
    public void setRegimeAtPrediction(String regimeAtPrediction) { this.regimeAtPrediction = regimeAtPrediction; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
