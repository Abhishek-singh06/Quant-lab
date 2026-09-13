package com.quantlab.prediction;

import com.quantlab.prediction.controller.QuantPredictionController;
import com.quantlab.prediction.model.ModelPredictionDTO;
import com.quantlab.prediction.model.ModelStatus;
import com.quantlab.prediction.model.ModelType;
import com.quantlab.prediction.model.QuantModelDTO;
import com.quantlab.prediction.service.ModelRegistryService;
import com.quantlab.prediction.service.PredictionService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuantPredictionController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuantPredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ModelRegistryService modelRegistryService;

    @MockBean
    private PredictionService predictionService;

    @Test
    void testGetLatestPredictionReturnsDTO() throws Exception {
        ModelPredictionDTO pred = new ModelPredictionDTO();
        pred.setPredictionId("PRD-TEST-123");
        pred.setSymbol("NIFTY 50");
        pred.setModelId("MOD-GB-01");
        pred.setModelVersion("GB_1D_v1.0");
        pred.setPredictionType(ModelType.REGRESSION);
        pred.setPredictedReturn(new BigDecimal("0.0055"));
        pred.setProbabilityPositive(new BigDecimal("0.7200"));
        pred.setTradingDate(LocalDate.of(2026, 9, 12));
        pred.setPredictionTimestamp(Instant.now());
        pred.setInformationAvailableAt(Instant.now());

        when(predictionService.getLatestPrediction(eq("NIFTY 50"), any())).thenReturn(Optional.of(pred));

        mockMvc.perform(get("/api/v1/models/predictions/latest?symbol=NIFTY 50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictionId").value("PRD-TEST-123"))
                .andExpect(jsonPath("$.symbol").value("NIFTY 50"))
                .andExpect(jsonPath("$.predictedReturn").value(0.0055))
                .andExpect(jsonPath("$.probabilityPositive").value(0.72));
    }

    @Test
    void testListModelsReturnsList() throws Exception {
        QuantModelDTO model = new QuantModelDTO();
        model.setModelId("MOD-GB-01");
        model.setModelName("Gradient Boosting 1D Return Regressor");
        model.setModelType(ModelType.REGRESSION);
        model.setAlgorithm("GRADIENT_BOOSTING");
        model.setModelVersion("GB_1D_v1.0");
        model.setStatus(ModelStatus.PRODUCTION);

        when(modelRegistryService.listModels(any(), any())).thenReturn(List.of(model));

        mockMvc.perform(get("/api/v1/models")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].modelId").value("MOD-GB-01"))
                .andExpect(jsonPath("$[0].algorithm").value("GRADIENT_BOOSTING"))
                .andExpect(jsonPath("$[0].status").value("PRODUCTION"));
    }
}
