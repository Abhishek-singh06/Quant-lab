package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class MacdCalculator implements TechnicalFeatureCalculator {

    public enum Component {
        MACD_LINE,
        SIGNAL_LINE,
        HISTOGRAM
    }

    private final String featureName;
    private final int fastPeriod;
    private final int slowPeriod;
    private final int signalPeriod;
    private final Component component;
    private final PriceSeriesType priceSeriesType;

    public MacdCalculator(String featureName, int fastPeriod, int slowPeriod, int signalPeriod,
                          Component component, PriceSeriesType priceSeriesType) {
        this.featureName = featureName;
        this.fastPeriod = fastPeriod;
        this.slowPeriod = slowPeriod;
        this.signalPeriod = signalPeriod;
        this.component = component;
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
        return "MACD_" + fastPeriod + "_" + slowPeriod + "_" + signalPeriod + "_v1.0";
    }

    @Override
    public FeatureCategory getCategory() {
        return FeatureCategory.TREND;
    }

    @Override
    public int getRequiredLookback() {
        return slowPeriod + signalPeriod;
    }

    @Override
    public PriceSeriesType getRequiredPriceSeriesType() {
        return priceSeriesType;
    }

    @Override
    public TechnicalFeatureResult calculate(TechnicalFeatureContext context) {
        List<PriceBar> bars = context.getPriceBars();
        int size = bars.size();
        int minRequired = (component == Component.MACD_LINE) ? slowPeriod : (slowPeriod + signalPeriod - 1);

        if (size < minRequired) {
            return TechnicalFeatureResult.insufficientHistory(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), minRequired, size
            );
        }

        // Compute Fast EMA and Slow EMA series
        double fastAlpha = 2.0 / (fastPeriod + 1.0);
        double slowAlpha = 2.0 / (slowPeriod + 1.0);

        // Compute fast EMA series
        double[] fastEma = computeEmaSeries(bars, fastPeriod, fastAlpha);
        // Compute slow EMA series
        double[] slowEma = computeEmaSeries(bars, slowPeriod, slowAlpha);

        // MACD Line = Fast EMA - Slow EMA (valid from index slowPeriod - 1)
        List<Double> macdLineSeries = new ArrayList<>();
        List<Integer> validIndices = new ArrayList<>();

        for (int i = slowPeriod - 1; i < size; i++) {
            double macdVal = fastEma[i] - slowEma[i];
            macdLineSeries.add(macdVal);
            validIndices.add(i);
        }

        if (component == Component.MACD_LINE) {
            double latestMacd = macdLineSeries.get(macdLineSeries.size() - 1);
            BigDecimal result = BigDecimal.valueOf(latestMacd).setScale(4, RoundingMode.HALF_UP);
            return TechnicalFeatureResult.success(
                    featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), getFormulaVersion()
            );
        }

        // Calculate Signal Line (EMA of MACD Line over signalPeriod)
        if (macdLineSeries.size() < signalPeriod) {
            return TechnicalFeatureResult.insufficientHistory(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), slowPeriod + signalPeriod, size
            );
        }

        double signalAlpha = 2.0 / (signalPeriod + 1.0);
        double sum = 0.0;
        for (int i = 0; i < signalPeriod; i++) {
            sum += macdLineSeries.get(i);
        }
        double signalEma = sum / signalPeriod;

        for (int i = signalPeriod; i < macdLineSeries.size(); i++) {
            signalEma = (macdLineSeries.get(i) - signalEma) * signalAlpha + signalEma;
        }

        double latestMacd = macdLineSeries.get(macdLineSeries.size() - 1);

        double finalValue = (component == Component.SIGNAL_LINE) ? signalEma : (latestMacd - signalEma);
        BigDecimal result = BigDecimal.valueOf(finalValue).setScale(4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }

    private double[] computeEmaSeries(List<PriceBar> bars, int period, double alpha) {
        int size = bars.size();
        double[] ema = new double[size];

        double sum = 0.0;
        for (int i = 0; i < period; i++) {
            sum += bars.get(i).getPrice(priceSeriesType).doubleValue();
        }
        double currentEma = sum / period;
        ema[period - 1] = currentEma;

        for (int i = period; i < size; i++) {
            double p = bars.get(i).getPrice(priceSeriesType).doubleValue();
            currentEma = (p - currentEma) * alpha + currentEma;
            ema[i] = currentEma;
        }
        return ema;
    }
}
