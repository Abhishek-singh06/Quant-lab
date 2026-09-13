package com.quantlab.regime.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class MarketRegimeDTO {

    private String symbol;
    private Instant regimeTimestamp;
    private LocalDate tradingDate;

    private DirectionRegime directionRegime;
    private VolatilityRegime volatilityRegime;
    private RiskRegime riskRegime;

    private BigDecimal directionScore;
    private BigDecimal volatilityScore;
    private BigDecimal riskScore;

    private BigDecimal confidence;

    private BigDecimal probBull;
    private BigDecimal probBear;
    private BigDecimal probSideways;

    private BigDecimal probRiskOn;
    private BigDecimal probRiskOff;

    private DirectionRegime previousDirectionRegime;
    private int daysInRegime;
    private boolean isTransition;

    private String explanation;
    private String modelVersion;
    private String featureVersion;
    private Instant sourceDataTimestamp;
    private Instant informationAvailableAt;
    private Instant calculatedAt;

    private List<RegimeComponentScoreDTO> componentScores;
    private Map<String, Object> drivers;
    private Map<String, Object> risks;

    public MarketRegimeDTO() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Instant getRegimeTimestamp() { return regimeTimestamp; }
    public void setRegimeTimestamp(Instant regimeTimestamp) { this.regimeTimestamp = regimeTimestamp; }
    public LocalDate getTradingDate() { return tradingDate; }
    public void setTradingDate(LocalDate tradingDate) { this.tradingDate = tradingDate; }
    public DirectionRegime getDirectionRegime() { return directionRegime; }
    public void setDirectionRegime(DirectionRegime directionRegime) { this.directionRegime = directionRegime; }
    public VolatilityRegime getVolatilityRegime() { return volatilityRegime; }
    public void setVolatilityRegime(VolatilityRegime volatilityRegime) { this.volatilityRegime = volatilityRegime; }
    public RiskRegime getRiskRegime() { return riskRegime; }
    public void setRiskRegime(RiskRegime riskRegime) { this.riskRegime = riskRegime; }
    public BigDecimal getDirectionScore() { return directionScore; }
    public void setDirectionScore(BigDecimal directionScore) { this.directionScore = directionScore; }
    public BigDecimal getVolatilityScore() { return volatilityScore; }
    public void setVolatilityScore(BigDecimal volatilityScore) { this.volatilityScore = volatilityScore; }
    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public BigDecimal getProbBull() { return probBull; }
    public void setProbBull(BigDecimal probBull) { this.probBull = probBull; }
    public BigDecimal getProbBear() { return probBear; }
    public void setProbBear(BigDecimal probBear) { this.probBear = probBear; }
    public BigDecimal getProbSideways() { return probSideways; }
    public void setProbSideways(BigDecimal probSideways) { this.probSideways = probSideways; }
    public BigDecimal getProbRiskOn() { return probRiskOn; }
    public void setProbRiskOn(BigDecimal probRiskOn) { this.probRiskOn = probRiskOn; }
    public BigDecimal getProbRiskOff() { return probRiskOff; }
    public void setProbRiskOff(BigDecimal probRiskOff) { this.probRiskOff = probRiskOff; }
    public DirectionRegime getPreviousDirectionRegime() { return previousDirectionRegime; }
    public void setPreviousDirectionRegime(DirectionRegime previousDirectionRegime) { this.previousDirectionRegime = previousDirectionRegime; }
    public int getDaysInRegime() { return daysInRegime; }
    public void setDaysInRegime(int daysInRegime) { this.daysInRegime = daysInRegime; }
    public boolean isTransition() { return isTransition; }
    public void setTransition(boolean transition) { isTransition = transition; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }
    public Instant getSourceDataTimestamp() { return sourceDataTimestamp; }
    public void setSourceDataTimestamp(Instant sourceDataTimestamp) { this.sourceDataTimestamp = sourceDataTimestamp; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
    public List<RegimeComponentScoreDTO> getComponentScores() { return componentScores; }
    public void setComponentScores(List<RegimeComponentScoreDTO> componentScores) { this.componentScores = componentScores; }
    public Map<String, Object> getDrivers() { return drivers; }
    public void setDrivers(Map<String, Object> drivers) { this.drivers = drivers; }
    public Map<String, Object> getRisks() { return risks; }
    public void setRisks(Map<String, Object> risks) { this.risks = risks; }
}
