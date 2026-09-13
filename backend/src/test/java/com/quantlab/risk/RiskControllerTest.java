package com.quantlab.risk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.risk.controller.RiskController;
import com.quantlab.risk.model.*;
import com.quantlab.risk.service.RiskAssessmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RiskAssessmentService riskService;

    @Test
    void testGetProfiles() throws Exception {
        RiskProfileDTO profile = new RiskProfileDTO(
                UUID.randomUUID(),
                "MODERATE",
                RiskProfileType.MODERATE,
                0.05,
                0.01,
                0.10,
                0.25,
                0.15,
                0.10,
                0.70,
                0.15,
                0.20,
                1000000.0,
                StopMethod.ATR_MULTIPLE,
                PositionSizingMethod.FIXED_RISK,
                false,
                false,
                1.0,
                0.05,
                0.50,
                35.0,
                "VOLATILITY_ADJUSTED",
                true,
                "RP_v1.0.0",
                Instant.now(),
                Instant.now()
        );

        when(riskService.getProfiles()).thenReturn(List.of(profile));

        mockMvc.perform(get("/api/v1/risk/profiles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MODERATE"))
                .andExpect(jsonPath("$[0].maxPortfolioRisk").value(0.05))
                .andExpect(jsonPath("$[0].maxPositionRisk").value(0.01));
    }

    @Test
    void testAssessRisk() throws Exception {
        RiskAssessmentRequestDTO request = new RiskAssessmentRequestDTO();
        request.setSymbol("TCS");
        request.setSignalType("BUY");
        request.setSignalScore(72.0);
        request.setSignalConfidence(0.85);
        request.setEntryPrice(3850.0);
        request.setTargetPrice(4200.0);
        request.setAtr(60.0);
        request.setStopMethod(StopMethod.ATR_MULTIPLE);
        request.setSizingMethod(PositionSizingMethod.FIXED_RISK);

        RiskAssessmentDTO response = new RiskAssessmentDTO();
        response.setId(UUID.randomUUID());
        response.setSymbol("TCS");
        response.setSignalType("BUY");
        response.setSignalScore(72.0);
        response.setSignalConfidence(0.85);
        response.setEntryPrice(3850.0);
        response.setStopPrice(3730.0);
        response.setStopDistance(120.0);
        response.setStopDistancePct(0.0312);
        response.setSuggestedAllocation(0.08);
        response.setMaximumAllocation(0.10);
        response.setRecommendedQuantity(20.0);
        response.setEstimatedDownside(2400.0);
        response.setPositionRiskAmount(2400.0);
        response.setPositionRiskPercent(0.0024);
        response.setPortfolioValue(1000000.0);
        response.setRiskDecision(RiskDecision.APPROVED);
        response.setRiskLevel(RiskLevel.MODERATE);
        response.setReasoning("Risk assessment approved. Suggested allocation: 8.00%.");

        when(riskService.assessRisk(any(RiskAssessmentRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/risk/assess")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("TCS"))
                .andExpect(jsonPath("$.suggestedAllocation").value(0.08))
                .andExpect(jsonPath("$.recommendedQuantity").value(20.0))
                .andExpect(jsonPath("$.estimatedDownside").value(2400.0))
                .andExpect(jsonPath("$.riskDecision").value("APPROVED"))
                .andExpect(jsonPath("$.riskLevel").value("MODERATE"));
    }

    @Test
    void testGetLatestAssessment() throws Exception {
        RiskAssessmentDTO response = new RiskAssessmentDTO();
        response.setId(UUID.randomUUID());
        response.setSymbol("INFY");
        response.setSignalType("BUY");
        response.setSignalScore(65.0);
        response.setSignalConfidence(0.78);
        response.setSuggestedAllocation(0.06);
        response.setRiskDecision(RiskDecision.APPROVED);
        response.setRiskLevel(RiskLevel.LOW);

        when(riskService.getLatestAssessment(eq("INFY"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/risk/assessments/INFY/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("INFY"))
                .andExpect(jsonPath("$.suggestedAllocation").value(0.06))
                .andExpect(jsonPath("$.riskDecision").value("APPROVED"));
    }
}
