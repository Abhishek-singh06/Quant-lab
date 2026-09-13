package com.quantlab.monitoring.service;

import com.quantlab.monitoring.entity.DataQualityEventEntity;
import com.quantlab.monitoring.repository.DataQualityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class DataQualityMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(DataQualityMonitoringService.class);

    private final DataQualityEventRepository dataQualityEventRepository;
    private final AlertLifecycleService alertLifecycleService;
    private final MonitoringMetricsService metricsService;

    public DataQualityMonitoringService(
            DataQualityEventRepository dataQualityEventRepository,
            AlertLifecycleService alertLifecycleService,
            MonitoringMetricsService metricsService) {
        this.dataQualityEventRepository = dataQualityEventRepository;
        this.alertLifecycleService = alertLifecycleService;
        this.metricsService = metricsService;
    }

    @Transactional
    public DataQualityEventEntity recordDataQualityEvent(
            String provider,
            String symbol,
            String eventType,
            String severity,
            String description,
            int affectedRecords,
            Instant sourceTimestamp,
            Instant availableTimestamp) {

        DataQualityEventEntity event = new DataQualityEventEntity();
        event.setId(UUID.randomUUID());
        event.setProvider(provider != null ? provider : "DEFAULT_PROVIDER");
        event.setSymbol(symbol);
        event.setEventType(eventType);
        event.setSeverity(severity != null ? severity.toUpperCase() : "WARNING");
        event.setDescription(description);
        event.setAffectedRecordsCount(affectedRecords > 0 ? affectedRecords : 1);
        event.setSourceTimestamp(sourceTimestamp);
        event.setAvailableTimestamp(availableTimestamp);
        event.setDetectedAt(Instant.now());

        DataQualityEventEntity saved = dataQualityEventRepository.save(event);

        if ("STALE_FEED".equalsIgnoreCase(eventType)) {
            metricsService.recordStaleDataEvent();
        }

        if ("ERROR".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
            log.warn("High severity data quality event detected [{}]: {}", eventType, description);
            alertLifecycleService.evaluateAndRaiseAlert(
                    "DQ_RULE_" + eventType,
                    "MARKET_DATA_" + provider,
                    affectedRecords,
                    description
            );
        }

        return saved;
    }
}
