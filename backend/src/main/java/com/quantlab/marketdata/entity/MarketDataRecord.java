package com.quantlab.marketdata.entity;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketDataStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "market_quotes",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_quote_symbol_exchange_timestamp",
            columnNames = {"symbol", "exchange", "timestamp", "data_type"}
        )
    },
    indexes = {
        @Index(name = "idx_quotes_symbol_exchange", columnList = "symbol, exchange"),
        @Index(name = "idx_quotes_timestamp", columnList = "timestamp"),
        @Index(name = "idx_quotes_run_id", columnList = "run_id"),
        @Index(name = "idx_quotes_status", columnList = "status")
    }
)
public class MarketDataRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Exchange exchange;

    @Column(name = "isin", length = 16)
    private String isin;

    @Column(name = "data_type", nullable = false, length = 16)
    private String dataType = "QUOTE";

    @Column(name = "last_price", precision = 18, scale = 4)
    private BigDecimal lastPrice;

    @Column(name = "open_price", precision = 18, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "high_price", precision = 18, scale = 4)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 18, scale = 4)
    private BigDecimal lowPrice;

    @Column(name = "close_price", precision = 18, scale = 4)
    private BigDecimal closePrice;

    @Column(name = "prev_close_price", precision = 18, scale = 4)
    private BigDecimal prevClosePrice;

    @Column(name = "price_change", precision = 18, scale = 4)
    private BigDecimal change;

    @Column(name = "change_percent", precision = 10, scale = 4)
    private BigDecimal changePercent;

    @Column(name = "volume")
    private Long volume;

    @Column(name = "total_traded_value", precision = 24, scale = 4)
    private BigDecimal totalTradedValue;

    @Column(name = "open_interest")
    private Long openInterest;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "source_timestamp")
    private Instant sourceTimestamp;

    @Column(name = "ingestion_timestamp", nullable = false)
    private Instant ingestionTimestamp;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "run_id", length = 64)
    private String runId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MarketDataStatus status = MarketDataStatus.VALID;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public MarketDataRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Exchange getExchange() { return exchange; }
    public void setExchange(Exchange exchange) { this.exchange = exchange; }
    public String getIsin() { return isin; }
    public void setIsin(String isin) { this.isin = isin; }
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    public BigDecimal getLastPrice() { return lastPrice; }
    public void setLastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; }
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    public BigDecimal getHighPrice() { return highPrice; }
    public void setHighPrice(BigDecimal highPrice) { this.highPrice = highPrice; }
    public BigDecimal getLowPrice() { return lowPrice; }
    public void setLowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; }
    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }
    public BigDecimal getPrevClosePrice() { return prevClosePrice; }
    public void setPrevClosePrice(BigDecimal prevClosePrice) { this.prevClosePrice = prevClosePrice; }
    public BigDecimal getChange() { return change; }
    public void setChange(BigDecimal change) { this.change = change; }
    public BigDecimal getChangePercent() { return changePercent; }
    public void setChangePercent(BigDecimal changePercent) { this.changePercent = changePercent; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    public BigDecimal getTotalTradedValue() { return totalTradedValue; }
    public void setTotalTradedValue(BigDecimal totalTradedValue) { this.totalTradedValue = totalTradedValue; }
    public Long getOpenInterest() { return openInterest; }
    public void setOpenInterest(Long openInterest) { this.openInterest = openInterest; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Instant getSourceTimestamp() { return sourceTimestamp; }
    public void setSourceTimestamp(Instant sourceTimestamp) { this.sourceTimestamp = sourceTimestamp; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public MarketDataStatus getStatus() { return status; }
    public void setStatus(MarketDataStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
