package com.quantlab.fundamental.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class CompanyFundamentalsDTO {

    private String symbol;
    private String companyName;
    private String sector;
    private String industry;
    private LocalDate latestPeriodEnd;
    private String latestFiscalPeriod;
    private ReportingBasis reportingBasis;
    private Instant publishedAt;
    private Instant availableAt;
    private Long ageDays;
    private String source;
    private String dataQualityScore;

    // Current Statements & Ratios
    private FinancialStatementDTO latestStatement;
    private FinancialRatiosDTO latestRatios;

    // Historical Time Series
    private List<FinancialStatementDTO> quarterlyStatements;
    private List<FinancialStatementDTO> annualStatements;
    private List<FinancialRatiosDTO> historicalRatios;

    public CompanyFundamentalsDTO() {}

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public LocalDate getLatestPeriodEnd() { return latestPeriodEnd; }
    public void setLatestPeriodEnd(LocalDate latestPeriodEnd) { this.latestPeriodEnd = latestPeriodEnd; }
    public String getLatestFiscalPeriod() { return latestFiscalPeriod; }
    public void setLatestFiscalPeriod(String latestFiscalPeriod) { this.latestFiscalPeriod = latestFiscalPeriod; }
    public ReportingBasis getReportingBasis() { return reportingBasis; }
    public void setReportingBasis(ReportingBasis reportingBasis) { this.reportingBasis = reportingBasis; }
    public Instant getPublishedAt() { return publishedAt; }
    public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
    public Instant getAvailableAt() { return availableAt; }
    public void setAvailableAt(Instant availableAt) { this.availableAt = availableAt; }
    public Long getAgeDays() { return ageDays; }
    public void setAgeDays(Long ageDays) { this.ageDays = ageDays; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getDataQualityScore() { return dataQualityScore; }
    public void setDataQualityScore(String dataQualityScore) { this.dataQualityScore = dataQualityScore; }
    public FinancialStatementDTO getLatestStatement() { return latestStatement; }
    public void setLatestStatement(FinancialStatementDTO latestStatement) { this.latestStatement = latestStatement; }
    public FinancialRatiosDTO getLatestRatios() { return latestRatios; }
    public void setLatestRatios(FinancialRatiosDTO latestRatios) { this.latestRatios = latestRatios; }
    public List<FinancialStatementDTO> getQuarterlyStatements() { return quarterlyStatements; }
    public void setQuarterlyStatements(List<FinancialStatementDTO> quarterlyStatements) { this.quarterlyStatements = quarterlyStatements; }
    public List<FinancialStatementDTO> getAnnualStatements() { return annualStatements; }
    public void setAnnualStatements(List<FinancialStatementDTO> annualStatements) { this.annualStatements = annualStatements; }
    public List<FinancialRatiosDTO> getHistoricalRatios() { return historicalRatios; }
    public void setHistoricalRatios(List<FinancialRatiosDTO> historicalRatios) { this.historicalRatios = historicalRatios; }
}
