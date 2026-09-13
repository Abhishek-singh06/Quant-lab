package com.quantlab.risk.entity;

import com.quantlab.risk.model.RiskDecision;
import com.quantlab.risk.model.RiskLevel;
import com.quantlab.risk.model.StopMethod;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessmentEntity {

    @Id
    private UUID id;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt;

    @Column(name = "portfolio_id")
    private UUID portfolioId;

    @Column(name = "risk_profile_id")
    private UUID riskProfileId;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "signal_type", nullable = false, length = 20)
    private String signalType;

    @Column(name = "signal_score", nullable = false)
    private double signalScore;

    @Column(name = "signal_confidence", nullable = false)
    private double signalConfidence;

    // Sizing outputs
    @Column(name = "suggested_allocation", nullable = false)
    private double suggestedAllocation;

    @Column(name = "maximum_allocation", nullable = false)
    private double maximumAllocation;

    @Column(name = "recommended_quantity", nullable = false)
    private double recommendedQuantity;

    @Column(name = "entry_price", nullable = false)
    private double entryPrice;

    @Column(name = "stop_price", nullable = false)
    private double stopPrice;

    @Column(name = "target_price")
    private Double targetPrice;

    @Column(name = "stop_distance", nullable = false)
    private double stopDistance;

    @Column(name = "stop_distance_pct", nullable = false)
    private double stopDistancePct;

    @Enumerated(EnumType.STRING)
    @Column(name = "stop_method", nullable = false, length = 32)
    private StopMethod stopMethod;

    // Risk metrics
    @Column(name = "position_risk_amount", nullable = false)
    private double positionRiskAmount;

    @Column(name = "position_risk_percent", nullable = false)
    private double positionRiskPercent;

    @Column(name = "estimated_downside", nullable = false)
    private double estimatedDownside;

    @Column(name = "portfolio_value", nullable = false)
    private double portfolioValue;

    @Column(name = "remaining_risk_budget", nullable = false)
    private double remainingRiskBudget;

    @Column(name = "portfolio_volatility")
    private Double portfolioVolatility;

    @Column(name = "security_volatility", nullable = false)
    private double securityVolatility;

    @Column(name = "expected_volatility")
    private Double expectedVolatility;

    @Column(name = "max_correlation")
    private Double maxCorrelation;

    @Column(name = "sector_exposure_after_trade")
    private Double sectorExposureAfterTrade;

    @Column(name = "current_drawdown", nullable = false)
    private double currentDrawdown;

    @Column(name = "risk_reward_ratio")
    private Double riskRewardRatio;

    // Decision
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_decision", nullable = false, length = 40)
    private RiskDecision riskDecision;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    // Trace and JSONB payloads
    @Column(name = "risk_trace", nullable = false, columnDefinition = "jsonb")
    private String riskTrace;

    @Column(name = "limiting_constraints", columnDefinition = "jsonb")
    private String limitingConstraints;

    @Column(name = "risk_warnings", columnDefinition = "jsonb")
    private String riskWarnings;

    @Column(name = "reasoning", nullable = false, columnDefinition = "text")
    private String reasoning;

    @Column(name = "data_quality_status", nullable = false, length = 30)
    private String dataQualityStatus;

    // Provenance
    @Column(name = "risk_engine_version", nullable = false, length = 32)
    private String riskEngineVersion = "RISK_v1.0.0";

    @Column(name = "risk_profile_version", nullable = false, length = 32)
    private String riskProfileVersion = "RP_v1.0.0";

    @Column(name = "signal_version", length = 32)
    private String signalVersion;

    @Column(name = "data_version", nullable = false, length = 32)
    private String dataVersion = "1";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RiskAdjustmentEntity> adjustments = new ArrayList<>();

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RiskWarningEntity> warnings = new ArrayList<>();

    public RiskAssessmentEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (timestamp == null) timestamp = Instant.now();
        if (calculatedAt == null) calculatedAt = Instant.now();
        if (informationAvailableAt == null) informationAvailableAt = Instant.now();
    }

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

    public String getRiskTrace() { return riskTrace; }
    public void setRiskTrace(String riskTrace) { this.riskTrace = riskTrace; }

    public String getLimitingConstraints() { return limitingConstraints; }
    public void setLimitingConstraints(String limitingConstraints) { this.limitingConstraints = limitingConstraints; }

    public String getRiskWarnings() { return riskWarnings; }
    public void setRiskWarnings(String riskWarnings) { this.riskWarnings = riskWarnings; }

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

    public List<RiskAdjustmentEntity> getAdjustments() { return adjustments; }
    public void setAdjustments(List<RiskAdjustmentEntity> adjustments) { this.adjustments = adjustments; }

    public List<RiskWarningEntity> getWarnings() { return warnings; }
    public void setWarnings(List<RiskWarningEntity> warnings) { this.warnings = warnings; }
}
