package com.quantlab.warehouse.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "index_constituents",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_idx_const_idx_id", columnList = "index_id"),
        @Index(name = "idx_idx_const_inst_id", columnList = "instrument_id"),
        @Index(name = "idx_idx_const_dates", columnList = "effective_from, effective_to")
    }
)
public class IndexConstituent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "index_id", nullable = false)
    private Long indexId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(precision = 8, scale = 4)
    private BigDecimal weight;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo; // null means currently an active member

    @Column(length = 64)
    private String source;

    @Column(name = "ingestion_timestamp", nullable = false)
    private Instant ingestionTimestamp = Instant.now();

    public IndexConstituent() {}

    public IndexConstituent(Long indexId, Long instrumentId, String symbol, BigDecimal weight,
                            LocalDate effectiveFrom, LocalDate effectiveTo, String source) {
        this.indexId = indexId;
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.weight = weight;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.source = source;
        this.ingestionTimestamp = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIndexId() { return indexId; }
    public void setIndexId(Long indexId) { this.indexId = indexId; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
}
