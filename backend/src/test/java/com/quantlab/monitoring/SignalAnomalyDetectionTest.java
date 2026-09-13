package com.quantlab.monitoring;

import com.quantlab.monitoring.entity.SignalAnomalyEventEntity;
import com.quantlab.monitoring.repository.SignalAnomalyEventRepository;
import com.quantlab.monitoring.service.AlertLifecycleService;
import com.quantlab.monitoring.service.SignalAnomalyDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SignalAnomalyDetectionTest {

    private SignalAnomalyEventRepository repository;
    private AlertLifecycleService alertLifecycleService;
    private SignalAnomalyDetectionService service;

    @BeforeEach
    void setUp() {
        repository = mock(SignalAnomalyEventRepository.class);
        alertLifecycleService = mock(AlertLifecycleService.class);
        service = new SignalAnomalyDetectionService(repository, alertLifecycleService);
    }

    @Test
    void testNormalSignalRateDoesNotTriggerAlert() {
        when(repository.save(any(SignalAnomalyEventEntity.class))).thenAnswer(i -> i.getArgument(0));

        SignalAnomalyEventEntity event = service.evaluateSignalAnomaly(
                "BUY", "BUY_SPIKE", 0.12, 0.10, 2.0, "NIFTY_50", "Normal BUY rate"
        );

        assertNotNull(event);
        assertEquals("INFO", event.getSeverity());
        verify(alertLifecycleService, never()).evaluateAndRaiseAlert(any(), any(), anyDouble(), any());
    }

    @Test
    void testBuySpikeTriggersAlert() {
        when(repository.save(any(SignalAnomalyEventEntity.class))).thenAnswer(i -> i.getArgument(0));

        SignalAnomalyEventEntity event = service.evaluateSignalAnomaly(
                "BUY", "BUY_SPIKE", 0.85, 0.15, 2.0, "BANKING", "Massive BUY spike in Banking"
        );

        assertNotNull(event);
        assertTrue("WARNING".equals(event.getSeverity()) || "CRITICAL".equals(event.getSeverity()));
        verify(alertLifecycleService, times(1)).evaluateAndRaiseAlert(
                eq("SIG_ANOMALY_BUY_SPIKE"), eq("SIGNAL_ENGINE"), eq(0.85), any()
        );
    }
}
