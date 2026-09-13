package com.quantlab.warehouse.entity;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.model.TimeGranularity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "historical_prices",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_hist_price_raw",
            columnNames = {"instrument_id", "exchange", "trading_date", "granularity"}
        )
    },
    indexes = {
        @Index(name = "idx_hist_raw_inst_date", columnList = "instrument_id, trading_date"),
        @Index(name = "idx_hist_raw_symbol_date", columnList = "symbol, trading_date"),
        @Index(name = "idx_hist_raw_date", columnList = "trading_date")
    }
)
public class HistoricalPriceRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Exchange exchange = Exchange.NSE;

    @Column(name = "trading_date", nullable = false)
    private LocalDate tradingDate;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(name = "open_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal open;

    @Column(name = "high_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal high;

    @Column(name = "low_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal low;

    @Column(name = "close_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal close;

    @Column(nullable = false)
    private Long volume;

    @Column(name = "total_traded_value", precision = 24, scale = 4)
    private BigDecimal totalTradedValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TimeGranularity granularity = TimeGranularity.DAILY;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "ingestion_timestamp", nullable = false)
    private Instant ingestionTimestamp = Instant.now();

    @Column(name = "data_version", nullable = false)
    private int dataVersion = 1;

    public HistoricalPriceRaw() {}

    public HistoricalPriceRaw(Long instrumentId, String symbol, Exchange exchange, LocalDate tradingDate,
                              Instant timestamp, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,
                              Long volume, BigDecimal totalTradedValue, TimeGranularity granularity, String source) {
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.tradingDate = tradingDate;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.totalTradedValue = totalTradedValue;
        this.granularity = granularity;
        this.source = source;
        this.ingestionTimestamp = Instant.now();
        this.dataVersion = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Exchange getExchange() { return exchange; }
    public void setExchange(Exchange exchange) { this.exchange = exchange; }
    public LocalDate getTradingDate() { return tradingDate; }
    public void setTradingDate(LocalDate tradingDate) { this.tradingDate = tradingDate; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public BigDecimal getOpen() { return open; }
    public void setOpen(BigDecimal open) { this.open = open; }
    public BigDecimal getHigh() { return high; }
    public void setHigh(BigDecimal high) { this.high = high; }
    public BigDecimal getLow() { return low; }
    public void setLow(BigDecimal low) { this.low = low; }
    public BigDecimal getClose() { return close; }
    public void setClose(BigDecimal close) { this.close = close; }
    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
    public BigDecimal getTotalTradedValue() { return totalTradedValue; }
    public void setTotalTradedValue(BigDecimal totalTradedValue) { this.totalTradedValue = totalTradedValue; }
    public TimeGranularity getGranularity() { return granularity; }
    public void setGranularity(TimeGranularity granularity) { this.granularity = granularity; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getIngestionTimestamp() { return ingestionTimestamp; }
    public void setIngestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; }
    public int getDataVersion() { return dataVersion; }
    public void setDataVersion(int dataVersion) { this.dataVersion = dataVersion; }
}
