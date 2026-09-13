package com.quantlab.monitoring;

import com.quantlab.monitoring.entity.ProviderHealthEntity;
import com.quantlab.monitoring.repository.ProviderHealthRepository;
import com.quantlab.monitoring.service.AlertLifecycleService;
import com.quantlab.monitoring.service.MonitoringMetricsService;
import com.quantlab.monitoring.service.ProviderHealthMonitoringService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProviderHealthMonitoringServiceTest {

    private ProviderHealthRepository providerHealthRepository;
    private AlertLifecycleService alertLifecycleService;
    private MonitoringMetricsService metricsService;
    private ProviderHealthMonitoringService service;

    @BeforeEach
    void setUp() {
        providerHealthRepository = mock(ProviderHealthRepository.class);
        alertLifecycleService = mock(AlertLifecycleService.class);
        metricsService = new MonitoringMetricsService(new SimpleMeterRegistry());

        service = new ProviderHealthMonitoringService(
                providerHealthRepository,
                alertLifecycleService,
                metricsService
        );
    }

    @Test
    void testRecordSuccessfulProviderUpdate() {
        when(providerHealthRepository.findByProvider("NSE")).thenReturn(Optional.empty());
        when(providerHealthRepository.save(any(ProviderHealthEntity.class))).thenAnswer(i -> i.getArgument(0));

        Instant marketTs = Instant.now().minusSeconds(10);
        ProviderHealthEntity health = service.recordProviderUpdate("NSE", marketTs, 0, 50, 50, 45.0);

        assertNotNull(health);
        assertEquals("NSE", health.getProvider());
        assertEquals("HEALTHY", health.getConnectionStatus());
        assertEquals("REAL_TIME", health.getDataFreshnessStatus());
        assertEquals(100.0, health.getHealthScore());
        assertEquals(100.0, health.getUniverseCoveragePct());
        assertEquals(0, health.getConsecutiveFailures());
        verify(alertLifecycleService, never()).evaluateAndRaiseAlert(any(), any(), anyDouble(), any());
    }

    @Test
    void testRecordProviderFailureTriggersCriticalAlert() {
        ProviderHealthEntity existing = new ProviderHealthEntity();
        existing.setProvider("NSE");
        existing.setConsecutiveFailures(4); // will become 5
        existing.setLastSuccessfulUpdate(Instant.now().minusSeconds(4000));

        when(providerHealthRepository.findByProvider("NSE")).thenReturn(Optional.of(existing));
        when(providerHealthRepository.save(any(ProviderHealthEntity.class))).thenAnswer(i -> i.getArgument(0));

        ProviderHealthEntity health = service.recordProviderUpdate("NSE", null, 1, 0, 50, 0.0);

        assertEquals("UNAVAILABLE", health.getConnectionStatus());
        assertEquals("NOT_AVAILABLE", health.getDataFreshnessStatus());
        assertEquals(0.0, health.getHealthScore());
        assertEquals(5, health.getConsecutiveFailures());
        verify(alertLifecycleService, times(1)).evaluateAndRaiseAlert(
                eq("DATA_FEED_STOPPED"), eq("FEED_NSE"), anyDouble(), any()
        );
    }
}
