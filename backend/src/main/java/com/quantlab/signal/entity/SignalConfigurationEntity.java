package com.quantlab.signal.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signal_configurations", schema = "market_data")
public class SignalConfigurationEntity {

    @Id
    private UUID id;

    @Column(name = "version", nullable = false, unique = true, length = 50)
    private String version;

    @Column(name = "min_supporting_categories", nullable = false)
    private Integer minSupportingCategories = 3;

    @Column(name = "buy_threshold", nullable = false)
    private Double buyThreshold = 35.0;

    @Column(name = "sell_threshold", nullable = false)
    private Double sellThreshold = -35.0;

    @Column(name = "min_confidence", nullable = false)
    private Double minConfidence = 0.50;

    @Column(name = "weights", nullable = false, columnDefinition = "JSONB")
    private String weights;

    @Column(name = "category_caps", nullable = false, columnDefinition = "JSONB")
    private String categoryCaps;

    @Column(name = "correlation_groups", nullable = false, columnDefinition = "JSONB")
    private String correlationGroups;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SignalConfigurationEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Integer getMinSupportingCategories() { return minSupportingCategories; }
    public void setMinSupportingCategories(Integer minSupportingCategories) { this.minSupportingCategories = minSupportingCategories; }

    public Double getBuyThreshold() { return buyThreshold; }
    public void setBuyThreshold(Double buyThreshold) { this.buyThreshold = buyThreshold; }

    public Double getSellThreshold() { return sellThreshold; }
    public void setSellThreshold(Double sellThreshold) { this.sellThreshold = sellThreshold; }

    public Double getMinConfidence() { return minConfidence; }
    public void setMinConfidence(Double minConfidence) { this.minConfidence = minConfidence; }

    public String getWeights() { return weights; }
    public void setWeights(String weights) { this.weights = weights; }

    public String getCategoryCaps() { return categoryCaps; }
    public void setCategoryCaps(String categoryCaps) { this.categoryCaps = categoryCaps; }

    public String getCorrelationGroups() { return correlationGroups; }
    public void setCorrelationGroups(String correlationGroups) { this.correlationGroups = correlationGroups; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
