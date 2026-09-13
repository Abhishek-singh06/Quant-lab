package com.quantlab.regime.model;

import java.time.LocalDate;

public class RegimeCalculationRequest {

    private String symbol = "NIFTY 50";
    private LocalDate fromDate;
    private LocalDate toDate;
    private String modelVersion = "REGIME_v1.0.0";
    private boolean forceRecalculate = false;

    private LocalDate tradingDate;
    private java.time.Instant asOfTimestamp;

    public RegimeCalculationRequest() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public boolean isForceRecalculate() { return forceRecalculate; }
    public void setForceRecalculate(boolean forceRecalculate) { this.forceRecalculate = forceRecalculate; }
    public LocalDate getTradingDate() { return tradingDate != null ? tradingDate : (toDate != null ? toDate : LocalDate.now()); }
    public void setTradingDate(LocalDate tradingDate) { this.tradingDate = tradingDate; }
    public java.time.Instant getAsOfTimestamp() { return asOfTimestamp != null ? asOfTimestamp : java.time.Instant.now(); }
    public void setAsOfTimestamp(java.time.Instant asOfTimestamp) { this.asOfTimestamp = asOfTimestamp; }
}
