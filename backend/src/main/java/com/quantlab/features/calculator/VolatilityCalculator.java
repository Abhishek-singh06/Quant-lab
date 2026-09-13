package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class VolatilityCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final int period;
    private final boolean annualized;
    private final PriceSeriesType priceSeriesType;

    public VolatilityCalculator(int period, boolean annualized) {
        this(annualized ? "VOLATILITY_" + period + "D" : "REALIZED_VOLATILITY_" + period + "D",
                period, annualized, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public VolatilityCalculator(String featureName, int period, boolean annualized, PriceSeriesType priceSeriesType) {
        this.featureName = featureName;
        this.period = period;
        this.annualized = annualized;
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
        return annualized ? "ANNUALIZED_VOLATILITY_252_v1.0" : "DAILY_VOLATILITY_v1.0";
    }

    @Override
    public FeatureCategory getCategory() {
        return FeatureCategory.VOLATILITY;
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

        // Compute 1-day simple returns over rolling window
        double[] returns = new double[period];
        double sum = 0.0;

        int startIdx = size - period;
        for (int i = 0; i < period; i++) {
            BigDecimal curr = bars.get(startIdx + i).getPrice(priceSeriesType);
            BigDecimal prev = bars.get(startIdx + i - 1).getPrice(priceSeriesType);

            if (curr == null || prev == null || prev.compareTo(BigDecimal.ZERO) <= 0) {
                return TechnicalFeatureResult.error(
                        featureName, context.getTargetDate(), context.getTargetTimestamp(),
                        getFeatureVersion(), "Invalid prices for return calculation"
                );
            }

            double r = (curr.doubleValue() - prev.doubleValue()) / prev.doubleValue();
            returns[i] = r;
            sum += r;
        }

        double mean = sum / period;
        double varianceSum = 0.0;
        for (double r : returns) {
            varianceSum += Math.pow(r - mean, 2);
        }

        // Sample standard deviation (N - 1)
        double stdDev = (period > 1) ? Math.sqrt(varianceSum / (period - 1)) : 0.0;

        double finalVal = annualized ? (stdDev * Math.sqrt(252.0) * 100.0) : (stdDev * 100.0);
        BigDecimal result = BigDecimal.valueOf(finalVal).setScale(4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
