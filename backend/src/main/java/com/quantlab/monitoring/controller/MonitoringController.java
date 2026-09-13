package com.quantlab.monitoring.controller;

import com.quantlab.monitoring.entity.FeatureDriftEventEntity;
import com.quantlab.monitoring.model.*;
import com.quantlab.monitoring.service.MonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    public MonitoringController(MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @GetMapping("/overview")
    public ResponseEntity<SystemOverviewHealthDTO> getSystemOverview() {
        return ResponseEntity.ok(monitoringService.getSystemOverview());
    }

    @GetMapping("/health-checks")
    public ResponseEntity<List<MonitoringHealthCheckDTO>> getHealthChecks() {
        return ResponseEntity.ok(monitoringService.getHealthChecks());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<MonitoringAlertDTO>> getAlerts(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(monitoringService.getAlerts(status));
    }

    @GetMapping("/alerts/{id}")
    public ResponseEntity<MonitoringAlertDTO> getAlert(@PathVariable UUID id) {
        return monitoringService.getAlert(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/alerts/{id}/acknowledge")
    public ResponseEntity<MonitoringAlertDTO> acknowledgeAlert(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String user = body != null ? body.get("user") : "OPERATOR";
        return monitoringService.acknowledgeAlert(id, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/alerts/{id}/resolve")
    public ResponseEntity<MonitoringAlertDTO> resolveAlert(@PathVariable UUID id) {
        return monitoringService.resolveAlert(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<MonitoringIncidentDTO>> getIncidents() {
        return ResponseEntity.ok(monitoringService.getIncidents());
    }

    @GetMapping("/providers")
    public ResponseEntity<List<ProviderHealthDTO>> getProviderHealth() {
        return ResponseEntity.ok(monitoringService.getProviderHealth());
    }

    @GetMapping("/data-quality-events")
    public ResponseEntity<List<DataQualityEventDTO>> getDataQualityEvents() {
        return ResponseEntity.ok(monitoringService.getDataQualityEvents());
    }

    @GetMapping("/feature-drift-events")
    public ResponseEntity<List<FeatureDriftEventDTO>> getFeatureDriftEvents() {
        return ResponseEntity.ok(monitoringService.getFeatureDriftEvents());
    }

    @GetMapping("/signal-anomaly-events")
    public ResponseEntity<List<SignalAnomalyEventDTO>> getSignalAnomalyEvents() {
        return ResponseEntity.ok(monitoringService.getSignalAnomalyEvents());
    }

    @GetMapping("/rules")
    public ResponseEntity<List<MonitoringRuleDTO>> getMonitoringRules() {
        return ResponseEntity.ok(monitoringService.getMonitoringRules());
    }

    @PostMapping("/feature-drift/calculate")
    public ResponseEntity<FeatureDriftEventEntity> calculateFeatureDrift(@RequestBody Map<String, Object> req) {
        String featureName = (String) req.getOrDefault("featureName", "UNKNOWN_FEATURE");
        String metricType = (String) req.getOrDefault("metricType", "PSI");
        double threshold = req.containsKey("threshold") ? Double.parseDouble(req.get("threshold").toString()) : 0.25;
        String referenceWindow = (String) req.getOrDefault("referenceWindow", "BASELINE_WINDOW");
        String evaluationWindow = (String) req.getOrDefault("evaluationWindow", "CURRENT_WINDOW");

        List<?> bList = (List<?>) req.get("baseline");
        List<?> cList = (List<?>) req.get("current");

        double[] baseline = bList != null ? bList.stream().mapToDouble(v -> Double.parseDouble(v.toString())).toArray() : new double[0];
        double[] current = cList != null ? cList.stream().mapToDouble(v -> Double.parseDouble(v.toString())).toArray() : new double[0];

        return ResponseEntity.ok(monitoringService.calculateAndRecordFeatureDrift(
                featureName, metricType, baseline, current, threshold, referenceWindow, evaluationWindow
        ));
    }
}
