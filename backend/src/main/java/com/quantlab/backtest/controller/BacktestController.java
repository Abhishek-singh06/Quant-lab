package com.quantlab.backtest.controller;

import com.quantlab.backtest.model.*;
import com.quantlab.backtest.service.BacktestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/backtests")
public class BacktestController {

    private final BacktestService backtestService;

    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    @GetMapping("/runs")
    public ResponseEntity<List<BacktestRunDTO>> getAllRuns() {
        return ResponseEntity.ok(backtestService.getAllRuns());
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<BacktestRunDTO> getRun(@PathVariable UUID runId) {
        return backtestService.getRun(runId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/runs/{runId}/result")
    public ResponseEntity<BacktestResultDTO> getBacktestResult(@PathVariable UUID runId) {
        return backtestService.getBacktestResult(runId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/runs/{runId}/trades")
    public ResponseEntity<List<BacktestTradeDTO>> getTrades(@PathVariable UUID runId) {
        return ResponseEntity.ok(backtestService.getTrades(runId));
    }

    @GetMapping("/runs/{runId}/equity-curve")
    public ResponseEntity<List<EquityCurvePointDTO>> getEquityCurve(@PathVariable UUID runId) {
        return ResponseEntity.ok(backtestService.getEquityCurve(runId));
    }

    @GetMapping("/runs/{runId}/metrics")
    public ResponseEntity<PerformanceMetricsDTO> getMetrics(@PathVariable UUID runId) {
        return backtestService.getMetrics(runId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/runs/{runId}/rejected-signals")
    public ResponseEntity<List<BacktestRejectedSignalDTO>> getRejectedSignals(@PathVariable UUID runId) {
        return ResponseEntity.ok(backtestService.getRejectedSignals(runId));
    }

    @PostMapping("/run")
    public ResponseEntity<BacktestRunDTO> executeBacktest(@RequestBody BacktestConfigDTO configDTO) {
        BacktestRunDTO run = backtestService.executeSimulation(configDTO);
        return ResponseEntity.ok(run);
    }
}
