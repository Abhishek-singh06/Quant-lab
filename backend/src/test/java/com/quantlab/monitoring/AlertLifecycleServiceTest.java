package com.quantlab.monitoring;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;
import com.quantlab.monitoring.entity.MonitoringRuleEntity;
import com.quantlab.monitoring.notification.MonitoringNotificationProvider;
import com.quantlab.monitoring.repository.MonitoringAlertRepository;
import com.quantlab.monitoring.repository.MonitoringRuleRepository;
import com.quantlab.monitoring.service.AlertLifecycleService;
import com.quantlab.monitoring.service.IncidentManagementService;
import com.quantlab.monitoring.service.MonitoringMetricsService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AlertLifecycleServiceTest {

    private MonitoringAlertRepository alertRepository;
    private MonitoringRuleRepository ruleRepository;
    private IncidentManagementService incidentManagementService;
    private MonitoringMetricsService metricsService;
    private MonitoringNotificationProvider notificationProvider;
    private AlertLifecycleService alertLifecycleService;

    @BeforeEach
    void setUp() {
        alertRepository = mock(MonitoringAlertRepository.class);
        ruleRepository = mock(MonitoringRuleRepository.class);
        incidentManagementService = mock(IncidentManagementService.class);
        metricsService = new MonitoringMetricsService(new SimpleMeterRegistry());
        notificationProvider = mock(MonitoringNotificationProvider.class);
        when(notificationProvider.isConfigured()).thenReturn(true);

        alertLifecycleService = new AlertLifecycleService(
                alertRepository,
                ruleRepository,
                incidentManagementService,
                metricsService,
                List.of(notificationProvider)
        );
    }

    @Test
    void testRaiseAlertWhenConditionMet() {
        MonitoringRuleEntity rule = new MonitoringRuleEntity();
        rule.setId("RULE_CPU_HIGH");
        rule.setName("High CPU Utilization");
        rule.setMetricName("CPU_USAGE");
        rule.setTargetComponent("SYSTEM");
        rule.setConditionOperator("GT");
        rule.setThresholdValue(80.0);
        rule.setSeverity("WARNING");
        rule.setCooldownSeconds(300);
        rule.setEnabled(true);

        when(ruleRepository.findById("RULE_CPU_HIGH")).thenReturn(Optional.of(rule));
        when(alertRepository.findByStatusOrderByCreatedAtDesc("OPEN")).thenReturn(Collections.emptyList());
        when(alertRepository.save(any(MonitoringAlertEntity.class))).thenAnswer(i -> i.getArgument(0));

        Optional<MonitoringAlertEntity> alertOpt = alertLifecycleService.evaluateAndRaiseAlert(
                "RULE_CPU_HIGH", "SYSTEM", 92.5, "CPU exceeded threshold"
        );

        assertTrue(alertOpt.isPresent());
        assertEquals("OPEN", alertOpt.get().getStatus());
        assertEquals("WARNING", alertOpt.get().getSeverity());
        verify(notificationProvider, times(1)).sendAlertNotification(any());
        verify(incidentManagementService, never()).escalateAlertToIncident(any());
    }

    @Test
    void testCriticalAlertEscalatesToIncident() {
        MonitoringRuleEntity rule = new MonitoringRuleEntity();
        rule.setId("FEED_DOWN");
        rule.setName("Data Feed Stopped");
        rule.setMetricName("FEED_HEALTH");
        rule.setTargetComponent("FEED_NSE");
        rule.setConditionOperator("GT");
        rule.setThresholdValue(180.0);
        rule.setSeverity("CRITICAL");
        rule.setCooldownSeconds(300);
        rule.setEnabled(true);

        when(ruleRepository.findById("FEED_DOWN")).thenReturn(Optional.of(rule));
        when(alertRepository.findByStatusOrderByCreatedAtDesc("OPEN")).thenReturn(Collections.emptyList());
        when(alertRepository.save(any(MonitoringAlertEntity.class))).thenAnswer(i -> i.getArgument(0));

        Optional<MonitoringAlertEntity> alertOpt = alertLifecycleService.evaluateAndRaiseAlert(
                "FEED_DOWN", "FEED_NSE", 360.0, "Feed has been down for 360 seconds"
        );

        assertTrue(alertOpt.isPresent());
        assertEquals("CRITICAL", alertOpt.get().getSeverity());
        verify(incidentManagementService, times(1)).escalateAlertToIncident(any());
    }

    @Test
    void testCooldownSuppressesDuplicateAlertCreation() {
        MonitoringRuleEntity rule = new MonitoringRuleEntity();
        rule.setId("RULE_MEMORY_HIGH");
        rule.setName("High Memory");
        rule.setConditionOperator("GT");
        rule.setThresholdValue(85.0);
        rule.setCooldownSeconds(600);
        rule.setEnabled(true);

        MonitoringAlertEntity existingAlert = new MonitoringAlertEntity();
        existingAlert.setId(UUID.randomUUID());
        existingAlert.setRuleId("RULE_MEMORY_HIGH");
        existingAlert.setComponent("JVM");
        existingAlert.setStatus("OPEN");
        existingAlert.setCreatedAt(Instant.now().minusSeconds(100)); // created 100s ago (< 600s cooldown)

        when(ruleRepository.findById("RULE_MEMORY_HIGH")).thenReturn(Optional.of(rule));
        when(alertRepository.findByStatusOrderByCreatedAtDesc("OPEN")).thenReturn(List.of(existingAlert));
        when(alertRepository.save(any(MonitoringAlertEntity.class))).thenAnswer(i -> i.getArgument(0));

        Optional<MonitoringAlertEntity> alertOpt = alertLifecycleService.evaluateAndRaiseAlert(
                "RULE_MEMORY_HIGH", "JVM", 90.0, "Memory at 90%"
        );

        assertTrue(alertOpt.isPresent());
        assertEquals(existingAlert.getId(), alertOpt.get().getId(), "Should update existing alert rather than create a new one");
    }
}
