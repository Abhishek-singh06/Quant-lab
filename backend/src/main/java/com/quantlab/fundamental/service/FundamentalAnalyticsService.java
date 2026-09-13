package com.quantlab.fundamental.service;

import com.quantlab.fundamental.entity.FinancialRatio;
import com.quantlab.fundamental.entity.FinancialStatement;
import com.quantlab.fundamental.entity.FundamentalFiling;
import com.quantlab.fundamental.entity.FundamentalIngestionRun;
import com.quantlab.fundamental.model.*;
import com.quantlab.fundamental.repository.FinancialRatioRepository;
import com.quantlab.fundamental.repository.FinancialStatementRepository;
import com.quantlab.fundamental.repository.FundamentalFilingRepository;
import com.quantlab.fundamental.repository.FundamentalIngestionRunRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FundamentalAnalyticsService {

    private final FundamentalFilingRepository filingRepository;
    private final FinancialStatementRepository statementRepository;
    private final FinancialRatioRepository ratioRepository;
    private final FundamentalIngestionRunRepository runRepository;
    private final FundamentalIngestionService ingestionService;

    public FundamentalAnalyticsService(
            FundamentalFilingRepository filingRepository,
            FinancialStatementRepository statementRepository,
            FinancialRatioRepository ratioRepository,
            FundamentalIngestionRunRepository runRepository,
            FundamentalIngestionService ingestionService) {
        this.filingRepository = filingRepository;
        this.statementRepository = statementRepository;
        this.ratioRepository = ratioRepository;
        this.runRepository = runRepository;
        this.ingestionService = ingestionService;
    }

    public CompanyFundamentalsDTO getFundamentals(String symbol, Instant asOf) {
        Instant effectiveAsOf = (asOf != null) ? asOf : Instant.now();

        // 1. Auto-seed if not present
        List<FinancialStatement> qStatements = statementRepository.findStatementsBySymbolAndPeriodTypeAsOf(
                symbol, PeriodType.QUARTERLY, ReportingBasis.CONSOLIDATED, effectiveAsOf
        );
        if (qStatements.isEmpty()) {
            ingestionService.ingestForSymbol(symbol);
            qStatements = statementRepository.findStatementsBySymbolAndPeriodTypeAsOf(
                    symbol, PeriodType.QUARTERLY, ReportingBasis.CONSOLIDATED, effectiveAsOf
            );
        }

        List<FinancialStatement> aStatements = statementRepository.findStatementsBySymbolAndPeriodTypeAsOf(
                symbol, PeriodType.ANNUAL, ReportingBasis.CONSOLIDATED, effectiveAsOf
        );
        List<FinancialRatio> ratios = ratioRepository.findRatiosBySymbolAsOf(symbol, effectiveAsOf);

        CompanyFundamentalsDTO dto = new CompanyFundamentalsDTO();
        dto.setSymbol(symbol);
        dto.setReportingBasis(ReportingBasis.CONSOLIDATED);

        // Enrich basic company info
        if ("RELIANCE".equalsIgnoreCase(symbol)) {
            dto.setCompanyName("Reliance Industries Limited");
            dto.setSector("Energy & Petrochemicals");
            dto.setIndustry("Oil & Gas Refining & Marketing");
        } else if ("TCS".equalsIgnoreCase(symbol)) {
            dto.setCompanyName("Tata Consultancy Services Limited");
            dto.setSector("Information Technology");
            dto.setIndustry("IT Services & Consulting");
        } else if ("HDFCBANK".equalsIgnoreCase(symbol)) {
            dto.setCompanyName("HDFC Bank Limited");
            dto.setSector("Financial Services");
            dto.setIndustry("Private Sector Bank");
        } else {
            dto.setCompanyName(symbol + " Enterprises Limited");
            dto.setSector("Diversified");
            dto.setIndustry("Diversified");
        }

        if (!qStatements.isEmpty()) {
            FinancialStatement latestStmt = qStatements.get(0);
            dto.setLatestPeriodEnd(latestStmt.getPeriodEnd());
            dto.setPublishedAt(latestStmt.getAvailableAt());
            dto.setAvailableAt(latestStmt.getAvailableAt());
            dto.setAgeDays(Duration.between(latestStmt.getAvailableAt(), Instant.now()).toDays());
            dto.setSource(latestStmt.getSource());
            dto.setLatestStatement(mapToStatementDTO(latestStmt));
        }

        if (!ratios.isEmpty()) {
            dto.setLatestRatios(mapToRatioDTO(ratios.get(0)));
        }

        List<FinancialStatementDTO> qDtos = new ArrayList<>();
        for (FinancialStatement s : qStatements) {
            qDtos.add(mapToStatementDTO(s));
        }
        dto.setQuarterlyStatements(qDtos);

        List<FinancialStatementDTO> aDtos = new ArrayList<>();
        for (FinancialStatement s : aStatements) {
            aDtos.add(mapToStatementDTO(s));
        }
        dto.setAnnualStatements(aDtos);

        List<FinancialRatiosDTO> rDtos = new ArrayList<>();
        for (FinancialRatio r : ratios) {
            rDtos.add(mapToRatioDTO(r));
        }
        dto.setHistoricalRatios(rDtos);

        return dto;
    }

    public List<FinancialStatementDTO> getFinancialStatements(String symbol, PeriodType periodType, ReportingBasis basis, Instant asOf) {
        Instant effectiveAsOf = (asOf != null) ? asOf : Instant.now();
        List<FinancialStatement> list = statementRepository.findStatementsBySymbolAndPeriodTypeAsOf(symbol, periodType, basis, effectiveAsOf);
        List<FinancialStatementDTO> dtos = new ArrayList<>();
        for (FinancialStatement s : list) {
            dtos.add(mapToStatementDTO(s));
        }
        return dtos;
    }

    public List<FinancialRatiosDTO> getFinancialRatios(String symbol, Instant asOf) {
        Instant effectiveAsOf = (asOf != null) ? asOf : Instant.now();
        List<FinancialRatio> list = ratioRepository.findRatiosBySymbolAsOf(symbol, effectiveAsOf);
        List<FinancialRatiosDTO> dtos = new ArrayList<>();
        for (FinancialRatio r : list) {
            dtos.add(mapToRatioDTO(r));
        }
        return dtos;
    }

    public List<FundamentalFiling> getFilings(String symbol, Instant asOf) {
        Instant effectiveAsOf = (asOf != null) ? asOf : Instant.now();
        return filingRepository.findFilingsBySymbolAsOf(symbol, effectiveAsOf);
    }

    public List<FundamentalIngestionRun> getRecentRuns() {
        return runRepository.findTop20ByOrderByStartTimeDesc();
    }

    private FinancialStatementDTO mapToStatementDTO(FinancialStatement s) {
        FinancialStatementDTO d = new FinancialStatementDTO();
        d.setId(s.getId());
        d.setFilingId(s.getFilingId());
        d.setSymbol(s.getSymbol());
        d.setPeriodEnd(s.getPeriodEnd());
        d.setPeriodType(s.getPeriodType());
        d.setReportingBasis(s.getReportingBasis());
        d.setAuditStatus(s.getAuditStatus());
        d.setPublishedAt(s.getAvailableAt());
        d.setAvailableAt(s.getAvailableAt());
        d.setAgeDays(Duration.between(s.getAvailableAt(), Instant.now()).toDays());
        d.setSource(s.getSource());
        d.setCurrency(s.getCurrency());
        d.setUnit(s.getUnit());

        d.setRevenue(s.getRevenue());
        d.setOperatingProfit(s.getOperatingProfit());
        d.setEbitda(s.getEbitda());
        d.setEbit(s.getEbit());
        d.setInterestExpense(s.getInterestExpense());
        d.setDepreciationAmortization(s.getDepreciationAmortization());
        d.setProfitBeforeTax(s.getProfitBeforeTax());
        d.setTaxExpense(s.getTaxExpense());
        d.setNetProfit(s.getNetProfit());
        d.setProfitAttributableToOwners(s.getProfitAttributableToOwners());
        d.setBasicEps(s.getBasicEps());
        d.setDilutedEps(s.getDilutedEps());

        d.setTotalAssets(s.getTotalAssets());
        d.setCurrentAssets(s.getCurrentAssets());
        d.setNonCurrentAssets(s.getNonCurrentAssets());
        d.setCashAndEquivalents(s.getCashAndEquivalents());
        d.setInventory(s.getInventory());
        d.setTradeReceivables(s.getTradeReceivables());
        d.setTotalLiabilities(s.getTotalLiabilities());
        d.setCurrentLiabilities(s.getCurrentLiabilities());
        d.setNonCurrentLiabilities(s.getNonCurrentLiabilities());
        d.setTotalDebt(s.getTotalDebt());
        d.setShortTermDebt(s.getShortTermDebt());
        d.setLongTermDebt(s.getLongTermDebt());
        d.setTotalEquity(s.getTotalEquity());
        d.setRetainedEarnings(s.getRetainedEarnings());

        d.setOperatingCashFlow(s.getOperatingCashFlow());
        d.setInvestingCashFlow(s.getInvestingCashFlow());
        d.setFinancingCashFlow(s.getFinancingCashFlow());
        d.setCapitalExpenditure(s.getCapitalExpenditure());
        d.setFreeCashFlow(s.getFreeCashFlow());

        return d;
    }

    private FinancialRatiosDTO mapToRatioDTO(FinancialRatio r) {
        FinancialRatiosDTO d = new FinancialRatiosDTO();
        d.setSymbol(r.getSymbol());
        d.setPeriodEnd(r.getPeriodEnd());
        d.setPeriodType(r.getPeriodType());
        d.setReportingBasis(r.getReportingBasis());
        d.setAvailableAt(r.getAvailableAt());
        d.setGrossMargin(r.getGrossMargin());
        d.setEbitdaMargin(r.getEbitdaMargin());
        d.setEbitMargin(r.getEbitMargin());
        d.setNetProfitMargin(r.getNetProfitMargin());
        d.setRoe(r.getRoe());
        d.setRoce(r.getRoce());
        d.setRoa(r.getRoa());
        d.setAssetTurnover(r.getAssetTurnover());
        d.setDebtToEquity(r.getDebtToEquity());
        d.setNetDebtToEbitda(r.getNetDebtToEbitda());
        d.setInterestCoverage(r.getInterestCoverage());
        d.setCurrentRatio(r.getCurrentRatio());
        d.setRevenueGrowthYoY(r.getRevenueGrowthYoY());
        d.setEbitdaGrowthYoY(r.getEbitdaGrowthYoY());
        d.setProfitGrowthYoY(r.getProfitGrowthYoY());
        d.setEpsGrowthYoY(r.getEpsGrowthYoY());
        d.setFcfGrowthYoY(r.getFcfGrowthYoY());
        d.setMarketCapCrores(r.getMarketCapCrores());
        d.setEnterpriseValueCrores(r.getEnterpriseValueCrores());
        d.setPeRatio(r.getPeRatio());
        d.setPbRatio(r.getPbRatio());
        d.setEvEbitda(r.getEvEbitda());
        d.setEvSales(r.getEvSales());
        d.setDividendYield(r.getDividendYield());
        d.setPayoutRatio(r.getPayoutRatio());
        d.setMethodologyVersion(r.getMethodologyVersion());
        d.setCalculatedAt(r.getCalculatedAt());
        return d;
    }
}
