package com.quantlab.fundamental.entity;

import com.quantlab.fundamental.model.AuditStatus;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "financial_statements",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_fs_inst_period", columnList = "instrument_id, period_end"),
        @Index(name = "idx_fs_symbol_available", columnList = "symbol, available_at")
    }
)
public class FinancialStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filing_id", nullable = false)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "audit_status", nullable = false, length = 32)
    private AuditStatus auditStatus;

    @Column(name = "available_at", nullable = false)
    private Instant availableAt;

    @Column(nullable = false, length = 8)
    private String currency = "INR";

    @Column(nullable = false, length = 16)
    private String unit = "CRORES";

    // Income Statement
    @Column(precision = 20, scale = 4)
    private BigDecimal revenue;

    @Column(name = "operating_profit", precision = 20, scale = 4)
    private BigDecimal operatingProfit;

    @Column(precision = 20, scale = 4)
    private BigDecimal ebitda;

    @Column(precision = 20, scale = 4)
    private BigDecimal ebit;

    @Column(name = "interest_expense", precision = 20, scale = 4)
    private BigDecimal interestExpense;

    @Column(name = "depreciation_amortization", precision = 20, scale = 4)
    private BigDecimal depreciationAmortization;

    @Column(name = "profit_before_tax", precision = 20, scale = 4)
    private BigDecimal profitBeforeTax;

    @Column(name = "tax_expense", precision = 20, scale = 4)
    private BigDecimal taxExpense;

    @Column(name = "net_profit", precision = 20, scale = 4)
    private BigDecimal netProfit; // PAT

    @Column(name = "profit_attributable_to_owners", precision = 20, scale = 4)
    private BigDecimal profitAttributableToOwners;

    @Column(name = "basic_eps", precision = 10, scale = 4)
    private BigDecimal basicEps;

    @Column(name = "diluted_eps", precision = 10, scale = 4)
    private BigDecimal dilutedEps;

    // Balance Sheet
    @Column(name = "total_assets", precision = 20, scale = 4)
    private BigDecimal totalAssets;

    @Column(name = "current_assets", precision = 20, scale = 4)
    private BigDecimal currentAssets;

    @Column(name = "non_current_assets", precision = 20, scale = 4)
    private BigDecimal nonCurrentAssets;

    @Column(name = "cash_and_equivalents", precision = 20, scale = 4)
    private BigDecimal cashAndEquivalents;

    @Column(precision = 20, scale = 4)
    private BigDecimal inventory;

    @Column(name = "trade_receivables", precision = 20, scale = 4)
    private BigDecimal tradeReceivables;

    @Column(name = "total_liabilities", precision = 20, scale = 4)
    private BigDecimal totalLiabilities;

    @Column(name = "current_liabilities", precision = 20, scale = 4)
    private BigDecimal currentLiabilities;

    @Column(name = "non_current_liabilities", precision = 20, scale = 4)
    private BigDecimal nonCurrentLiabilities;

    @Column(name = "total_debt", precision = 20, scale = 4)
    private BigDecimal totalDebt;

    @Column(name = "short_term_debt", precision = 20, scale = 4)
    private BigDecimal shortTermDebt;

    @Column(name = "long_term_debt", precision = 20, scale = 4)
    private BigDecimal longTermDebt;

    @Column(name = "total_equity", precision = 20, scale = 4)
    private BigDecimal totalEquity;

    @Column(name = "retained_earnings", precision = 20, scale = 4)
    private BigDecimal retainedEarnings;

    // Cash Flow
    @Column(name = "operating_cash_flow", precision = 20, scale = 4)
    private BigDecimal operatingCashFlow;

    @Column(name = "investing_cash_flow", precision = 20, scale = 4)
    private BigDecimal investingCashFlow;

    @Column(name = "financing_cash_flow", precision = 20, scale = 4)
    private BigDecimal financingCashFlow;

    @Column(name = "capital_expenditure", precision = 20, scale = 4)
    private BigDecimal capitalExpenditure;

    @Column(name = "free_cash_flow", precision = 20, scale = 4)
    private BigDecimal freeCashFlow;

    @Column(name = "sector_specific_metrics", columnDefinition = "TEXT")
    private String sectorSpecificMetrics;

    @Column(nullable = false, length = 64)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public FinancialStatement() {}

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
    public AuditStatus getAuditStatus() { return auditStatus; }
    public void setAuditStatus(AuditStatus auditStatus) { this.auditStatus = auditStatus; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
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
    public String getSectorSpecificMetrics() { return sectorSpecificMetrics; }
    public void setSectorSpecificMetrics(String sectorSpecificMetrics) { this.sectorSpecificMetrics = sectorSpecificMetrics; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
