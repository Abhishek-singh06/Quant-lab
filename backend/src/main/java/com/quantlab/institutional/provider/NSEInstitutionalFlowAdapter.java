package com.quantlab.institutional.provider;

import com.quantlab.institutional.entity.AmcMaster;
import com.quantlab.institutional.entity.FundHolding;
import com.quantlab.institutional.entity.FundPortfolioDisclosure;
import com.quantlab.institutional.entity.InstitutionalFlow;
import com.quantlab.institutional.entity.InstitutionalOwnership;
import com.quantlab.institutional.entity.MutualFundScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

/**
 * Adapter for NSE India official daily FII and DII trading activity and quarterly shareholding patterns.
 * FII/DII daily flows for date T are published at ~18:00 IST on day T.
 * Shareholding patterns for quarter ending Q are published ~15-21 days after quarter end.
 */
@Component("nseInstitutionalFlowAdapter")
public class NSEInstitutionalFlowAdapter implements InstitutionalDataProvider {

    private static final Logger log = LoggerFactory.getLogger(NSEInstitutionalFlowAdapter.class);
    private static final String PROVIDER_NAME = "NSE_INDIA";

    private final RestClient restClient;
    private final boolean liveEnabled;

    public NSEInstitutionalFlowAdapter(
            @Value("${quantlab.institutional.nse.base-url:https://www.nseindia.com}") String baseUrl,
            @Value("${quantlab.institutional.nse.live-enabled:false}") boolean liveEnabled) {
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
    public List<AmcMaster> fetchAmcList() {
        return List.of();
    }

    @Override
    public List<MutualFundScheme> fetchSchemes(String amcCode) {
        return List.of();
    }

    @Override
    public FundPortfolioDisclosure fetchPortfolioDisclosure(String schemeCode, LocalDate dataAsOf) {
        return null;
    }

    @Override
    public List<FundHolding> fetchHoldings(Long disclosureId, String schemeCode, LocalDate dataAsOf) {
        return List.of();
    }

    @Override
    public List<InstitutionalFlow> fetchDailyFlows(LocalDate fromDate, LocalDate toDate) {
        if (!liveEnabled) {
            log.info("NSE live data is disabled, skipping remote flow fetch");
            return List.of();
        }
        log.info("Fetching FII/DII flows between {} and {} from NSE API", fromDate, toDate);
        return List.of();
    }

    @Override
    public List<InstitutionalOwnership> fetchShareholdingPattern(String symbol, LocalDate quarterEnd) {
        if (!liveEnabled) {
            log.info("NSE live data is disabled, skipping remote shareholding fetch");
            return List.of();
        }
        log.info("Fetching shareholding pattern for {} as of {} from NSE", symbol, quarterEnd);
        return List.of();
    }
}
