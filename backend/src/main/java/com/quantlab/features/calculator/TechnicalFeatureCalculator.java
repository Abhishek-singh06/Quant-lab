package com.quantlab.features.calculator;

import com.quantlab.features.model.FeatureCategory;
import com.quantlab.features.model.PriceSeriesType;
import com.quantlab.features.model.TechnicalFeatureContext;
import com.quantlab.features.model.TechnicalFeatureResult;

import java.util.List;

/**
 * Common extensible interface for deterministic, point-in-time technical feature calculators.
 */
public interface TechnicalFeatureCalculator {

    String getFeatureName();

    String getFeatureVersion();

    String getFormulaVersion();

    FeatureCategory getCategory();

    int getRequiredLookback();

    PriceSeriesType getRequiredPriceSeriesType();

    /**
     * Calculates the feature value for the context's target bar.
     * The context contains all historical price bars up to target time T.
     */
    TechnicalFeatureResult calculate(TechnicalFeatureContext context);
}
