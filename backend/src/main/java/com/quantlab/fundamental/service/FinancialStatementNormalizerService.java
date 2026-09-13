package com.quantlab.fundamental.service;

import com.quantlab.fundamental.entity.FinancialStatement;
import com.quantlab.fundamental.entity.FundamentalFiling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Normalizes financial statements, validates accounting integrity,
 * and assigns data quality confidence levels.
 */
@Service
public class FinancialStatementNormalizerService {

    private static final Logger log = LoggerFactory.getLogger(FinancialStatementNormalizerService.class);

    public String validateAndAssignQuality(FundamentalFiling filing, FinancialStatement statement) {
        if (filing == null || statement == null) {
            return "LOW";
        }

        // 1. Period Date Check
        if (filing.getPeriodStart() == null || filing.getPeriodEnd() == null ||
            !filing.getPeriodStart().isBefore(filing.getPeriodEnd())) {
            log.warn("Invalid period dates for filing {}", filing.getId());
            return "LOW";
        }

        // 2. Revenue and Profit Availability
        if (statement.getRevenue() == null || statement.getNetProfit() == null) {
            return "MEDIUM";
        }

        // 3. Balance Sheet Consistency Check (Assets ≈ Liabilities + Equity)
        BigDecimal assets = statement.getTotalAssets();
        BigDecimal liab = statement.getTotalLiabilities();
        BigDecimal equity = statement.getTotalEquity();

        if (assets != null && liab != null && equity != null) {
            BigDecimal liabPlusEquity = liab.add(equity);
            BigDecimal diff = assets.subtract(liabPlusEquity).abs();
            if (assets.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pctDiff = diff.divide(assets, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100.00"));
                if (pctDiff.compareTo(new BigDecimal("5.0")) > 0) {
                    log.info("Balance sheet discrepancy for {}: Assets={}, Liab+Equity={}, Diff%={}",
                        statement.getSymbol(), assets, liabPlusEquity, pctDiff);
                    return "MEDIUM";
                }
            }
        }

        return "HIGH";
    }
}
