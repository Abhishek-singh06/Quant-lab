package com.quantlab.monitoring.notification;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;

public interface MonitoringNotificationProvider {
    String getChannelName();
    boolean isConfigured();
    boolean sendAlertNotification(MonitoringAlertEntity alert);
}
