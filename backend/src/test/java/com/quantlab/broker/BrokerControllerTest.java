package com.quantlab.broker;

import com.quantlab.broker.controller.BrokerController;
import com.quantlab.broker.model.ComplianceStatusDTO;
import com.quantlab.broker.model.SafetyLockDTO;
import com.quantlab.broker.service.BrokerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrokerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BrokerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrokerService brokerService;

    @Test
    void testGetSafetyLockStatus() throws Exception {
        SafetyLockDTO lock = new SafetyLockDTO("EMERGENCY_STOP", false, "Normal operations", null, null);
        when(brokerService.getSafetyLockStatus()).thenReturn(lock);

        mockMvc.perform(get("/api/v1/broker/safety-lock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lockId").value("EMERGENCY_STOP"))
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void testGetComplianceStatus() throws Exception {
        ComplianceStatusDTO compliance = new ComplianceStatusDTO(
                UUID.randomUUID(),
                "ZERODHA_KITE",
                "APPROVED_FOR_MANUAL_LIVE_ORDERS",
                "SEBI Retail Algo & API Mandate 2024/2025",
                true,
                true,
                "COMPLIANCE_OFFICER",
                Instant.now(),
                "Manual confirmation enforced"
        );
        when(brokerService.getComplianceStatus()).thenReturn(compliance);

        mockMvc.perform(get("/api/v1/broker/compliance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isManualConfirmationEnforced").value(true))
                .andExpect(jsonPath("$.reviewStatus").value("APPROVED_FOR_MANUAL_LIVE_ORDERS"));
    }

    @Test
    void testGetAccounts() throws Exception {
        when(brokerService.getAccounts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/broker/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
