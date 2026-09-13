package com.quantlab.paper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.paper.controller.PaperTradingController;
import com.quantlab.paper.model.*;
import com.quantlab.paper.service.PaperTradingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaperTradingController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaperTradingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaperTradingService paperTradingService;

    @Test
    void testGetAllSessions() throws Exception {
        UUID sessionId = UUID.randomUUID();
        PaperTradingSessionDTO session = new PaperTradingSessionDTO(
                sessionId,
                "PAPER-LIVE-SESSION-001",
                ExecutionMode.PAPER_TRADING,
                PaperTradingStatus.RUNNING,
                ClockType.LIVE_CLOCK,
                "AUTHORIZED_NSE_FEED",
                DataFreshnessStatus.REAL_TIME,
                Instant.now(),
                null,
                "v1.0.0",
                "v1.0.0",
                12,
                8,
                8,
                null,
                Instant.now(),
                Instant.now()
        );

        when(paperTradingService.getAllSessions()).thenReturn(List.of(session));

        mockMvc.perform(get("/api/v1/paper/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("PAPER-LIVE-SESSION-001"))
                .andExpect(jsonPath("$[0].executionMode").value("PAPER_TRADING"))
                .andExpect(jsonPath("$[0].status").value("RUNNING"));
    }

    @Test
    void testGetPortfolios() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        PaperPortfolioDTO portfolio = new PaperPortfolioDTO(
                portfolioId,
                UUID.randomUUID(),
                "PORTFOLIO-PAPER-ST-001",
                "SHORT_TERM",
                UUID.randomUUID(),
                "INR",
                1000000.0,
                850000.0,
                850000.0,
                0.0,
                156200.0,
                1006200.0,
                1012000.0,
                0.57,
                1.2,
                0.155,
                0.155,
                1.0,
                8400.0,
                -2200.0,
                120.0,
                45.0,
                0.0,
                "ACTIVE",
                Instant.now(),
                Instant.now()
        );

        when(paperTradingService.getAllPortfolios()).thenReturn(List.of(portfolio));

        mockMvc.perform(get("/api/v1/paper/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("PORTFOLIO-PAPER-ST-001"))
                .andExpect(jsonPath("$[0].currency").value("INR"))
                .andExpect(jsonPath("$[0].totalPortfolioValue").value(1006200.0));
    }

    @Test
    void testGetDecisions() throws Exception {
        UUID decisionId = UUID.randomUUID();
        PaperTradingDecisionDTO decision = new PaperTradingDecisionDTO(
                decisionId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RELIANCE",
                Instant.now(),
                "SHORT_TERM",
                "BUY",
                "BUY signal generated with 82% confidence",
                UUID.randomUUID(),
                "v1.0.0",
                78.5,
                0.82,
                0.021,
                0.014,
                "POSITIVE",
                0.78,
                UUID.randomUUID(),
                "GB_1D_v1.0",
                UUID.randomUUID(),
                "v1.0.0",
                0.08,
                0.10,
                32,
                2950.0,
                2890.0,
                3080.0,
                "MODERATE",
                "{\"technical\": \"bullish_crossover\"}",
                "{}",
                "HIGH_QUALITY",
                "1",
                "v1.0.0",
                Instant.now(),
                Instant.now(),
                "EXECUTED",
                Instant.now()
        );

        when(paperTradingService.getAllDecisions()).thenReturn(List.of(decision));

        mockMvc.perform(get("/api/v1/paper/decisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("RELIANCE"))
                .andExpect(jsonPath("$[0].decision").value("BUY"))
                .andExpect(jsonPath("$[0].status").value("EXECUTED"));
    }

    @Test
    void testGetHealthReport() throws Exception {
        PaperTradingHealthReportDTO healthReport = new PaperTradingHealthReportDTO(
                "HEALTHY",
                ExecutionMode.PAPER_TRADING,
                true,
                "AUTHORIZED_NSE_FEED",
                Map.of(
                        "MARKET_DATA", "PASS",
                        "FEATURE_ENGINE", "PASS",
                        "MODEL", "PASS",
                        "SIGNAL", "PASS",
                        "RISK", "PASS",
                        "PORTFOLIO", "PASS",
                        "DATABASE", "PASS",
                        "EXECUTION", "PASS"
                ),
                "Paper trading engine is operational in isolated mode (zero real money / broker routing disabled).",
                Instant.now()
        );

        when(paperTradingService.getHealthReport()).thenReturn(healthReport);

        mockMvc.perform(get("/api/v1/paper/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.executionMode").value("PAPER_TRADING"))
                .andExpect(jsonPath("$.liveDataAvailable").value(true));
    }
}
