package com.quantlab.signal;

import com.quantlab.signal.controller.SignalController;
import com.quantlab.signal.model.*;
import com.quantlab.signal.service.SignalEngineService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignalController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SignalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignalEngineService signalService;

    @Test
    void testGetLatestSignal() throws Exception {
        UUID sigId = UUID.randomUUID();
        SignalDTO mockSignal = new SignalDTO(
            sigId,
            UUID.randomUUID(),
            1L,
            "RELIANCE",
            Instant.now(),
            Instant.now(),
            Instant.now(),
            SignalType.BUY,
            68.5,
            0.82,
            0.0075,
            0.012,
            0.625,
            "BULLISH",
            ConflictSeverity.LOW,
            12.0,
            SignalQualityStatus.HIGH_QUALITY,
            0.95,
            "BUY supported by technical, fundamental, and ML models",
            List.of(Map.of("statement", "BUY supported")),
            List.of(Map.of("category", "TECHNICAL")),
            List.of(),
            Map.of(),
            "SIGNAL_v1.0.0",
            "SIGNAL_CFG_v1.0",
            "1.0.0",
            "M1_v1.0",
            "REGIME_v1.0",
            "1",
            true
        );

        when(signalService.getLatestSignal(eq("RELIANCE"))).thenReturn(Optional.of(mockSignal));

        mockMvc.perform(get("/api/v1/signals/RELIANCE")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.symbol").value("RELIANCE"))
            .andExpect(jsonPath("$.signal").value("BUY"))
            .andExpect(jsonPath("$.signalScore").value(68.5))
            .andExpect(jsonPath("$.confidence").value(0.82));
    }
}
