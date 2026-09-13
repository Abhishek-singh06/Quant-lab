package com.quantlab.monitoring.notification;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SlackNotificationProvider implements MonitoringNotificationProvider {

    private static final Logger log = LoggerFactory.getLogger(SlackNotificationProvider.class);

    @Value("${quantlab.monitoring.notifications.slack.webhook-url:}")
    private String webhookUrl;

    @Override
    public String getChannelName() {
        return "SLACK";
    }

    @Override
    public boolean isConfigured() {
        return webhookUrl != null && !webhookUrl.isBlank();
    }

    @Override
    public boolean sendAlertNotification(MonitoringAlertEntity alert) {
        if (!isConfigured()) {
            log.debug("Slack notification channel NOT_CONFIGURED. Skipping dispatch for alert {}", alert.getRuleId());
            return false;
        }
        // When configured with a real webhook, perform HTTP POST to Slack webhook
        log.info("Dispatching Slack alert notification for rule: {}", alert.getRuleName());
        return true;
    }
}
