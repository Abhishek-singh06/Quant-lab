package com.quantlab.global.controller;

import com.quantlab.global.entity.GlobalInstrument;
import com.quantlab.global.entity.GlobalMarketIngestionRun;
import com.quantlab.global.model.GlobalMarketRegimeDTO;
import com.quantlab.global.model.GlobalMarketSnapshotDTO;
import com.quantlab.global.model.GlobalMarketStatusDTO;
import com.quantlab.global.service.GlobalMarketAnalyticsService;
import com.quantlab.global.service.GlobalMarketIngestionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping({"/api/v1/global", "/api/v1/global-market"})
@CrossOrigin(origins = "*")
public class GlobalMarketIntelligenceController {

    private final GlobalMarketIngestionService ingestionService;
    private final GlobalMarketAnalyticsService analyticsService;

    public GlobalMarketIntelligenceController(
            GlobalMarketIngestionService ingestionService,
            GlobalMarketAnalyticsService analyticsService) {
        this.ingestionService = ingestionService;
        this.analyticsService = analyticsService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<GlobalMarketIngestionRun> triggerIngestion() {
        GlobalMarketIngestionRun run = ingestionService.ingestLatestGlobalData();
        return ResponseEntity.ok(run);
    }

    @GetMapping("/instruments")
    public ResponseEntity<List<GlobalInstrument>> getInstruments() {
        return ResponseEntity.ok(analyticsService.getGlobalInstruments());
    }

    @GetMapping("/snapshots/latest")
    public ResponseEntity<List<GlobalMarketSnapshotDTO>> getLatestSnapshots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        return ResponseEntity.ok(analyticsService.getLatestSnapshots(asOf));
    }

    @GetMapping("/snapshots/history/{symbol}")
    public ResponseEntity<List<GlobalMarketSnapshotDTO>> getHistory(
            @PathVariable String symbol,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        if (fromDate == null) fromDate = LocalDate.now().minusDays(30);
        if (toDate == null) toDate = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getInstrumentHistory(symbol, fromDate, toDate, asOf));
    }

    @GetMapping("/regime/latest")
    public ResponseEntity<GlobalMarketRegimeDTO> getLatestRegime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        return ResponseEntity.ok(analyticsService.getLatestRegime(asOf));
    }

    @GetMapping("/regime/history")
    public ResponseEntity<List<GlobalMarketRegimeDTO>> getRegimeHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toTime) {
        if (fromTime == null) fromTime = Instant.now().minus(java.time.Duration.ofDays(30));
        if (toTime == null) toTime = Instant.now();
        return ResponseEntity.ok(analyticsService.getRegimeHistory(fromTime, toTime));
    }

    @GetMapping("/status")
    public ResponseEntity<List<GlobalMarketStatusDTO>> getMarketStatuses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        return ResponseEntity.ok(analyticsService.getGlobalMarketStatuses(asOf));
    }
}
