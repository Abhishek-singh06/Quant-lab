package com.quantlab.monitoring.notification;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingNotificationProvider implements MonitoringNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationProvider.class);

    @Override
    public String getChannelName() {
        return "SYSTEM_LOG";
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public boolean sendAlertNotification(MonitoringAlertEntity alert) {
        if ("CRITICAL".equalsIgnoreCase(alert.getSeverity())) {
            log.error("[ALERT-CRITICAL] Rule: {} | Component: {} | Message: {} | Observed: {} | Threshold: {}",
                    alert.getRuleName(), alert.getComponent(), alert.getMessage(), alert.getObservedValue(), alert.getThresholdValue());
        } else if ("WARNING".equalsIgnoreCase(alert.getSeverity()) || "ERROR".equalsIgnoreCase(alert.getSeverity())) {
            log.warn("[ALERT-{}] Rule: {} | Component: {} | Message: {}",
                    alert.getSeverity(), alert.getRuleName(), alert.getComponent(), alert.getMessage());
        } else {
            log.info("[ALERT-{}] Rule: {} | Component: {} | Message: {}",
                    alert.getSeverity(), alert.getRuleName(), alert.getComponent(), alert.getMessage());
        }
        return true;
    }
}
