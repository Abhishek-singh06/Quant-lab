package com.quantlab.risk.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_adjustments")
public class RiskAdjustmentEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private RiskAssessmentEntity assessment;

    @Column(name = "adjustment_type", nullable = false, length = 50)
    private String adjustmentType;

    @Column(name = "multiplier", nullable = false)
    private double multiplier;

    @Column(name = "base_allocation", nullable = false)
    private double baseAllocation;

    @Column(name = "adjusted_allocation", nullable = false)
    private double adjustedAllocation;

    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RiskAdjustmentEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RiskAssessmentEntity getAssessment() { return assessment; }
    public void setAssessment(RiskAssessmentEntity assessment) { this.assessment = assessment; }

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
