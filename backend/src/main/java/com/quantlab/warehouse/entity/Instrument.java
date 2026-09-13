package com.quantlab.warehouse.entity;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.model.InstrumentStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "instruments",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_inst_current_symbol", columnList = "current_symbol"),
        @Index(name = "idx_inst_isin", columnList = "isin"),
        @Index(name = "idx_inst_exchange", columnList = "exchange"),
        @Index(name = "idx_inst_status", columnList = "status")
    }
)
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "isin", length = 16, unique = true)
    private String isin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Exchange exchange = Exchange.NSE;

    @Column(name = "current_symbol", nullable = false, length = 32)
    private String currentSymbol;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "listing_date")
    private LocalDate listingDate;

    @Column(name = "delisting_date")
    private LocalDate delistingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InstrumentStatus status = InstrumentStatus.ACTIVE;

    @Column(length = 64)
    private String sector;

    @Column(length = 64)
    private String industry;

    @Column(name = "is_index", nullable = false)
    private boolean isIndex = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    public Instrument() {}

    public Instrument(String isin, Exchange exchange, String currentSymbol, String companyName,
                      LocalDate listingDate, InstrumentStatus status, String sector, String industry, boolean isIndex) {
        this.isin = isin;
        this.exchange = exchange;
        this.currentSymbol = currentSymbol;
        this.companyName = companyName;
        this.listingDate = listingDate;
        this.status = status;
        this.sector = sector;
        this.industry = industry;
        this.isIndex = isIndex;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public Exchange getExchange() { return exchange; }
    public void setExchange(Exchange exchange) { this.exchange = exchange; }
    public String getCurrentSymbol() { return currentSymbol; }
    public void setCurrentSymbol(String currentSymbol) { this.currentSymbol = currentSymbol; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public LocalDate getListingDate() { return listingDate; }
    public void setListingDate(LocalDate listingDate) { this.listingDate = listingDate; }
    public LocalDate getDelistingDate() { return delistingDate; }
    public void setDelistingDate(LocalDate delistingDate) { this.delistingDate = delistingDate; }
    public InstrumentStatus getStatus() { return status; }
    public void setStatus(InstrumentStatus status) { this.status = status; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public boolean isIndex() { return isIndex; }
    public void setIndex(boolean index) { isIndex = index; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
