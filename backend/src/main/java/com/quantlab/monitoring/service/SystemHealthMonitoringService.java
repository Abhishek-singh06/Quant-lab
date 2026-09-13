package com.quantlab.monitoring.service;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;
import com.quantlab.monitoring.entity.ProviderHealthEntity;
import com.quantlab.monitoring.model.SystemOverviewHealthDTO;
import com.quantlab.monitoring.repository.MonitoringAlertRepository;
import com.quantlab.monitoring.repository.ProviderHealthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemHealthMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(SystemHealthMonitoringService.class);

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final MonitoringAlertRepository alertRepository;
    private final ProviderHealthRepository providerHealthRepository;

    @Autowired
    public SystemHealthMonitoringService(
            DataSource dataSource,
            @Autowired(required = false) RedisConnectionFactory redisConnectionFactory,
            MonitoringAlertRepository alertRepository,
            ProviderHealthRepository providerHealthRepository) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.alertRepository = alertRepository;
        this.providerHealthRepository = providerHealthRepository;
    }

    public SystemOverviewHealthDTO getRealSystemOverview() {
        Map<String, String> subsystemStatus = new LinkedHashMap<>();

        // 1. Real Database Connectivity
        boolean dbHealthy = checkDatabaseHealth();
        subsystemStatus.put("POSTGRESQL", dbHealthy ? "HEALTHY" : "CRITICAL");

        // 2. Real Redis Connectivity
        String redisStatus = checkRedisHealth();
        subsystemStatus.put("REDIS_CACHE", redisStatus);

        // 3. Real JVM Memory
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memUsagePct = maxMemory > 0 ? ((double) usedMemory / maxMemory) * 100.0 : 0.0;
        subsystemStatus.put("JVM_MEMORY", memUsagePct > 85.0 ? "WARNING" : "HEALTHY");

        // 4. Real Disk Storage
        File root = new File(".");
        long usableSpace = root.getUsableSpace();
        long totalSpace = root.getTotalSpace();
        double diskUsagePct = totalSpace > 0 ? ((double) (totalSpace - usableSpace) / totalSpace) * 100.0 : 0.0;
        subsystemStatus.put("DISK_STORAGE", usableSpace < (1024L * 1024 * 1024) ? "WARNING" : "HEALTHY");

        // 5. Market Data Provider Health
        List<ProviderHealthEntity> providers = providerHealthRepository.findAllByOrderByUpdatedAtDesc();
        if (providers.isEmpty()) {
            subsystemStatus.put("MARKET_DATA", "UNKNOWN");
        } else {
            boolean anyCritical = providers.stream().anyMatch(p -> "CRITICAL".equalsIgnoreCase(p.getConnectionStatus()) || "UNAVAILABLE".equalsIgnoreCase(p.getConnectionStatus()));
            boolean anyDegraded = providers.stream().anyMatch(p -> "DEGRADED".equalsIgnoreCase(p.getConnectionStatus()) || "WARNING".equalsIgnoreCase(p.getConnectionStatus()));
            subsystemStatus.put("MARKET_DATA", anyCritical ? "CRITICAL" : (anyDegraded ? "DEGRADED" : "HEALTHY"));
        }

        // 6. Subsystem status based on open alerts
        List<MonitoringAlertEntity> openAlerts = alertRepository.findByStatusOrderByCreatedAtDesc("OPEN");
        long criticalCount = openAlerts.stream().filter(a -> "CRITICAL".equalsIgnoreCase(a.getSeverity())).count();

        subsystemStatus.put("FEATURE_ENGINE", hasOpenAlertsFor(openAlerts, "FEATURE") ? "DEGRADED" : "HEALTHY");
        subsystemStatus.put("MODELS", hasOpenAlertsFor(openAlerts, "MODEL") ? "DEGRADED" : "HEALTHY");
        subsystemStatus.put("SIGNALS", hasOpenAlertsFor(openAlerts, "SIGNAL") ? "DEGRADED" : "HEALTHY");
        subsystemStatus.put("RISK_ENGINE", hasOpenAlertsFor(openAlerts, "RISK") ? "DEGRADED" : "HEALTHY");
        subsystemStatus.put("PAPER_TRADING", hasOpenAlertsFor(openAlerts, "PAPER") ? "DEGRADED" : "HEALTHY");

        // 7. Overall System Status
        String overallStatus = "HEALTHY";
        if (!dbHealthy || criticalCount > 0) {
            overallStatus = "CRITICAL";
        } else if (!openAlerts.isEmpty() || "DEGRADED".equals(subsystemStatus.get("MARKET_DATA")) || memUsagePct > 85.0) {
            overallStatus = "WARNING";
        }

        // 8. Real CPU calculation
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuLoad = osBean.getSystemLoadAverage();
        if (cpuLoad < 0) {
            cpuLoad = (double) Runtime.getRuntime().availableProcessors();
        }

        return new SystemOverviewHealthDTO(
                overallStatus,
                subsystemStatus,
                openAlerts.size(),
                (int) criticalCount,
                Math.round(cpuLoad * 10.0) / 10.0,
                Math.round(memUsagePct * 10.0) / 10.0,
                false,
                Instant.now()
        );
    }

    private boolean checkDatabaseHealth() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            log.warn("Database health check failed: {}", e.getMessage());
            return false;
        }
    }

    private String checkRedisHealth() {
        if (redisConnectionFactory == null) {
            return "NOT_CONFIGURED";
        }
        try {
            var conn = redisConnectionFactory.getConnection();
            String ping = conn.ping();
            conn.close();
            return "PONG".equalsIgnoreCase(ping) ? "HEALTHY" : "CRITICAL";
        } catch (Exception e) {
            log.debug("Redis health ping failed: {}", e.getMessage());
            return "UNAVAILABLE";
        }
    }

    private boolean hasOpenAlertsFor(List<MonitoringAlertEntity> alerts, String prefix) {
        return alerts.stream().anyMatch(a -> a.getComponent() != null && a.getComponent().toUpperCase().contains(prefix));
    }
}
