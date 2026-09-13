package com.quantlab.regime.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class RegimeComponentScoreDTO {

    private RegimeComponentType componentType;
    private String componentName;
    private BigDecimal rawValue;
    private BigDecimal normalizedValue;
    private BigDecimal componentScore; // -100 to +100
    private BigDecimal configuredWeight;
    private BigDecimal effectiveWeight;
    private String confidence;
    private String source;
    private Instant informationAvailableAt;
    private Map<String, Object> details;

    public RegimeComponentScoreDTO() {}

    public RegimeComponentType getComponentType() { return componentType; }
    public void setComponentType(RegimeComponentType componentType) { this.componentType = componentType; }
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
    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
