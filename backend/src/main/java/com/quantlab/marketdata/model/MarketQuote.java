package com.quantlab.marketdata.model;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketQuote(
    String symbol,
    Exchange exchange,
    String isin,
    BigDecimal lastPrice,
    BigDecimal openPrice,
    BigDecimal highPrice,
    BigDecimal lowPrice,
    BigDecimal closePrice,
    BigDecimal prevClosePrice,
    BigDecimal change,
    BigDecimal changePercent,
    Long volume,
    BigDecimal totalTradedValue,
    Long openInterest,
    Instant timestamp,
    Instant sourceTimestamp,
    Instant ingestionTimestamp,
    String source,
    String runId,
    MarketDataStatus status
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String symbol;
        private Exchange exchange = Exchange.NSE;
        private String isin;
        private BigDecimal lastPrice;
        private BigDecimal openPrice;
        private BigDecimal highPrice;
        private BigDecimal lowPrice;
        private BigDecimal closePrice;
        private BigDecimal prevClosePrice;
        private BigDecimal change;
        private BigDecimal changePercent;
        private Long volume;
        private BigDecimal totalTradedValue;
        private Long openInterest;
        private Instant timestamp;
        private Instant sourceTimestamp;
        private Instant ingestionTimestamp = Instant.now();
        private String source = "NSE";
        private String runId;
        private MarketDataStatus status = MarketDataStatus.VALID;

        public Builder symbol(String symbol) { this.symbol = symbol; return this; }
        public Builder exchange(Exchange exchange) { this.exchange = exchange; return this; }
        public Builder isin(String isin) { this.isin = isin; return this; }
        public Builder lastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; return this; }
        public Builder openPrice(BigDecimal openPrice) { this.openPrice = openPrice; return this; }
        public Builder highPrice(BigDecimal highPrice) { this.highPrice = highPrice; return this; }
        public Builder lowPrice(BigDecimal lowPrice) { this.lowPrice = lowPrice; return this; }
        public Builder closePrice(BigDecimal closePrice) { this.closePrice = closePrice; return this; }
        public Builder prevClosePrice(BigDecimal prevClosePrice) { this.prevClosePrice = prevClosePrice; return this; }
        public Builder change(BigDecimal change) { this.change = change; return this; }
        public Builder changePercent(BigDecimal changePercent) { this.changePercent = changePercent; return this; }
        public Builder volume(Long volume) { this.volume = volume; return this; }
        public Builder totalTradedValue(BigDecimal totalTradedValue) { this.totalTradedValue = totalTradedValue; return this; }
        public Builder openInterest(Long openInterest) { this.openInterest = openInterest; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder sourceTimestamp(Instant sourceTimestamp) { this.sourceTimestamp = sourceTimestamp; return this; }
        public Builder ingestionTimestamp(Instant ingestionTimestamp) { this.ingestionTimestamp = ingestionTimestamp; return this; }
        public Builder source(String source) { this.source = source; return this; }
        public Builder runId(String runId) { this.runId = runId; return this; }
        public Builder status(MarketDataStatus status) { this.status = status; return this; }

        public MarketQuote build() {
            return new MarketQuote(
                symbol, exchange, isin, lastPrice, openPrice, highPrice, lowPrice,
                closePrice, prevClosePrice, change, changePercent, volume,
                totalTradedValue, openInterest, timestamp, sourceTimestamp,
                ingestionTimestamp, source, runId, status
            );
        }
    }
}
