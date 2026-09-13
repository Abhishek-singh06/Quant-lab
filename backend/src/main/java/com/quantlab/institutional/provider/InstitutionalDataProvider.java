package com.quantlab.institutional.provider;

import com.quantlab.institutional.entity.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Provider contract for ingesting mutual fund portfolios, institutional shareholding,
 * and FII/DII institutional cash flow data from real Indian market sources.
 */
public interface InstitutionalDataProvider {

    String getProviderName();

    boolean isAvailable();

    List<AmcMaster> fetchAmcList();

    List<MutualFundScheme> fetchSchemes(String amcCode);

    FundPortfolioDisclosure fetchPortfolioDisclosure(String schemeCode, LocalDate dataAsOf);

    List<FundHolding> fetchHoldings(Long disclosureId, String schemeCode, LocalDate dataAsOf);

    List<InstitutionalFlow> fetchDailyFlows(LocalDate fromDate, LocalDate toDate);

    List<InstitutionalOwnership> fetchShareholdingPattern(String symbol, LocalDate quarterEnd);
}
