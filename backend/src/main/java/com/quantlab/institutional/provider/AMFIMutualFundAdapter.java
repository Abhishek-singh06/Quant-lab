package com.quantlab.institutional.provider;

import com.quantlab.institutional.entity.AmcMaster;
import com.quantlab.institutional.entity.FundHolding;
import com.quantlab.institutional.entity.FundPortfolioDisclosure;
import com.quantlab.institutional.entity.InstitutionalFlow;
import com.quantlab.institutional.entity.InstitutionalOwnership;
import com.quantlab.institutional.entity.MutualFundScheme;
import com.quantlab.institutional.model.PortfolioScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for AMFI (Association of Mutual Funds in India) disclosures.
 * Mandatorily reflects monthly periodic disclosure lags (typically available ~10th-15th of following month).
 */
@Component("amfiMutualFundAdapter")
public class AMFIMutualFundAdapter implements InstitutionalDataProvider {

    private static final Logger log = LoggerFactory.getLogger(AMFIMutualFundAdapter.class);
    private static final String PROVIDER_NAME = "AMFI_INDIA";
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    private final RestClient restClient;
    private final boolean liveEnabled;

    public AMFIMutualFundAdapter(
            @Value("${quantlab.institutional.amfi.base-url:https://www.amfiindia.com}") String baseUrl,
            @Value("${quantlab.institutional.amfi.live-enabled:false}") boolean liveEnabled) {
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
        if (!liveEnabled) {
            log.info("AMFI live integration is disabled, skipping remote AMC fetch");
            return List.of();
        }
        log.info("Fetching AMC list from AMFI portal");
        return List.of();
    }

    @Override
    public List<MutualFundScheme> fetchSchemes(String amcCode) {
        if (!liveEnabled) {
            return List.of();
        }
        log.info("Fetching schemes for AMC {} from AMFI", amcCode);
        return List.of();
    }

    @Override
    public FundPortfolioDisclosure fetchPortfolioDisclosure(String schemeCode, LocalDate dataAsOf) {
        if (!liveEnabled) {
            return null;
        }
        // AMFI monthly disclosure rule: data_as_of end of month is published ~10th-15th of following month
        LocalDate publishedAt = dataAsOf.plusMonths(1).withDayOfMonth(10);
        Instant availableAt = publishedAt.atTime(18, 30).atZone(IST_ZONE).toInstant();

        FundPortfolioDisclosure disclosure = new FundPortfolioDisclosure();
        disclosure.setDataAsOf(dataAsOf);
        disclosure.setPublishedAt(publishedAt);
        disclosure.setAvailableAt(availableAt);
        disclosure.setPortfolioScope(PortfolioScope.COMPLETE);
        disclosure.setSource(PROVIDER_NAME);
        return disclosure;
    }

    @Override
    public List<FundHolding> fetchHoldings(Long disclosureId, String schemeCode, LocalDate dataAsOf) {
        if (!liveEnabled) {
            return List.of();
        }
        return new ArrayList<>();
    }

    @Override
    public List<InstitutionalFlow> fetchDailyFlows(LocalDate fromDate, LocalDate toDate) {
        // AMFI does not publish daily exchange flows; NSE/BSE does
        return List.of();
    }

    @Override
    public List<InstitutionalOwnership> fetchShareholdingPattern(String symbol, LocalDate quarterEnd) {
        // AMFI does not publish exchange-wide shareholding patterns
        return List.of();
    }
}
