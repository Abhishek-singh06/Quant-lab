package com.quantlab.marketdata.alert;

import com.quantlab.marketdata.model.AlertEvent;
import com.quantlab.marketdata.model.AlertSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default structured logging alert service.
 * Dispatches operational alerts to logs, in-memory history, and provides extension hook
 * for Slack, PagerDuty, or Webhook notification integrations.
 */
@Service
public class LoggingAlertService implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(LoggingAlertService.class);

    private final List<AlertEvent> recentAlerts = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void sendAlert(AlertEvent event) {
        if (event == null) return;

        recentAlerts.add(event);
        if (recentAlerts.size() > 1000) {
            recentAlerts.remove(0);
        }

        String formatted = String.format("[ALERT - %s] [%s] %s | Provider=%s | RunId=%s | %s",
                event.severity(), event.alertType(), event.title(), event.provider(), event.runId(), event.message());

        if (event.severity() == AlertSeverity.CRITICAL || event.severity() == AlertSeverity.ERROR) {
            log.error(formatted);
        } else if (event.severity() == AlertSeverity.WARNING) {
            log.warn(formatted);
        } else {
            log.info(formatted);
        }
    }

    public List<AlertEvent> getRecentAlerts() {
        return List.copyOf(recentAlerts);
    }
}
