package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class DrawdownCalculator implements TechnicalFeatureCalculator {

    public enum Mode {
        CURRENT,
        MAX_ROLLING
    }

    private final String featureName;
    private final int window;
    private final Mode mode;
    private final PriceSeriesType priceSeriesType;

    public DrawdownCalculator(String featureName, Mode mode) {
        this(featureName, 252, mode, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public DrawdownCalculator(String featureName, int window, Mode mode, PriceSeriesType priceSeriesType) {
        this.featureName = featureName;
        this.window = window;
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
        return "DRAWDOWN_" + mode.name() + "_v1.0";
    }

    @Override
    public FeatureCategory getCategory() {
        return FeatureCategory.STATISTICAL;
    }

    @Override
    public int getRequiredLookback() {
        return window;
    }

    @Override
    public PriceSeriesType getRequiredPriceSeriesType() {
        return priceSeriesType;
    }

    @Override
    public TechnicalFeatureResult calculate(TechnicalFeatureContext context) {
        List<PriceBar> bars = context.getPriceBars();
        int size = bars.size();

        if (size < window) {
            return TechnicalFeatureResult.insufficientHistory(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), window, size
            );
        }

        int startIdx = size - window;
        double peak = Double.NEGATIVE_INFINITY;
        double maxDrawdown = 0.0; // Drawdown is <= 0

        for (int i = startIdx; i < size; i++) {
            BigDecimal price = bars.get(i).getPrice(priceSeriesType);
            if (price == null) {
                return TechnicalFeatureResult.error(
                        featureName, context.getTargetDate(), context.getTargetTimestamp(),
                        getFeatureVersion(), "Null price found during drawdown calculation"
                );
            }
            double p = price.doubleValue();
            if (p > peak) {
                peak = p;
            }
            double dd = (peak > 0) ? ((p - peak) / peak * 100.0) : 0.0;
            if (dd < maxDrawdown) {
                maxDrawdown = dd;
            }
        }

        double value;
        if (mode == Mode.CURRENT) {
            BigDecimal currentPrice = context.getLatestBar().getPrice(priceSeriesType);
            double curr = currentPrice.doubleValue();
            value = (peak > 0) ? ((curr - peak) / peak * 100.0) : 0.0;
        } else {
            value = maxDrawdown;
        }

        BigDecimal result = BigDecimal.valueOf(value).setScale(4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
