package com.quantlab.global.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class GlobalMarketRegimeDTO {

    private Instant timestamp;
    private RegimeLabel regimeLabel;
    private BigDecimal compositeScore;
    private BigDecimal equityScore;
    private BigDecimal volatilityScore;
    private BigDecimal ratesScore;
    private BigDecimal dollarScore;
    private BigDecimal commodityScore;
    private BigDecimal asiaScore;
    private BigDecimal europeScore;
    private RegimeConfidence confidence;
    private String explanation;
    private String methodologyVersion;
    private int sourceSnapshotCount;
    private Instant calculatedAt;
    private Map<String, BigDecimal> componentBreakdown;

    public GlobalMarketRegimeDTO() {}

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
    public Map<String, BigDecimal> getComponentBreakdown() { return componentBreakdown; }
    public void setComponentBreakdown(Map<String, BigDecimal> componentBreakdown) { this.componentBreakdown = componentBreakdown; }
}
