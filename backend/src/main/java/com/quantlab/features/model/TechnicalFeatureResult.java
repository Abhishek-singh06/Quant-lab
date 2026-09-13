package com.quantlab.features.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Encapsulates the output of a single technical feature calculation.
 */
public class TechnicalFeatureResult {

    private final String featureName;
    private final BigDecimal value;
    private final LocalDate tradingDate;
    private final Instant featureTimestamp;
    private final String featureVersion;
    private final String calculationVersion;
    private final ValidationStatus status;
    private final String message;

    private TechnicalFeatureResult(String featureName, BigDecimal value, LocalDate tradingDate, Instant featureTimestamp,
                                   String featureVersion, String calculationVersion, ValidationStatus status, String message) {
        this.featureName = featureName;
        this.value = value;
        this.tradingDate = tradingDate;
        this.featureTimestamp = featureTimestamp;
        this.featureVersion = featureVersion;
        this.calculationVersion = calculationVersion;
        this.status = status;
        this.message = message;
    }

    public static TechnicalFeatureResult success(String featureName, BigDecimal value, LocalDate tradingDate,
                                                Instant featureTimestamp, String featureVersion, String calculationVersion) {
        return new TechnicalFeatureResult(featureName, value, tradingDate, featureTimestamp, featureVersion, calculationVersion, ValidationStatus.VALID, null);
    }

    public static TechnicalFeatureResult insufficientHistory(String featureName, LocalDate tradingDate,
                                                             Instant featureTimestamp, String featureVersion, int required, int available) {
        return new TechnicalFeatureResult(featureName, null, tradingDate, featureTimestamp, featureVersion, "1.0.0",
                ValidationStatus.INSUFFICIENT_HISTORY, "Requires " + required + " bars, available: " + available);
    }

    public static TechnicalFeatureResult error(String featureName, LocalDate tradingDate,
                                              Instant featureTimestamp, String featureVersion, String errorMsg) {
        return new TechnicalFeatureResult(featureName, null, tradingDate, featureTimestamp, featureVersion, "1.0.0",
                ValidationStatus.CALCULATION_ERROR, errorMsg);
    }

    public String getFeatureName() { return featureName; }
    public BigDecimal getValue() { return value; }
    public LocalDate getTradingDate() { return tradingDate; }
    public Instant getFeatureTimestamp() { return featureTimestamp; }
    public String getFeatureVersion() { return featureVersion; }
    public String getCalculationVersion() { return calculationVersion; }
    public ValidationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isValid() { return status == ValidationStatus.VALID && value != null; }
}
