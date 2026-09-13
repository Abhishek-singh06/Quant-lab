package com.quantlab.features.service;

import com.quantlab.features.model.TechnicalFeatureResult;
import com.quantlab.features.model.ValidationStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TechnicalFeatureValidatorService {

    public ValidationStatus validateResult(TechnicalFeatureResult result) {
        if (result == null || result.getStatus() != ValidationStatus.VALID) {
            return result != null ? result.getStatus() : ValidationStatus.NULL_VALUE;
        }

        BigDecimal val = result.getValue();
        if (val == null) {
            return ValidationStatus.NULL_VALUE;
        }

        String name = result.getFeatureName();
        double dVal = val.doubleValue();

        if (Double.isNaN(dVal) || Double.isInfinite(dVal)) {
            return ValidationStatus.CALCULATION_ERROR;
        }

        // 1. RSI bounds [0, 100]
        if (name.startsWith("RSI_") && (dVal < -0.0001 || dVal > 100.0001)) {
            return ValidationStatus.OUT_OF_BOUNDS;
        }

        // 2. 52-Week Position bounds [0, 1]
        if (name.equals("WEEK_52_POSITION") && (dVal < -0.0001 || dVal > 1.0001)) {
            return ValidationStatus.OUT_OF_BOUNDS;
        }

        // 3. Volume Ratio >= 0
        if (name.startsWith("VOLUME_RATIO_") && dVal < -0.0001) {
            return ValidationStatus.OUT_OF_BOUNDS;
        }

        // 4. ATR >= 0
        if (name.startsWith("ATR_") && dVal < -0.0001) {
            return ValidationStatus.OUT_OF_BOUNDS;
        }

        // 5. Volatility >= 0
        if (name.contains("VOLATILITY") && dVal < -0.0001) {
            return ValidationStatus.OUT_OF_BOUNDS;
        }

        // 6. Drawdown <= 0
        if (name.contains("DRAWDOWN") && dVal > 0.0001) {
            return ValidationStatus.OUT_OF_BOUNDS;
        }

        // 7. Moving averages > 0
        if ((name.startsWith("SMA_") || name.startsWith("EMA_")) && dVal <= 0.0) {
            return ValidationStatus.OUT_OF_BOUNDS;
        }

        return ValidationStatus.VALID;
    }
}
