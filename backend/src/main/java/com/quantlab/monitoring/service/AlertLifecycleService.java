package com.quantlab.monitoring.service;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;
import com.quantlab.monitoring.entity.MonitoringRuleEntity;
import com.quantlab.monitoring.notification.MonitoringNotificationProvider;
import com.quantlab.monitoring.repository.MonitoringAlertRepository;
import com.quantlab.monitoring.repository.MonitoringRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AlertLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(AlertLifecycleService.class);

    private final MonitoringAlertRepository alertRepository;
    private final MonitoringRuleRepository ruleRepository;
    private final IncidentManagementService incidentManagementService;
    private final MonitoringMetricsService metricsService;
    private final List<MonitoringNotificationProvider> notificationProviders;

    public AlertLifecycleService(
            MonitoringAlertRepository alertRepository,
            MonitoringRuleRepository ruleRepository,
            IncidentManagementService incidentManagementService,
            MonitoringMetricsService metricsService,
            List<MonitoringNotificationProvider> notificationProviders) {
        this.alertRepository = alertRepository;
        this.ruleRepository = ruleRepository;
        this.incidentManagementService = incidentManagementService;
        this.metricsService = metricsService;
        this.notificationProviders = notificationProviders;
    }

    @Transactional
    public Optional<MonitoringAlertEntity> evaluateAndRaiseAlert(
            String ruleId,
            String component,
            double observedValue,
            String message) {

        Optional<MonitoringRuleEntity> ruleOpt = ruleRepository.findById(ruleId);
        String ruleName = ruleId;
        String severity = "WARNING";
        String alertType = "GENERIC_MONITORING_ALERT";
        double threshold = 0.0;
        int cooldownSeconds = 300;
        String runbookRef = "docs/runbooks/monitoring.md";

        if (ruleOpt.isPresent()) {
            MonitoringRuleEntity rule = ruleOpt.get();
            if (!rule.isEnabled()) {
                log.debug("Rule [{}] is disabled. Skipping evaluation.", ruleId);
                return Optional.empty();
            }
            ruleName = rule.getName();
            severity = rule.getSeverity();
            threshold = rule.getThresholdValue();
            cooldownSeconds = rule.getCooldownSeconds();
            runbookRef = rule.getRunbookRef();
            alertType = rule.getMetricName();

            // Evaluate condition operator
            boolean conditionMet = evaluateCondition(rule.getConditionOperator(), observedValue, threshold);
            if (!conditionMet) {
                return Optional.empty();
            }
        }

        // 1. Deduplication & Cooldown Check
        List<MonitoringAlertEntity> existingAlerts = alertRepository.findByStatusOrderByCreatedAtDesc("OPEN").stream()
                .filter(a -> ruleId.equals(a.getRuleId()) && component.equalsIgnoreCase(a.getComponent()))
                .toList();

        if (!existingAlerts.isEmpty()) {
            MonitoringAlertEntity existing = existingAlerts.get(0);
            Instant now = Instant.now();
            long secondsSinceLast = Duration.between(existing.getCreatedAt(), now).getSeconds();

            if (secondsSinceLast < cooldownSeconds) {
                log.debug("Alert for rule [{}] on [{}] is within cooldown ({}s < {}s). Updating observed value.",
                        ruleId, component, secondsSinceLast, cooldownSeconds);
                existing.setObservedValue(String.valueOf(observedValue));
                existing.setMessage(message);
                return Optional.of(alertRepository.save(existing));
            }
        }

        // 2. Create new Alert
        MonitoringAlertEntity alert = new MonitoringAlertEntity();
        alert.setId(UUID.randomUUID());
        alert.setRuleId(ruleId);
        alert.setRuleName(ruleName);
        alert.setAlertType(alertType);
        alert.setComponent(component);
        alert.setSeverity(severity);
        alert.setStatus("OPEN");
        alert.setObservedValue(String.valueOf(observedValue));
        alert.setThresholdValue(String.valueOf(threshold));
        alert.setMessage(message);
        alert.setRunbookRef(runbookRef);
        alert.setCreatedAt(Instant.now());

        MonitoringAlertEntity saved = alertRepository.save(alert);
        metricsService.recordAlert(severity, component);

        // 3. Dispatch Notifications
        for (MonitoringNotificationProvider provider : notificationProviders) {
            try {
                if (provider.isConfigured()) {
                    provider.sendAlertNotification(saved);
                }
            } catch (Exception e) {
                log.error("Failed to send notification via {}: {}", provider.getChannelName(), e.getMessage());
            }
        }

        // 4. Critical Escalation to Incidents
        if ("CRITICAL".equalsIgnoreCase(severity)) {
            incidentManagementService.escalateAlertToIncident(saved);
        }

        return Optional.of(saved);
    }

    private boolean evaluateCondition(String operator, double observed, double threshold) {
        if (operator == null) return observed >= threshold;
        return switch (operator.toUpperCase()) {
            case "GT" -> observed > threshold;
            case "GTE" -> observed >= threshold;
            case "LT" -> observed < threshold;
            case "LTE" -> observed <= threshold;
            case "EQ" -> Math.abs(observed - threshold) < 1e-6;
            case "NEQ" -> Math.abs(observed - threshold) >= 1e-6;
            default -> observed >= threshold;
        };
    }
}
