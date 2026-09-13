package com.quantlab.features.model;

import java.time.LocalDate;
import java.util.List;

public class FeatureCalculationRequest {

    private List<String> symbols;
    private List<String> featureNames; // If empty, calculates all enabled features
    private LocalDate fromDate;
    private LocalDate toDate;
    private String timeframe = "1D";
    private boolean forceRecalculate = false;

    public FeatureCalculationRequest() {}

    public List<String> getSymbols() { return symbols; }
    public void setSymbols(List<String> symbols) { this.symbols = symbols; }
    public List<String> getFeatureNames() { return featureNames; }
    public void setFeatureNames(List<String> featureNames) { this.featureNames = featureNames; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public boolean isForceRecalculate() { return forceRecalculate; }
    public void setForceRecalculate(boolean forceRecalculate) { this.forceRecalculate = forceRecalculate; }
}
