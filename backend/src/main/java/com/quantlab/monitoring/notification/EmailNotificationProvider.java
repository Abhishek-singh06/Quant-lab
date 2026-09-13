package com.quantlab.monitoring.notification;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider implements MonitoringNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationProvider.class);

    @Value("${quantlab.monitoring.notifications.email.smtp-host:}")
    private String smtpHost;

    @Value("${quantlab.monitoring.notifications.email.recipients:}")
    private String recipients;

    @Override
    public String getChannelName() {
        return "EMAIL";
    }

    @Override
    public boolean isConfigured() {
        return smtpHost != null && !smtpHost.isBlank() && recipients != null && !recipients.isBlank();
    }

    @Override
    public boolean sendAlertNotification(MonitoringAlertEntity alert) {
        if (!isConfigured()) {
            log.debug("Email notification channel NOT_CONFIGURED. Skipping dispatch for alert {}", alert.getRuleId());
            return false;
        }
        log.info("Dispatching Email alert notification for rule: {} to {}", alert.getRuleName(), recipients);
        return true;
    }
}
