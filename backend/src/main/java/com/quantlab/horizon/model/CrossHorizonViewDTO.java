package com.quantlab.horizon.model;

import java.time.Instant;
import java.util.Map;

public class CrossHorizonViewDTO {
    private String symbol;
    private Instant asOf;
    private HorizonPredictionDTO shortTerm;
    private HorizonPredictionDTO mediumTerm;
    private HorizonPredictionDTO longTerm;
    private HorizonConflictDTO conflict;
    private String shortTermStatus;
    private String mediumTermStatus;
    private String longTermStatus;
    private Map<String, String> modelVersions;

    public CrossHorizonViewDTO() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Instant getAsOf() { return asOf; }
    public void setAsOf(Instant asOf) { this.asOf = asOf; }

    public HorizonPredictionDTO getShortTerm() { return shortTerm; }
    public void setShortTerm(HorizonPredictionDTO shortTerm) { this.shortTerm = shortTerm; }

    public HorizonPredictionDTO getMediumTerm() { return mediumTerm; }
    public void setMediumTerm(HorizonPredictionDTO mediumTerm) { this.mediumTerm = mediumTerm; }

    public HorizonPredictionDTO getLongTerm() { return longTerm; }
    public void setLongTerm(HorizonPredictionDTO longTerm) { this.longTerm = longTerm; }

    public HorizonConflictDTO getConflict() { return conflict; }
    public void setConflict(HorizonConflictDTO conflict) { this.conflict = conflict; }

    public String getShortTermStatus() { return shortTermStatus; }
    public void setShortTermStatus(String shortTermStatus) { this.shortTermStatus = shortTermStatus; }

    public String getMediumTermStatus() { return mediumTermStatus; }
    public void setMediumTermStatus(String mediumTermStatus) { this.mediumTermStatus = mediumTermStatus; }

    public String getLongTermStatus() { return longTermStatus; }
    public void setLongTermStatus(String longTermStatus) { this.longTermStatus = longTermStatus; }

    public Map<String, String> getModelVersions() { return modelVersions; }
    public void setModelVersions(Map<String, String> modelVersions) { this.modelVersions = modelVersions; }
}
