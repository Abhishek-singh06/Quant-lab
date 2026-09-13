package com.quantlab.features.calculator;

import com.quantlab.features.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class RelativeStrengthCalculator implements TechnicalFeatureCalculator {

    private final String featureName;
    private final String benchmarkSymbol;
    private final int period;
    private final PriceSeriesType priceSeriesType;

    public RelativeStrengthCalculator(String benchmarkSymbol, int period) {
        this("RS_" + benchmarkSymbol.replace(" ", "_") + "_" + period, benchmarkSymbol, period, PriceSeriesType.SPLIT_ADJUSTED);
    }

    public RelativeStrengthCalculator(String featureName, String benchmarkSymbol, int period, PriceSeriesType priceSeriesType) {
        this.featureName = featureName;
        this.benchmarkSymbol = benchmarkSymbol;
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
        return "EXCESS_RETURN_SPREAD_v1.0";
    }

    @Override
    public FeatureCategory getCategory() {
        return FeatureCategory.RELATIVE_STRENGTH;
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
        List<PriceBar> stockBars = context.getPriceBars();
        List<PriceBar> benchBars = context.getBenchmarkBars();

        int stockSize = stockBars.size();
        int benchSize = benchBars != null ? benchBars.size() : 0;

        if (stockSize < period + 1 || benchSize < period + 1) {
            return TechnicalFeatureResult.insufficientHistory(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), period + 1, Math.min(stockSize, benchSize)
            );
        }

        // Stock Return over N days
        BigDecimal stockCurr = stockBars.get(stockSize - 1).getPrice(priceSeriesType);
        BigDecimal stockPrior = stockBars.get(stockSize - 1 - period).getPrice(priceSeriesType);

        // Benchmark Return over N days
        BigDecimal benchCurr = benchBars.get(benchSize - 1).getPrice(priceSeriesType);
        BigDecimal benchPrior = benchBars.get(benchSize - 1 - period).getPrice(priceSeriesType);

        if (stockPrior == null || stockPrior.compareTo(BigDecimal.ZERO) <= 0 ||
            benchPrior == null || benchPrior.compareTo(BigDecimal.ZERO) <= 0 ||
            stockCurr == null || benchCurr == null) {
            return TechnicalFeatureResult.error(
                    featureName, context.getTargetDate(), context.getTargetTimestamp(),
                    getFeatureVersion(), "Invalid prices for relative strength calculation"
            );
        }

        double stockReturn = (stockCurr.doubleValue() - stockPrior.doubleValue()) / stockPrior.doubleValue();
        double benchReturn = (benchCurr.doubleValue() - benchPrior.doubleValue()) / benchPrior.doubleValue();

        // Excess return spread in percentage points: (Stock Return - Benchmark Return) * 100
        double spread = (stockReturn - benchReturn) * 100.0;
        BigDecimal result = BigDecimal.valueOf(spread).setScale(4, RoundingMode.HALF_UP);

        return TechnicalFeatureResult.success(
                featureName, result, context.getTargetDate(), context.getTargetTimestamp(),
                getFeatureVersion(), getFormulaVersion()
        );
    }
}
