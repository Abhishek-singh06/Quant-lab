package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_decisions")
public class PaperDecisionEntity {

    @Id
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false, length = 32)
    private String horizon;

    @Column(nullable = false, length = 20)
    private String decision;

    @Column(name = "decision_reason", nullable = false, columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "signal_version", length = 32)
    private String signalVersion;

    @Column(name = "signal_score", nullable = false)
    private Double signalScore;

    @Column(name = "signal_confidence", nullable = false)
    private Double signalConfidence;

    @Column(name = "expected_return")
    private Double expectedReturn;

    @Column(name = "expected_volatility")
    private Double expectedVolatility;

    @Column(name = "predicted_direction", length = 20)
    private String predictedDirection;

    @Column(name = "predicted_probability")
    private Double predictedProbability;

    @Column(name = "prediction_id")
    private UUID predictionId;

    @Column(name = "model_version", length = 32)
    private String modelVersion;

    @Column(name = "risk_assessment_id")
    private UUID riskAssessmentId;

    @Column(name = "risk_engine_version", length = 32)
    private String riskEngineVersion;

    @Column(name = "suggested_allocation", nullable = false)
    private Double suggestedAllocation;

    @Column(name = "maximum_allocation", nullable = false)
    private Double maximumAllocation;

    @Column(name = "recommended_quantity", nullable = false)
    private Integer recommendedQuantity;

    @Column(name = "entry_price", nullable = false)
    private Double entryPrice;

    @Column(name = "stop_price")
    private Double stopPrice;

    @Column(name = "target_price")
    private Double targetPrice;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "supporting_evidence", columnDefinition = "TEXT")
    private String supportingEvidence;

    @Column(name = "opposing_evidence", columnDefinition = "TEXT")
    private String opposingEvidence;

    @Column(name = "data_quality_status", nullable = false, length = 32)
    private String dataQualityStatus = "HIGH_QUALITY";

    @Column(name = "data_version", nullable = false, length = 32)
    private String dataVersion = "1";

    @Column(name = "feature_version", nullable = false, length = 32)
    private String featureVersion = "v1.0.0";

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(nullable = false, length = 32)
    private String status = "RECORDED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }

    public String getSignalVersion() { return signalVersion; }
    public void setSignalVersion(String signalVersion) { this.signalVersion = signalVersion; }

    public Double getSignalScore() { return signalScore; }
    public void setSignalScore(Double signalScore) { this.signalScore = signalScore; }

    public Double getSignalConfidence() { return signalConfidence; }
    public void setSignalConfidence(Double signalConfidence) { this.signalConfidence = signalConfidence; }

    public Double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(Double expectedReturn) { this.expectedReturn = expectedReturn; }

    public Double getExpectedVolatility() { return expectedVolatility; }
    public void setExpectedVolatility(Double expectedVolatility) { this.expectedVolatility = expectedVolatility; }

    public String getPredictedDirection() { return predictedDirection; }
    public void setPredictedDirection(String predictedDirection) { this.predictedDirection = predictedDirection; }

    public Double getPredictedProbability() { return predictedProbability; }
    public void setPredictedProbability(Double predictedProbability) { this.predictedProbability = predictedProbability; }

    public UUID getPredictionId() { return predictionId; }
    public void setPredictionId(UUID predictionId) { this.predictionId = predictionId; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public UUID getRiskAssessmentId() { return riskAssessmentId; }
    public void setRiskAssessmentId(UUID riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }

    public String getRiskEngineVersion() { return riskEngineVersion; }
    public void setRiskEngineVersion(String riskEngineVersion) { this.riskEngineVersion = riskEngineVersion; }

    public Double getSuggestedAllocation() { return suggestedAllocation; }
    public void setSuggestedAllocation(Double suggestedAllocation) { this.suggestedAllocation = suggestedAllocation; }

    public Double getMaximumAllocation() { return maximumAllocation; }
    public void setMaximumAllocation(Double maximumAllocation) { this.maximumAllocation = maximumAllocation; }

    public Integer getRecommendedQuantity() { return recommendedQuantity; }
    public void setRecommendedQuantity(Integer recommendedQuantity) { this.recommendedQuantity = recommendedQuantity; }

    public Double getEntryPrice() { return entryPrice; }
    public void setEntryPrice(Double entryPrice) { this.entryPrice = entryPrice; }

    public Double getStopPrice() { return stopPrice; }
    public void setStopPrice(Double stopPrice) { this.stopPrice = stopPrice; }

    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getSupportingEvidence() { return supportingEvidence; }
    public void setSupportingEvidence(String supportingEvidence) { this.supportingEvidence = supportingEvidence; }

    public String getOpposingEvidence() { return opposingEvidence; }
    public void setOpposingEvidence(String opposingEvidence) { this.opposingEvidence = opposingEvidence; }

    public String getDataQualityStatus() { return dataQualityStatus; }
    public void setDataQualityStatus(String dataQualityStatus) { this.dataQualityStatus = dataQualityStatus; }

    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }

    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }

    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
