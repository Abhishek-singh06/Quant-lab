package com.quantlab.institutional.entity;

import com.quantlab.institutional.model.HoldingChangeType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "fund_holdings",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_holding_disc_inst", columnNames = {"disclosure_id", "instrument_id"})
    },
    indexes = {
        @Index(name = "idx_fh_scheme_id", columnList = "scheme_id"),
        @Index(name = "idx_fh_instrument_id", columnList = "instrument_id"),
        @Index(name = "idx_fh_symbol", columnList = "symbol"),
        @Index(name = "idx_fh_available_at", columnList = "available_at"),
        @Index(name = "idx_fh_data_as_of", columnList = "data_as_of")
    }
)
public class FundHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disclosure_id", nullable = false)
    private Long disclosureId;

    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "holding_date", nullable = false)
    private LocalDate holdingDate;

    @Column(name = "data_as_of", nullable = false)
    private LocalDate dataAsOf;

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "market_value", precision = 20, scale = 4)
    private BigDecimal marketValue;

    @Column(name = "portfolio_weight", nullable = false, precision = 8, scale = 4)
    private BigDecimal portfolioWeight;

    @Column(name = "weight_change_pp", precision = 8, scale = 4)
    private BigDecimal weightChangePp; // Percentage points change (e.g. 4.0% -> 5.0% = +1.0 pp)

    @Column(name = "relative_weight_change_pct", precision = 8, scale = 4)
    private BigDecimal relativeWeightChangePercent; // Relative % increase (e.g. +25.0%)

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 32)
    private HoldingChangeType changeType = HoldingChangeType.UNCHANGED;

    @Column(name = "asset_class", length = 32)
    private String assetClass = "EQUITY";

    @Column(length = 64)
    private String sector;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "ingestion_timestamp", nullable = false)
    private Instant ingestionTimestamp = Instant.now();

    public FundHolding() {}

    public FundHolding(Long disclosureId, Long schemeId, Long instrumentId, String symbol,
                       String companyName, LocalDate holdingDate, LocalDate dataAsOf,
                       LocalDate publishedAt, Instant availableAt, Long quantity,
                       BigDecimal marketValue, BigDecimal portfolioWeight, String assetClass,
                       String sector, String source) {
        this.disclosureId = disclosureId;
        this.schemeId = schemeId;
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.companyName = companyName;
        this.holdingDate = holdingDate;
        this.dataAsOf = dataAsOf;
        this.publishedAt = publishedAt;
        this.availableAt = availableAt;
        this.quantity = quantity;
        this.marketValue = marketValue;
        this.portfolioWeight = portfolioWeight;
        this.assetClass = assetClass;
        this.sector = sector;
        this.source = source;
        this.ingestionTimestamp = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDisclosureId() { return disclosureId; }
    public void setDisclosureId(Long disclosureId) { this.disclosureId = disclosureId; }
    public Long getSchemeId() { return schemeId; }
    public void setSchemeId(Long schemeId) { this.schemeId = schemeId; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public LocalDate getHoldingDate() { return holdingDate; }
    public void setHoldingDate(LocalDate holdingDate) { this.holdingDate = holdingDate; }
    public LocalDate getDataAsOf() { return dataAsOf; }
    public void setDataAsOf(LocalDate dataAsOf) { this.dataAsOf = dataAsOf; }
    public LocalDate getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDate publishedAt) { this.publishedAt = publishedAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
    public BigDecimal getPortfolioWeight() { return portfolioWeight; }
    public void setPortfolioWeight(BigDecimal portfolioWeight) { this.portfolioWeight = portfolioWeight; }
    public BigDecimal getWeightChangePp() { return weightChangePp; }
    public void setWeightChangePp(BigDecimal weightChangePp) { this.weightChangePp = weightChangePp; }
    public BigDecimal getRelativeWeightChangePercent() { return relativeWeightChangePercent; }
    public void setRelativeWeightChangePercent(BigDecimal relativeWeightChangePercent) { this.relativeWeightChangePercent = relativeWeightChangePercent; }
    public HoldingChangeType getChangeType() { return changeType; }
    public void setChangeType(HoldingChangeType changeType) { this.changeType = changeType; }
    public String getAssetClass() { return assetClass; }
    public void setAssetClass(String assetClass) { this.assetClass = assetClass; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
}
