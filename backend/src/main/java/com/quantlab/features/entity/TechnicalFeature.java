package com.quantlab.features.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "technical_features", schema = "market_data",
       uniqueConstraints = @UniqueConstraint(columnNames = {"instrument_id", "feature_name", "feature_timestamp", "timeframe", "feature_version"}))
public class TechnicalFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Column(name = "feature_name", nullable = false, length = 64)
    private String featureName;

    @Column(name = "feature_value", precision = 24, scale = 8)
    private BigDecimal featureValue;

    @Column(name = "feature_timestamp", nullable = false)
    private Instant featureTimestamp;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "timeframe", nullable = false, length = 16)
    private String timeframe = "1D";

    @Column(name = "frequency", nullable = false, length = 16)
    private String frequency = "DAILY";

    @Column(name = "feature_version", nullable = false, length = 64)
    private String featureVersion = "1.0.0";

    @Column(name = "calculation_version", nullable = false, length = 64)
    private String calculationVersion = "1.0.0";

    @Column(name = "data_version", nullable = false)
    private int dataVersion = 1;

    @Column(name = "source_data_timestamp", nullable = false)
    private Instant sourceDataTimestamp;

    @Column(name = "information_available_at", nullable = false)
    private Instant informationAvailableAt;

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    @Column(name = "source", nullable = false, length = 64)
    private String source = "TECHNICAL_FEATURE_ENGINE";

    @Column(name = "ingestion_run_id", length = 64)
    private String ingestionRunId;

    public TechnicalFeature() {}

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
    public int getDataVersion() { return dataVersion; }
    public void setDataVersion(int dataVersion) { this.dataVersion = dataVersion; }
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
