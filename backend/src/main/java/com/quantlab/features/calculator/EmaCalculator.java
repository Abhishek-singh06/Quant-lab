package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class EmaCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final int period;
    private final PriceSeriesType priceSeriesType;

    public EmaCalculator(int period) {
        this("EMA_" + period, period, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public EmaCalculator(String featureName, int period, PriceSeriesType priceSeriesType) {
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
        return "EMA_v1.0";
    }

    @Override
    public FeatureCategory getCategory() {
        return FeatureCategory.TREND;
    }

    @Override
    public int getRequiredLookback() {
        return period;
    }

    @Override
    public PriceSeriesType getRequiredPriceSeriesType() {
        return priceSeriesType;
    }

    @Override
    public TechnicalFeatureResult calculate(TechnicalFeatureContext context) {
        List<PriceBar> bars = context.getPriceBars();
        int size = bars.size();

        if (size < period) {
            return TechnicalFeatureResult.insufficientHistory(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), period, size
            );
        }

        // Multiplier: 2 / (N + 1)
        double multiplier = 2.0 / (period + 1.0);

        // Initial EMA is SMA of first 'period' bars in the series
        double sum = 0.0;
        for (int i = 0; i < period; i++) {
            BigDecimal p = bars.get(i).getPrice(priceSeriesType);
            if (p == null) {
                return TechnicalFeatureResult.error(
                        featureName, context.getTargetDate(), context.getTargetTimestamp(),
                        getFeatureVersion(), "Null price found within initial EMA period"
                );
            }
            sum += p.doubleValue();
        }

        double ema = sum / period;

        // Roll EMA forward to the end of available history
        for (int i = period; i < size; i++) {
            BigDecimal p = bars.get(i).getPrice(priceSeriesType);
            if (p != null) {
                ema = (p.doubleValue() - ema) * multiplier + ema;
            }
        }

        BigDecimal result = BigDecimal.valueOf(ema).setScale(4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
