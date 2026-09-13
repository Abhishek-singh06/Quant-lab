package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class MomentumCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final int period;
    private final PriceSeriesType priceSeriesType;

    public MomentumCalculator(int period) {
        this("MOMENTUM_" + period, period, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public MomentumCalculator(String featureName, int period, PriceSeriesType priceSeriesType) {
        this.featureName = featureName;
        this.period = period;
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
        return "PRICE_MOMENTUM_RATIO_v1.0";
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

        if (size < period + 1) {
            return TechnicalFeatureResult.insufficientHistory(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), period + 1, size
            );
        }

        BigDecimal current = bars.get(size - 1).getPrice(priceSeriesType);
        BigDecimal prior = bars.get(size - 1 - period).getPrice(priceSeriesType);

        if (current == null || prior == null || prior.compareTo(BigDecimal.ZERO) <= 0) {
            return TechnicalFeatureResult.error(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), "Invalid or non-positive price for momentum"
            );
        }

        // Percentage change: ((Current / Prior) - 1) * 100
        BigDecimal momentum = current.subtract(prior)
                .divide(prior, 6, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100.00"));

        return TechnicalFeatureResult.success(
                featureName, momentum.setScale(4, RoundingMode.HALF_UP),
                context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
