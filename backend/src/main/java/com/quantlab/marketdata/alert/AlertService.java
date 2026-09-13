package com.quantlab.marketdata.alert;

import com.quantlab.marketdata.model.AlertEvent;

public interface AlertService {
    void sendAlert(AlertEvent event);
}
