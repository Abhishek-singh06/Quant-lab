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
 * Adapter for licensed enterprise fundamental data feeds.
 */
@Component("licensedFundamentalDataProvider")
public class LicensedFundamentalDataProvider implements FundamentalDataProvider {

    private static final Logger log = LoggerFactory.getLogger(LicensedFundamentalDataProvider.class);
    private static final String PROVIDER_NAME = "AUTHORIZED_FUNDAMENTAL_FEED";

    private final RestClient restClient;
    private final boolean liveEnabled;
    private final String apiKey;

    public LicensedFundamentalDataProvider(
            @Value("${quantlab.fundamental.authorized.base-url:https://api.fundamentaldata.com}") String baseUrl,
            @Value("${quantlab.fundamental.authorized.api-key:}") String apiKey,
            @Value("${quantlab.fundamental.authorized.live-enabled:false}") boolean liveEnabled) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.liveEnabled = liveEnabled;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return liveEnabled && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public List<FundamentalFiling> fetchCompanyFilings(String symbol) {
        if (!isAvailable()) {
            log.info("Authorized fundamental provider disabled or unconfigured, skipping remote call for {}", symbol);
            return List.of();
        }
        return new ArrayList<>();
    }

    @Override
    public FinancialStatement fetchFinancialStatement(FundamentalFiling filing) {
        if (!isAvailable()) {
            return null;
        }
        return null;
    }

    @Override
    public List<FinancialStatement> fetchHistoricalStatements(String symbol, PeriodType periodType, ReportingBasis basis) {
        if (!isAvailable()) {
            return List.of();
        }
        return new ArrayList<>();
    }
}
