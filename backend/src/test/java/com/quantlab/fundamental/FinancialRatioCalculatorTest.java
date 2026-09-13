package com.quantlab.fundamental;

import com.quantlab.fundamental.entity.FinancialRatio;
import com.quantlab.fundamental.entity.FinancialStatement;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import com.quantlab.fundamental.service.FinancialRatioCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinancialRatioCalculatorTest {

    private FinancialRatioCalculatorService ratioService;

    @BeforeEach
    void setUp() {
        ratioService = new FinancialRatioCalculatorService();
    }

    @Test
    void testComputeRatiosProducesAccurateMetrics() {
        FinancialStatement current = new FinancialStatement();
        current.setFilingId(101L);
        current.setInstrumentId(1L);
        current.setSymbol("RELIANCE");
        current.setPeriodEnd(LocalDate.of(2025, 3, 31));
        current.setPeriodType(PeriodType.ANNUAL);
        current.setReportingBasis(ReportingBasis.CONSOLIDATED);
        current.setAvailableAt(Instant.parse("2025-05-15T12:30:00Z"));

        current.setRevenue(new BigDecimal("100000.00"));
        current.setEbitda(new BigDecimal("20000.00"));
        current.setEbit(new BigDecimal("15000.00"));
        current.setInterestExpense(new BigDecimal("3000.00"));
        current.setNetProfit(new BigDecimal("10000.00"));
        current.setTotalAssets(new BigDecimal("200000.00"));
        current.setCurrentAssets(new BigDecimal("50000.00"));
        current.setCurrentLiabilities(new BigDecimal("25000.00"));
        current.setTotalEquity(new BigDecimal("100000.00"));
        current.setTotalDebt(new BigDecimal("40000.00"));
        current.setCashAndEquivalents(new BigDecimal("10000.00"));
        current.setBasicEps(new BigDecimal("100.00"));

        FinancialStatement prior = new FinancialStatement();
        prior.setRevenue(new BigDecimal("80000.00"));
        prior.setEbitda(new BigDecimal("16000.00"));
        prior.setNetProfit(new BigDecimal("8000.00"));
        prior.setBasicEps(new BigDecimal("80.00"));

        BigDecimal price = new BigDecimal("2500.00");
        Long totalShares = 1000000000L;
        BigDecimal mcap = new BigDecimal("250000.00"); // 250k Cr

        FinancialRatio ratios = ratioService.computeRatios(current, prior, price, totalShares, mcap);

        assertNotNull(ratios);
        assertEquals(new BigDecimal("20.0000"), ratios.getEbitdaMargin());
        assertEquals(new BigDecimal("15.0000"), ratios.getEbitMargin());
        assertEquals(new BigDecimal("10.0000"), ratios.getNetProfitMargin());
        assertEquals(new BigDecimal("10.0000"), ratios.getRoe()); // 10k / 100k
        assertEquals(new BigDecimal("0.4000"), ratios.getDebtToEquity()); // 40k / 100k
        assertEquals(new BigDecimal("5.0000"), ratios.getInterestCoverage()); // 15k / 3k
        assertEquals(new BigDecimal("2.0000"), ratios.getCurrentRatio()); // 50k / 25k

        // YoY Growth
        assertEquals(new BigDecimal("25.0000"), ratios.getRevenueGrowthYoY()); // (100k - 80k) / 80k
        assertEquals(new BigDecimal("25.0000"), ratios.getProfitGrowthYoY()); // (10k - 8k) / 8k

        // Valuation
        assertEquals(new BigDecimal("25.00"), ratios.getPeRatio()); // 250k / 10k
        assertEquals(new BigDecimal("2.50"), ratios.getPbRatio()); // 250k / 100k
    }
}
