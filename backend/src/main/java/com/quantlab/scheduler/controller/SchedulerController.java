package com.quantlab.scheduler.controller;

import com.quantlab.scheduler.model.JobExecutionAudit;
import com.quantlab.scheduler.service.QuantLabSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scheduler")
public class SchedulerController {

    private final QuantLabSchedulerService schedulerService;

    public SchedulerController(QuantLabSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<Map<String, JobExecutionAudit>> getLatestJobStatus() {
        return ResponseEntity.ok(schedulerService.getLatestJobStatus());
    }

    @GetMapping("/history")
    public ResponseEntity<List<JobExecutionAudit>> getExecutionHistory() {
        return ResponseEntity.ok(schedulerService.getExecutionHistory());
    }

    @PostMapping("/jobs/{jobName}/trigger")
    public ResponseEntity<JobExecutionAudit> triggerJobManually(@PathVariable String jobName) {
        JobExecutionAudit result;
        switch (jobName.toLowerCase()) {
            case "market-data-ingestion" -> result = schedulerService.runMarketDataIngestion();
            case "news-ingestion" -> result = schedulerService.runNewsIngestion();
            case "provider-health-check" -> result = schedulerService.runProviderHealthCheck();
            case "signal-anomaly-detection" -> result = schedulerService.runSignalAnomalyDetection();
            case "system-health-monitoring" -> result = schedulerService.runSystemHealthMonitoring();
            case "alert-evaluation" -> result = schedulerService.runAlertEvaluation();
            default -> {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.ok(result);
    }
}
