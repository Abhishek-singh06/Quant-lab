package com.quantlab.regime.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "regime_model_versions", schema = "market_data")
public class RegimeModelVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_version", nullable = false, unique = true, length = 32)
    private String modelVersion;

    @Column(name = "model_type", nullable = false, length = 32)
    private String modelType = "WEIGHTED_COMPOSITE_PROBABILISTIC";

    @Column(name = "feature_version", nullable = false, length = 16)
    private String featureVersion = "1.0.0";

    @Column(name = "normalization_version", nullable = false, length = 16)
    private String normalizationVersion = "1.0.0";

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "weights_json", nullable = false, columnDefinition = "JSONB")
    private String weightsJson;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public RegimeModelVersionEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }
    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }
    public String getNormalizationVersion() { return normalizationVersion; }
    public void setNormalizationVersion(String normalizationVersion) { this.normalizationVersion = normalizationVersion; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getWeightsJson() { return weightsJson; }
    public void setWeightsJson(String weightsJson) { this.weightsJson = weightsJson; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
