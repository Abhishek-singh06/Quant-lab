package com.quantlab.warehouse.entity;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.model.AdjustmentMethodology;
import com.quantlab.warehouse.model.TimeGranularity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "historical_prices_adjusted",
    schema = "market_data",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_hist_price_adj",
            columnNames = {"instrument_id", "exchange", "trading_date", "granularity", "methodology"}
        )
    },
    indexes = {
        @Index(name = "idx_hist_adj_inst_date", columnList = "instrument_id, trading_date"),
        @Index(name = "idx_hist_adj_symbol_date", columnList = "symbol, trading_date"),
        @Index(name = "idx_hist_adj_methodology", columnList = "methodology")
    }
)
public class HistoricalPriceAdjusted {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_price_id", nullable = false)
    private Long rawPriceId;

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

    @Column(name = "adj_open", nullable = false, precision = 18, scale = 4)
    private BigDecimal adjOpen;

    @Column(name = "adj_high", nullable = false, precision = 18, scale = 4)
    private BigDecimal adjHigh;

    @Column(name = "adj_low", nullable = false, precision = 18, scale = 4)
    private BigDecimal adjLow;

    @Column(name = "adj_close", nullable = false, precision = 18, scale = 4)
    private BigDecimal adjClose;

    @Column(name = "total_return_close", precision = 18, scale = 4)
    private BigDecimal totalReturnClose;

    @Column(name = "cumulative_split_factor", nullable = false, precision = 18, scale = 8)
    private BigDecimal cumulativeSplitFactor = BigDecimal.ONE;

    @Column(name = "cumulative_dividend_factor", precision = 18, scale = 8)
    private BigDecimal cumulativeDividendFactor = BigDecimal.ONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AdjustmentMethodology methodology = AdjustmentMethodology.SPLIT_ADJUSTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TimeGranularity granularity = TimeGranularity.DAILY;

    @Column(name = "adjustment_timestamp", nullable = false)
    private Instant adjustmentTimestamp = Instant.now();

    @Column(name = "data_version", nullable = false)
    private int dataVersion = 1;

    public HistoricalPriceAdjusted() {}

    public HistoricalPriceAdjusted(Long rawPriceId, Long instrumentId, String symbol, Exchange exchange,
                                   LocalDate tradingDate, Instant timestamp, BigDecimal adjOpen, BigDecimal adjHigh,
                                   BigDecimal adjLow, BigDecimal adjClose, BigDecimal totalReturnClose,
                                   BigDecimal cumulativeSplitFactor, BigDecimal cumulativeDividendFactor,
                                   AdjustmentMethodology methodology, TimeGranularity granularity) {
        this.rawPriceId = rawPriceId;
        this.instrumentId = instrumentId;
        this.symbol = symbol;
        this.exchange = exchange;
        this.tradingDate = tradingDate;
        this.timestamp = timestamp;
        this.adjOpen = adjOpen;
        this.adjHigh = adjHigh;
        this.adjLow = adjLow;
        this.adjClose = adjClose;
        this.totalReturnClose = totalReturnClose;
        this.cumulativeSplitFactor = cumulativeSplitFactor;
        this.cumulativeDividendFactor = cumulativeDividendFactor;
        this.methodology = methodology;
        this.granularity = granularity;
        this.adjustmentTimestamp = Instant.now();
        this.dataVersion = 1;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRawPriceId() { return rawPriceId; }
    public void setRawPriceId(Long rawPriceId) { this.rawPriceId = rawPriceId; }
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
    public BigDecimal getAdjOpen() { return adjOpen; }
    public void setAdjOpen(BigDecimal adjOpen) { this.adjOpen = adjOpen; }
    public BigDecimal getAdjHigh() { return adjHigh; }
    public void setAdjHigh(BigDecimal adjHigh) { this.adjHigh = adjHigh; }
    public BigDecimal getAdjLow() { return adjLow; }
    public void setAdjLow(BigDecimal adjLow) { this.adjLow = adjLow; }
    public BigDecimal getAdjClose() { return adjClose; }
    public void setAdjClose(BigDecimal adjClose) { this.adjClose = adjClose; }
    public BigDecimal getTotalReturnClose() { return totalReturnClose; }
    public void setTotalReturnClose(BigDecimal totalReturnClose) { this.totalReturnClose = totalReturnClose; }
    public BigDecimal getCumulativeSplitFactor() { return cumulativeSplitFactor; }
    public void setCumulativeSplitFactor(BigDecimal cumulativeSplitFactor) { this.cumulativeSplitFactor = cumulativeSplitFactor; }
    public BigDecimal getCumulativeDividendFactor() { return cumulativeDividendFactor; }
    public void setCumulativeDividendFactor(BigDecimal cumulativeDividendFactor) { this.cumulativeDividendFactor = cumulativeDividendFactor; }
    public AdjustmentMethodology getMethodology() { return methodology; }
    public void setMethodology(AdjustmentMethodology methodology) { this.methodology = methodology; }
    public TimeGranularity getGranularity() { return granularity; }
    public void setGranularity(TimeGranularity granularity) { this.granularity = granularity; }
    public Instant getAdjustmentTimestamp() { return adjustmentTimestamp; }
    public void setAdjustmentTimestamp(Instant adjustmentTimestamp) { this.adjustmentTimestamp = adjustmentTimestamp; }
    public int getDataVersion() { return dataVersion; }
    public void setDataVersion(int dataVersion) { this.dataVersion = dataVersion; }
}
