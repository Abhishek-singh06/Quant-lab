package com.quantlab.fundamental.provider;

import com.quantlab.fundamental.entity.FinancialStatement;
import com.quantlab.fundamental.entity.FundamentalFiling;
import com.quantlab.fundamental.model.PeriodType;
import com.quantlab.fundamental.model.ReportingBasis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for NSE India official corporate filings and financial results.
 */
@Component("nseFundamentalDataProvider")
public class NSEFundamentalDataProvider implements FundamentalDataProvider {

    private static final Logger log = LoggerFactory.getLogger(NSEFundamentalDataProvider.class);
    private static final String PROVIDER_NAME = "NSE_XBRL";

    private final RestClient restClient;
    private final boolean liveEnabled;

    public NSEFundamentalDataProvider(
            @Value("${quantlab.fundamental.nse.base-url:https://www.nseindia.com}") String baseUrl,
            @Value("${quantlab.fundamental.nse.live-enabled:false}") boolean liveEnabled) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.liveEnabled = liveEnabled;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return liveEnabled;
    }

    @Override
    public List<FundamentalFiling> fetchCompanyFilings(String symbol) {
        if (!liveEnabled) {
            log.info("NSE live fundamental integration disabled, skipping remote fetch for {}", symbol);
            return List.of();
        }
        return new ArrayList<>();
    }

    @Override
    public FinancialStatement fetchFinancialStatement(FundamentalFiling filing) {
        if (!liveEnabled) {
            return null;
        }
        return null;
    }

    @Override
    public List<FinancialStatement> fetchHistoricalStatements(String symbol, PeriodType periodType, ReportingBasis basis) {
        if (!liveEnabled) {
            return List.of();
        }
        return new ArrayList<>();
    }
}
