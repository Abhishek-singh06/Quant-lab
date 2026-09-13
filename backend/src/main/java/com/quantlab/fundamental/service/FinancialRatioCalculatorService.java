package com.quantlab.fundamental.service;

import com.quantlab.fundamental.entity.FinancialRatio;
import com.quantlab.fundamental.entity.FinancialStatement;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Transparent, deterministic financial ratio calculation engine.
 * Computes profitability, quality, solvency, coverage, growth, and valuation metrics with documented formulas.
 */
@Service
public class FinancialRatioCalculatorService {

    public static final String METHODOLOGY_VERSION = "1.0.0";

    public FinancialRatio computeRatios(
            FinancialStatement current,
            FinancialStatement priorYear,
            BigDecimal currentPrice,
            Long totalShares,
            BigDecimal currentMarketCap) {

        FinancialRatio r = new FinancialRatio();
        r.setFilingId(current.getFilingId());
        r.setInstrumentId(current.getInstrumentId());
        r.setSymbol(current.getSymbol());
        r.setPeriodEnd(current.getPeriodEnd());
        r.setPeriodType(current.getPeriodType());
        r.setReportingBasis(current.getReportingBasis());
        r.setAvailableAt(current.getAvailableAt());
        r.setMethodologyVersion(METHODOLOGY_VERSION);
        r.setCalculatedAt(Instant.now());

        BigDecimal rev = current.getRevenue();
        BigDecimal ebitda = current.getEbitda();
        BigDecimal ebit = current.getEbit();
        BigDecimal pat = current.getNetProfit();
        BigDecimal totalDebt = current.getTotalDebt() != null ? current.getTotalDebt() : BigDecimal.ZERO;
        BigDecimal cash = current.getCashAndEquivalents() != null ? current.getCashAndEquivalents() : BigDecimal.ZERO;
        BigDecimal equity = current.getTotalEquity();
        BigDecimal assets = current.getTotalAssets();
        BigDecimal currentAssets = current.getCurrentAssets();
        BigDecimal currentLiab = current.getCurrentLiabilities();
        BigDecimal interest = current.getInterestExpense();

        // Margins
        if (rev != null && rev.compareTo(BigDecimal.ZERO) > 0) {
            if (ebitda != null) {
                r.setEbitdaMargin(ebitda.divide(rev, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
            }
            if (ebit != null) {
                r.setEbitMargin(ebit.divide(rev, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
            }
            if (pat != null) {
                r.setNetProfitMargin(pat.divide(rev, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
            }
        }

        // ROE = Net Profit / Equity (or Avg Equity)
        if (pat != null && equity != null && equity.compareTo(BigDecimal.ZERO) > 0) {
            r.setRoe(pat.divide(equity, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
        }

        // Capital Employed = Total Assets - Current Liabilities (or Equity + Total Debt)
        BigDecimal capitalEmployed = null;
        if (assets != null && currentLiab != null) {
            capitalEmployed = assets.subtract(currentLiab);
        } else if (equity != null) {
            capitalEmployed = equity.add(totalDebt);
        }

        // ROCE = EBIT / Capital Employed
        if (ebit != null && capitalEmployed != null && capitalEmployed.compareTo(BigDecimal.ZERO) > 0) {
            r.setRoce(ebit.divide(capitalEmployed, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
        }

        // ROA = Net Profit / Total Assets
        if (pat != null && assets != null && assets.compareTo(BigDecimal.ZERO) > 0) {
            r.setRoa(pat.divide(assets, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
        }

        // Asset Turnover = Revenue / Total Assets
        if (rev != null && assets != null && assets.compareTo(BigDecimal.ZERO) > 0) {
            r.setAssetTurnover(rev.divide(assets, 4, RoundingMode.HALF_UP));
        }

        // Solvency & Coverage
        if (totalDebt != null && equity != null && equity.compareTo(BigDecimal.ZERO) > 0) {
            r.setDebtToEquity(totalDebt.divide(equity, 4, RoundingMode.HALF_UP));
        }

        BigDecimal netDebt = totalDebt.subtract(cash);
        if (ebitda != null && ebitda.compareTo(BigDecimal.ZERO) > 0) {
            r.setNetDebtToEbitda(netDebt.divide(ebitda, 4, RoundingMode.HALF_UP));
        }

        if (ebit != null && interest != null && interest.compareTo(BigDecimal.ZERO) > 0) {
            r.setInterestCoverage(ebit.divide(interest, 4, RoundingMode.HALF_UP));
        }

        if (currentAssets != null && currentLiab != null && currentLiab.compareTo(BigDecimal.ZERO) > 0) {
            r.setCurrentRatio(currentAssets.divide(currentLiab, 4, RoundingMode.HALF_UP));
        }

        // YoY Growth
        if (priorYear != null) {
            if (rev != null && priorYear.getRevenue() != null && priorYear.getRevenue().compareTo(BigDecimal.ZERO) > 0) {
                r.setRevenueGrowthYoY(rev.subtract(priorYear.getRevenue())
                        .divide(priorYear.getRevenue(), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
            }
            if (ebitda != null && priorYear.getEbitda() != null && priorYear.getEbitda().compareTo(BigDecimal.ZERO) > 0) {
                r.setEbitdaGrowthYoY(ebitda.subtract(priorYear.getEbitda())
                        .divide(priorYear.getEbitda(), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
            }
            if (pat != null && priorYear.getNetProfit() != null && priorYear.getNetProfit().compareTo(BigDecimal.ZERO) > 0) {
                r.setProfitGrowthYoY(pat.subtract(priorYear.getNetProfit())
                        .divide(priorYear.getNetProfit(), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
            }
            if (current.getBasicEps() != null && priorYear.getBasicEps() != null && priorYear.getBasicEps().compareTo(BigDecimal.ZERO) > 0) {
                r.setEpsGrowthYoY(current.getBasicEps().subtract(priorYear.getBasicEps())
                        .divide(priorYear.getBasicEps(), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(4, RoundingMode.HALF_UP));
            }
        }

        // Valuation Ratios (Point-in-Time)
        if (currentMarketCap != null && currentMarketCap.compareTo(BigDecimal.ZERO) > 0) {
            r.setMarketCapCrores(currentMarketCap);
            BigDecimal ev = currentMarketCap.add(netDebt);
            r.setEnterpriseValueCrores(ev);

            if (pat != null && pat.compareTo(BigDecimal.ZERO) > 0) {
                r.setPeRatio(currentMarketCap.divide(pat, 2, RoundingMode.HALF_UP));
            }
            if (equity != null && equity.compareTo(BigDecimal.ZERO) > 0) {
                r.setPbRatio(currentMarketCap.divide(equity, 2, RoundingMode.HALF_UP));
            }
            if (ebitda != null && ebitda.compareTo(BigDecimal.ZERO) > 0) {
                r.setEvEbitda(ev.divide(ebitda, 2, RoundingMode.HALF_UP));
            }
            if (rev != null && rev.compareTo(BigDecimal.ZERO) > 0) {
                r.setEvSales(ev.divide(rev, 2, RoundingMode.HALF_UP));
            }
        }

        return r;
    }
}
