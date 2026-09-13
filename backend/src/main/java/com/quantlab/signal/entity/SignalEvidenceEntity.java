package com.quantlab.signal.entity;

import com.quantlab.signal.model.EvidenceCategory;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signal_evidence", schema = "market_data")
public class SignalEvidenceEntity {

    @Id
    private UUID id;

    @Column(name = "signal_id", nullable = false)
    private UUID signalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private EvidenceCategory category;

    @Column(name = "feature_name", nullable = false, length = 100)
    private String featureName;

    @Column(name = "raw_value")
    private Double rawValue;

    @Column(name = "raw_value_str", columnDefinition = "TEXT")
    private String rawValueStr;

    @Column(name = "normalized_score", nullable = false)
    private Double normalizedScore;

    @Column(name = "direction", nullable = false, length = 20)
    private String direction;

    @Column(name = "strength", nullable = false)
    private Double strength;

    @Column(name = "quality", nullable = false)
    private Double quality;

    @Column(name = "freshness", nullable = false)
    private Double freshness;

    @Column(name = "confidence", nullable = false)
    private Double confidence;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "contribution", nullable = false)
    private Double contribution;

    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @Column(name = "source_timestamp", nullable = false)
    private Instant sourceTimestamp;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SignalEvidenceEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }

    public EvidenceCategory getCategory() { return category; }
    public void setCategory(EvidenceCategory category) { this.category = category; }

    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }

    public Double getRawValue() { return rawValue; }
    public void setRawValue(Double rawValue) { this.rawValue = rawValue; }

    public String getRawValueStr() { return rawValueStr; }
    public void setRawValueStr(String rawValueStr) { this.rawValueStr = rawValueStr; }

    public Double getNormalizedScore() { return normalizedScore; }
    public void setNormalizedScore(Double normalizedScore) { this.normalizedScore = normalizedScore; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public Double getStrength() { return strength; }
    public void setStrength(Double strength) { this.strength = strength; }

    public Double getQuality() { return quality; }
    public void setQuality(Double quality) { this.quality = quality; }

    public Double getFreshness() { return freshness; }
    public void setFreshness(Double freshness) { this.freshness = freshness; }

    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getContribution() { return contribution; }
    public void setContribution(Double contribution) { this.contribution = contribution; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getSourceTimestamp() { return sourceTimestamp; }
    public void setSourceTimestamp(Instant sourceTimestamp) { this.sourceTimestamp = sourceTimestamp; }

    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
