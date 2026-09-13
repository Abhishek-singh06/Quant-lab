package com.quantlab.institutional.entity;

import com.quantlab.institutional.model.FlowFrequency;
import com.quantlab.institutional.model.InstitutionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "institutional_flows",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_flow_date_type_freq", columnNames = {"trade_date", "institution_type", "flow_frequency"})
    },
    indexes = {
        @Index(name = "idx_if_trade_date", columnList = "trade_date"),
        @Index(name = "idx_if_inst_type", columnList = "institution_type"),
        @Index(name = "idx_if_available_at", columnList = "available_at"),
        @Index(name = "idx_if_data_as_of", columnList = "data_as_of")
    }
)
public class InstitutionalFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(nullable = false, length = 32)
    private String market = "NSE";

    @Enumerated(EnumType.STRING)
    @Column(name = "institution_type", nullable = false, length = 32)
    private InstitutionType institutionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "flow_frequency", nullable = false, length = 32)
    private FlowFrequency flowFrequency = FlowFrequency.DAILY;

    @Column(name = "buy_value", nullable = false, precision = 20, scale = 4)
    private BigDecimal buyValue;

    @Column(name = "sell_value", nullable = false, precision = 20, scale = 4)
    private BigDecimal sellValue;

    @Column(name = "net_value", nullable = false, precision = 20, scale = 4)
    private BigDecimal netValue;

    @Column(name = "data_as_of", nullable = false)
    private LocalDate dataAsOf;

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "ingestion_timestamp", nullable = false)
    private Instant ingestionTimestamp = Instant.now();

    public InstitutionalFlow() {}

    public InstitutionalFlow(LocalDate tradeDate, String market, InstitutionType institutionType,
                             FlowFrequency flowFrequency, BigDecimal buyValue, BigDecimal sellValue,
                             BigDecimal netValue, LocalDate dataAsOf, LocalDate publishedAt,
                             Instant availableAt, String source) {
        this.tradeDate = tradeDate;
        this.market = market;
        this.institutionType = institutionType;
        this.flowFrequency = flowFrequency;
        this.buyValue = buyValue;
        this.sellValue = sellValue;
        this.netValue = netValue;
        this.dataAsOf = dataAsOf;
        this.publishedAt = publishedAt;
        this.availableAt = availableAt;
        this.source = source;
        this.ingestionTimestamp = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    public String getMarket() { return market; }
    public void setMarket(String market) { this.market = market; }
    public InstitutionType getInstitutionType() { return institutionType; }
    public void setInstitutionType(InstitutionType institutionType) { this.institutionType = institutionType; }
    public FlowFrequency getFlowFrequency() { return flowFrequency; }
    public void setFlowFrequency(FlowFrequency flowFrequency) { this.flowFrequency = flowFrequency; }
    public BigDecimal getBuyValue() { return buyValue; }
    public void setBuyValue(BigDecimal buyValue) { this.buyValue = buyValue; }
    public BigDecimal getSellValue() { return sellValue; }
    public void setSellValue(BigDecimal sellValue) { this.sellValue = sellValue; }
    public BigDecimal getNetValue() { return netValue; }
    public void setNetValue(BigDecimal netValue) { this.netValue = netValue; }
    public LocalDate getDataAsOf() { return dataAsOf; }
    public void setDataAsOf(LocalDate dataAsOf) { this.dataAsOf = dataAsOf; }
    public LocalDate getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDate publishedAt) { this.publishedAt = publishedAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
}
