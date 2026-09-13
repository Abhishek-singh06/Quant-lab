package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class RsiCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final int period;
    private final PriceSeriesType priceSeriesType;

    public RsiCalculator(int period) {
        this("RSI_" + period, period, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public RsiCalculator(String featureName, int period, PriceSeriesType priceSeriesType) {
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
        return "RSI_WILDER_v1.0";
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

        // Calculate differences
        double[] gains = new double[size];
        double[] losses = new double[size];

        for (int i = 1; i < size; i++) {
            BigDecimal prev = bars.get(i - 1).getPrice(priceSeriesType);
            BigDecimal curr = bars.get(i).getPrice(priceSeriesType);
            if (prev == null || curr == null) {
                return TechnicalFeatureResult.error(
                        featureName, context.getTargetDate(), context.getTargetTimestamp(),
                        getFeatureVersion(), "Null price found during RSI difference calculation"
                );
            }
            double change = curr.doubleValue() - prev.doubleValue();
            gains[i] = Math.max(change, 0.0);
            losses[i] = Math.max(-change, 0.0);
        }

        // First average gain and loss (SMA of initial N periods)
        double avgGain = 0.0;
        double avgLoss = 0.0;
        for (int i = 1; i <= period; i++) {
            avgGain += gains[i];
            avgLoss += losses[i];
        }
        avgGain /= period;
        avgLoss /= period;

        // Wilder Smoothing from period + 1 to the latest bar
        for (int i = period + 1; i < size; i++) {
            avgGain = (avgGain * (period - 1) + gains[i]) / period;
            avgLoss = (avgLoss * (period - 1) + losses[i]) / period;
        }

        double rsi;
        if (avgLoss == 0.0) {
            rsi = (avgGain == 0.0) ? 50.0 : 100.0;
        } else if (avgGain == 0.0) {
            rsi = 0.0;
        } else {
            double rs = avgGain / avgLoss;
            rsi = 100.0 - (100.0 / (1.0 + rs));
        }

        BigDecimal result = BigDecimal.valueOf(rsi).setScale(4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
