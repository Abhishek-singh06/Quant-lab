package com.quantlab.fundamental;

import com.quantlab.fundamental.controller.FundamentalIntelligenceController;
import com.quantlab.fundamental.model.CompanyFundamentalsDTO;
import com.quantlab.fundamental.model.FinancialRatiosDTO;
import com.quantlab.fundamental.model.FinancialStatementDTO;
import com.quantlab.fundamental.model.ReportingBasis;
import com.quantlab.fundamental.service.FundamentalAnalyticsService;
import com.quantlab.fundamental.service.FundamentalIngestionService;
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

@WebMvcTest(controllers = FundamentalIntelligenceController.class)
@AutoConfigureMockMvc(addFilters = false)
class FundamentalIntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FundamentalAnalyticsService analyticsService;

    @MockBean
    private FundamentalIngestionService ingestionService;

    @Test
    void testGetCompanyFundamentalsReturnsEnrichedData() throws Exception {
        CompanyFundamentalsDTO dto = new CompanyFundamentalsDTO();
        dto.setSymbol("RELIANCE");
        dto.setCompanyName("Reliance Industries Limited");
        dto.setSector("Energy & Petrochemicals");
        dto.setReportingBasis(ReportingBasis.CONSOLIDATED);
        dto.setLatestPeriodEnd(LocalDate.of(2025, 12, 31));
        dto.setAvailableAt(Instant.parse("2026-01-22T12:30:00Z"));
        dto.setSource("NSE_CORPORATE_FILING");

        FinancialStatementDTO stmt = new FinancialStatementDTO();
        stmt.setRevenue(new BigDecimal("245000.00"));
        stmt.setNetProfit(new BigDecimal("19700.00"));
        dto.setLatestStatement(stmt);

        FinancialRatiosDTO ratios = new FinancialRatiosDTO();
        ratios.setPeRatio(new BigDecimal("24.50"));
        ratios.setRoe(new BigDecimal("14.20"));
        dto.setLatestRatios(ratios);

        when(analyticsService.getFundamentals(eq("RELIANCE"), any())).thenReturn(dto);

        mockMvc.perform(get("/api/v1/fundamentals/company/RELIANCE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("RELIANCE"))
                .andExpect(jsonPath("$.companyName").value("Reliance Industries Limited"))
                .andExpect(jsonPath("$.reportingBasis").value("CONSOLIDATED"))
                .andExpect(jsonPath("$.latestStatement.revenue").value(245000.00))
                .andExpect(jsonPath("$.latestRatios.peRatio").value(24.50));
    }

    @Test
    void testGetFinancialStatementsReturnsList() throws Exception {
        FinancialStatementDTO stmt = new FinancialStatementDTO();
        stmt.setSymbol("TCS");
        stmt.setRevenue(new BigDecimal("64250.00"));
        stmt.setNetProfit(new BigDecimal("12300.00"));

        when(analyticsService.getFinancialStatements(eq("TCS"), any(), any(), any()))
                .thenReturn(List.of(stmt));

        mockMvc.perform(get("/api/v1/fundamentals/statements/TCS")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("TCS"))
                .andExpect(jsonPath("$[0].revenue").value(64250.00));
    }
}
