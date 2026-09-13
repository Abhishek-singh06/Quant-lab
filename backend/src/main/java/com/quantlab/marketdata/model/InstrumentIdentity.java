package com.quantlab.marketdata.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Normalized instrument identity encapsulating exchange ticker, ISIN,
 * tick size, lot size, and symbol history for Indian Equities.
 */
public class InstrumentIdentity {

    private String instrumentId;
    private Exchange exchange;
    private String symbol;
    private String isin;
    private String assetClass; // e.g. "EQUITY", "INDEX"
    private String currency;   // e.g. "INR"
    private int lotSize;
    private double tickSize;
    private List<String> historicalSymbols = new ArrayList<>();
    private LocalDate validFrom;
    private LocalDate validTo;

    public InstrumentIdentity() {
        this.lotSize = 1;
        this.tickSize = 0.05;
        this.currency = "INR";
        this.assetClass = "EQUITY";
    }

    public InstrumentIdentity(String instrumentId, Exchange exchange, String symbol, String isin) {
        this();
        this.instrumentId = instrumentId;
        this.exchange = exchange;
        this.symbol = symbol;
        this.isin = isin;
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(String instrumentId) {
        this.instrumentId = instrumentId;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getAssetClass() {
        return assetClass;
    }

    public void setAssetClass(String assetClass) {
        this.assetClass = assetClass;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getLotSize() {
        return lotSize;
    }

    public void setLotSize(int lotSize) {
        this.lotSize = lotSize;
    }

    public double getTickSize() {
        return tickSize;
    }

    public void setTickSize(double tickSize) {
        this.tickSize = tickSize;
    }

    public List<String> getHistoricalSymbols() {
        return historicalSymbols;
    }

    public void setHistoricalSymbols(List<String> historicalSymbols) {
        this.historicalSymbols = historicalSymbols;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstrumentIdentity that)) return false;
        return Objects.equals(instrumentId, that.instrumentId) &&
               exchange == that.exchange &&
               Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instrumentId, exchange, symbol);
    }

    @Override
    public String toString() {
        return "InstrumentIdentity{" +
                "instrumentId='" + instrumentId + '\'' +
                ", exchange=" + exchange +
                ", symbol='" + symbol + '\'' +
                ", isin='" + isin + '\'' +
                ", lotSize=" + lotSize +
                ", tickSize=" + tickSize +
                '}';
    }
}
