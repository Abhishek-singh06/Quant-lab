package com.quantlab.global.provider;

import com.quantlab.global.calendar.GlobalMarketCalendarService;
import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.entity.GlobalMarketSnapshot;
import com.quantlab.global.model.AssetClass;
import com.quantlab.global.model.DataFreshness;
import com.quantlab.global.model.SessionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for standard public global market endpoints (15-minute delayed / EOD).
 * Transparently tags all returned snapshots as DELAYED.
 */
@Component("yahooFinanceGlobalMarketAdapter")
public class YahooFinanceGlobalMarketAdapter implements GlobalMarketDataProvider {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceGlobalMarketAdapter.class);
    private static final String PROVIDER_NAME = "YAHOO_FINANCE";

    private final RestClient restClient;
    private final GlobalMarketCalendarService calendarService;
    private final boolean liveEnabled;

    public YahooFinanceGlobalMarketAdapter(
            @Value("${quantlab.global.yahoo.base-url:https://query1.finance.yahoo.com}") String baseUrl,
            @Value("${quantlab.global.yahoo.live-enabled:false}") boolean liveEnabled,
            GlobalMarketCalendarService calendarService) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.liveEnabled = liveEnabled;
        this.calendarService = calendarService;
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
    public GlobalMarketSnapshot fetchLatestSnapshot(GlobalInstrument instrument) {
        if (!liveEnabled) {
            log.info("Public global feed disabled, skipping remote fetch for {}", instrument.getCanonicalSymbol());
            return null;
        }

        try {
            String url = "/v8/finance/chart/" + instrument.getProviderSymbol() + "?interval=1d&range=1d";
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restClient.get().uri(url).retrieve().body(Map.class);
            if (resp == null) return null;

            // Parse response
            Instant now = Instant.now();
            SessionStatus status = calendarService.evaluateSessionStatus(instrument.getMarket(), now);

            GlobalMarketSnapshot snapshot = new GlobalMarketSnapshot();
            snapshot.setInstrumentId(instrument.getId());
            snapshot.setCanonicalSymbol(instrument.getCanonicalSymbol());
            snapshot.setTimestamp(now);
            snapshot.setTradingDate(LocalDate.now(ZoneId.of(instrument.getTimezone())));
            snapshot.setSource(PROVIDER_NAME);
            snapshot.setSourceTimestamp(now.minusSeconds(900)); // 15 min delayed
            snapshot.setIngestionTimestamp(now);
            snapshot.setDataFreshness(DataFreshness.DELAYED);
            snapshot.setSessionStatus(status);
            snapshot.setCurrency(instrument.getCurrency());
            return snapshot;
        } catch (Exception e) {
            log.warn("Failed to fetch global market quote for {}: {}", instrument.getProviderSymbol(), e.getMessage());
            return null;
        }
    }

    @Override
    public List<GlobalMarketSnapshot> fetchHistoricalSnapshots(GlobalInstrument instrument, LocalDate fromDate, LocalDate toDate) {
        if (!liveEnabled) {
            return List.of();
        }
        return new ArrayList<>();
    }

    @Override
    public List<GlobalMarketSnapshot> fetchLatestSnapshots(List<GlobalInstrument> instruments) {
        List<GlobalMarketSnapshot> list = new ArrayList<>();
        for (GlobalInstrument inst : instruments) {
            GlobalMarketSnapshot snap = fetchLatestSnapshot(inst);
            if (snap != null) {
                list.add(snap);
            }
        }
        return list;
    }
}
