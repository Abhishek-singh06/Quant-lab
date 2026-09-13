package com.quantlab.institutional.entity;

import com.quantlab.institutional.model.InstitutionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "institutional_ownership",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inst_ownership_inst_date_type", columnNames = {"instrument_id", "period_end", "institution_type"})
    },
    indexes = {
        @Index(name = "idx_io_instrument_id", columnList = "instrument_id"),
        @Index(name = "idx_io_symbol", columnList = "symbol"),
        @Index(name = "idx_io_period_end", columnList = "period_end"),
        @Index(name = "idx_io_available_at", columnList = "available_at"),
        @Index(name = "idx_io_data_as_of", columnList = "data_as_of")
    }
)
public class InstitutionalOwnership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "data_as_of", nullable = false)
    private LocalDate dataAsOf;

    @Column(name = "published_at", nullable = false)
    private LocalDate publishedAt;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "institution_type", nullable = false, length = 32)
    private InstitutionType institutionType;

    @Column(length = 64)
    private String category;

    @Column(name = "ownership_percentage", nullable = false, precision = 8, scale = 4)
    private BigDecimal ownershipPercentage;

    @Column(name = "share_quantity")
    private Long shareQuantity;

    @Column(name = "market_value", precision = 20, scale = 4)
    private BigDecimal marketValue;

    @Column(name = "change_in_ownership_pp", precision = 8, scale = 4)
    private BigDecimal changeInOwnershipPp;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "ingestion_timestamp", nullable = false)
    private Instant ingestionTimestamp = Instant.now();

    public InstitutionalOwnership() {}

    public InstitutionalOwnership(Long instrumentId, String symbol, LocalDate periodEnd,
                                  LocalDate dataAsOf, LocalDate publishedAt, Instant availableAt,
                                  InstitutionType institutionType, String category,
                                  BigDecimal ownershipPercentage, Long shareQuantity,
                                  BigDecimal marketValue, BigDecimal changeInOwnershipPp,
                                  String source) {
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.periodEnd = periodEnd;
        this.dataAsOf = dataAsOf;
        this.publishedAt = publishedAt;
        this.availableAt = availableAt;
        this.institutionType = institutionType;
        this.category = category;
        this.ownershipPercentage = ownershipPercentage;
        this.shareQuantity = shareQuantity;
        this.marketValue = marketValue;
        this.changeInOwnershipPp = changeInOwnershipPp;
        this.source = source;
        this.ingestionTimestamp = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public LocalDate getDataAsOf() { return dataAsOf; }
    public void setDataAsOf(LocalDate dataAsOf) { this.dataAsOf = dataAsOf; }
    public LocalDate getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDate publishedAt) { this.publishedAt = publishedAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public InstitutionType getInstitutionType() { return institutionType; }
    public void setInstitutionType(InstitutionType institutionType) { this.institutionType = institutionType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getOwnershipPercentage() { return ownershipPercentage; }
    public void setOwnershipPercentage(BigDecimal ownershipPercentage) { this.ownershipPercentage = ownershipPercentage; }
    public Long getShareQuantity() { return shareQuantity; }
    public void setShareQuantity(Long shareQuantity) { this.shareQuantity = shareQuantity; }
    public BigDecimal getMarketValue() { return marketValue; }
    public void setMarketValue(BigDecimal marketValue) { this.marketValue = marketValue; }
    public BigDecimal getChangeInOwnershipPp() { return changeInOwnershipPp; }
    public void setChangeInOwnershipPp(BigDecimal changeInOwnershipPp) { this.changeInOwnershipPp = changeInOwnershipPp; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
}
