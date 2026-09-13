package com.quantlab.fundamental.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class FinancialRatiosDTO {

    private String symbol;
    private LocalDate periodEnd;
    private PeriodType periodType;
    private ReportingBasis reportingBasis;
    private Instant availableAt;

    // Profitability & Quality
    private BigDecimal grossMargin;
    private BigDecimal ebitdaMargin;
    private BigDecimal ebitMargin;
    private BigDecimal netProfitMargin;
    private BigDecimal roe; // Net Income / Avg Equity
    private BigDecimal roce; // EBIT / Capital Employed
    private BigDecimal roa;
    private BigDecimal assetTurnover;

    // Solvency & Coverage
    private BigDecimal debtToEquity;
    private BigDecimal netDebtToEbitda;
    private BigDecimal interestCoverage;
    private BigDecimal currentRatio;

    // Growth (YoY)
    private BigDecimal revenueGrowthYoY;
    private BigDecimal ebitdaGrowthYoY;
    private BigDecimal profitGrowthYoY;
    private BigDecimal epsGrowthYoY;
    private BigDecimal fcfGrowthYoY;

    // Valuation Ratios (Point-in-Time)
    private BigDecimal marketCapCrores;
    private BigDecimal enterpriseValueCrores;
    private BigDecimal peRatio;
    private BigDecimal pbRatio;
    private BigDecimal evEbitda;
    private BigDecimal evSales;
    private BigDecimal dividendYield;
    private BigDecimal payoutRatio;

    private String methodologyVersion;
    private Instant calculatedAt;

    public FinancialRatiosDTO() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public PeriodType getPeriodType() { return periodType; }
    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }
    public ReportingBasis getReportingBasis() { return reportingBasis; }
    public void setReportingBasis(ReportingBasis reportingBasis) { this.reportingBasis = reportingBasis; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public BigDecimal getGrossMargin() { return grossMargin; }
    public void setGrossMargin(BigDecimal grossMargin) { this.grossMargin = grossMargin; }
    public BigDecimal getEbitdaMargin() { return ebitdaMargin; }
    public void setEbitdaMargin(BigDecimal ebitdaMargin) { this.ebitdaMargin = ebitdaMargin; }
    public BigDecimal getEbitMargin() { return ebitMargin; }
    public void setEbitMargin(BigDecimal ebitMargin) { this.ebitMargin = ebitMargin; }
    public BigDecimal getNetProfitMargin() { return netProfitMargin; }
    public void setNetProfitMargin(BigDecimal netProfitMargin) { this.netProfitMargin = netProfitMargin; }
    public BigDecimal getRoe() { return roe; }
    public void setRoe(BigDecimal roe) { this.roe = roe; }
    public BigDecimal getRoce() { return roce; }
    public void setRoce(BigDecimal roce) { this.roce = roce; }
    public BigDecimal getRoa() { return roa; }
    public void setRoa(BigDecimal roa) { this.roa = roa; }
    public BigDecimal getAssetTurnover() { return assetTurnover; }
    public void setAssetTurnover(BigDecimal assetTurnover) { this.assetTurnover = assetTurnover; }
    public BigDecimal getDebtToEquity() { return debtToEquity; }
    public void setDebtToEquity(BigDecimal debtToEquity) { this.debtToEquity = debtToEquity; }
    public BigDecimal getNetDebtToEbitda() { return netDebtToEbitda; }
    public void setNetDebtToEbitda(BigDecimal netDebtToEbitda) { this.netDebtToEbitda = netDebtToEbitda; }
    public BigDecimal getInterestCoverage() { return interestCoverage; }
    public void setInterestCoverage(BigDecimal interestCoverage) { this.interestCoverage = interestCoverage; }
    public BigDecimal getCurrentRatio() { return currentRatio; }
    public void setCurrentRatio(BigDecimal currentRatio) { this.currentRatio = currentRatio; }
    public BigDecimal getRevenueGrowthYoY() { return revenueGrowthYoY; }
    public void setRevenueGrowthYoY(BigDecimal revenueGrowthYoY) { this.revenueGrowthYoY = revenueGrowthYoY; }
    public BigDecimal getEbitdaGrowthYoY() { return ebitdaGrowthYoY; }
    public void setEbitdaGrowthYoY(BigDecimal ebitdaGrowthYoY) { this.ebitdaGrowthYoY = ebitdaGrowthYoY; }
    public BigDecimal getProfitGrowthYoY() { return profitGrowthYoY; }
    public void setProfitGrowthYoY(BigDecimal profitGrowthYoY) { this.profitGrowthYoY = profitGrowthYoY; }
    public BigDecimal getEpsGrowthYoY() { return epsGrowthYoY; }
    public void setEpsGrowthYoY(BigDecimal epsGrowthYoY) { this.epsGrowthYoY = epsGrowthYoY; }
    public BigDecimal getFcfGrowthYoY() { return fcfGrowthYoY; }
    public void setFcfGrowthYoY(BigDecimal fcfGrowthYoY) { this.fcfGrowthYoY = fcfGrowthYoY; }
    public BigDecimal getMarketCapCrores() { return marketCapCrores; }
    public void setMarketCapCrores(BigDecimal marketCapCrores) { this.marketCapCrores = marketCapCrores; }
    public BigDecimal getEnterpriseValueCrores() { return enterpriseValueCrores; }
    public void setEnterpriseValueCrores(BigDecimal enterpriseValueCrores) { this.enterpriseValueCrores = enterpriseValueCrores; }
    public BigDecimal getPeRatio() { return peRatio; }
    public void setPeRatio(BigDecimal peRatio) { this.peRatio = peRatio; }
    public BigDecimal getPbRatio() { return pbRatio; }
    public void setPbRatio(BigDecimal pbRatio) { this.pbRatio = pbRatio; }
    public BigDecimal getEvEbitda() { return evEbitda; }
    public void setEvEbitda(BigDecimal evEbitda) { this.evEbitda = evEbitda; }
    public BigDecimal getEvSales() { return evSales; }
    public void setEvSales(BigDecimal evSales) { this.evSales = evSales; }
    public BigDecimal getDividendYield() { return dividendYield; }
    public void setDividendYield(BigDecimal dividendYield) { this.dividendYield = dividendYield; }
    public BigDecimal getPayoutRatio() { return payoutRatio; }
    public void setPayoutRatio(BigDecimal payoutRatio) { this.payoutRatio = payoutRatio; }
    public String getMethodologyVersion() { return methodologyVersion; }
    public void setMethodologyVersion(String methodologyVersion) { this.methodologyVersion = methodologyVersion; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
