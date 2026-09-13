package com.quantlab.fundamental.entity;

import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "financial_ratios",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_fr_inst_available", columnList = "instrument_id, available_at"),
        @Index(name = "idx_fr_symbol_period", columnList = "symbol, period_end")
    }
)
public class FinancialRatio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filing_id")
    private Long filingId;

    @Column(name = "instrument_id", nullable = false)
    private Long instrumentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 32)
    private PeriodType periodType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reporting_basis", nullable = false, length = 32)
    private ReportingBasis reportingBasis;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    // Profitability & Quality
    @Column(name = "gross_margin", precision = 8, scale = 4)
    private BigDecimal grossMargin;

    @Column(name = "ebitda_margin", precision = 8, scale = 4)
    private BigDecimal ebitdaMargin;

    @Column(name = "ebit_margin", precision = 8, scale = 4)
    private BigDecimal ebitMargin;

    @Column(name = "net_profit_margin", precision = 8, scale = 4)
    private BigDecimal netProfitMargin;

    @Column(precision = 8, scale = 4)
    private BigDecimal roe; // Net Income / Avg Equity

    @Column(precision = 8, scale = 4)
    private BigDecimal roce; // EBIT / Capital Employed

    @Column(precision = 8, scale = 4)
    private BigDecimal roa;

    @Column(name = "asset_turnover", precision = 8, scale = 4)
    private BigDecimal assetTurnover;

    // Solvency & Coverage
    @Column(name = "debt_to_equity", precision = 8, scale = 4)
    private BigDecimal debtToEquity;

    @Column(name = "net_debt_to_ebitda", precision = 8, scale = 4)
    private BigDecimal netDebtToEbitda;

    @Column(name = "interest_coverage", precision = 8, scale = 4)
    private BigDecimal interestCoverage;

    @Column(name = "current_ratio", precision = 8, scale = 4)
    private BigDecimal currentRatio;

    // Growth (YoY)
    @Column(name = "revenue_growth_yoy", precision = 8, scale = 4)
    private BigDecimal revenueGrowthYoY;

    @Column(name = "ebitda_growth_yoy", precision = 8, scale = 4)
    private BigDecimal ebitdaGrowthYoY;

    @Column(name = "profit_growth_yoy", precision = 8, scale = 4)
    private BigDecimal profitGrowthYoY;

    @Column(name = "eps_growth_yoy", precision = 8, scale = 4)
    private BigDecimal epsGrowthYoY;

    @Column(name = "fcf_growth_yoy", precision = 8, scale = 4)
    private BigDecimal fcfGrowthYoY;

    // Valuation Ratios (Point-in-Time)
    @Column(name = "market_cap_crores", precision = 20, scale = 4)
    private BigDecimal marketCapCrores;

    @Column(name = "enterprise_value_crores", precision = 20, scale = 4)
    private BigDecimal enterpriseValueCrores;

    @Column(name = "pe_ratio", precision = 10, scale = 4)
    private BigDecimal peRatio;

    @Column(name = "pb_ratio", precision = 10, scale = 4)
    private BigDecimal pbRatio;

    @Column(name = "ev_ebitda", precision = 10, scale = 4)
    private BigDecimal evEbitda;

    @Column(name = "ev_sales", precision = 10, scale = 4)
    private BigDecimal evSales;

    @Column(name = "dividend_yield", precision = 8, scale = 4)
    private BigDecimal dividendYield;

    @Column(name = "payout_ratio", precision = 8, scale = 4)
    private BigDecimal payoutRatio;

    @Column(name = "methodology_version", nullable = false, length = 32)
    private String methodologyVersion = "1.0.0";

    @Column(name = "calculated_at", nullable = false)
    private Instant calculatedAt = Instant.now();

    public FinancialRatio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFilingId() { return filingId; }
    public void setFilingId(Long filingId) { this.filingId = filingId; }
    public Long getInstrumentId() { return instrumentId; }
    public void setInstrumentId(Long instrumentId) { this.instrumentId = instrumentId; }
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
