package com.quantlab.regime.controller;

import com.quantlab.regime.model.MarketRegimeDTO;
import com.quantlab.regime.model.RegimeCalculationRequest;
import com.quantlab.regime.model.RegimeEvaluationDTO;
import com.quantlab.regime.service.MarketRegimeEngineService;
import com.quantlab.regime.service.RegimeEvaluationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/regime")
public class MarketRegimeController {

    private final MarketRegimeEngineService regimeEngineService;
    private final RegimeEvaluationService evaluationService;

    public MarketRegimeController(
            MarketRegimeEngineService regimeEngineService,
            RegimeEvaluationService evaluationService) {
        this.regimeEngineService = regimeEngineService;
        this.evaluationService = evaluationService;
    }

    @GetMapping("/latest")
    public ResponseEntity<MarketRegimeDTO> getLatestRegime(
            @RequestParam(name = "symbol", defaultValue = "NIFTY 50") String symbol,
            @RequestParam(name = "asOf", required = false) Instant asOf) {
        return regimeEngineService.getLatestRegime(symbol, asOf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<List<MarketRegimeDTO>> getRegimeHistory(
            @RequestParam(name = "symbol", defaultValue = "NIFTY 50") String symbol,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(name = "asOf", required = false) Instant asOf) {
        List<MarketRegimeDTO> history = regimeEngineService.getRegimeHistory(symbol, from, to, asOf);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<MarketRegimeDTO>> getRecentRegimes(
            @RequestParam(name = "symbol", defaultValue = "NIFTY 50") String symbol,
            @RequestParam(name = "limit", defaultValue = "30") int limit) {
        List<MarketRegimeDTO> recent = regimeEngineService.getRecentRegimes(symbol, limit);
        return ResponseEntity.ok(recent);
    }

    @PostMapping("/calculate")
    public ResponseEntity<MarketRegimeDTO> calculateRegime(@RequestBody RegimeCalculationRequest request) {
        String symbol = request != null && request.getSymbol() != null ? request.getSymbol() : "NIFTY 50";
        LocalDate tradingDate = request != null && request.getTradingDate() != null ? request.getTradingDate() : LocalDate.now();
        Instant asOf = request != null && request.getAsOfTimestamp() != null ? request.getAsOfTimestamp() : Instant.now();

        MarketRegimeDTO dto = regimeEngineService.calculateAndSaveRegime(symbol, tradingDate, asOf);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/evaluate/walk-forward")
    public ResponseEntity<RegimeEvaluationDTO> evaluateWalkForward(@RequestBody(required = false) Map<String, Object> req) {
        String modelVersion = req != null && req.get("modelVersion") != null ? req.get("modelVersion").toString() : null;
        LocalDate trainStart = req != null && req.get("trainStart") != null ? LocalDate.parse(req.get("trainStart").toString()) : null;
        LocalDate trainEnd = req != null && req.get("trainEnd") != null ? LocalDate.parse(req.get("trainEnd").toString()) : null;
        LocalDate testStart = req != null && req.get("testStart") != null ? LocalDate.parse(req.get("testStart").toString()) : null;
        LocalDate testEnd = req != null && req.get("testEnd") != null ? LocalDate.parse(req.get("testEnd").toString()) : null;

        RegimeEvaluationDTO dto = evaluationService.runWalkForwardEvaluation(modelVersion, trainStart, trainEnd, testStart, testEnd);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/evaluation-history")
    public ResponseEntity<List<RegimeEvaluationDTO>> getEvaluationHistory(
            @RequestParam(name = "modelVersion", required = false) String modelVersion) {
        List<RegimeEvaluationDTO> history = evaluationService.getEvaluationHistory(modelVersion);
        return ResponseEntity.ok(history);
    }
}
