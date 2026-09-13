package com.quantlab.fundamental.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public class FinancialStatementDTO {

    private Long id;
    private Long filingId;
    private String symbol;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private String fiscalYear;
    private String fiscalQuarter;
    private PeriodType periodType;
    private ReportingBasis reportingBasis;
    private AuditStatus auditStatus;
    private Instant publishedAt;
    private Instant availableAt;
    private Long ageDays;
    private String source;
    private String sourceDocumentUrl;
    private String currency = "INR";
    private String unit = "CRORES";

    // Income Statement
    private BigDecimal revenue;
    private BigDecimal operatingProfit;
    private BigDecimal ebitda;
    private BigDecimal ebit;
    private BigDecimal interestExpense;
    private BigDecimal depreciationAmortization;
    private BigDecimal profitBeforeTax;
    private BigDecimal taxExpense;
    private BigDecimal netProfit;
    private BigDecimal profitAttributableToOwners;
    private BigDecimal basicEps;
    private BigDecimal dilutedEps;

    // Balance Sheet
    private BigDecimal totalAssets;
    private BigDecimal currentAssets;
    private BigDecimal nonCurrentAssets;
    private BigDecimal cashAndEquivalents;
    private BigDecimal inventory;
    private BigDecimal tradeReceivables;
    private BigDecimal totalLiabilities;
    private BigDecimal currentLiabilities;
    private BigDecimal nonCurrentLiabilities;
    private BigDecimal totalDebt;
    private BigDecimal shortTermDebt;
    private BigDecimal longTermDebt;
    private BigDecimal totalEquity;
    private BigDecimal retainedEarnings;

    // Cash Flow
    private BigDecimal operatingCashFlow;
    private BigDecimal investingCashFlow;
    private BigDecimal financingCashFlow;
    private BigDecimal capitalExpenditure;
    private BigDecimal freeCashFlow;

    // Sector specific metrics (NIM, GNPA, NNPA, etc.)
    private Map<String, Object> sectorSpecificMetrics;

    public FinancialStatementDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFilingId() { return filingId; }
    public void setFilingId(Long filingId) { this.filingId = filingId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }
    public String getFiscalQuarter() { return fiscalQuarter; }
    public void setFiscalQuarter(String fiscalQuarter) { this.fiscalQuarter = fiscalQuarter; }
    public PeriodType getPeriodType() { return periodType; }
    public void setPeriodType(PeriodType periodType) { this.periodType = periodType; }
    public ReportingBasis getReportingBasis() { return reportingBasis; }
    public void setReportingBasis(ReportingBasis reportingBasis) { this.reportingBasis = reportingBasis; }
    public AuditStatus getAuditStatus() { return auditStatus; }
    public void setAuditStatus(AuditStatus auditStatus) { this.auditStatus = auditStatus; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public Long getAgeDays() { return ageDays; }
    public void setAgeDays(Long ageDays) { this.ageDays = ageDays; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getSourceDocumentUrl() { return sourceDocumentUrl; }
    public void setSourceDocumentUrl(String sourceDocumentUrl) { this.sourceDocumentUrl = sourceDocumentUrl; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    public BigDecimal getOperatingProfit() { return operatingProfit; }
    public void setOperatingProfit(BigDecimal operatingProfit) { this.operatingProfit = operatingProfit; }
    public BigDecimal getEbitda() { return ebitda; }
    public void setEbitda(BigDecimal ebitda) { this.ebitda = ebitda; }
    public BigDecimal getEbit() { return ebit; }
    public void setEbit(BigDecimal ebit) { this.ebit = ebit; }
    public BigDecimal getInterestExpense() { return interestExpense; }
    public void setInterestExpense(BigDecimal interestExpense) { this.interestExpense = interestExpense; }
    public BigDecimal getDepreciationAmortization() { return depreciationAmortization; }
    public void setDepreciationAmortization(BigDecimal depreciationAmortization) { this.depreciationAmortization = depreciationAmortization; }
    public BigDecimal getProfitBeforeTax() { return profitBeforeTax; }
    public void setProfitBeforeTax(BigDecimal profitBeforeTax) { this.profitBeforeTax = profitBeforeTax; }
    public BigDecimal getTaxExpense() { return taxExpense; }
    public void setTaxExpense(BigDecimal taxExpense) { this.taxExpense = taxExpense; }
    public BigDecimal getNetProfit() { return netProfit; }
    public void setNetProfit(BigDecimal netProfit) { this.netProfit = netProfit; }
    public BigDecimal getProfitAttributableToOwners() { return profitAttributableToOwners; }
    public void setProfitAttributableToOwners(BigDecimal profitAttributableToOwners) { this.profitAttributableToOwners = profitAttributableToOwners; }
    public BigDecimal getBasicEps() { return basicEps; }
    public void setBasicEps(BigDecimal basicEps) { this.basicEps = basicEps; }
    public BigDecimal getDilutedEps() { return dilutedEps; }
    public void setDilutedEps(BigDecimal dilutedEps) { this.dilutedEps = dilutedEps; }
    public BigDecimal getTotalAssets() { return totalAssets; }
    public void setTotalAssets(BigDecimal totalAssets) { this.totalAssets = totalAssets; }
    public BigDecimal getCurrentAssets() { return currentAssets; }
    public void setCurrentAssets(BigDecimal currentAssets) { this.currentAssets = currentAssets; }
    public BigDecimal getNonCurrentAssets() { return nonCurrentAssets; }
    public void setNonCurrentAssets(BigDecimal nonCurrentAssets) { this.nonCurrentAssets = nonCurrentAssets; }
    public BigDecimal getCashAndEquivalents() { return cashAndEquivalents; }
    public void setCashAndEquivalents(BigDecimal cashAndEquivalents) { this.cashAndEquivalents = cashAndEquivalents; }
    public BigDecimal getInventory() { return inventory; }
    public void setInventory(BigDecimal inventory) { this.inventory = inventory; }
    public BigDecimal getTradeReceivables() { return tradeReceivables; }
    public void setTradeReceivables(BigDecimal tradeReceivables) { this.tradeReceivables = tradeReceivables; }
    public BigDecimal getTotalLiabilities() { return totalLiabilities; }
    public void setTotalLiabilities(BigDecimal totalLiabilities) { this.totalLiabilities = totalLiabilities; }
    public BigDecimal getCurrentLiabilities() { return currentLiabilities; }
    public void setCurrentLiabilities(BigDecimal currentLiabilities) { this.currentLiabilities = currentLiabilities; }
    public BigDecimal getNonCurrentLiabilities() { return nonCurrentLiabilities; }
    public void setNonCurrentLiabilities(BigDecimal nonCurrentLiabilities) { this.nonCurrentLiabilities = nonCurrentLiabilities; }
    public BigDecimal getTotalDebt() { return totalDebt; }
    public void setTotalDebt(BigDecimal totalDebt) { this.totalDebt = totalDebt; }
    public BigDecimal getShortTermDebt() { return shortTermDebt; }
    public void setShortTermDebt(BigDecimal shortTermDebt) { this.shortTermDebt = shortTermDebt; }
    public BigDecimal getLongTermDebt() { return longTermDebt; }
    public void setLongTermDebt(BigDecimal longTermDebt) { this.longTermDebt = longTermDebt; }
    public BigDecimal getTotalEquity() { return totalEquity; }
    public void setTotalEquity(BigDecimal totalEquity) { this.totalEquity = totalEquity; }
    public BigDecimal getRetainedEarnings() { return retainedEarnings; }
    public void setRetainedEarnings(BigDecimal retainedEarnings) { this.retainedEarnings = retainedEarnings; }
    public BigDecimal getOperatingCashFlow() { return operatingCashFlow; }
    public void setOperatingCashFlow(BigDecimal operatingCashFlow) { this.operatingCashFlow = operatingCashFlow; }
    public BigDecimal getInvestingCashFlow() { return investingCashFlow; }
    public void setInvestingCashFlow(BigDecimal investingCashFlow) { this.investingCashFlow = investingCashFlow; }
    public BigDecimal getFinancingCashFlow() { return financingCashFlow; }
    public void setFinancingCashFlow(BigDecimal financingCashFlow) { this.financingCashFlow = financingCashFlow; }
    public BigDecimal getCapitalExpenditure() { return capitalExpenditure; }
    public void setCapitalExpenditure(BigDecimal capitalExpenditure) { this.capitalExpenditure = capitalExpenditure; }
    public BigDecimal getFreeCashFlow() { return freeCashFlow; }
    public void setFreeCashFlow(BigDecimal freeCashFlow) { this.freeCashFlow = freeCashFlow; }
    public Map<String, Object> getSectorSpecificMetrics() { return sectorSpecificMetrics; }
    public void setSectorSpecificMetrics(Map<String, Object> sectorSpecificMetrics) { this.sectorSpecificMetrics = sectorSpecificMetrics; }
}
