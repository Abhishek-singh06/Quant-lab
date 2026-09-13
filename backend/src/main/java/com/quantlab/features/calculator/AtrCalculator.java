package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class AtrCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final int period;
    private final boolean isPercent;
    private final PriceSeriesType priceSeriesType;

    public AtrCalculator(int period, boolean isPercent) {
        this(isPercent ? "ATR_PERCENT_" + period : "ATR_" + period, period, isPercent, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public AtrCalculator(String featureName, int period, boolean isPercent, PriceSeriesType priceSeriesType) {
        this.featureName = featureName;
        this.period = period;
        this.isPercent = isPercent;
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
        return isPercent ? "ATR_PERCENT_WILDER_v1.0" : "ATR_WILDER_v1.0";
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

        double[] tr = new double[size];
        for (int i = 1; i < size; i++) {
            PriceBar curr = bars.get(i);
            PriceBar prev = bars.get(i - 1);

            double h = curr.getHigh(priceSeriesType).doubleValue();
            double l = curr.getLow(priceSeriesType).doubleValue();
            double prevClose = prev.getPrice(priceSeriesType).doubleValue();

            double tr1 = h - l;
            double tr2 = Math.abs(h - prevClose);
            double tr3 = Math.abs(l - prevClose);

            tr[i] = Math.max(tr1, Math.max(tr2, tr3));
        }

        // First ATR is SMA of TR over first 'period' bars
        double sum = 0.0;
        for (int i = 1; i <= period; i++) {
            sum += tr[i];
        }
        double atr = sum / period;

        // Wilder smoothing
        for (int i = period + 1; i < size; i++) {
            atr = (atr * (period - 1) + tr[i]) / period;
        }

        BigDecimal finalVal;
        if (isPercent) {
            BigDecimal currPrice = context.getLatestBar().getPrice(priceSeriesType);
            if (currPrice == null || currPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return TechnicalFeatureResult.error(
                        featureName, context.getTargetDate(), context.getTargetTimestamp(),
                        getFeatureVersion(), "Invalid or zero price for ATR percentage"
                );
            }
            double pct = (atr / currPrice.doubleValue()) * 100.0;
            finalVal = BigDecimal.valueOf(pct).setScale(4, RoundingMode.HALF_UP);
        } else {
            finalVal = BigDecimal.valueOf(atr).setScale(4, RoundingMode.HALF_UP);
        }

        return TechnicalFeatureResult.success(
                featureName, finalVal, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
