package com.quantlab.backtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.backtest.controller.BacktestController;
import com.quantlab.backtest.model.*;
import com.quantlab.backtest.service.BacktestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

@WebMvcTest(BacktestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BacktestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BacktestService backtestService;

    @Test
    void testGetAllRuns() throws Exception {
        UUID runId = UUID.randomUUID();
        BacktestRunDTO run = new BacktestRunDTO(
                runId,
                UUID.randomUUID(),
                "NIFTY_MOMENTUM_RUN",
                BacktestStatus.COMPLETED,
                "v1.0.0",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30),
                125,
                18,
                1000000.0,
                1145200.0,
                145200.0,
                2450.0,
                1120.0,
                4800.0,
                null,
                185L,
                "PRODUCTION_READY",
                Map.of("trust_level", "PRODUCTION_READY"),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(backtestService.getAllRuns()).thenReturn(List.of(run));

        mockMvc.perform(get("/api/v1/backtests/runs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("NIFTY_MOMENTUM_RUN"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].finalEquity").value(1145200.0))
                .andExpect(jsonPath("$[0].totalTradesCount").value(18));
    }

    @Test
    void testGetBacktestResult() throws Exception {
        UUID runId = UUID.randomUUID();
        PerformanceMetricsDTO metrics = new PerformanceMetricsDTO(
                14.52,
                18.4,
                12.3,
                1.68,
                2.15,
                4.85,
                14,
                3.79,
                66.67,
                2.45,
                1.25,
                2.80,
                -1.45,
                1.93,
                18,
                12,
                6,
                3.4,
                0.78,
                4.25,
                1.12,
                Map.of(),
                Map.of(),
                Map.of()
        );

        BacktestResultDTO result = new BacktestResultDTO(
                runId,
                null,
                null,
                metrics,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        when(backtestService.getBacktestResult(eq(runId))).thenReturn(Optional.of(result));

        mockMvc.perform(get("/api/v1/backtests/runs/" + runId + "/result")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId").value(runId.toString()))
                .andExpect(jsonPath("$.metrics.sharpeRatio").value(1.68))
                .andExpect(jsonPath("$.metrics.totalReturnPct").value(14.52))
                .andExpect(jsonPath("$.metrics.winRatePct").value(66.67));
    }

    @Test
    void testExecuteBacktest() throws Exception {
        BacktestConfigDTO cfg = new BacktestConfigDTO(
                null,
                "API_TRIGGERED_BT",
                "Testing API",
                "SHORT_TERM",
                "NIFTY_50",
                List.of("RELIANCE", "TCS"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30),
                1000000.0,
                0.05,
                "DAILY",
                "NEXT_BAR_OPEN",
                CostModelType.REALISTIC_INDIAN,
                SlippageModelType.FIXED_BPS,
                3.0,
                10.0,
                2.5,
                0.345,
                0.18,
                1.5,
                5.0,
                0.20,
                0.35,
                0.15,
                "NIFTY_50",
                "v1.0.0"
        );

        UUID runId = UUID.randomUUID();
        BacktestRunDTO run = new BacktestRunDTO(
                runId,
                UUID.randomUUID(),
                "RUN_API_TRIGGERED_BT",
                BacktestStatus.COMPLETED,
                "v1.0.0",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30),
                125,
                18,
                1000000.0,
                1145200.0,
                145200.0,
                2450.0,
                1120.0,
                4800.0,
                null,
                185L,
                "PRODUCTION_READY",
                Map.of(),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        );

        when(backtestService.executeSimulation(any(BacktestConfigDTO.class))).thenReturn(run);

        mockMvc.perform(post("/api/v1/backtests/run")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cfg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("RUN_API_TRIGGERED_BT"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
