package com.quantlab.marketdata.controller;

import com.quantlab.global.model.GlobalMarketRegimeDTO;
import com.quantlab.global.service.GlobalMarketAnalyticsService;
import com.quantlab.marketdata.entity.MarketDataRecord;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.MarketStatusInfo;
import com.quantlab.marketdata.model.ProviderHealthState;
import com.quantlab.marketdata.service.MarketDataQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MarketTerminalOverviewControllerTest {

    private MarketDataQueryService queryService;
    private GlobalMarketAnalyticsService analyticsService;
    private MarketTerminalOverviewController controller;

    @BeforeEach
    void setUp() {
        queryService = Mockito.mock(MarketDataQueryService.class);
        analyticsService = Mockito.mock(GlobalMarketAnalyticsService.class);
        controller = new MarketTerminalOverviewController(queryService, analyticsService);
    }

    @Test
    @DisplayName("Should assemble terminal overview with live or fallback index metrics")
    void testGetTerminalOverview() {
        when(queryService.getActiveProviderName()).thenReturn("NSE_DIRECT");
        when(queryService.getProviderHealthState()).thenReturn(ProviderHealthState.HEALTHY);
        when(queryService.getMarketStatus(Exchange.NSE)).thenReturn(new MarketStatusInfo(
                Exchange.NSE, "OPEN", true, "Regular Trading", Instant.now(), "NSE_DIRECT"
        ));

        MarketDataRecord rec = new MarketDataRecord();
        rec.setSymbol("NIFTY 50");
        rec.setClose(new BigDecimal("25000.00"));
        rec.setOpen(new BigDecimal("24900.00"));
        rec.setTimestamp(Instant.now());
        rec.setSource("NSE_DIRECT");
        rec.setFreshnessStatus("REALTIME");

        when(queryService.getLatestQuotes(anyList(), eq(Exchange.NSE))).thenReturn(List.of(rec));

        GlobalMarketRegimeDTO regime = new GlobalMarketRegimeDTO(
                Instant.now(), "BULL_TREND", 0.65, 0.8, -0.4, 0.2, -0.3, 0.5, 0.7, "Risk-on equities", true
        );
        when(analyticsService.getLatestRegime(any())).thenReturn(regime);

        ResponseEntity<MarketTerminalOverviewController.TerminalOverviewResponse> response = controller.getTerminalOverview();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        MarketTerminalOverviewController.TerminalOverviewResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("NSE_DIRECT", body.provider());
        assertEquals("HEALTHY", body.providerStatus());
        assertEquals(4, body.majorIndices().size());
        assertEquals(6, body.sectors().size());
        assertNotNull(body.globalRegime());
        assertEquals("BULL_TREND", body.globalRegime().getRegimeLabel());
    }
}
