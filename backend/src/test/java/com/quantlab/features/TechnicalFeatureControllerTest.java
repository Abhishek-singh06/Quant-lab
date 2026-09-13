package com.quantlab.features;

import com.quantlab.features.controller.TechnicalFeatureController;
import com.quantlab.features.model.FeatureCategory;
import com.quantlab.features.model.FeatureDefinitionDTO;
import com.quantlab.features.model.PriceSeriesType;
import com.quantlab.features.model.TechnicalFeatureDTO;
import com.quantlab.features.service.TechnicalFeatureBatchService;
import com.quantlab.features.service.TechnicalFeatureQueryService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TechnicalFeatureController.class)
@AutoConfigureMockMvc(addFilters = false)
class TechnicalFeatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TechnicalFeatureQueryService queryService;

    @MockBean
    private TechnicalFeatureBatchService batchService;

    @Test
    void testGetLatestFeaturesReturnsList() throws Exception {
        TechnicalFeatureDTO dto = new TechnicalFeatureDTO();
        dto.setSymbol("RELIANCE");
        dto.setFeatureName("RSI_14");
        dto.setFeatureValue(new BigDecimal("58.4500"));
        dto.setTradingDate(LocalDate.of(2026, 9, 12));
        dto.setFeatureTimestamp(Instant.parse("2026-09-12T10:00:00Z"));
        dto.setInformationAvailableAt(Instant.parse("2026-09-12T10:00:00Z"));
        dto.setTimeframe("1D");
        dto.setFeatureVersion("1.0.0");

        when(queryService.getLatestFeatures(eq("RELIANCE"), any())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/features/technical/RELIANCE/latest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("RELIANCE"))
                .andExpect(jsonPath("$[0].featureName").value("RSI_14"))
                .andExpect(jsonPath("$[0].featureValue").value(58.4500));
    }

    @Test
    void testGetDefinitionsReturnsRegistry() throws Exception {
        FeatureDefinitionDTO def = new FeatureDefinitionDTO(
                "SMA_20", FeatureCategory.TREND, "20-Day Simple Moving Average",
                20, "1D", "1.0.0", "SMA_v1.0", PriceSeriesType.SPLIT_ADJUSTED, true
        );

        when(queryService.getDefinitions()).thenReturn(List.of(def));

        mockMvc.perform(get("/api/v1/features/definitions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].featureName").value("SMA_20"))
                .andExpect(jsonPath("$[0].category").value("TREND"));
    }
}
