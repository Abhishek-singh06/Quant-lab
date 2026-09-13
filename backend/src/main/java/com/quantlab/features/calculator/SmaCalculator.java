package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class SmaCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final int period;
    private final PriceSeriesType priceSeriesType;

    public SmaCalculator(int period) {
        this("SMA_" + period, period, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public SmaCalculator(String featureName, int period, PriceSeriesType priceSeriesType) {
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
        return "SMA_v1.0";
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

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = size - period; i < size; i++) {
            BigDecimal price = bars.get(i).getPrice(priceSeriesType);
            if (price == null) {
                return TechnicalFeatureResult.error(
                        featureName, context.getTargetDate(), context.getTargetTimestamp(),
                        getFeatureVersion(), "Null price found within rolling SMA window"
                );
            }
            sum = sum.add(price);
        }

        BigDecimal sma = sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, sma, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
