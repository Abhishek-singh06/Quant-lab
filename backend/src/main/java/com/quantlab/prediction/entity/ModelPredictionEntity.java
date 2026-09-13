package com.quantlab.prediction.entity;

import com.quantlab.prediction.model.ModelType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "model_predictions", schema = "market_data")
public class ModelPredictionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prediction_id", nullable = false, unique = true, length = 64)
    private String predictionId;

    @Column(name = "model_id", nullable = false, length = 64)
    private String modelId;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "prediction_timestamp", nullable = false)
    private Instant predictionTimestamp;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "target_horizon", nullable = false, length = 16)
    private String targetHorizon;

    @Enumerated(EnumType.STRING)
    @Column(name = "prediction_type", nullable = false, length = 32)
    private ModelType predictionType;

    @Column(name = "predicted_return", precision = 10, scale = 6)
    private BigDecimal predictedReturn;

    @Column(name = "probability_positive", precision = 6, scale = 4)
    private BigDecimal probabilityPositive;

    @Column(name = "probability_negative", precision = 6, scale = 4)
    private BigDecimal probabilityNegative;

    @Column(name = "predicted_class")
    private Integer predictedClass;

    @Column(name = "predicted_volatility", precision = 10, scale = 6)
    private BigDecimal predictedVolatility;

    @Column(name = "actual_return", precision = 10, scale = 6)
    private BigDecimal actualReturn;

    @Column(name = "actual_volatility", precision = 10, scale = 6)
    private BigDecimal actualVolatility;

    @Column(name = "feature_contributions", columnDefinition = "JSONB")
    private String featureContributions;

    @Column(name = "regime_at_prediction", length = 32)
    private String regimeAtPrediction;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    public ModelPredictionEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getFeatureContributions() { return featureContributions; }
    public void setFeatureContributions(String featureContributions) { this.featureContributions = featureContributions; }
    public String getRegimeAtPrediction() { return regimeAtPrediction; }
    public void setRegimeAtPrediction(String regimeAtPrediction) { this.regimeAtPrediction = regimeAtPrediction; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
