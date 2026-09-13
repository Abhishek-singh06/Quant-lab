package com.quantlab.prediction.model;

public enum TargetHorizon {
    HORIZON_1D("1D"),
    HORIZON_5D("5D"),
    HORIZON_10D("10D"),
    HORIZON_20D("20D"),
    HORIZON_63D("63D");

    private final String label;

    TargetHorizon(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
