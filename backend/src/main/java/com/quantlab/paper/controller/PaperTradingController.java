package com.quantlab.paper.controller;

import com.quantlab.paper.model.*;
import com.quantlab.paper.service.PaperTradingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/paper")
public class PaperTradingController {

    private final PaperTradingService paperTradingService;

    public PaperTradingController(PaperTradingService paperTradingService) {
        this.paperTradingService = paperTradingService;
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<PaperTradingSessionDTO>> getAllSessions() {
        return ResponseEntity.ok(paperTradingService.getAllSessions());
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<PaperTradingSessionDTO> getSession(@PathVariable UUID id) {
        return paperTradingService.getSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions")
    public ResponseEntity<PaperTradingSessionDTO> createSession(@RequestBody(required = false) Map<String, Object> body) {
        String name = body != null && body.containsKey("name") ? (String) body.get("name") : null;
        String dataProvider = body != null && body.containsKey("dataProvider") ? (String) body.get("dataProvider") : null;
        String horizon = body != null && body.containsKey("horizon") ? (String) body.get("horizon") : null;
        Double initialCapital = body != null && body.containsKey("initialCapital") ? Double.valueOf(body.get("initialCapital").toString()) : 1000000.0;

        return ResponseEntity.ok(paperTradingService.createSession(name, dataProvider, ClockType.LIVE_CLOCK, horizon, initialCapital));
    }

    @PostMapping("/sessions/{id}/start")
    public ResponseEntity<PaperTradingSessionDTO> startSession(@PathVariable UUID id) {
        return paperTradingService.startSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/{id}/pause")
    public ResponseEntity<PaperTradingSessionDTO> pauseSession(@PathVariable UUID id) {
        return paperTradingService.pauseSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/{id}/resume")
    public ResponseEntity<PaperTradingSessionDTO> resumeSession(@PathVariable UUID id) {
        return paperTradingService.resumeSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sessions/{id}/stop")
    public ResponseEntity<PaperTradingSessionDTO> stopSession(@PathVariable UUID id) {
        return paperTradingService.stopSession(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/portfolios")
    public ResponseEntity<List<PaperPortfolioDTO>> getAllPortfolios() {
        return ResponseEntity.ok(paperTradingService.getAllPortfolios());
    }

    @GetMapping("/portfolios/{id}")
    public ResponseEntity<PaperPortfolioDTO> getPortfolio(@PathVariable UUID id) {
        return paperTradingService.getPortfolio(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/portfolios/{id}/positions")
    public ResponseEntity<List<PaperPositionDTO>> getPositions(@PathVariable UUID id) {
        return ResponseEntity.ok(paperTradingService.getPositions(id));
    }

    @GetMapping("/portfolios/{id}/orders")
    public ResponseEntity<List<PaperOrderDTO>> getOrders(@PathVariable UUID id) {
        return ResponseEntity.ok(paperTradingService.getOrders(id));
    }

    @GetMapping("/portfolios/{id}/fills")
    public ResponseEntity<List<PaperFillDTO>> getFills(@PathVariable UUID id) {
        return ResponseEntity.ok(paperTradingService.getFills(id));
    }

    @GetMapping("/portfolios/{id}/ledger")
    public ResponseEntity<List<PaperLedgerDTO>> getLedger(@PathVariable UUID id) {
        return ResponseEntity.ok(paperTradingService.getLedger(id));
    }

    @GetMapping("/portfolios/{id}/equity")
    public ResponseEntity<List<PaperEquityCurveDTO>> getEquityCurve(@PathVariable UUID id) {
        return ResponseEntity.ok(paperTradingService.getEquityCurve(id));
    }

    @GetMapping("/portfolios/{id}/risk")
    public ResponseEntity<List<PaperRiskMonitoringDTO>> getRiskMonitoring(@PathVariable UUID id) {
        return ResponseEntity.ok(paperTradingService.getRiskMonitoring(id));
    }

    @GetMapping("/decisions")
    public ResponseEntity<List<PaperTradingDecisionDTO>> getAllDecisions() {
        return ResponseEntity.ok(paperTradingService.getAllDecisions());
    }

    @GetMapping("/decisions/{id}")
    public ResponseEntity<PaperTradingDecisionDTO> getDecision(@PathVariable UUID id) {
        return paperTradingService.getDecision(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/outcomes/signals")
    public ResponseEntity<List<PaperSignalOutcomeDTO>> getSignalOutcomes() {
        return ResponseEntity.ok(paperTradingService.getSignalOutcomes());
    }

    @GetMapping("/outcomes/predictions")
    public ResponseEntity<List<PaperPredictionOutcomeDTO>> getPredictionOutcomes() {
        return ResponseEntity.ok(paperTradingService.getPredictionOutcomes());
    }

    @GetMapping("/models")
    public ResponseEntity<List<PaperModelMonitoringDTO>> getModelMonitoring() {
        return ResponseEntity.ok(paperTradingService.getModelMonitoring());
    }

    @GetMapping("/data-health")
    public ResponseEntity<LiveDataHealthDTO> getDataHealth() {
        return ResponseEntity.ok(paperTradingService.getDataHealth());
    }

    @GetMapping("/health")
    public ResponseEntity<PaperTradingHealthReportDTO> getHealthReport() {
        return ResponseEntity.ok(paperTradingService.getHealthReport());
    }
}
