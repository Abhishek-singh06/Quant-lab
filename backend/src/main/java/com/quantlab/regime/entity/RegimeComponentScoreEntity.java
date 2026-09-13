package com.quantlab.regime.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "regime_component_scores", schema = "market_data")
public class RegimeComponentScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regime_id", nullable = false)
    private MarketRegimeEntity marketRegime;

    @Column(name = "component_name", nullable = false, length = 64)
    private String componentName;

    @Column(name = "raw_value", precision = 18, scale = 4)
    private BigDecimal rawValue;

    @Column(name = "normalized_value", precision = 10, scale = 4)
    private BigDecimal normalizedValue;

    @Column(name = "component_score", nullable = false, precision = 10, scale = 4)
    private BigDecimal componentScore;

    @Column(name = "configured_weight", nullable = false, precision = 6, scale = 4)
    private BigDecimal configuredWeight;

    @Column(name = "effective_weight", nullable = false, precision = 6, scale = 4)
    private BigDecimal effectiveWeight;

    @Column(name = "confidence", nullable = false, length = 16)
    private String confidence = "HIGH";

    @Column(name = "source", nullable = false, length = 64)
    private String source;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    public RegimeComponentScoreEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public MarketRegimeEntity getMarketRegime() { return marketRegime; }
    public void setMarketRegime(MarketRegimeEntity marketRegime) { this.marketRegime = marketRegime; }
    public String getComponentName() { return componentName; }
    public void setComponentName(String componentName) { this.componentName = componentName; }
    public BigDecimal getRawValue() { return rawValue; }
    public void setRawValue(BigDecimal rawValue) { this.rawValue = rawValue; }
    public BigDecimal getNormalizedValue() { return normalizedValue; }
    public void setNormalizedValue(BigDecimal normalizedValue) { this.normalizedValue = normalizedValue; }
    public BigDecimal getComponentScore() { return componentScore; }
    public void setComponentScore(BigDecimal componentScore) { this.componentScore = componentScore; }
    public BigDecimal getConfiguredWeight() { return configuredWeight; }
    public void setConfiguredWeight(BigDecimal configuredWeight) { this.configuredWeight = configuredWeight; }
    public BigDecimal getEffectiveWeight() { return effectiveWeight; }
    public void setEffectiveWeight(BigDecimal effectiveWeight) { this.effectiveWeight = effectiveWeight; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
