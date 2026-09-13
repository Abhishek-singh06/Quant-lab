package com.quantlab.features.entity;

import com.quantlab.features.model.FeatureCategory;
import com.quantlab.features.model.PriceSeriesType;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "feature_definitions", schema = "market_data")
public class FeatureDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "feature_name", nullable = false, unique = true, length = 64)
    private String featureName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 32)
    private FeatureCategory category;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "default_lookback", nullable = false)
    private int defaultLookback;

    @Column(name = "timeframe", nullable = false, length = 16)
    private String timeframe = "1D";

    @Column(name = "feature_version", nullable = false, length = 16)
    private String featureVersion = "1.0.0";

    @Column(name = "formula_version", nullable = false, length = 16)
    private String formulaVersion = "1.0.0";

    @Enumerated(EnumType.STRING)
    @Column(name = "price_series_type", nullable = false, length = 32)
    private PriceSeriesType priceSeriesType = PriceSeriesType.SPLIT_ADJUSTED;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public FeatureDefinition() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    public FeatureCategory getCategory() { return category; }
    public void setCategory(FeatureCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDefaultLookback() { return defaultLookback; }
    public void setDefaultLookback(int defaultLookback) { this.defaultLookback = defaultLookback; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }
    public String getFormulaVersion() { return formulaVersion; }
    public void setFormulaVersion(String formulaVersion) { this.formulaVersion = formulaVersion; }
    public PriceSeriesType getPriceSeriesType() { return priceSeriesType; }
    public void setPriceSeriesType(PriceSeriesType priceSeriesType) { this.priceSeriesType = priceSeriesType; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
