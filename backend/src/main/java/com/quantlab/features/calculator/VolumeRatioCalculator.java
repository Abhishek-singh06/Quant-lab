package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class VolumeRatioCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final int period;

    public VolumeRatioCalculator(int period) {
        this("VOLUME_RATIO_" + period, period);
    }

    public VolumeRatioCalculator(String featureName, int period) {
        this.featureName = featureName;
        this.period = period;
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
        return "VOLUME_RATIO_v1.0";
    }

    @Override
    public FeatureCategory getCategory() {
        return FeatureCategory.VOLUME;
    }

    @Override
    public int getRequiredLookback() {
        return period;
    }

    @Override
    public PriceSeriesType getRequiredPriceSeriesType() {
        return PriceSeriesType.RAW;
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

        long currentVol = bars.get(size - 1).getVolume();
        long sumVol = 0;

        for (int i = size - period; i < size; i++) {
            sumVol += bars.get(i).getVolume();
        }

        double avgVol = (double) sumVol / period;
        if (avgVol <= 0.0) {
            return TechnicalFeatureResult.success(
                    featureName, BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                    context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), getFormulaVersion()
            );
        }

        double ratio = currentVol / avgVol;
        BigDecimal result = BigDecimal.valueOf(ratio).setScale(4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
