package com.quantlab.risk.model;

import java.util.List;
import java.util.Map;

public class RiskTraceDTO {
    private String symbol;
    private double entryPrice;
    private double stopPrice;
    private double stopDistance;
    private double stopDistancePct;
    private String stopMethod;
    private String sizingMethod;
    private double unconstrainedAllocation;
    private double constrainedAllocation;
    private double finalSuggestedAllocation;
    private double finalRecommendedQuantity;
    private Map<String, Double> constraintLimits;
    private String limitingConstraint;
    private List<RiskAdjustmentDTO> adjustments;
    private List<RiskWarningDTO> warnings;
    private Map<String, Object> debugInfo;

    public RiskTraceDTO() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getEntryPrice() { return entryPrice; }
    public void setEntryPrice(double entryPrice) { this.entryPrice = entryPrice; }

    public double getStopPrice() { return stopPrice; }
    public void setStopPrice(double stopPrice) { this.stopPrice = stopPrice; }

    public double getStopDistance() { return stopDistance; }
    public void setStopDistance(double stopDistance) { this.stopDistance = stopDistance; }

    public double getStopDistancePct() { return stopDistancePct; }
    public void setStopDistancePct(double stopDistancePct) { this.stopDistancePct = stopDistancePct; }

    public String getStopMethod() { return stopMethod; }
    public void setStopMethod(String stopMethod) { this.stopMethod = stopMethod; }

    public String getSizingMethod() { return sizingMethod; }
    public void setSizingMethod(String sizingMethod) { this.sizingMethod = sizingMethod; }

    public double getUnconstrainedAllocation() { return unconstrainedAllocation; }
    public void setUnconstrainedAllocation(double unconstrainedAllocation) { this.unconstrainedAllocation = unconstrainedAllocation; }

    public double getConstrainedAllocation() { return constrainedAllocation; }
    public void setConstrainedAllocation(double constrainedAllocation) { this.constrainedAllocation = constrainedAllocation; }

    public double getFinalSuggestedAllocation() { return finalSuggestedAllocation; }
    public void setFinalSuggestedAllocation(double finalSuggestedAllocation) { this.finalSuggestedAllocation = finalSuggestedAllocation; }

    public double getFinalRecommendedQuantity() { return finalRecommendedQuantity; }
    public void setFinalRecommendedQuantity(double finalRecommendedQuantity) { this.finalRecommendedQuantity = finalRecommendedQuantity; }

    public Map<String, Double> getConstraintLimits() { return constraintLimits; }
    public void setConstraintLimits(Map<String, Double> constraintLimits) { this.constraintLimits = constraintLimits; }

    public String getLimitingConstraint() { return limitingConstraint; }
    public void setLimitingConstraint(String limitingConstraint) { this.limitingConstraint = limitingConstraint; }

    public List<RiskAdjustmentDTO> getAdjustments() { return adjustments; }
    public void setAdjustments(List<RiskAdjustmentDTO> adjustments) { this.adjustments = adjustments; }

    public List<RiskWarningDTO> getWarnings() { return warnings; }
    public void setWarnings(List<RiskWarningDTO> warnings) { this.warnings = warnings; }

    public Map<String, Object> getDebugInfo() { return debugInfo; }
    public void setDebugInfo(Map<String, Object> debugInfo) { this.debugInfo = debugInfo; }
}
