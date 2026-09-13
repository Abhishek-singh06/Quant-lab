package com.quantlab.institutional.controller;

import com.quantlab.institutional.entity.InstitutionalIngestionRun;
import com.quantlab.institutional.entity.MutualFundScheme;
import com.quantlab.institutional.model.FundPortfolioDTO;
import com.quantlab.institutional.model.InstitutionalFlowDTO;
import com.quantlab.institutional.model.StockInstitutionalOwnershipDTO;
import com.quantlab.institutional.repository.MutualFundSchemeRepository;
import com.quantlab.institutional.service.InstitutionalAnalyticsService;
import com.quantlab.institutional.service.InstitutionalIngestionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/institutional")
@CrossOrigin(origins = "*")
public class InstitutionalIntelligenceController {

    private final InstitutionalIngestionService ingestionService;
    private final InstitutionalAnalyticsService analyticsService;
    private final MutualFundSchemeRepository schemeRepository;

    public InstitutionalIntelligenceController(
            InstitutionalIngestionService ingestionService,
            InstitutionalAnalyticsService analyticsService,
            MutualFundSchemeRepository schemeRepository) {
        this.ingestionService = ingestionService;
        this.analyticsService = analyticsService;
        this.schemeRepository = schemeRepository;
    }

    @PostMapping("/ingest")
    public ResponseEntity<InstitutionalIngestionRun> triggerIngestion() {
        InstitutionalIngestionRun run = ingestionService.ingestAllSampleData();
        return ResponseEntity.ok(run);
    }

    @GetMapping("/ownership/{symbol}")
    public ResponseEntity<StockInstitutionalOwnershipDTO> getStockOwnership(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        StockInstitutionalOwnershipDTO ownership = analyticsService.getStockInstitutionalOwnership(symbol, asOf);
        return ResponseEntity.ok(ownership);
    }

    @GetMapping("/portfolio/{schemeCode}")
    public ResponseEntity<FundPortfolioDTO> getFundPortfolio(
            @PathVariable String schemeCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        FundPortfolioDTO portfolio = analyticsService.getFundPortfolio(schemeCode, asOf);
        if (portfolio == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(portfolio);
    }

    @GetMapping("/flows")
    public ResponseEntity<List<InstitutionalFlowDTO>> getFlows(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        if (fromDate == null) {
            fromDate = LocalDate.now().minusDays(30);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        List<InstitutionalFlowDTO> flows = analyticsService.getInstitutionalFlows(fromDate, toDate, asOf);
        return ResponseEntity.ok(flows);
    }

    @GetMapping("/schemes")
    public ResponseEntity<List<MutualFundScheme>> getSchemes() {
        return ResponseEntity.ok(schemeRepository.findAll());
    }
}
