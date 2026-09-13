package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class ReturnsCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final int period;
    private final boolean isLog;
    private final PriceSeriesType priceSeriesType;

    public ReturnsCalculator(String featureName, int period, boolean isLog, PriceSeriesType priceSeriesType) {
        this.featureName = featureName;
        this.period = period;
        this.isLog = isLog;
        this.priceSeriesType = priceSeriesType;
    }

    @Override
    public String getFeatureName() {
        return featureName;
    }

    @Override
    public String getFeatureVersion() {
        return "1.0.0";
    }

    @Override
    public String getFormulaVersion() {
        return isLog ? "LOG_RETURN_v1.0" : "SIMPLE_RETURN_v1.0";
    }

    @Override
    public FeatureCategory getCategory() {
        return FeatureCategory.MOMENTUM;
    }

    @Override
    public int getRequiredLookback() {
        return period + 1;
    }

    @Override
    public PriceSeriesType getRequiredPriceSeriesType() {
        return priceSeriesType;
    }

    @Override
    public TechnicalFeatureResult calculate(TechnicalFeatureContext context) {
        List<PriceBar> bars = context.getPriceBars();
        int size = bars.size();
        PriceBar current = context.getLatestBar();

        if (size < period + 1 || current == null) {
            return TechnicalFeatureResult.insufficientHistory(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), period + 1, size
            );
        }

        PriceBar prior = bars.get(size - 1 - period);
        BigDecimal currentPrice = current.getPrice(priceSeriesType);
        BigDecimal priorPrice = prior.getPrice(priceSeriesType);

        if (priorPrice == null || priorPrice.compareTo(BigDecimal.ZERO) <= 0 || currentPrice == null) {
            return TechnicalFeatureResult.error(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), "Invalid or non-positive price encountered"
            );
        }

        BigDecimal returnValue;
        if (isLog) {
            double logReturn = Math.log(currentPrice.doubleValue() / priorPrice.doubleValue());
            returnValue = BigDecimal.valueOf(logReturn).setScale(8, RoundingMode.HALF_UP);
        } else {
            // (Current - Prior) / Prior
            returnValue = currentPrice.subtract(priorPrice)
                    .divide(priorPrice, 8, RoundingMode.HALF_UP);
        }

        return TechnicalFeatureResult.success(
                featureName, returnValue, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
