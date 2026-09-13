package com.quantlab.marketdata.controller;

import com.quantlab.marketdata.entity.MarketDataIngestionRun;
import com.quantlab.marketdata.entity.MarketDataRecord;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.model.IngestionRunStatus;
import com.quantlab.marketdata.model.MarketStatusInfo;
import com.quantlab.marketdata.service.MarketDataIngestionService;
import com.quantlab.marketdata.service.MarketDataQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MarketDataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarketDataQueryService queryService;

    @MockBean
    private MarketDataIngestionService ingestionService;

    @Test
    void getQuoteReturnsOkWhenFound() throws Exception {
        MarketDataRecord record = new MarketDataRecord();
        record.setSymbol("RELIANCE");
        record.setExchange(Exchange.NSE);
        record.setLastPrice(new BigDecimal("2450.50"));
        record.setTimestamp(Instant.now());

        when(queryService.getLatestQuote(eq("RELIANCE"), eq(Exchange.NSE))).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/v1/market-data/quotes/RELIANCE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("RELIANCE"))
            .andExpect(jsonPath("$.lastPrice").value(2450.50));
    }

    @Test
    void getMarketStatusReturnsOk() throws Exception {
        MarketStatusInfo info = new MarketStatusInfo(
            Exchange.NSE, "Normal Trading", true, "2026-09-12", Instant.now(), "Market Open"
        );

        when(queryService.getMarketStatus(any())).thenReturn(info);

        mockMvc.perform(get("/api/v1/market-data/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exchange").value("NSE"))
            .andExpect(jsonPath("$.isOpen").value(true));
    }

    @Test
    void postIngestTriggersRun() throws Exception {
        MarketDataIngestionRun run = new MarketDataIngestionRun("run-123", "MOCK", Instant.now());
        run.setRecordsReceived(10);
        run.setRecordsAccepted(10);
        run.complete(IngestionRunStatus.SUCCESS);

        when(ingestionService.ingestQuotes(any(), any())).thenReturn(run);

        mockMvc.perform(post("/api/v1/market-data/ingest"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.runId").value("run-123"))
            .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void getProviderHealthReturnsOk() throws Exception {
        when(queryService.getActiveProviderName()).thenReturn("NSE");
        when(queryService.getProviderHealthState()).thenReturn(com.quantlab.marketdata.model.ProviderHealthState.NOT_CONFIGURED);

        mockMvc.perform(get("/api/v1/market-data/provider/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.provider").value("NSE"))
            .andExpect(jsonPath("$.healthState").value("NOT_CONFIGURED"))
            .andExpect(jsonPath("$.isConfigured").value(false));
    }

    @Test
    void getProviderSmokeTestReturnsResult() throws Exception {
        com.quantlab.marketdata.model.ProviderSmokeTestResult result = new com.quantlab.marketdata.model.ProviderSmokeTestResult(
            "NSE",
            com.quantlab.marketdata.model.ProviderHealthState.CONNECTIVITY_OK,
            true,
            true,
            "Non-trading smoke test passed",
            45L,
            Instant.now(),
            java.util.Map.of("marketState", "Normal Trading")
        );

        when(queryService.runProviderSmokeTest()).thenReturn(result);

        mockMvc.perform(get("/api/v1/market-data/provider/smoke-test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.providerName").value("NSE"))
            .andExpect(jsonPath("$.healthState").value("CONNECTIVITY_OK"))
            .andExpect(jsonPath("$.connectivityOk").value(true));
    }
}
