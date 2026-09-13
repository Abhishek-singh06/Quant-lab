package com.quantlab.monitoring;

import com.quantlab.monitoring.controller.MonitoringController;
import com.quantlab.monitoring.model.*;
import com.quantlab.monitoring.service.MonitoringService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MonitoringController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MonitoringService monitoringService;

    @Test
    void testGetSystemOverview() throws Exception {
        SystemOverviewHealthDTO overview = new SystemOverviewHealthDTO(
                "HEALTHY",
                Map.of("POSTGRESQL", "HEALTHY", "REDIS_CACHE", "NOT_CONFIGURED"),
                0,
                0,
                2.5,
                42.0,
                false,
                Instant.now()
        );

        when(monitoringService.getSystemOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/v1/monitoring/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallSystemStatus").value("HEALTHY"))
                .andExpect(jsonPath("$.subsystemStatus.REDIS_CACHE").value("NOT_CONFIGURED"));
    }

    @Test
    void testGetAlerts() throws Exception {
        when(monitoringService.getAlerts(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/monitoring/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testGetProviders() throws Exception {
        when(monitoringService.getProviderHealth()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/monitoring/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
