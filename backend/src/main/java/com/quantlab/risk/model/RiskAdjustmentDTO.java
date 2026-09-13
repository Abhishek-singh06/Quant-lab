package com.quantlab.risk.model;

import java.time.Instant;
import java.util.UUID;

public class RiskAdjustmentDTO {
    private UUID id;
    private UUID assessmentId;
    private String adjustmentType;
    private double multiplier;
    private double baseAllocation;
    private double adjustedAllocation;
    private String reason;
    private Instant createdAt;

    public RiskAdjustmentDTO() {}

    public RiskAdjustmentDTO(String adjustmentType, double multiplier, double baseAllocation, double adjustedAllocation, String reason) {
        this.adjustmentType = adjustmentType;
        this.multiplier = multiplier;
        this.baseAllocation = baseAllocation;
        this.adjustedAllocation = adjustedAllocation;
        this.reason = reason;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }

    public String getAdjustmentType() { return adjustmentType; }
    public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }

    public double getMultiplier() { return multiplier; }
    public void setMultiplier(double multiplier) { this.multiplier = multiplier; }

    public double getBaseAllocation() { return baseAllocation; }
    public void setBaseAllocation(double baseAllocation) { this.baseAllocation = baseAllocation; }

    public double getAdjustedAllocation() { return adjustedAllocation; }
    public void setAdjustedAllocation(double adjustedAllocation) { this.adjustedAllocation = adjustedAllocation; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
