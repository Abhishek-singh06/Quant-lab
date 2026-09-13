package com.quantlab.fundamental.provider;

import com.quantlab.fundamental.entity.FinancialStatement;
import com.quantlab.fundamental.entity.FundamentalFiling;
import com.quantlab.fundamental.model.AuditStatus;
import com.quantlab.fundamental.model.FilingType;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic mock provider for local development, financial modeling, and integration testing.
 * Accurately models corporate reporting lag (e.g. Q4 ended March 31 published May 15).
 */
@Component("mockFundamentalDataProvider")
public class MockFundamentalDataProvider implements FundamentalDataProvider {

    private static final String PROVIDER_NAME = "MOCK_FUNDAMENTAL_SOURCE";
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public List<FundamentalFiling> fetchCompanyFilings(String symbol) {
        List<FundamentalFiling> filings = new ArrayList<>();
        long instId = getInstrumentIdForSymbol(symbol);

        // Q1 FY26 (Ended June 30, 2025, Published July 25, 2025)
        filings.add(createFiling(instId, symbol, "FY2026", "Q1",
            LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30),
            PeriodType.QUARTERLY, ReportingBasis.CONSOLIDATED, AuditStatus.UNAUDITED,
            LocalDate.of(2025, 7, 25), 1, false, null));

        // Q2 FY26 (Ended Sep 30, 2025, Published Oct 20, 2025)
        filings.add(createFiling(instId, symbol, "FY2026", "Q2",
            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 9, 30),
            PeriodType.QUARTERLY, ReportingBasis.CONSOLIDATED, AuditStatus.UNAUDITED,
            LocalDate.of(2025, 10, 20), 1, false, null));

        // Q3 FY26 (Ended Dec 31, 2025, Published Jan 22, 2026)
        filings.add(createFiling(instId, symbol, "FY2026", "Q3",
            LocalDate.of(2025, 10, 1), LocalDate.of(2025, 12, 31),
            PeriodType.QUARTERLY, ReportingBasis.CONSOLIDATED, AuditStatus.UNAUDITED,
            LocalDate.of(2026, 1, 22), 1, false, null));

        // Q4 FY25 (Ended March 31, 2025, Published May 15, 2025)
        filings.add(createFiling(instId, symbol, "FY2025", "Q4",
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31),
            PeriodType.QUARTERLY, ReportingBasis.CONSOLIDATED, AuditStatus.AUDITED,
            LocalDate.of(2025, 5, 15), 1, false, null));

        // Annual FY25 (Ended March 31, 2025, Published May 15, 2025)
        filings.add(createFiling(instId, symbol, "FY2025", null,
            LocalDate.of(2024, 4, 1), LocalDate.of(2025, 3, 31),
            PeriodType.ANNUAL, ReportingBasis.CONSOLIDATED, AuditStatus.AUDITED,
            LocalDate.of(2025, 5, 15), 1, false, null));

        return filings;
    }

    @Override
    public FinancialStatement fetchFinancialStatement(FundamentalFiling filing) {
        FinancialStatement s = new FinancialStatement();
        s.setFilingId(filing.getId());
        s.setInstrumentId(filing.getInstrumentId());
        s.setSymbol(filing.getSymbol());
        s.setPeriodEnd(filing.getPeriodEnd());
        s.setPeriodType(filing.getPeriodType());
        s.setReportingBasis(filing.getReportingBasis());
        s.setAuditStatus(filing.getAuditStatus());
        s.setAvailableAt(filing.getAvailableAt());
        s.setCurrency("INR");
        s.setUnit("CRORES");
        s.setSource(PROVIDER_NAME);

        if ("RELIANCE".equalsIgnoreCase(filing.getSymbol())) {
            populateReliance(s, filing);
        } else if ("TCS".equalsIgnoreCase(filing.getSymbol())) {
            populateTcs(s, filing);
        } else if ("HDFCBANK".equalsIgnoreCase(filing.getSymbol())) {
            populateHdfcBank(s, filing);
        } else {
            populateGeneric(s, filing);
        }

        return s;
    }

    @Override
    public List<FinancialStatement> fetchHistoricalStatements(String symbol, PeriodType periodType, ReportingBasis basis) {
        List<FundamentalFiling> filings = fetchCompanyFilings(symbol);
        List<FinancialStatement> list = new ArrayList<>();
        for (FundamentalFiling f : filings) {
            if (f.getPeriodType() == periodType && f.getReportingBasis() == basis) {
                list.add(fetchFinancialStatement(f));
            }
        }
        return list;
    }

    private void populateReliance(FinancialStatement s, FundamentalFiling f) {
        boolean isAnnual = f.getPeriodType() == PeriodType.ANNUAL;
        BigDecimal mult = isAnnual ? new BigDecimal("4.0") : BigDecimal.ONE;

        s.setRevenue(new BigDecimal("245000.0").multiply(mult));
        s.setOperatingProfit(new BigDecimal("42500.0").multiply(mult));
        s.setEbitda(new BigDecimal("43500.0").multiply(mult));
        s.setEbit(new BigDecimal("32000.0").multiply(mult));
        s.setInterestExpense(new BigDecimal("5500.0").multiply(mult));
        s.setDepreciationAmortization(new BigDecimal("11500.0").multiply(mult));
        s.setProfitBeforeTax(new BigDecimal("26500.0").multiply(mult));
        s.setTaxExpense(new BigDecimal("6800.0").multiply(mult));
        s.setNetProfit(new BigDecimal("19700.0").multiply(mult));
        s.setProfitAttributableToOwners(new BigDecimal("19300.0").multiply(mult));
        s.setBasicEps(isAnnual ? new BigDecimal("116.50") : new BigDecimal("29.10"));
        s.setDilutedEps(isAnnual ? new BigDecimal("116.50") : new BigDecimal("29.10"));

        s.setTotalAssets(new BigDecimal("1780000.0"));
        s.setCurrentAssets(new BigDecimal("450000.0"));
        s.setNonCurrentAssets(new BigDecimal("1330000.0"));
        s.setCashAndEquivalents(new BigDecimal("185000.0"));
        s.setInventory(new BigDecimal("142000.0"));
        s.setTradeReceivables(new BigDecimal("35000.0"));
        s.setTotalLiabilities(new BigDecimal("980000.0"));
        s.setCurrentLiabilities(new BigDecimal("390000.0"));
        s.setNonCurrentLiabilities(new BigDecimal("590000.0"));
        s.setTotalDebt(new BigDecimal("325000.0"));
        s.setShortTermDebt(new BigDecimal("85000.0"));
        s.setLongTermDebt(new BigDecimal("240000.0"));
        s.setTotalEquity(new BigDecimal("800000.0"));
        s.setRetainedEarnings(new BigDecimal("650000.0"));

        s.setOperatingCashFlow(new BigDecimal("38000.0").multiply(mult));
        s.setInvestingCashFlow(new BigDecimal("-28000.0").multiply(mult));
        s.setFinancingCashFlow(new BigDecimal("-7000.0").multiply(mult));
        s.setCapitalExpenditure(new BigDecimal("25000.0").multiply(mult));
        s.setFreeCashFlow(new BigDecimal("13000.0").multiply(mult));
    }

    private void populateTcs(FinancialStatement s, FundamentalFiling f) {
        boolean isAnnual = f.getPeriodType() == PeriodType.ANNUAL;
        BigDecimal mult = isAnnual ? new BigDecimal("4.0") : BigDecimal.ONE;

        s.setRevenue(new BigDecimal("64250.0").multiply(mult));
        s.setOperatingProfit(new BigDecimal("16800.0").multiply(mult));
        s.setEbitda(new BigDecimal("17200.0").multiply(mult));
        s.setEbit(new BigDecimal("15800.0").multiply(mult));
        s.setInterestExpense(new BigDecimal("220.0").multiply(mult));
        s.setDepreciationAmortization(new BigDecimal("1400.0").multiply(mult));
        s.setProfitBeforeTax(new BigDecimal("16500.0").multiply(mult));
        s.setTaxExpense(new BigDecimal("4200.0").multiply(mult));
        s.setNetProfit(new BigDecimal("12300.0").multiply(mult));
        s.setProfitAttributableToOwners(new BigDecimal("12250.0").multiply(mult));
        s.setBasicEps(isAnnual ? new BigDecimal("135.20") : new BigDecimal("33.80"));
        s.setDilutedEps(isAnnual ? new BigDecimal("135.20") : new BigDecimal("33.80"));

        s.setTotalAssets(new BigDecimal("154000.0"));
        s.setCurrentAssets(new BigDecimal("98000.0"));
        s.setNonCurrentAssets(new BigDecimal("56000.0"));
        s.setCashAndEquivalents(new BigDecimal("42000.0"));
        s.setInventory(BigDecimal.ZERO);
        s.setTradeReceivables(new BigDecimal("44000.0"));
        s.setTotalLiabilities(new BigDecimal("52000.0"));
        s.setCurrentLiabilities(new BigDecimal("41000.0"));
        s.setNonCurrentLiabilities(new BigDecimal("11000.0"));
        s.setTotalDebt(BigDecimal.ZERO);
        s.setTotalEquity(new BigDecimal("102000.0"));
        s.setRetainedEarnings(new BigDecimal("98000.0"));

        s.setOperatingCashFlow(new BigDecimal("13500.0").multiply(mult));
        s.setInvestingCashFlow(new BigDecimal("-2200.0").multiply(mult));
        s.setFinancingCashFlow(new BigDecimal("-11000.0").multiply(mult));
        s.setCapitalExpenditure(new BigDecimal("1500.0").multiply(mult));
        s.setFreeCashFlow(new BigDecimal("12000.0").multiply(mult));

        s.setSectorSpecificMetrics("{\"ebitMargin\": 24.58, \"attritionRate\": 12.3, \"headcount\": 612724}");
    }

    private void populateHdfcBank(FinancialStatement s, FundamentalFiling f) {
        boolean isAnnual = f.getPeriodType() == PeriodType.ANNUAL;
        BigDecimal mult = isAnnual ? new BigDecimal("4.0") : BigDecimal.ONE;

        s.setRevenue(new BigDecimal("85500.0").multiply(mult)); // Total Income
        s.setOperatingProfit(new BigDecimal("24800.0").multiply(mult));
        s.setEbitda(new BigDecimal("24800.0").multiply(mult));
        s.setEbit(new BigDecimal("23500.0").multiply(mult));
        s.setInterestExpense(new BigDecimal("48000.0").multiply(mult));
        s.setProfitBeforeTax(new BigDecimal("22500.0").multiply(mult));
        s.setTaxExpense(new BigDecimal("5700.0").multiply(mult));
        s.setNetProfit(new BigDecimal("16800.0").multiply(mult));
        s.setBasicEps(isAnnual ? new BigDecimal("88.40") : new BigDecimal("22.10"));
        s.setDilutedEps(isAnnual ? new BigDecimal("88.40") : new BigDecimal("22.10"));

        s.setTotalAssets(new BigDecimal("3650000.0"));
        s.setCurrentAssets(new BigDecimal("750000.0"));
        s.setNonCurrentAssets(new BigDecimal("2900000.0"));
        s.setCashAndEquivalents(new BigDecimal("240000.0"));
        s.setTotalLiabilities(new BigDecimal("3200000.0"));
        s.setTotalDebt(new BigDecimal("450000.0"));
        s.setTotalEquity(new BigDecimal("450000.0"));

        s.setOperatingCashFlow(new BigDecimal("22000.0").multiply(mult));
        s.setCapitalExpenditure(new BigDecimal("2500.0").multiply(mult));
        s.setFreeCashFlow(new BigDecimal("19500.0").multiply(mult));

        s.setSectorSpecificMetrics("{\"nim\": 3.47, \"gnpa\": 1.36, \"nnpa\": 0.38, \"crar\": 19.80, \"pcr\": 72.5}");
    }

    private void populateGeneric(FinancialStatement s, FundamentalFiling f) {
        boolean isAnnual = f.getPeriodType() == PeriodType.ANNUAL;
        BigDecimal mult = isAnnual ? new BigDecimal("4.0") : BigDecimal.ONE;

        s.setRevenue(new BigDecimal("18500.0").multiply(mult));
        s.setOperatingProfit(new BigDecimal("6200.0").multiply(mult));
        s.setEbitda(new BigDecimal("6500.0").multiply(mult));
        s.setEbit(new BigDecimal("5800.0").multiply(mult));
        s.setInterestExpense(new BigDecimal("80.0").multiply(mult));
        s.setProfitBeforeTax(new BigDecimal("6600.0").multiply(mult));
        s.setTaxExpense(new BigDecimal("1650.0").multiply(mult));
        s.setNetProfit(new BigDecimal("4950.0").multiply(mult));
        s.setBasicEps(isAnnual ? new BigDecimal("16.40") : new BigDecimal("4.10"));
        s.setDilutedEps(isAnnual ? new BigDecimal("16.40") : new BigDecimal("4.10"));

        s.setTotalAssets(new BigDecimal("85000.0"));
        s.setCurrentAssets(new BigDecimal("42000.0"));
        s.setTotalLiabilities(new BigDecimal("22000.0"));
        s.setTotalEquity(new BigDecimal("63000.0"));
        s.setTotalDebt(new BigDecimal("1200.0"));

        s.setOperatingCashFlow(new BigDecimal("5200.0").multiply(mult));
        s.setCapitalExpenditure(new BigDecimal("1100.0").multiply(mult));
        s.setFreeCashFlow(new BigDecimal("4100.0").multiply(mult));
    }

    private FundamentalFiling createFiling(Long instId, String symbol, String fy, String qtr,
                                          LocalDate pStart, LocalDate pEnd, PeriodType pType,
                                          ReportingBasis basis, AuditStatus audit,
                                          LocalDate pubDate, int ver, boolean restated, String reason) {
        Instant pubInstant = pubDate.atTime(18, 0).atZone(IST_ZONE).toInstant();
        FundamentalFiling f = new FundamentalFiling();
        f.setInstrumentId(instId);
        f.setSymbol(symbol);
        f.setFilingType(pType == PeriodType.ANNUAL ? FilingType.ANNUAL_REPORT : FilingType.FINANCIAL_RESULT);
        f.setFiscalYear(fy);
        f.setFiscalQuarter(qtr);
        f.setPeriodStart(pStart);
        f.setPeriodEnd(pEnd);
        f.setPeriodType(pType);
        f.setReportingBasis(basis);
        f.setAuditStatus(audit);
        f.setPublishedAt(pubInstant);
        f.setAvailableAt(pubInstant);
        f.setSource(PROVIDER_NAME);
        f.setSourceDocumentUrl("https://www.nseindia.com/filings/" + symbol + "_" + pEnd + ".pdf");
        f.setVersion(ver);
        f.setRestatement(restated);
        f.setRestatementReason(reason);
        f.setDataQualityScore("HIGH");
        return f;
    }

    private long getInstrumentIdForSymbol(String sym) {
        if ("RELIANCE".equalsIgnoreCase(sym)) return 1L;
        if ("TCS".equalsIgnoreCase(sym)) return 2L;
        if ("HDFCBANK".equalsIgnoreCase(sym)) return 3L;
        if ("INFY".equalsIgnoreCase(sym)) return 4L;
        if ("ITC".equalsIgnoreCase(sym)) return 5L;
        return 1L;
    }
}
