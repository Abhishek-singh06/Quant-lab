package com.quantlab.institutional.provider;

import com.quantlab.institutional.entity.*;
import com.quantlab.institutional.model.FlowFrequency;
import com.quantlab.institutional.model.HoldingChangeType;
import com.quantlab.institutional.model.InstitutionType;
import com.quantlab.institutional.model.PortfolioScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Deterministic mock provider for local development, simulation, and integration testing.
 * Strictly models point-in-time disclosure lags (e.g. July 31 disclosure published Aug 15).
 */
@Component("mockInstitutionalDataProvider")
public class MockInstitutionalDataProvider implements InstitutionalDataProvider {

    private static final String PROVIDER_NAME = "MOCK_INSTITUTIONAL_SOURCE";
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
    public List<AmcMaster> fetchAmcList() {
        List<AmcMaster> amcs = new ArrayList<>();
        amcs.add(new AmcMaster("HDFC_MF", "HDFC Asset Management Company Ltd", "123456", "HDFC Mutual Fund"));
        amcs.add(new AmcMaster("SBI_MF", "SBI Funds Management Ltd", "123457", "SBI Mutual Fund"));
        amcs.add(new AmcMaster("PPFAS_MF", "PPFAS Asset Management Pvt Ltd", "123458", "Parag Parikh Mutual Fund"));
        return amcs;
    }

    @Override
    public List<MutualFundScheme> fetchSchemes(String amcCode) {
        List<MutualFundScheme> schemes = new ArrayList<>();
        if ("PPFAS_MF".equalsIgnoreCase(amcCode)) {
            MutualFundScheme s = new MutualFundScheme(
                3L, "PPFAS Mutual Fund", "PPFAS_FLEXICAP", "Parag Parikh Flexi Cap Fund - Growth",
                "EQUITY_FLEXI_CAP", "FLEXI_CAP", "DIRECT", "GROWTH", "INF879O01019"
            );
            s.setAumCrores(new BigDecimal("72500.50"));
            schemes.add(s);
        } else if ("HDFC_MF".equalsIgnoreCase(amcCode)) {
            MutualFundScheme s = new MutualFundScheme(
                1L, "HDFC Mutual Fund", "HDFC_TOP100", "HDFC Top 100 Fund - Growth",
                "EQUITY_LARGE_CAP", "LARGE_CAP", "DIRECT", "GROWTH", "INF179K01BE2"
            );
            s.setAumCrores(new BigDecimal("34200.00"));
            schemes.add(s);
        } else {
            MutualFundScheme s = new MutualFundScheme(
                2L, "SBI Mutual Fund", "SBI_BLUECHIP", "SBI Bluechip Fund - Regular Growth",
                "EQUITY_LARGE_CAP", "LARGE_CAP", "REGULAR", "GROWTH", "INF200K01135"
            );
            s.setAumCrores(new BigDecimal("48100.20"));
            schemes.add(s);
        }
        return schemes;
    }

    @Override
    public FundPortfolioDisclosure fetchPortfolioDisclosure(String schemeCode, LocalDate dataAsOf) {
        // AMFI standard rule: month-end portfolio published on 10th-15th of next month
        LocalDate publishedAt = dataAsOf.plusMonths(1).withDayOfMonth(12);
        Instant availableAt = publishedAt.atTime(18, 30).atZone(IST_ZONE).toInstant();

        FundPortfolioDisclosure disc = new FundPortfolioDisclosure();
        disc.setSchemeId(1L);
        disc.setDataAsOf(dataAsOf);
        disc.setPublishedAt(publishedAt);
        disc.setAvailableAt(availableAt);
        disc.setPortfolioScope(PortfolioScope.COMPLETE);
        disc.setTotalAum(new BigDecimal("725005000000.00"));
        disc.setEquityHoldingPercent(new BigDecimal("84.50"));
        disc.setDebtHoldingPercent(new BigDecimal("12.30"));
        disc.setCashHoldingPercent(new BigDecimal("3.20"));
        disc.setSource(PROVIDER_NAME);
        return disc;
    }

    @Override
    public List<FundHolding> fetchHoldings(Long disclosureId, String schemeCode, LocalDate dataAsOf) {
        LocalDate publishedAt = dataAsOf.plusMonths(1).withDayOfMonth(12);
        Instant availableAt = publishedAt.atTime(18, 30).atZone(IST_ZONE).toInstant();

        List<FundHolding> holdings = new ArrayList<>();
        holdings.add(new FundHolding(
            disclosureId, 1L, 1L, "HDFCBANK", "HDFC Bank Ltd",
            dataAsOf, dataAsOf, publishedAt, availableAt,
            12500000L, new BigDecimal("20500000000.00"), new BigDecimal("8.25"),
            "EQUITY", "Financial Services", PROVIDER_NAME
        ));

        holdings.add(new FundHolding(
            disclosureId, 1L, 2L, "RELIANCE", "Reliance Industries Ltd",
            dataAsOf, dataAsOf, publishedAt, availableAt,
            7500000L, new BigDecimal("18200000000.00"), new BigDecimal("7.30"),
            "EQUITY", "Oil & Gas", PROVIDER_NAME
        ));

        holdings.add(new FundHolding(
            disclosureId, 1L, 3L, "INFY", "Infosys Ltd",
            dataAsOf, dataAsOf, publishedAt, availableAt,
            9000000L, new BigDecimal("14500000000.00"), new BigDecimal("5.85"),
            "EQUITY", "Information Technology", PROVIDER_NAME
        ));

        holdings.add(new FundHolding(
            disclosureId, 1L, 4L, "ICICIBANK", "ICICI Bank Ltd",
            dataAsOf, dataAsOf, publishedAt, availableAt,
            11000000L, new BigDecimal("13200000000.00"), new BigDecimal("5.30"),
            "EQUITY", "Financial Services", PROVIDER_NAME
        ));

        holdings.add(new FundHolding(
            disclosureId, 1L, 5L, "ITC", "ITC Ltd",
            dataAsOf, dataAsOf, publishedAt, availableAt,
            24000000L, new BigDecimal("11500000000.00"), new BigDecimal("4.60"),
            "EQUITY", "Fast Moving Consumer Goods", PROVIDER_NAME
        ));

        return holdings;
    }

    @Override
    public List<InstitutionalFlow> fetchDailyFlows(LocalDate fromDate, LocalDate toDate) {
        List<InstitutionalFlow> flows = new ArrayList<>();
        LocalDate curr = fromDate;
        while (!curr.isAfter(toDate)) {
            // NSE publishes end-of-day flow around 18:30 IST on trade day
            Instant availableAt = curr.atTime(18, 30).atZone(IST_ZONE).toInstant();

            // FII flow
            BigDecimal fiiBuy = new BigDecimal("8420.50");
            BigDecimal fiiSell = new BigDecimal("7110.20");
            flows.add(new InstitutionalFlow(
                curr, "NSE", InstitutionType.FII, FlowFrequency.DAILY,
                fiiBuy, fiiSell, fiiBuy.subtract(fiiSell), curr, curr, availableAt, PROVIDER_NAME
            ));

            // DII flow
            BigDecimal diiBuy = new BigDecimal("6350.10");
            BigDecimal diiSell = new BigDecimal("5120.40");
            flows.add(new InstitutionalFlow(
                curr, "NSE", InstitutionType.DII, FlowFrequency.DAILY,
                diiBuy, diiSell, diiBuy.subtract(diiSell), curr, curr, availableAt, PROVIDER_NAME
            ));

            curr = curr.plusDays(1);
        }
        return flows;
    }

    @Override
    public List<InstitutionalOwnership> fetchShareholdingPattern(String symbol, LocalDate quarterEnd) {
        // Quarterly disclosure published ~21 days after quarter end
        LocalDate publishedAt = quarterEnd.plusDays(21);
        Instant availableAt = publishedAt.atTime(18, 30).atZone(IST_ZONE).toInstant();

        List<InstitutionalOwnership> list = new ArrayList<>();
        list.add(new InstitutionalOwnership(
            1L, symbol, quarterEnd, quarterEnd, publishedAt, availableAt,
            InstitutionType.FII, "FOREIGN_PORTFOLIO_INVESTORS",
            new BigDecimal("34.50"), 1850000000L, new BigDecimal("2775000000000.00"),
            new BigDecimal("0.45"), PROVIDER_NAME
        ));

        list.add(new InstitutionalOwnership(
            1L, symbol, quarterEnd, quarterEnd, publishedAt, availableAt,
            InstitutionType.DII, "MUTUAL_FUNDS_AND_UTI",
            new BigDecimal("18.20"), 976000000L, new BigDecimal("1464000000000.00"),
            new BigDecimal("0.30"), PROVIDER_NAME
        ));

        list.add(new InstitutionalOwnership(
            1L, symbol, quarterEnd, quarterEnd, publishedAt, availableAt,
            InstitutionType.INSURANCE, "INSURANCE_COMPANIES",
            new BigDecimal("7.80"), 418000000L, new BigDecimal("627000000000.00"),
            new BigDecimal("-0.10"), PROVIDER_NAME
        ));

        return list;
    }
}
