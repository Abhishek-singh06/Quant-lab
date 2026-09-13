package com.quantlab.global.entity;

import com.quantlab.global.model.DataFreshness;
import com.quantlab.global.model.SessionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "global_market_snapshots",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_gms_inst_timestamp_src", columnNames = {"instrument_id", "timestamp", "source"})
    },
    indexes = {
        @Index(name = "idx_gms_inst_id", columnList = "instrument_id"),
        @Index(name = "idx_gms_symbol", columnList = "canonical_symbol"),
        @Index(name = "idx_gms_timestamp", columnList = "timestamp"),
        @Index(name = "idx_gms_trading_date", columnList = "trading_date"),
        @Index(name = "idx_gms_source_time", columnList = "source_timestamp")
    }
)
public class GlobalMarketSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(name = "canonical_symbol", nullable = false, length = 32)
    private String canonicalSymbol;

    @Column(nullable = false)
    private Instant timestamp; // UTC observation or close timestamp

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(name = "open_val", precision = 20, scale = 6)
    private BigDecimal open;

    @Column(name = "high_val", precision = 20, scale = 6)
    private BigDecimal high;

    @Column(name = "low_val", precision = 20, scale = 6)
    private BigDecimal low;

    @Column(name = "close_val", nullable = false, precision = 20, scale = 6)
    private BigDecimal close;

    @Column(name = "previous_close", precision = 20, scale = 6)
    private BigDecimal previousClose;

    @Column(name = "price_change", precision = 20, scale = 6)
    private BigDecimal change;

    @Column(name = "change_percent", precision = 10, scale = 4)
    private BigDecimal changePercent;

    private Long volume;

    @Column(name = "yield_rate", precision = 10, scale = 4)
    private BigDecimal yieldRate;

    @Column(length = 16)
    private String currency;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "source_timestamp", nullable = false)
    private Instant sourceTimestamp;

    @Column(name = "ingestion_timestamp", nullable = false)
    private Instant ingestionTimestamp = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "data_freshness", nullable = false, length = 32)
    private DataFreshness dataFreshness = DataFreshness.DELAYED;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false, length = 32)
    private SessionStatus sessionStatus = SessionStatus.CLOSED;

    @Column(name = "data_status", nullable = false, length = 32)
    private String dataStatus = "VALID";

    public GlobalMarketSnapshot() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getCanonicalSymbol() { return canonicalSymbol; }
    public void setCanonicalSymbol(String canonicalSymbol) { this.canonicalSymbol = canonicalSymbol; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public LocalDate getTradingDate() { return tradingDate; }
    public void setTradingDate(LocalDate tradingDate) { this.tradingDate = tradingDate; }
    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }
    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }
    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }
    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }
    public BigDecimal getPreviousClose() { return previousClose; }
    public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }
    public BigDecimal getChange() { return change; }
    public void setChange(BigDecimal change) { this.change = change; }
    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    public BigDecimal getYieldRate() { return yieldRate; }
    public void setYieldRate(BigDecimal yieldRate) { this.yieldRate = yieldRate; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getSourceTimestamp() { return sourceTimestamp; }
    public void setSourceTimestamp(Instant sourceTimestamp) { this.sourceTimestamp = sourceTimestamp; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
    public DataFreshness getDataFreshness() { return dataFreshness; }
    public void setDataFreshness(DataFreshness dataFreshness) { this.dataFreshness = dataFreshness; }
    public SessionStatus getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(SessionStatus sessionStatus) { this.sessionStatus = sessionStatus; }
    public String getDataStatus() { return dataStatus; }
    public void setDataStatus(String dataStatus) { this.dataStatus = dataStatus; }
}
