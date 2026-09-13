package com.quantlab.global;

import com.quantlab.global.controller.GlobalMarketIntelligenceController;
import com.quantlab.global.model.DataFreshness;
import com.quantlab.global.model.GlobalMarketRegimeDTO;
import com.quantlab.global.model.GlobalMarketSnapshotDTO;
import com.quantlab.global.model.RegimeConfidence;
import com.quantlab.global.model.RegimeLabel;
import com.quantlab.global.model.SessionStatus;
import com.quantlab.global.service.GlobalMarketAnalyticsService;
import com.quantlab.global.service.GlobalMarketIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalMarketIntelligenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class GlobalMarketIntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GlobalMarketIngestionService ingestionService;

    @MockBean
    private GlobalMarketAnalyticsService analyticsService;

    @Test
    void testGetLatestSnapshotsExposesFreshnessAndSource() throws Exception {
        GlobalMarketSnapshotDTO dto = new GlobalMarketSnapshotDTO();
        dto.setCanonicalSymbol("SPX");
        dto.setInstrumentName("S&P 500 Index");
        dto.setClose(new BigDecimal("5864.67"));
        dto.setChangePercent(new BigDecimal("0.75"));
        dto.setSource("AUTHORIZED_GLOBAL_FEED");
        dto.setDataFreshness(DataFreshness.DELAYED);
        dto.setSessionStatus(SessionStatus.OPEN);
        dto.setAgeMinutes(15L);

        when(analyticsService.getLatestSnapshots(any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/global/snapshots/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].canonicalSymbol").value("SPX"))
                .andExpect(jsonPath("$[0].dataFreshness").value("DELAYED"))
                .andExpect(jsonPath("$[0].source").value("AUTHORIZED_GLOBAL_FEED"))
                .andExpect(jsonPath("$[0].sessionStatus").value("OPEN"));
    }

    @Test
    void testGetLatestRegimeReturnsExplainableComponents() throws Exception {
        GlobalMarketRegimeDTO regimeDTO = new GlobalMarketRegimeDTO();
        regimeDTO.setRegimeLabel(RegimeLabel.RISK_ON);
        regimeDTO.setCompositeScore(new BigDecimal("42.5000"));
        regimeDTO.setEquityScore(new BigDecimal("55.0000"));
        regimeDTO.setVolatilityScore(new BigDecimal("35.0000"));
        regimeDTO.setConfidence(RegimeConfidence.HIGH);
        regimeDTO.setExplanation("Global risk appetite positive led by broad US rally.");

        when(analyticsService.getLatestRegime(any())).thenReturn(regimeDTO);

        mockMvc.perform(get("/api/v1/global/regime/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.regimeLabel").value("RISK_ON"))
                .andExpect(jsonPath("$.compositeScore").value(42.5000))
                .andExpect(jsonPath("$.confidence").value("HIGH"));
    }
}
