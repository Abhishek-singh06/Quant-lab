package com.quantlab.global.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class GlobalMarketSnapshotDTO {

    private String canonicalSymbol;
    private String providerSymbol;
    private String instrumentName;
    private AssetClass assetClass;
    private GlobalMarket market;
    private String country;
    private String currency;
    private String timezone;
    private Instant timestamp; // UTC
    private LocalDate tradingDate;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal changePercent;
    private Long volume;
    private BigDecimal yieldRate;
    private String source;
    private Instant sourceTimestamp;
    private Instant ingestionTimestamp;
    private DataFreshness dataFreshness;
    private SessionStatus sessionStatus;
    private Long ageMinutes;

    public GlobalMarketSnapshotDTO() {}

    public String getCanonicalSymbol() { return canonicalSymbol; }
    public void setCanonicalSymbol(String canonicalSymbol) { this.canonicalSymbol = canonicalSymbol; }
    public String getProviderSymbol() { return providerSymbol; }
    public void setProviderSymbol(String providerSymbol) { this.providerSymbol = providerSymbol; }
    public String getInstrumentName() { return instrumentName; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
    public AssetClass getAssetClass() { return assetClass; }
    public void setAssetClass(AssetClass assetClass) { this.assetClass = assetClass; }
    public GlobalMarket getMarket() { return market; }
    public void setMarket(GlobalMarket market) { this.market = market; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
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
    public Long getAgeMinutes() { return ageMinutes; }
    public void setAgeMinutes(Long ageMinutes) { this.ageMinutes = ageMinutes; }
}
