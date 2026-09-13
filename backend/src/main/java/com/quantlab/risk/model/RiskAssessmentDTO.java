package com.quantlab.risk.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RiskAssessmentDTO {
    private UUID id;
    private Instant timestamp;
    private Instant informationAvailableAt;
    private Instant calculatedAt;
    private UUID portfolioId;
    private UUID riskProfileId;
    private UUID signalId;
    private String symbol;
    private String signalType;
    private double signalScore;
    private double signalConfidence;

    // Sizing outputs
    private double suggestedAllocation;
    private double maximumAllocation;
    private double recommendedQuantity;
    private double entryPrice;
    private double stopPrice;
    private Double targetPrice;
    private double stopDistance;
    private double stopDistancePct;
    private StopMethod stopMethod;

    // Risk metrics
    private double positionRiskAmount;
    private double positionRiskPercent;
    private double estimatedDownside;
    private double portfolioValue;
    private double remainingRiskBudget;
    private Double portfolioVolatility;
    private double securityVolatility;
    private Double expectedVolatility;
    private Double maxCorrelation;
    private Double sectorExposureAfterTrade;
    private double currentDrawdown;
    private Double riskRewardRatio;

    // Decision
    private RiskDecision riskDecision;
    private RiskLevel riskLevel;

    // Traces and warnings
    private RiskTraceDTO riskTrace;
    private Map<String, Double> limitingConstraints;
    private List<RiskWarningDTO> riskWarnings;
    private List<RiskAdjustmentDTO> adjustments;
    private String reasoning;
    private String dataQualityStatus;

    // Provenance
    private String riskEngineVersion;
    private String riskProfileVersion;
    private String signalVersion;
    private String dataVersion;
    private Instant createdAt;

    public RiskAssessmentDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }

    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getRiskProfileId() { return riskProfileId; }
    public void setRiskProfileId(UUID riskProfileId) { this.riskProfileId = riskProfileId; }

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }

    public double getSignalScore() { return signalScore; }
    public void setSignalScore(double signalScore) { this.signalScore = signalScore; }

    public double getSignalConfidence() { return signalConfidence; }
    public void setSignalConfidence(double signalConfidence) { this.signalConfidence = signalConfidence; }

    public double getSuggestedAllocation() { return suggestedAllocation; }
    public void setSuggestedAllocation(double suggestedAllocation) { this.suggestedAllocation = suggestedAllocation; }

    public double getMaximumAllocation() { return maximumAllocation; }
    public void setMaximumAllocation(double maximumAllocation) { this.maximumAllocation = maximumAllocation; }

    public double getRecommendedQuantity() { return recommendedQuantity; }
    public void setRecommendedQuantity(double recommendedQuantity) { this.recommendedQuantity = recommendedQuantity; }

    public double getEntryPrice() { return entryPrice; }
    public void setEntryPrice(double entryPrice) { this.entryPrice = entryPrice; }

    public double getStopPrice() { return stopPrice; }
    public void setStopPrice(double stopPrice) { this.stopPrice = stopPrice; }

    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }

    public double getStopDistance() { return stopDistance; }
    public void setStopDistance(double stopDistance) { this.stopDistance = stopDistance; }

    public double getStopDistancePct() { return stopDistancePct; }
    public void setStopDistancePct(double stopDistancePct) { this.stopDistancePct = stopDistancePct; }

    public StopMethod getStopMethod() { return stopMethod; }
    public void setStopMethod(StopMethod stopMethod) { this.stopMethod = stopMethod; }

    public double getPositionRiskAmount() { return positionRiskAmount; }
    public void setPositionRiskAmount(double positionRiskAmount) { this.positionRiskAmount = positionRiskAmount; }

    public double getPositionRiskPercent() { return positionRiskPercent; }
    public void setPositionRiskPercent(double positionRiskPercent) { this.positionRiskPercent = positionRiskPercent; }

    public double getEstimatedDownside() { return estimatedDownside; }
    public void setEstimatedDownside(double estimatedDownside) { this.estimatedDownside = estimatedDownside; }

    public double getPortfolioValue() { return portfolioValue; }
    public void setPortfolioValue(double portfolioValue) { this.portfolioValue = portfolioValue; }

    public double getRemainingRiskBudget() { return remainingRiskBudget; }
    public void setRemainingRiskBudget(double remainingRiskBudget) { this.remainingRiskBudget = remainingRiskBudget; }

    public Double getPortfolioVolatility() { return portfolioVolatility; }
    public void setPortfolioVolatility(Double portfolioVolatility) { this.portfolioVolatility = portfolioVolatility; }

    public double getSecurityVolatility() { return securityVolatility; }
    public void setSecurityVolatility(double securityVolatility) { this.securityVolatility = securityVolatility; }

    public Double getExpectedVolatility() { return expectedVolatility; }
    public void setExpectedVolatility(Double expectedVolatility) { this.expectedVolatility = expectedVolatility; }

    public Double getMaxCorrelation() { return maxCorrelation; }
    public void setMaxCorrelation(Double maxCorrelation) { this.maxCorrelation = maxCorrelation; }

    public Double getSectorExposureAfterTrade() { return sectorExposureAfterTrade; }
    public void setSectorExposureAfterTrade(Double sectorExposureAfterTrade) { this.sectorExposureAfterTrade = sectorExposureAfterTrade; }

    public double getCurrentDrawdown() { return currentDrawdown; }
    public void setCurrentDrawdown(double currentDrawdown) { this.currentDrawdown = currentDrawdown; }

    public Double getRiskRewardRatio() { return riskRewardRatio; }
    public void setRiskRewardRatio(Double riskRewardRatio) { this.riskRewardRatio = riskRewardRatio; }

    public RiskDecision getRiskDecision() { return riskDecision; }
    public void setRiskDecision(RiskDecision riskDecision) { this.riskDecision = riskDecision; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public RiskTraceDTO getRiskTrace() { return riskTrace; }
    public void setRiskTrace(RiskTraceDTO riskTrace) { this.riskTrace = riskTrace; }

    public Map<String, Double> getLimitingConstraints() { return limitingConstraints; }
    public void setLimitingConstraints(Map<String, Double> limitingConstraints) { this.limitingConstraints = limitingConstraints; }

    public List<RiskWarningDTO> getRiskWarnings() { return riskWarnings; }
    public void setRiskWarnings(List<RiskWarningDTO> riskWarnings) { this.riskWarnings = riskWarnings; }

    public List<RiskAdjustmentDTO> getAdjustments() { return adjustments; }
    public void setAdjustments(List<RiskAdjustmentDTO> adjustments) { this.adjustments = adjustments; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public String getDataQualityStatus() { return dataQualityStatus; }
    public void setDataQualityStatus(String dataQualityStatus) { this.dataQualityStatus = dataQualityStatus; }

    public String getRiskEngineVersion() { return riskEngineVersion; }
    public void setRiskEngineVersion(String riskEngineVersion) { this.riskEngineVersion = riskEngineVersion; }

    public String getRiskProfileVersion() { return riskProfileVersion; }
    public void setRiskProfileVersion(String riskProfileVersion) { this.riskProfileVersion = riskProfileVersion; }

    public String getSignalVersion() { return signalVersion; }
    public void setSignalVersion(String signalVersion) { this.signalVersion = signalVersion; }

    public String getDataVersion() { return dataVersion; }
    public void setDataVersion(String dataVersion) { this.dataVersion = dataVersion; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
