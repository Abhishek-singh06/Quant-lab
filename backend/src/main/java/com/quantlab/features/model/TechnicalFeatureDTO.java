package com.quantlab.features.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class TechnicalFeatureDTO {

    private Long id;
    private Long instrumentId;
    private String symbol;
    private String featureName;
    private BigDecimal featureValue;
    private Instant featureTimestamp;
    private LocalDate tradingDate;
    private String timeframe;
    private String frequency;
    private String featureVersion;
    private String calculationVersion;
    private Instant sourceDataTimestamp;
    private Instant informationAvailableAt;
    private Instant calculatedAt;
    private String source;
    private String ingestionRunId;

    public TechnicalFeatureDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getFeatureName() { return featureName; }
    public void setFeatureName(String featureName) { this.featureName = featureName; }
    public BigDecimal getFeatureValue() { return featureValue; }
    public void setFeatureValue(BigDecimal featureValue) { this.featureValue = featureValue; }
    public Instant getFeatureTimestamp() { return featureTimestamp; }
    public void setFeatureTimestamp(Instant featureTimestamp) { this.featureTimestamp = featureTimestamp; }
    public LocalDate getTradingDate() { return tradingDate; }
    public void setTradingDate(LocalDate tradingDate) { this.tradingDate = tradingDate; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }
    public String getCalculationVersion() { return calculationVersion; }
    public void setCalculationVersion(String calculationVersion) { this.calculationVersion = calculationVersion; }
    public Instant getSourceDataTimestamp() { return sourceDataTimestamp; }
    public void setSourceDataTimestamp(Instant sourceDataTimestamp) { this.sourceDataTimestamp = sourceDataTimestamp; }
    public Instant getInformationAvailableAt() { return informationAvailableAt; }
    public void setInformationAvailableAt(Instant informationAvailableAt) { this.informationAvailableAt = informationAvailableAt; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getIngestionRunId() { return ingestionRunId; }
    public void setIngestionRunId(String ingestionRunId) { this.ingestionRunId = ingestionRunId; }
}
