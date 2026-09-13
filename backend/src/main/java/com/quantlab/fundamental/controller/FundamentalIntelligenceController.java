package com.quantlab.fundamental.controller;

import com.quantlab.fundamental.entity.FundamentalFiling;
import com.quantlab.fundamental.entity.FundamentalIngestionRun;
import com.quantlab.fundamental.model.*;
import com.quantlab.fundamental.service.FundamentalAnalyticsService;
import com.quantlab.fundamental.service.FundamentalIngestionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/fundamentals")
@CrossOrigin(origins = "*")
public class FundamentalIntelligenceController {

    private final FundamentalAnalyticsService analyticsService;
    private final FundamentalIngestionService ingestionService;

    public FundamentalIntelligenceController(
            FundamentalAnalyticsService analyticsService,
            FundamentalIngestionService ingestionService) {
        this.analyticsService = analyticsService;
        this.ingestionService = ingestionService;
    }

    @GetMapping("/company/{symbol}")
    public ResponseEntity<CompanyFundamentalsDTO> getCompanyFundamentals(
            @PathVariable("symbol") String symbol,
            @RequestParam(value = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        CompanyFundamentalsDTO dto = analyticsService.getFundamentals(symbol.toUpperCase(), asOf);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/statements/{symbol}")
    public ResponseEntity<List<FinancialStatementDTO>> getFinancialStatements(
            @PathVariable("symbol") String symbol,
            @RequestParam(value = "periodType", defaultValue = "QUARTERLY") PeriodType periodType,
            @RequestParam(value = "reportingBasis", defaultValue = "CONSOLIDATED") ReportingBasis reportingBasis,
            @RequestParam(value = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        List<FinancialStatementDTO> statements = analyticsService.getFinancialStatements(
                symbol.toUpperCase(), periodType, reportingBasis, asOf
        );
        return ResponseEntity.ok(statements);
    }

    @GetMapping("/ratios/{symbol}")
    public ResponseEntity<List<FinancialRatiosDTO>> getFinancialRatios(
            @PathVariable("symbol") String symbol,
            @RequestParam(value = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        List<FinancialRatiosDTO> ratios = analyticsService.getFinancialRatios(symbol.toUpperCase(), asOf);
        return ResponseEntity.ok(ratios);
    }

    @GetMapping("/filings/{symbol}")
    public ResponseEntity<List<FundamentalFiling>> getFilings(
            @PathVariable("symbol") String symbol,
            @RequestParam(value = "asOf", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        List<FundamentalFiling> filings = analyticsService.getFilings(symbol.toUpperCase(), asOf);
        return ResponseEntity.ok(filings);
    }

    @GetMapping("/runs")
    public ResponseEntity<List<FundamentalIngestionRun>> getRecentRuns() {
        return ResponseEntity.ok(analyticsService.getRecentRuns());
    }

    @PostMapping("/ingest/{symbol}")
    public ResponseEntity<FundamentalIngestionRun> triggerIngestion(@PathVariable("symbol") String symbol) {
        FundamentalIngestionRun run = ingestionService.ingestForSymbol(symbol.toUpperCase());
        return ResponseEntity.ok(run);
    }

    @PostMapping("/ingest-all")
    public ResponseEntity<FundamentalIngestionRun> triggerUniverseIngestion(
            @RequestBody(required = false) List<String> symbols) {
        List<String> targetSymbols = (symbols != null && !symbols.isEmpty())
                ? symbols
                : Arrays.asList("RELIANCE", "TCS", "HDFCBANK", "INFY", "ITC");
        FundamentalIngestionRun run = ingestionService.ingestAllUniverse(targetSymbols);
        return ResponseEntity.ok(run);
    }
}
