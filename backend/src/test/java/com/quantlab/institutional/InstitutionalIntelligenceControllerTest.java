package com.quantlab.institutional;

import com.quantlab.institutional.controller.InstitutionalIntelligenceController;
import com.quantlab.institutional.model.FundPortfolioDTO;
import com.quantlab.institutional.model.InstitutionalFlowDTO;
import com.quantlab.institutional.model.StockInstitutionalOwnershipDTO;
import com.quantlab.institutional.repository.MutualFundSchemeRepository;
import com.quantlab.institutional.service.InstitutionalAnalyticsService;
import com.quantlab.institutional.service.InstitutionalIngestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InstitutionalIntelligenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class InstitutionalIntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstitutionalIngestionService ingestionService;

    @MockBean
    private InstitutionalAnalyticsService analyticsService;

    @MockBean
    private MutualFundSchemeRepository schemeRepository;

    @Test
    void testGetStockOwnershipReturnsFreshnessProvenance() throws Exception {
        StockInstitutionalOwnershipDTO dto = new StockInstitutionalOwnershipDTO(
                "HDFCBANK",
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2026, 7, 21),
                java.time.Instant.now(),
                "NSE_INDIA",
                45L,
                new BigDecimal("34.5000"),
                12,
                2,
                1,
                new BigDecimal("1.2500"),
                List.of()
        );

        when(analyticsService.getStockInstitutionalOwnership(eq("HDFCBANK"), any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/institutional/ownership/HDFCBANK")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("HDFCBANK"))
                .andExpect(jsonPath("$.dataAsOf").value("2026-06-30"))
                .andExpect(jsonPath("$.publishedAt").value("2026-07-21"))
                .andExpect(jsonPath("$.source").value("NSE_INDIA"))
                .andExpect(jsonPath("$.totalDisclosedMfWeight").value(34.5000));
    }

    @Test
    void testGetFundPortfolioReturnsHoldingsWithWeightChanges() throws Exception {
        FundPortfolioDTO dto = new FundPortfolioDTO(
                1L,
                "Parag Parikh Flexi Cap Fund",
                "PPFAS_FLEXICAP",
                "PPFAS Mutual Fund",
                "Flexi Cap",
                LocalDate.of(2026, 7, 31),
                LocalDate.of(2026, 8, 12),
                java.time.Instant.now(),
                "AMFI_INDIA",
                28L,
                new BigDecimal("50000.00"),
                com.quantlab.institutional.model.PortfolioScope.FULL_PORTFOLIO,
                List.of(),
                java.util.Map.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        when(analyticsService.getFundPortfolio(eq("PPFAS_FLEXICAP"), any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/institutional/portfolio/PPFAS_FLEXICAP")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemeCode").value("PPFAS_FLEXICAP"))
                .andExpect(jsonPath("$.dataAsOf").value("2026-07-31"))
                .andExpect(jsonPath("$.source").value("AMFI_INDIA"));
    }
}
