package com.quantlab.features;

import com.quantlab.features.calculator.*;
import com.quantlab.features.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TechnicalFeatureCalculatorsTest {

    private List<PriceBar> testBars;
    private List<PriceBar> benchmarkBars;

    @BeforeEach
    void setUp() {
        testBars = new ArrayList<>();
        benchmarkBars = new ArrayList<>();
        LocalDate start = LocalDate.of(2025, 1, 1);

        double[] prices = {
                100.0, 102.0, 101.0, 104.0, 105.0,
                107.0, 106.0, 108.0, 110.0, 109.0,
                111.0, 113.0, 112.0, 115.0, 116.0,
                118.0, 117.0, 120.0, 122.0, 121.0,
                125.0, 124.0, 126.0, 128.0, 130.0
        };

        for (int i = 0; i < prices.length; i++) {
            LocalDate d = start.plusDays(i);
            Instant ts = d.atTime(15, 30).toInstant(java.time.ZoneOffset.UTC);
            BigDecimal p = BigDecimal.valueOf(prices[i]);
            BigDecimal h = p.multiply(BigDecimal.valueOf(1.01));
            BigDecimal l = p.multiply(BigDecimal.valueOf(0.99));
            long vol = 100000L + i * 1000L;

            testBars.add(new PriceBar(d, ts, p, h, l, p, vol, p, h, l, p, p));

            // Benchmark (e.g. constant upward drift)
            BigDecimal bp = BigDecimal.valueOf(20000.0 + i * 100.0);
            benchmarkBars.add(new PriceBar(d, ts, bp, bp, bp, bp, 500000L, bp, bp, bp, bp, bp));
        }
    }

    private TechnicalFeatureContext createContext(List<PriceBar> bars, List<PriceBar> bench) {
        PriceBar latest = bars.get(bars.size() - 1);
        return new TechnicalFeatureContext(
                1L, "TEST_SYM", latest.getTradingDate(), latest.getTimestamp(),
                Timeframe.ONE_DAY, bars, bench, Collections.emptyMap()
        );
    }

    @Test
    void testReturnsCalculator() {
        ReturnsCalculator simpleCalc = new ReturnsCalculator("RETURN_5D", 5, false, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = simpleCalc.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        // Last bar price: 130.0, 5 bars prior: 121.0 -> (130 - 121) / 121 = 0.07438017
        assertEquals(new BigDecimal("0.07438017"), res.getValue());
    }

    @Test
    void testSmaCalculator() {
        SmaCalculator sma5 = new SmaCalculator("SMA_5", 5, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = sma5.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        // Last 5 prices: 125, 124, 126, 128, 130 -> Avg = 126.6000
        assertEquals(new BigDecimal("126.6000"), res.getValue());
    }

    @Test
    void testEmaCalculator() {
        EmaCalculator ema5 = new EmaCalculator("EMA_5", 5, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = ema5.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        assertNotNull(res.getValue());
        assertTrue(res.getValue().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testRsiCalculatorWithinBounds() {
        RsiCalculator rsi14 = new RsiCalculator("RSI_14", 14, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = rsi14.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        double val = res.getValue().doubleValue();
        assertTrue(val >= 0.0 && val <= 100.0, "RSI must be between 0 and 100, got " + val);
    }

    @Test
    void testMacdCalculator() {
        MacdCalculator macdLine = new MacdCalculator("MACD_LINE", 5, 10, 3, MacdCalculator.Component.MACD_LINE, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = macdLine.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        assertNotNull(res.getValue());
    }

    @Test
    void testAtrCalculator() {
        AtrCalculator atr14 = new AtrCalculator("ATR_14", 14, false, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = atr14.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        assertTrue(res.getValue().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testVolatilityCalculator() {
        VolatilityCalculator vol10 = new VolatilityCalculator("VOLATILITY_10D", 10, true, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = vol10.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        assertTrue(res.getValue().compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testVolumeRatioCalculator() {
        VolumeRatioCalculator vr5 = new VolumeRatioCalculator("VOLUME_RATIO_5", 5);
        TechnicalFeatureResult res = vr5.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        assertTrue(res.getValue().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testRelativeStrengthCalculator() {
        RelativeStrengthCalculator rs5 = new RelativeStrengthCalculator("RS_BENCH_5", "BENCH", 5, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = rs5.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        assertNotNull(res.getValue());
    }

    @Test
    void testWeek52PositionCalculator() {
        Week52PositionCalculator posCalc = new Week52PositionCalculator("WEEK_52_POSITION", 20, Week52PositionCalculator.Mode.POSITION, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = posCalc.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        double pos = res.getValue().doubleValue();
        assertTrue(pos >= 0.0 && pos <= 1.0, "52W Position must be in [0, 1], got " + pos);
    }

    @Test
    void testDrawdownCalculator() {
        DrawdownCalculator ddCalc = new DrawdownCalculator("DRAWDOWN", 20, DrawdownCalculator.Mode.CURRENT, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = ddCalc.calculate(createContext(testBars, benchmarkBars));

        assertTrue(res.isValid());
        assertTrue(res.getValue().compareTo(BigDecimal.ZERO) <= 0, "Drawdown must be <= 0, got " + res.getValue());
    }

    @Test
    void testInsufficientHistoryGracefulHandling() {
        SmaCalculator sma50 = new SmaCalculator("SMA_50", 50, PriceSeriesType.SPLIT_ADJUSTED);
        TechnicalFeatureResult res = sma50.calculate(createContext(testBars, benchmarkBars));

        assertFalse(res.isValid());
        assertEquals(ValidationStatus.INSUFFICIENT_HISTORY, res.getStatus());
        assertNull(res.getValue());
    }
}
