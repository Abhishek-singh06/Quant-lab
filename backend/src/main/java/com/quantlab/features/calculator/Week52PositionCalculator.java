package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class Week52PositionCalculator implements TechnicalFeatureCalculator {

    public enum Mode {
        HIGH,
        LOW,
        POSITION,
        DISTANCE_FROM_HIGH,
        DISTANCE_FROM_LOW
    }

    private final String featureName;
    private final int lookback;
    private final Mode mode;
    private final PriceSeriesType priceSeriesType;

    public Week52PositionCalculator(String featureName, Mode mode) {
        this(featureName, 252, mode, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public Week52PositionCalculator(String featureName, int lookback, Mode mode, PriceSeriesType priceSeriesType) {
        this.featureName = featureName;
        this.lookback = lookback;
        this.mode = mode;
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
        return "WEEK_52_" + mode.name() + "_v1.0";
    }

    @Override
    public FeatureCategory getCategory() {
        return FeatureCategory.STATISTICAL;
    }

    @Override
    public int getRequiredLookback() {
        return lookback;
    }

    @Override
    public PriceSeriesType getRequiredPriceSeriesType() {
        return priceSeriesType;
    }

    @Override
    public TechnicalFeatureResult calculate(TechnicalFeatureContext context) {
        List<PriceBar> bars = context.getPriceBars();
        int size = bars.size();

        if (size < lookback) {
            return TechnicalFeatureResult.insufficientHistory(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), lookback, size
            );
        }

        double maxHigh = Double.NEGATIVE_INFINITY;
        double minLow = Double.POSITIVE_INFINITY;

        int startIdx = size - lookback;
        for (int i = startIdx; i < size; i++) {
            PriceBar bar = bars.get(i);
            double h = bar.getHigh(priceSeriesType).doubleValue();
            double l = bar.getLow(priceSeriesType).doubleValue();
            if (h > maxHigh) maxHigh = h;
            if (l < minLow) minLow = l;
        }

        BigDecimal currentPrice = context.getLatestBar().getPrice(priceSeriesType);
        if (currentPrice == null) {
            return TechnicalFeatureResult.error(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), "Null current price for 52-week calculation"
            );
        }

        double curr = currentPrice.doubleValue();
        double value;

        switch (mode) {
            case HIGH -> value = maxHigh;
            case LOW -> value = minLow;
            case POSITION -> {
                if (maxHigh == minLow) {
                    value = 0.5; // midpoint if flat
                } else {
                    value = (curr - minLow) / (maxHigh - minLow);
                    // clamp [0, 1]
                    value = Math.max(0.0, Math.min(1.0, value));
                }
            }
            case DISTANCE_FROM_HIGH -> value = (maxHigh > 0) ? ((curr - maxHigh) / maxHigh * 100.0) : 0.0;
            case DISTANCE_FROM_LOW -> value = (minLow > 0) ? ((curr - minLow) / minLow * 100.0) : 0.0;
            default -> value = 0.0;
        }

        BigDecimal result = BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
