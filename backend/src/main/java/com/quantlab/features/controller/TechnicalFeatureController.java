package com.quantlab.features.controller;

import com.quantlab.features.entity.FeatureCalculationRun;
import com.quantlab.features.model.FeatureCalculationRequest;
import com.quantlab.features.model.FeatureDefinitionDTO;
import com.quantlab.features.model.TechnicalFeatureDTO;
import com.quantlab.features.service.TechnicalFeatureBatchService;
import com.quantlab.features.service.TechnicalFeatureQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@CrossOrigin(origins = "*")
public class TechnicalFeatureController {

    private final TechnicalFeatureQueryService queryService;
    private final TechnicalFeatureBatchService batchService;

    public TechnicalFeatureController(
            TechnicalFeatureQueryService queryService,
            TechnicalFeatureBatchService batchService) {
        this.queryService = queryService;
        this.batchService = batchService;
    }

    @GetMapping("/technical/{symbol}/latest")
    public ResponseEntity<List<TechnicalFeatureDTO>> getLatestFeatures(
            @PathVariable("symbol") String symbol,
            @RequestParam(value = "timeframe", defaultValue = "1D") String timeframe) {
        List<TechnicalFeatureDTO> list = queryService.getLatestFeatures(symbol.toUpperCase(), timeframe);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/technical/{symbol}/point-in-time")
    public ResponseEntity<List<TechnicalFeatureDTO>> getFeaturesPointInTime(
            @PathVariable("symbol") String symbol,
            @RequestParam(value = "timeframe", defaultValue = "1D") String timeframe,
            @RequestParam("asOf") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant asOf) {
        List<TechnicalFeatureDTO> list = queryService.getFeaturesAvailableAt(symbol.toUpperCase(), timeframe, asOf);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/technical/series")
    public ResponseEntity<List<TechnicalFeatureDTO>> getFeatureTimeSeries(
            @RequestParam("instrumentId") Long instrumentId,
            @RequestParam("featureName") String featureName,
            @RequestParam(value = "timeframe", defaultValue = "1D") String timeframe,
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<TechnicalFeatureDTO> list = queryService.getFeatureTimeSeries(instrumentId, featureName, timeframe, from, to);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/definitions")
    public ResponseEntity<List<FeatureDefinitionDTO>> getDefinitions() {
        return ResponseEntity.ok(queryService.getDefinitions());
    }

    @GetMapping("/runs")
    public ResponseEntity<List<FeatureCalculationRun>> getRecentRuns() {
        return ResponseEntity.ok(queryService.getRecentRuns());
    }

    @PostMapping("/calculate/{symbol}")
    public ResponseEntity<FeatureCalculationRun> triggerCalculationForSymbol(
            @PathVariable("symbol") String symbol,
            @RequestBody(required = false) FeatureCalculationRequest request) {
        FeatureCalculationRequest req = (request != null) ? request : new FeatureCalculationRequest();
        req.setSymbols(List.of(symbol.toUpperCase()));
        FeatureCalculationRun run = batchService.calculateForRequest(req);
        return ResponseEntity.ok(run);
    }

    @PostMapping("/calculate-batch")
    public ResponseEntity<FeatureCalculationRun> triggerBatchCalculation(
            @RequestBody FeatureCalculationRequest request) {
        FeatureCalculationRun run = batchService.calculateForRequest(request);
        return ResponseEntity.ok(run);
    }
}
