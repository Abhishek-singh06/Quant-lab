package com.quantlab.regime.entity;

import com.quantlab.regime.model.DirectionRegime;
import com.quantlab.regime.model.RiskRegime;
import com.quantlab.regime.model.VolatilityRegime;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "market_regimes", schema = "market_data",
       uniqueConstraints = @UniqueConstraint(columnNames = {"symbol", "regime_timestamp", "model_version"}))
public class MarketRegimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol = "NIFTY 50";

    @Column(name = "regime_timestamp", nullable = false)
    private Instant regimeTimestamp;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction_regime", nullable = false, length = 32)
    private DirectionRegime directionRegime;

    @Enumerated(EnumType.STRING)
    @Column(name = "volatility_regime", nullable = false, length = 32)
    private VolatilityRegime volatilityRegime;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_regime", nullable = false, length = 32)
    private RiskRegime riskRegime;

    @Column(name = "direction_score", nullable = false, precision = 10, scale = 4)
    private BigDecimal directionScore;

    @Column(name = "volatility_score", nullable = false, precision = 10, scale = 4)
    private BigDecimal volatilityScore;

    @Column(name = "risk_score", nullable = false, precision = 10, scale = 4)
    private BigDecimal riskScore;

    @Column(name = "confidence", nullable = false, precision = 6, scale = 4)
    private BigDecimal confidence;

    @Column(name = "prob_bull", nullable = false, precision = 6, scale = 4)
    private BigDecimal probBull;

    @Column(name = "prob_bear", nullable = false, precision = 6, scale = 4)
    private BigDecimal probBear;

    @Column(name = "prob_sideways", nullable = false, precision = 6, scale = 4)
    private BigDecimal probSideways;

    @Column(name = "prob_risk_on", nullable = false, precision = 6, scale = 4)
    private BigDecimal probRiskOn;

    @Column(name = "prob_risk_off", nullable = false, precision = 6, scale = 4)
    private BigDecimal probRiskOff;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_direction_regime", length = 32)
    private DirectionRegime previousDirectionRegime;

    @Column(name = "days_in_regime", nullable = false)
    private int daysInRegime = 1;

    @Column(name = "is_transition", nullable = false)
    private boolean isTransition = false;

    @Column(name = "explanation", nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion = "REGIME_v1.0.0";

    @Column(name = "feature_version", nullable = false, length = 16)
    private String featureVersion = "1.0.0";

    @Column(name = "data_version", nullable = false)
    private int dataVersion = 1;

    @Column(name = "source_data_timestamp", nullable = false)
    private Instant sourceDataTimestamp;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    @OneToMany(mappedBy = "marketRegime", fetch = FetchType.LAZY)
    private List<RegimeComponentScoreEntity> componentScores = new ArrayList<>();

    public MarketRegimeEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public int getDataVersion() { return dataVersion; }
    public void setDataVersion(int dataVersion) { this.dataVersion = dataVersion; }
    public Instant getSourceDataTimestamp() { return sourceDataTimestamp; }
    public void setSourceDataTimestamp(Instant sourceDataTimestamp) { this.sourceDataTimestamp = sourceDataTimestamp; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
    public List<RegimeComponentScoreEntity> getComponentScores() { return componentScores; }
    public void setComponentScores(List<RegimeComponentScoreEntity> componentScores) { this.componentScores = componentScores; }
}
