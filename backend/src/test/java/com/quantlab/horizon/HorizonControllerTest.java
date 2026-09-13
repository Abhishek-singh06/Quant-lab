package com.quantlab.horizon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.horizon.controller.HorizonController;
import com.quantlab.horizon.model.*;
import com.quantlab.horizon.service.HorizonEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HorizonController.class)
@AutoConfigureMockMvc(addFilters = false)
public class HorizonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HorizonEngineService horizonService;

    @Test
    void testGetCrossHorizonView() throws Exception {
        CrossHorizonViewDTO mockView = new CrossHorizonViewDTO();
        mockView.setSymbol("RELIANCE");
        mockView.setAsOf(Instant.now());

        HorizonPredictionDTO st = new HorizonPredictionDTO();
        st.setId(UUID.randomUUID());
        st.setSymbol("RELIANCE");
        st.setHorizon(TradingHorizon.SHORT_TERM);
        st.setExpectedReturn(0.0082);
        st.setOutlook(HorizonOutlook.BULLISH);
        st.setConfidence(0.68);
        mockView.setShortTerm(st);

        HorizonPredictionDTO mt = new HorizonPredictionDTO();
        mt.setId(UUID.randomUUID());
        mt.setSymbol("RELIANCE");
        mt.setHorizon(TradingHorizon.MEDIUM_TERM);
        mt.setExpectedReturn(0.045);
        mt.setOutlook(HorizonOutlook.BULLISH);
        mt.setConfidence(0.72);
        mockView.setMediumTerm(mt);

        HorizonPredictionDTO lt = new HorizonPredictionDTO();
        lt.setId(UUID.randomUUID());
        lt.setSymbol("RELIANCE");
        lt.setHorizon(TradingHorizon.LONG_TERM);
        lt.setExpectedReturn(0.185);
        lt.setOutlook(HorizonOutlook.BULLISH);
        lt.setConfidence(0.84);
        mockView.setLongTerm(lt);

        HorizonConflictDTO conflict = new HorizonConflictDTO();
        conflict.setSymbol("RELIANCE");
        conflict.setConflictDetected(false);
        conflict.setConflictSeverity("NONE");
        conflict.setExplanation("Full bullish confluence");
        mockView.setConflict(conflict);

        when(horizonService.getCrossHorizonView(eq("RELIANCE"))).thenReturn(mockView);

        mockMvc.perform(get("/api/v1/horizons/RELIANCE/cross-horizon")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("RELIANCE"))
                .andExpect(jsonPath("$.shortTerm.expectedReturn").value(0.0082))
                .andExpect(jsonPath("$.mediumTerm.expectedReturn").value(0.045))
                .andExpect(jsonPath("$.longTerm.expectedReturn").value(0.185))
                .andExpect(jsonPath("$.conflict.conflictDetected").value(false));
    }

    @Test
    void testGetLatestPrediction() throws Exception {
        HorizonPredictionDTO st = new HorizonPredictionDTO();
        st.setId(UUID.randomUUID());
        st.setSymbol("TCS");
        st.setHorizon(TradingHorizon.SHORT_TERM);
        st.setExpectedReturn(0.012);
        st.setOutlook(HorizonOutlook.BULLISH);
        st.setConfidence(0.75);

        when(horizonService.getLatestPrediction(eq("TCS"), eq(TradingHorizon.SHORT_TERM))).thenReturn(st);

        mockMvc.perform(get("/api/v1/horizons/TCS/predictions/SHORT_TERM/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("TCS"))
                .andExpect(jsonPath("$.horizon").value("SHORT_TERM"))
                .andExpect(jsonPath("$.expectedReturn").value(0.012));
    }
}
