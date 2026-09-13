package com.quantlab.regime;

import com.quantlab.regime.controller.MarketRegimeController;
import com.quantlab.regime.model.DirectionRegime;
import com.quantlab.regime.model.MarketRegimeDTO;
import com.quantlab.regime.model.RegimeEvaluationDTO;
import com.quantlab.regime.model.RiskRegime;
import com.quantlab.regime.model.VolatilityRegime;
import com.quantlab.regime.service.MarketRegimeEngineService;
import com.quantlab.regime.service.RegimeEvaluationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MarketRegimeController.class)
@AutoConfigureMockMvc(addFilters = false)
class MarketRegimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MarketRegimeEngineService regimeEngineService;

    @MockBean
    private RegimeEvaluationService evaluationService;

    @Test
    void testGetLatestRegimeReturnsRegimeDTO() throws Exception {
        MarketRegimeDTO dto = new MarketRegimeDTO();
        dto.setSymbol("NIFTY 50");
        dto.setTradingDate(LocalDate.of(2026, 9, 12));
        dto.setRegimeTimestamp(Instant.parse("2026-09-12T10:00:00Z"));
        dto.setDirectionRegime(DirectionRegime.BULL);
        dto.setVolatilityRegime(VolatilityRegime.LOW_VOL);
        dto.setRiskRegime(RiskRegime.RISK_ON);
        dto.setDirectionScore(new BigDecimal("42.5000"));
        dto.setConfidence(new BigDecimal("0.8500"));
        dto.setExplanation("Market classified as BULL with 85.0% confidence.");

        when(regimeEngineService.getLatestRegime(eq("NIFTY 50"), any())).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/regime/latest?symbol=NIFTY 50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("NIFTY 50"))
                .andExpect(jsonPath("$.directionRegime").value("BULL"))
                .andExpect(jsonPath("$.volatilityRegime").value("LOW_VOL"))
                .andExpect(jsonPath("$.riskRegime").value("RISK_ON"))
                .andExpect(jsonPath("$.confidence").value(0.85));
    }

    @Test
    void testEvaluateWalkForwardReturnsRunDTO() throws Exception {
        RegimeEvaluationDTO eval = new RegimeEvaluationDTO();
        eval.setRunId("RUN-TEST1234");
        eval.setModelVersion("v1.0.0-multi-signal-composite");
        eval.setEvaluationType("WALK_FORWARD");
        eval.setBullSharpe(new BigDecimal("1.8500"));
        eval.setBaseline1Sma50Sharpe(new BigDecimal("1.1200"));
        eval.setSummaryReport(Map.of("bullAccuracyHitRate", 0.742));

        when(evaluationService.runWalkForwardEvaluation(any(), any(), any(), any(), any())).thenReturn(eval);

        mockMvc.perform(post("/api/v1/regime/evaluate/walk-forward")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId").value("RUN-TEST1234"))
                .andExpect(jsonPath("$.bullSharpe").value(1.85))
                .andExpect(jsonPath("$.baseline1Sma50Sharpe").value(1.12));
    }
}
