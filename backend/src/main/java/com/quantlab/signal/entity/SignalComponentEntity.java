package com.quantlab.signal.entity;

import com.quantlab.signal.model.EvidenceCategory;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signal_components", schema = "market_data")
public class SignalComponentEntity {

    @Id
    private UUID id;

    @Column(name = "signal_id", nullable = false)
    private UUID signalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private EvidenceCategory category;

    @Column(name = "category_score", nullable = false)
    private Double categoryScore;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "weighted_contribution", nullable = false)
    private Double weightedContribution;

    @Column(name = "direction", nullable = false, length = 20)
    private String direction;

    @Column(name = "strength", nullable = false)
    private Double strength;

    @Column(name = "quality", nullable = false)
    private Double quality;

    @Column(name = "freshness", nullable = false)
    private Double freshness;

    @Column(name = "is_present", nullable = false)
    private Boolean isPresent = true;

    @Column(name = "missing_reason", columnDefinition = "TEXT")
    private String missingReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SignalComponentEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }

    public EvidenceCategory getCategory() { return category; }
    public void setCategory(EvidenceCategory category) { this.category = category; }

    public Double getCategoryScore() { return categoryScore; }
    public void setCategoryScore(Double categoryScore) { this.categoryScore = categoryScore; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getWeightedContribution() { return weightedContribution; }
    public void setWeightedContribution(Double weightedContribution) { this.weightedContribution = weightedContribution; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public Double getStrength() { return strength; }
    public void setStrength(Double strength) { this.strength = strength; }

    public Double getQuality() { return quality; }
    public void setQuality(Double quality) { this.quality = quality; }

    public Double getFreshness() { return freshness; }
    public void setFreshness(Double freshness) { this.freshness = freshness; }

    public Boolean getIsPresent() { return isPresent; }
    public void setIsPresent(Boolean isPresent) { this.isPresent = isPresent; }

    public String getMissingReason() { return missingReason; }
    public void setMissingReason(String missingReason) { this.missingReason = missingReason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
