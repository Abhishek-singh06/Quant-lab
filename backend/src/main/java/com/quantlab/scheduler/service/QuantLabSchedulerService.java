package com.quantlab.scheduler.service;

import com.quantlab.marketdata.detector.IndianTradingCalendar;
import com.quantlab.marketdata.model.Exchange;
import com.quantlab.marketdata.service.MarketDataIngestionService;
import com.quantlab.monitoring.service.*;
import com.quantlab.news.service.NewsIngestionService;
import com.quantlab.scheduler.config.SchedulerProperties;
import com.quantlab.scheduler.model.JobExecutionAudit;
import com.quantlab.scheduler.model.JobExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Master Background Automation & Scheduled Jobs Engine for QuantLab.
 * 
 * Safety, Idempotency & Operational Guarantees:
 * - Schedulers NEVER place live broker orders or execute unconfirmed trades.
 * - Idempotent runs: Active locks prevent overlapping duplicate executions.
 * - Market-Hours Aware: Market-data jobs cleanly skip during weekends, market holidays, and outside 09:15-15:30 IST.
 * - Non-Fatal Failures: Failed jobs log diagnostic errors and record audit metrics without corrupting ledger or crash loops.
 * - Configurable: Master switch and per-job flags controlled via environment variables.
 */
@Service
@EnableScheduling
public class QuantLabSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(QuantLabSchedulerService.class);
    private static final int MAX_AUDIT_HISTORY = 100;

    private final SchedulerProperties properties;
    private final IndianTradingCalendar tradingCalendar;
    private final MarketDataIngestionService marketDataIngestionService;
    private final NewsIngestionService newsIngestionService;
    private final SystemHealthMonitoringService systemHealthMonitoringService;
    private final ProviderHealthMonitoringService providerHealthMonitoringService;
    private final SignalAnomalyDetectionService signalAnomalyDetectionService;
    private final AlertLifecycleService alertLifecycleService;

    private final Map<String, AtomicBoolean> jobExecutionLocks = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<JobExecutionAudit> executionHistory = new ConcurrentLinkedDeque<>();
    private final Map<String, JobExecutionAudit> latestJobStatus = new ConcurrentHashMap<>();

    public QuantLabSchedulerService(
            SchedulerProperties properties,
            IndianTradingCalendar tradingCalendar,
            MarketDataIngestionService marketDataIngestionService,
            NewsIngestionService newsIngestionService,
            SystemHealthMonitoringService systemHealthMonitoringService,
            ProviderHealthMonitoringService providerHealthMonitoringService,
            SignalAnomalyDetectionService signalAnomalyDetectionService,
            AlertLifecycleService alertLifecycleService) {
        this.properties = properties;
        this.tradingCalendar = tradingCalendar;
        this.marketDataIngestionService = marketDataIngestionService;
        this.newsIngestionService = newsIngestionService;
        this.systemHealthMonitoringService = systemHealthMonitoringService;
        this.providerHealthMonitoringService = providerHealthMonitoringService;
        this.signalAnomalyDetectionService = signalAnomalyDetectionService;
        this.alertLifecycleService = alertLifecycleService;
    }

    // 1. Market Data Ingestion Job (Every 1 min - Market Hours Only)
    @Scheduled(fixedRateString = "${quantlab.scheduler.jobs.market-data.rate:60000}", initialDelay = 10000)
    public JobExecutionAudit runMarketDataIngestion() {
        return executeJob("market-data-ingestion", true, () -> {
            var run = marketDataIngestionService.ingestQuotes(null, Exchange.NSE);
            return Map.of("runId", run.getRunId(), "status", run.getStatus().name(), "accepted", run.getRecordsAccepted());
        });
    }

    // 2. News / Corporate Intelligence Ingestion Job (Every 15 mins)
    @Scheduled(fixedRateString = "${quantlab.scheduler.jobs.news.rate:900000}", initialDelay = 30000)
    public JobExecutionAudit runNewsIngestion() {
        return executeJob("news-ingestion", false, () -> {
            var run = newsIngestionService.ingestNewsAndFilings();
            return Map.of("runId", run.getRunId(), "status", run.getStatus().name(), "inserted", run.getArticlesInserted());
        });
    }

    // 3. Provider Health Check Job (Every 1 min)
    @Scheduled(fixedRateString = "${quantlab.scheduler.jobs.provider-health.rate:60000}", initialDelay = 15000)
    public JobExecutionAudit runProviderHealthCheck() {
        return executeJob("provider-health-check", false, () -> {
            var health = providerHealthMonitoringService.recordProviderUpdate("NSE", Instant.now(), 0, 50, 50, 25.0);
            return Map.of("provider", health.getProvider(), "status", health.getConnectionStatus());
        });
    }

    // 4. Signal Anomaly Detection Job (Every 15 mins)
    @Scheduled(fixedRateString = "${quantlab.scheduler.jobs.signal-anomaly.rate:900000}", initialDelay = 45000)
    public JobExecutionAudit runSignalAnomalyDetection() {
        return executeJob("signal-anomaly-detection", false, () -> {
            var anomaly = signalAnomalyDetectionService.evaluateSignalAnomaly("ALL_SIGNALS", "GENERAL_DRIFT", 0.05, 0.05, 1.5, "ALL", "Scheduled health check");
            return Map.of("severity", anomaly.getSeverity(), "category", anomaly.getAnomalyCategory());
        });
    }

    // 5. System Health Monitoring Job (Every 1 min)
    @Scheduled(fixedRateString = "${quantlab.scheduler.jobs.system-health.rate:60000}", initialDelay = 5000)
    public JobExecutionAudit runSystemHealthMonitoring() {
        return executeJob("system-health-monitoring", false, () -> {
            var health = systemHealthMonitoringService.getRealSystemOverview();
            return Map.of("status", health.overallSystemStatus(), "subsystems", health.subsystemStatus().size(), "dbPoolPct", health.databasePoolUsagePct());
        });
    }

    // 6. Alert Rule Evaluation & Incident Escalation Job (Every 1 min)
    @Scheduled(fixedRateString = "${quantlab.scheduler.jobs.alert-evaluation.rate:60000}", initialDelay = 20000)
    public JobExecutionAudit runAlertEvaluation() {
        return executeJob("alert-evaluation", false, () -> {
            var alert = alertLifecycleService.evaluateAndRaiseAlert("SYSTEM_HEARTBEAT", "SCHEDULER", 1.0, "Scheduler health check heartbeat");
            return Map.of("evaluated", true, "alertRaised", alert.isPresent());
        });
    }

    /**
     * Core robust job executor with locking, market-hours check, timing, and error handling.
     */
    public JobExecutionAudit executeJob(String jobName, boolean marketHoursOnly, JobTask task) {
        String runId = UUID.randomUUID().toString();
        Instant start = Instant.now();

        // 1. Check Master Scheduler Switch
        if (!properties.isEnabled()) {
            return recordAudit(new JobExecutionAudit(
                jobName, runId, JobExecutionStatus.DISABLED, start, Instant.now(), 0L,
                "Scheduler is globally disabled (QUANTLAB_SCHEDULER_ENABLED=false)", marketHoursOnly, 0, Map.of()
            ));
        }

        // 2. Check Job-Level Switch
        var jobConfig = properties.getJobConfig(jobName);
        if (!jobConfig.isEnabled()) {
            return recordAudit(new JobExecutionAudit(
                jobName, runId, JobExecutionStatus.DISABLED, start, Instant.now(), 0L,
                "Job '" + jobName + "' is disabled in configuration", marketHoursOnly, 0, Map.of()
            ));
        }

        // 3. Market-Hours Check
        if (marketHoursOnly && !tradingCalendar.isMarketOpen(start)) {
            return recordAudit(new JobExecutionAudit(
                jobName, runId, JobExecutionStatus.SKIPPED, start, Instant.now(), 0L,
                "Skipped: Market is currently closed outside normal NSE/BSE trading hours", marketHoursOnly, 0, Map.of()
            ));
        }

        // 4. Acquire Job Lock (Prevents overlapping executions)
        AtomicBoolean lock = jobExecutionLocks.computeIfAbsent(jobName, k -> new AtomicBoolean(false));
        if (!lock.compareAndSet(false, true)) {
            return recordAudit(new JobExecutionAudit(
                jobName, runId, JobExecutionStatus.SKIPPED, start, Instant.now(), 0L,
                "Skipped: Previous execution of '" + jobName + "' is still running", marketHoursOnly, 0, Map.of()
            ));
        }

        try {
            log.info("[Scheduler] Starting job '{}' (runId: {})", jobName, runId);
            Map<String, Object> resultMetadata = task.execute();
            Instant end = Instant.now();
            long duration = end.toEpochMilli() - start.toEpochMilli();

            JobExecutionAudit audit = new JobExecutionAudit(
                jobName, runId, JobExecutionStatus.SUCCESS, start, end, duration,
                "Job completed successfully", marketHoursOnly, 1, resultMetadata != null ? resultMetadata : Map.of()
            );
            log.info("[Scheduler] Job '{}' completed successfully in {} ms", jobName, duration);
            return recordAudit(audit);

        } catch (Exception ex) {
            Instant end = Instant.now();
            long duration = end.toEpochMilli() - start.toEpochMilli();
            log.error("[Scheduler] Job '{}' failed: {}", jobName, ex.getMessage(), ex);

            JobExecutionAudit audit = new JobExecutionAudit(
                jobName, runId, JobExecutionStatus.FAILED, start, end, duration,
                "Job failed: " + ex.getMessage(), marketHoursOnly, 0, Map.of("error", ex.getMessage())
            );
            return recordAudit(audit);

        } finally {
            lock.set(false);
        }
    }

    public List<JobExecutionAudit> getExecutionHistory() {
        return new ArrayList<>(executionHistory);
    }

    public Map<String, JobExecutionAudit> getLatestJobStatus() {
        return Collections.unmodifiableMap(latestJobStatus);
    }

    private JobExecutionAudit recordAudit(JobExecutionAudit audit) {
        latestJobStatus.put(audit.jobName(), audit);
        executionHistory.addFirst(audit);
        while (executionHistory.size() > MAX_AUDIT_HISTORY) {
            executionHistory.removeLast();
        }
        return audit;
    }

    @FunctionalInterface
    public interface JobTask {
        Map<String, Object> execute() throws Exception;
    }
}
