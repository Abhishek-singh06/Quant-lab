package com.quantlab.global.entity;

import com.quantlab.global.model.RegimeConfidence;
import com.quantlab.global.model.RegimeLabel;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "global_market_regimes",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_gmr_timestamp", columnNames = {"timestamp"})
    },
    indexes = {
        @Index(name = "idx_gmr_timestamp", columnList = "timestamp"),
        @Index(name = "idx_gmr_regime_label", columnList = "regime_label")
    }
)
public class GlobalMarketRegime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant timestamp; // Regime evaluation timestamp in UTC

    @Enumerated(EnumType.STRING)
    @Column(name = "regime_label", nullable = false, length = 32)
    private RegimeLabel regimeLabel;

    @Column(name = "composite_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal compositeScore; // Normalized -100 to +100

    @Column(name = "equity_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal equityScore;

    @Column(name = "volatility_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal volatilityScore;

    @Column(name = "rates_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal ratesScore;

    @Column(name = "dollar_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal dollarScore;

    @Column(name = "commodity_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal commodityScore;

    @Column(name = "asia_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal asiaScore;

    @Column(name = "europe_score", nullable = false, precision = 8, scale = 4)
    private BigDecimal europeScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RegimeConfidence confidence = RegimeConfidence.HIGH;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "methodology_version", nullable = false, length = 32)
    private String methodologyVersion = "1.0.0";

    @Column(name = "source_snapshot_count", nullable = false)
    private int sourceSnapshotCount;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    public GlobalMarketRegime() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public RegimeLabel getRegimeLabel() { return regimeLabel; }
    public void setRegimeLabel(RegimeLabel regimeLabel) { this.regimeLabel = regimeLabel; }
    public BigDecimal getCompositeScore() { return compositeScore; }
    public void setCompositeScore(BigDecimal compositeScore) { this.compositeScore = compositeScore; }
    public BigDecimal getEquityScore() { return equityScore; }
    public void setEquityScore(BigDecimal equityScore) { this.equityScore = equityScore; }
    public BigDecimal getVolatilityScore() { return volatilityScore; }
    public void setVolatilityScore(BigDecimal volatilityScore) { this.volatilityScore = volatilityScore; }
    public BigDecimal getRatesScore() { return ratesScore; }
    public void setRatesScore(BigDecimal ratesScore) { this.ratesScore = ratesScore; }
    public BigDecimal getDollarScore() { return dollarScore; }
    public void setDollarScore(BigDecimal dollarScore) { this.dollarScore = dollarScore; }
    public BigDecimal getCommodityScore() { return commodityScore; }
    public void setCommodityScore(BigDecimal commodityScore) { this.commodityScore = commodityScore; }
    public BigDecimal getAsiaScore() { return asiaScore; }
    public void setAsiaScore(BigDecimal asiaScore) { this.asiaScore = asiaScore; }
    public BigDecimal getEuropeScore() { return europeScore; }
    public void setEuropeScore(BigDecimal europeScore) { this.europeScore = europeScore; }
    public RegimeConfidence getConfidence() { return confidence; }
    public void setConfidence(RegimeConfidence confidence) { this.confidence = confidence; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getMethodologyVersion() { return methodologyVersion; }
    public void setMethodologyVersion(String methodologyVersion) { this.methodologyVersion = methodologyVersion; }
    public int getSourceSnapshotCount() { return sourceSnapshotCount; }
    public void setSourceSnapshotCount(int sourceSnapshotCount) { this.sourceSnapshotCount = sourceSnapshotCount; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
