package com.quantlab.features.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Immutable standard price bar representation for technical indicator calculations.
 */
public class PriceBar {

    private final LocalDate tradingDate;
    private final Instant timestamp;
    private final BigDecimal open;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal close;
    private final Long volume;

    private final BigDecimal adjOpen;
    private final BigDecimal adjHigh;
    private final BigDecimal adjLow;
    private final BigDecimal adjClose;
    private final BigDecimal totalReturnClose;

    public PriceBar(LocalDate tradingDate, Instant timestamp,
                    BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume,
                    BigDecimal adjOpen, BigDecimal adjHigh, BigDecimal adjLow, BigDecimal adjClose,
                    BigDecimal totalReturnClose) {
        this.tradingDate = tradingDate;
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume != null ? volume : 0L;
        this.adjOpen = adjOpen != null ? adjOpen : open;
        this.adjHigh = adjHigh != null ? adjHigh : high;
        this.adjLow = adjLow != null ? adjLow : low;
        this.adjClose = adjClose != null ? adjClose : close;
        this.totalReturnClose = totalReturnClose != null ? totalReturnClose : this.adjClose;
    }

    public LocalDate getTradingDate() { return tradingDate; }
    public Instant getTimestamp() { return timestamp; }
    public BigDecimal getOpen() { return open; }
    public BigDecimal getHigh() { return high; }
    public BigDecimal getLow() { return low; }
    public BigDecimal getClose() { return close; }
    public Long getVolume() { return volume; }
    public BigDecimal getAdjOpen() { return adjOpen; }
    public BigDecimal getAdjHigh() { return adjHigh; }
    public BigDecimal getAdjLow() { return adjLow; }
    public BigDecimal getAdjClose() { return adjClose; }
    public BigDecimal getTotalReturnClose() { return totalReturnClose; }

    public BigDecimal getPrice(PriceSeriesType seriesType) {
        if (seriesType == null) return adjClose;
        return switch (seriesType) {
            case RAW -> close;
            case TOTAL_RETURN -> totalReturnClose;
            case SPLIT_ADJUSTED -> adjClose;
        };
    }

    public BigDecimal getHigh(PriceSeriesType seriesType) {
        return (seriesType == PriceSeriesType.RAW) ? high : adjHigh;
    }

    public BigDecimal getLow(PriceSeriesType seriesType) {
        return (seriesType == PriceSeriesType.RAW) ? low : adjLow;
    }
}
