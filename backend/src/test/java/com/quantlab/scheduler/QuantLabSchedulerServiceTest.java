package com.quantlab.scheduler;

import com.quantlab.marketdata.detector.IndianTradingCalendar;
import com.quantlab.marketdata.service.MarketDataIngestionService;
import com.quantlab.monitoring.service.*;
import com.quantlab.news.service.NewsIngestionService;
import com.quantlab.scheduler.config.SchedulerProperties;
import com.quantlab.scheduler.model.JobExecutionAudit;
import com.quantlab.scheduler.model.JobExecutionStatus;
import com.quantlab.scheduler.service.QuantLabSchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class QuantLabSchedulerServiceTest {

    private SchedulerProperties properties;
    private IndianTradingCalendar tradingCalendar;
    private MarketDataIngestionService marketDataIngestionService;
    private NewsIngestionService newsIngestionService;
    private SystemHealthMonitoringService systemHealthMonitoringService;
    private ProviderHealthMonitoringService providerHealthMonitoringService;
    private SignalAnomalyDetectionService signalAnomalyDetectionService;
    private AlertLifecycleService alertLifecycleService;

    private QuantLabSchedulerService schedulerService;

    @BeforeEach
    void setUp() {
        properties = new SchedulerProperties();
        properties.setEnabled(true);

        tradingCalendar = mock(IndianTradingCalendar.class);
        marketDataIngestionService = mock(MarketDataIngestionService.class);
        newsIngestionService = mock(NewsIngestionService.class);
        systemHealthMonitoringService = mock(SystemHealthMonitoringService.class);
        providerHealthMonitoringService = mock(ProviderHealthMonitoringService.class);
        signalAnomalyDetectionService = mock(SignalAnomalyDetectionService.class);
        alertLifecycleService = mock(AlertLifecycleService.class);

        schedulerService = new QuantLabSchedulerService(
            properties,
            tradingCalendar,
            marketDataIngestionService,
            newsIngestionService,
            systemHealthMonitoringService,
            providerHealthMonitoringService,
            signalAnomalyDetectionService,
            alertLifecycleService
        );
    }

    @Test
    void disabledSchedulerReturnsDisabledStatus() {
        properties.setEnabled(false);

        JobExecutionAudit audit = schedulerService.executeJob("test-job", false, () -> Map.of("key", "val"));

        assertNotNull(audit);
        assertEquals(JobExecutionStatus.DISABLED, audit.status());
        assertTrue(audit.message().contains("globally disabled"));
    }

    @Test
    void disabledIndividualJobReturnsDisabledStatus() {
        properties.getJobConfig("disabled-job").setEnabled(false);

        JobExecutionAudit audit = schedulerService.executeJob("disabled-job", false, () -> Map.of("key", "val"));

        assertNotNull(audit);
        assertEquals(JobExecutionStatus.DISABLED, audit.status());
        assertTrue(audit.message().contains("is disabled"));
    }

    @Test
    void marketHoursRestrictedJobSkipsWhenMarketClosed() {
        when(tradingCalendar.isMarketOpen(any(Instant.class))).thenReturn(false);

        JobExecutionAudit audit = schedulerService.executeJob("market-job", true, () -> Map.of("key", "val"));

        assertNotNull(audit);
        assertEquals(JobExecutionStatus.SKIPPED, audit.status());
        assertTrue(audit.message().contains("Market is currently closed"));
    }

    @Test
    void enabledJobExecutesSuccessfullyAndRecordsAudit() {
        when(tradingCalendar.isMarketOpen(any(Instant.class))).thenReturn(true);

        JobExecutionAudit audit = schedulerService.executeJob("successful-job", false, () -> Map.of("records", 150));

        assertNotNull(audit);
        assertEquals(JobExecutionStatus.SUCCESS, audit.status());
        assertEquals("successful-job", audit.jobName());
        assertNotNull(audit.runId());
        assertEquals(150, audit.metadata().get("records"));
        assertEquals(1, schedulerService.getExecutionHistory().size());
    }

    @Test
    void jobFailureIsRecordedInAuditWithoutThrowingException() {
        JobExecutionAudit audit = schedulerService.executeJob("failing-job", false, () -> {
            throw new RuntimeException("Simulated upstream network timeout");
        });

        assertNotNull(audit);
        assertEquals(JobExecutionStatus.FAILED, audit.status());
        assertTrue(audit.message().contains("Simulated upstream network timeout"));
        assertEquals("Simulated upstream network timeout", audit.metadata().get("error"));
    }
}
