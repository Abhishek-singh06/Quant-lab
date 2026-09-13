package com.quantlab.warehouse.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "instrument_history",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_inst_hist_inst_id", columnList = "instrument_id"),
        @Index(name = "idx_inst_hist_symbol", columnList = "symbol"),
        @Index(name = "idx_inst_hist_dates", columnList = "valid_from, valid_to")
    }
)
public class InstrumentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo; // null indicates currently active

    @Column(name = "change_reason", length = 255)
    private String changeReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public InstrumentHistory() {}

    public InstrumentHistory(Long instrumentId, String symbol, String companyName,
                             LocalDate validFrom, LocalDate validTo, String changeReason) {
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.companyName = companyName;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.changeReason = changeReason;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
