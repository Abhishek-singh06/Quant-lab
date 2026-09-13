package com.quantlab.monitoring;

import com.quantlab.monitoring.service.FeatureDriftCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureDriftCalculatorTest {

    private FeatureDriftCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FeatureDriftCalculator();
    }

    @Test
    void testPsiIdenticalDistributionsReturnsNormal() {
        double[] baseline = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[] current = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};

        FeatureDriftCalculator.DriftResult result = calculator.calculatePsi(baseline, current, 5, 0.25);
        assertEquals("PSI", result.metricType());
        assertTrue(result.observedValue() < 0.10, "Identical distributions should have PSI < 0.10");
        assertEquals("NORMAL", result.driftStatus());
    }

    @Test
    void testPsiDriftedDistributionReturnsCritical() {
        double[] baseline = {1.0, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 5.5, 6.0};
        double[] current = {20.0, 22.0, 25.0, 30.0, 35.0, 40.0, 45.0, 50.0, 55.0, 60.0};

        FeatureDriftCalculator.DriftResult result = calculator.calculatePsi(baseline, current, 5, 0.25);
        assertEquals("PSI", result.metricType());
        assertTrue(result.observedValue() >= 0.25, "Heavily drifted distribution should have PSI >= 0.25");
        assertEquals("CRITICAL", result.driftStatus());
    }

    @Test
    void testKsTestIdenticalDistributions() {
        double[] baseline = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] current = {1.0, 2.0, 3.0, 4.0, 5.0};

        FeatureDriftCalculator.DriftResult result = calculator.calculateKsTest(baseline, current, 0.30);
        assertEquals("KS_TEST", result.metricType());
        assertEquals(0.0, result.observedValue(), 1e-4);
        assertEquals("NORMAL", result.driftStatus());
    }

    @Test
    void testKsTestShiftedDistributionTriggersWarningOrCritical() {
        double[] baseline = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] current = {10.0, 12.0, 13.0, 14.0, 15.0};

        FeatureDriftCalculator.DriftResult result = calculator.calculateKsTest(baseline, current, 0.50);
        assertEquals("KS_TEST", result.metricType());
        assertTrue(result.observedValue() >= 0.50);
        assertEquals("CRITICAL", result.driftStatus());
    }

    @Test
    void testWassersteinDistance() {
        double[] baseline = {0.0, 1.0, 2.0, 3.0, 4.0};
        double[] current = {5.0, 6.0, 7.0, 8.0, 9.0};

        FeatureDriftCalculator.DriftResult result = calculator.calculateWassersteinDistance(baseline, current, 3.0);
        assertEquals("WASSERSTEIN", result.metricType());
        assertEquals(5.0, result.observedValue(), 0.1);
        assertEquals("CRITICAL", result.driftStatus());
    }

    @Test
    void testMeanDifference() {
        double[] baseline = {10.0, 10.0, 10.0, 10.0, 10.0};
        double[] current = {10.0, 10.0, 10.0, 10.0, 10.0};

        FeatureDriftCalculator.DriftResult result = calculator.calculateMeanDifference(baseline, current, 2.0);
        assertEquals("MEAN_DIFF", result.metricType());
        assertEquals(0.0, result.observedValue(), 1e-4);
        assertEquals("NORMAL", result.driftStatus());
    }
}
