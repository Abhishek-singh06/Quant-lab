package com.quantlab.fundamental.provider;

import com.quantlab.fundamental.entity.FinancialStatement;
import com.quantlab.fundamental.entity.FundamentalFiling;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;

import java.util.List;

/**
 * Provider interface contract for ingesting legitimate fundamental corporate filings
 * and financial statements for Indian listed companies.
 */
public interface FundamentalDataProvider {

    String getProviderName();

    boolean isAvailable();

    List<FundamentalFiling> fetchCompanyFilings(String symbol);

    FinancialStatement fetchFinancialStatement(FundamentalFiling filing);

    List<FinancialStatement> fetchHistoricalStatements(String symbol, PeriodType periodType, ReportingBasis basis);
}
