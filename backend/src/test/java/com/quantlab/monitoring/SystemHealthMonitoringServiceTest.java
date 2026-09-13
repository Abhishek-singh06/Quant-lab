package com.quantlab.monitoring;

import com.quantlab.monitoring.model.SystemOverviewHealthDTO;
import com.quantlab.monitoring.repository.MonitoringAlertRepository;
import com.quantlab.monitoring.repository.ProviderHealthRepository;
import com.quantlab.monitoring.service.SystemHealthMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class SystemHealthMonitoringServiceTest {

    private DataSource dataSource;
    private MonitoringAlertRepository alertRepository;
    private ProviderHealthRepository providerHealthRepository;
    private SystemHealthMonitoringService service;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        Connection mockConn = mock(Connection.class);
        when(mockConn.isValid(anyInt())).thenReturn(true);
        when(dataSource.getConnection()).thenReturn(mockConn);

        alertRepository = mock(MonitoringAlertRepository.class);
        when(alertRepository.findByStatusOrderByCreatedAtDesc("OPEN")).thenReturn(Collections.emptyList());

        providerHealthRepository = mock(ProviderHealthRepository.class);
        when(providerHealthRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(Collections.emptyList());

        // redisConnectionFactory is null -> should report NOT_CONFIGURED
        service = new SystemHealthMonitoringService(
                dataSource, null, alertRepository, providerHealthRepository
        );
    }

    @Test
    void testRealSystemHealthWithNullRedisReportsNotConfigured() {
        SystemOverviewHealthDTO overview = service.getRealSystemOverview();

        assertNotNull(overview);
        assertEquals("HEALTHY", overview.subsystemStatus().get("POSTGRESQL"));
        assertEquals("NOT_CONFIGURED", overview.subsystemStatus().get("REDIS_CACHE"), "Unconfigured Redis should explicitly report NOT_CONFIGURED");
        assertEquals(0, overview.activeAlertsCount());
        assertEquals(0, overview.criticalAlertsCount());
        assertTrue(overview.databasePoolUsagePct() >= 0.0);
    }

    @Test
    void testDatabaseFailureReportsCriticalStatus() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        SystemOverviewHealthDTO overview = service.getRealSystemOverview();

        assertNotNull(overview);
        assertEquals("CRITICAL", overview.subsystemStatus().get("POSTGRESQL"));
        assertEquals("CRITICAL", overview.overallSystemStatus());
    }
}
