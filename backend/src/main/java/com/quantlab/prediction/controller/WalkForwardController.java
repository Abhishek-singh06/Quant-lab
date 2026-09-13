package com.quantlab.prediction.controller;

import com.quantlab.prediction.model.*;
import com.quantlab.prediction.service.WalkForwardOrchestratorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/walk-forward")
public class WalkForwardController {

    private final WalkForwardOrchestratorService walkForwardService;

    public WalkForwardController(WalkForwardOrchestratorService walkForwardService) {
        this.walkForwardService = walkForwardService;
    }

    @GetMapping("/runs")
    public ResponseEntity<List<WalkForwardRunDTO>> listRuns() {
        List<WalkForwardRunDTO> runs = walkForwardService.listRuns();
        return ResponseEntity.ok(runs);
    }

    @GetMapping("/runs/{runId}")
    public ResponseEntity<WalkForwardRunDTO> getRunById(@PathVariable("runId") String runId) {
        return walkForwardService.getRunById(runId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/runs/{runId}/folds")
    public ResponseEntity<List<WalkForwardFoldDTO>> getFoldsByRunId(@PathVariable("runId") String runId) {
        List<WalkForwardFoldDTO> folds = walkForwardService.getFoldsByRunId(runId);
        return ResponseEntity.ok(folds);
    }

    @PostMapping("/execute")
    public ResponseEntity<WalkForwardRunDTO> executeWalkForward(@RequestBody(required = false) Map<String, Object> req) {
        String name = req != null && req.get("runName") != null ? req.get("runName").toString() : "Production_WF_Run_v1.0";
        WalkForwardMode mode = req != null && req.get("mode") != null
                ? WalkForwardMode.valueOf(req.get("mode").toString().toUpperCase()) : WalkForwardMode.EXPANDING;
        LocalDate trainStart = req != null && req.get("initialTrainStart") != null
                ? LocalDate.parse(req.get("initialTrainStart").toString()) : LocalDate.of(2018, 1, 1);
        LocalDate trainEnd = req != null && req.get("initialTrainEnd") != null
                ? LocalDate.parse(req.get("initialTrainEnd").toString()) : LocalDate.of(2021, 12, 31);
        String stepSize = req != null && req.get("stepSize") != null ? req.get("stepSize").toString() : "ANNUAL";
        ModelType modelType = req != null && req.get("modelType") != null
                ? ModelType.valueOf(req.get("modelType").toString().toUpperCase()) : ModelType.REGRESSION;
        String target = req != null && req.get("targetDefinition") != null
                ? req.get("targetDefinition").toString() : "future_return_1d";

        WalkForwardRunDTO run = walkForwardService.executeWalkForwardRun(name, mode, trainStart, trainEnd, stepSize, modelType, target);
        return ResponseEntity.ok(run);
    }
}
