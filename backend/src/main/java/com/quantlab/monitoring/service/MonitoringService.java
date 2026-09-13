package com.quantlab.monitoring.service;

import com.quantlab.monitoring.entity.*;
import com.quantlab.monitoring.model.*;
import com.quantlab.monitoring.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class MonitoringService {

    private final MonitoringHealthCheckRepository healthCheckRepository;
    private final MonitoringAlertRepository alertRepository;
    private final MonitoringIncidentRepository incidentRepository;
    private final ProviderHealthRepository providerHealthRepository;
    private final DataQualityEventRepository dataQualityEventRepository;
    private final FeatureDriftEventRepository featureDriftEventRepository;
    private final SignalAnomalyEventRepository signalAnomalyEventRepository;
    private final MonitoringRuleRepository monitoringRuleRepository;
    private final SystemHealthMonitoringService systemHealthMonitoringService;
    private final FeatureDriftCalculator featureDriftCalculator;
    private final DataQualityMonitoringService dataQualityMonitoringService;
    private final SignalAnomalyDetectionService signalAnomalyDetectionService;
    private final ProviderHealthMonitoringService providerHealthMonitoringService;
    private final AlertLifecycleService alertLifecycleService;

    public MonitoringService(
            MonitoringHealthCheckRepository healthCheckRepository,
            MonitoringAlertRepository alertRepository,
            MonitoringIncidentRepository incidentRepository,
            ProviderHealthRepository providerHealthRepository,
            DataQualityEventRepository dataQualityEventRepository,
            FeatureDriftEventRepository featureDriftEventRepository,
            SignalAnomalyEventRepository signalAnomalyEventRepository,
            MonitoringRuleRepository monitoringRuleRepository,
            SystemHealthMonitoringService systemHealthMonitoringService,
            FeatureDriftCalculator featureDriftCalculator,
            DataQualityMonitoringService dataQualityMonitoringService,
            SignalAnomalyDetectionService signalAnomalyDetectionService,
            ProviderHealthMonitoringService providerHealthMonitoringService,
            AlertLifecycleService alertLifecycleService) {
        this.healthCheckRepository = healthCheckRepository;
        this.alertRepository = alertRepository;
        this.incidentRepository = incidentRepository;
        this.providerHealthRepository = providerHealthRepository;
        this.dataQualityEventRepository = dataQualityEventRepository;
        this.featureDriftEventRepository = featureDriftEventRepository;
        this.signalAnomalyEventRepository = signalAnomalyEventRepository;
        this.monitoringRuleRepository = monitoringRuleRepository;
        this.systemHealthMonitoringService = systemHealthMonitoringService;
        this.featureDriftCalculator = featureDriftCalculator;
        this.dataQualityMonitoringService = dataQualityMonitoringService;
        this.signalAnomalyDetectionService = signalAnomalyDetectionService;
        this.providerHealthMonitoringService = providerHealthMonitoringService;
        this.alertLifecycleService = alertLifecycleService;
    }

    public SystemOverviewHealthDTO getSystemOverview() {
        return systemHealthMonitoringService.getRealSystemOverview();
    }

    public List<MonitoringHealthCheckDTO> getHealthChecks() {
        return healthCheckRepository.findAllByOrderByCheckedAtDesc().stream()
                .map(this::mapHealthCheck)
                .toList();
    }

    public List<MonitoringAlertDTO> getAlerts(String status) {
        if (status != null && !status.isBlank()) {
            return alertRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                    .map(this::mapAlert)
                    .toList();
        }
        return alertRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapAlert)
                .toList();
    }

    public Optional<MonitoringAlertDTO> getAlert(UUID id) {
        return alertRepository.findById(id).map(this::mapAlert);
    }

    @Transactional
    public Optional<MonitoringAlertDTO> acknowledgeAlert(UUID id, String user) {
        return alertRepository.findById(id).map(alert -> {
            alert.setStatus("ACKNOWLEDGED");
            alert.setAcknowledgedBy(user != null ? user : "OPERATOR");
            alert.setAcknowledgedAt(Instant.now());
            return mapAlert(alertRepository.save(alert));
        });
    }

    @Transactional
    public Optional<MonitoringAlertDTO> resolveAlert(UUID id) {
        return alertRepository.findById(id).map(alert -> {
            alert.setStatus("RESOLVED");
            alert.setResolvedAt(Instant.now());
            return mapAlert(alertRepository.save(alert));
        });
    }

    public List<MonitoringIncidentDTO> getIncidents() {
        return incidentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapIncident)
                .toList();
    }

    public List<ProviderHealthDTO> getProviderHealth() {
        return providerHealthRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::mapProviderHealth)
                .toList();
    }

    public List<DataQualityEventDTO> getDataQualityEvents() {
        return dataQualityEventRepository.findAllByOrderByDetectedAtDesc().stream()
                .map(this::mapDataQualityEvent)
                .toList();
    }

    public List<FeatureDriftEventDTO> getFeatureDriftEvents() {
        return featureDriftEventRepository.findAllByOrderByDetectedAtDesc().stream()
                .map(this::mapFeatureDriftEvent)
                .toList();
    }

    public List<SignalAnomalyEventDTO> getSignalAnomalyEvents() {
        return signalAnomalyEventRepository.findAllByOrderByDetectedAtDesc().stream()
                .map(this::mapSignalAnomalyEvent)
                .toList();
    }

    public List<MonitoringRuleDTO> getMonitoringRules() {
        return monitoringRuleRepository.findAll().stream()
                .map(this::mapMonitoringRule)
                .toList();
    }

    @Transactional
    public FeatureDriftEventEntity calculateAndRecordFeatureDrift(
            String featureName,
            String metricType,
            double[] baseline,
            double[] current,
            double threshold,
            String referenceWindow,
            String evaluationWindow) {

        FeatureDriftCalculator.DriftResult result = switch (metricType.toUpperCase()) {
            case "KS_TEST" -> featureDriftCalculator.calculateKsTest(baseline, current, threshold);
            case "WASSERSTEIN" -> featureDriftCalculator.calculateWassersteinDistance(baseline, current, threshold);
            case "MEAN_DIFF" -> featureDriftCalculator.calculateMeanDifference(baseline, current, threshold);
            default -> featureDriftCalculator.calculatePsi(baseline, current, 10, threshold);
        };

        FeatureDriftEventEntity entity = new FeatureDriftEventEntity();
        entity.setId(UUID.randomUUID());
        entity.setFeatureName(featureName);
        entity.setMetricType(result.metricType());
        entity.setObservedValue(result.observedValue());
        entity.setThreshold(result.threshold());
        entity.setDriftStatus(result.driftStatus());
        entity.setSampleSize(result.sampleSize());
        entity.setReferenceWindow(referenceWindow);
        entity.setEvaluationWindow(evaluationWindow);
        entity.setDetectedAt(Instant.now());

        FeatureDriftEventEntity saved = featureDriftEventRepository.save(entity);

        if ("CRITICAL".equalsIgnoreCase(result.driftStatus()) || "WARNING".equalsIgnoreCase(result.driftStatus())) {
            alertLifecycleService.evaluateAndRaiseAlert(
                    "DRIFT_" + featureName,
                    "FEATURE_STORE",
                    result.observedValue(),
                    String.format("Feature %s exhibited %s with %s = %.4f (threshold: %.4f)",
                            featureName, result.driftStatus(), result.metricType(), result.observedValue(), threshold)
            );
        }

        return saved;
    }

    private MonitoringHealthCheckDTO mapHealthCheck(MonitoringHealthCheckEntity e) {
        return new MonitoringHealthCheckDTO(
                e.getId(), e.getComponent(), e.getStatus(), e.getLatencyMs(), e.getMessage(), e.getCheckedAt()
        );
    }

    private MonitoringAlertDTO mapAlert(MonitoringAlertEntity e) {
        return new MonitoringAlertDTO(
                e.getId(), e.getRuleId(), e.getRuleName(), e.getAlertType(), e.getComponent(),
                e.getSeverity(), e.getStatus(), e.getObservedValue(), e.getThresholdValue(),
                e.getMessage(), e.getRunbookRef(), e.getAcknowledgedBy(), e.getAcknowledgedAt(),
                e.getResolvedAt(), e.getCreatedAt()
        );
    }

    private MonitoringIncidentDTO mapIncident(MonitoringIncidentEntity e) {
        return new MonitoringIncidentDTO(
                e.getId(), e.getIncidentNumber(), e.getTitle(), e.getSeverity(), e.getStatus(),
                e.getAffectedComponents(), e.getRootCause(), e.getCreatedAt(), e.getResolvedAt()
        );
    }

    private ProviderHealthDTO mapProviderHealth(ProviderHealthEntity e) {
        return new ProviderHealthDTO(
                e.getId(), e.getProvider(), e.getConnectionStatus(), e.getDataFreshnessStatus(),
                e.getLastSuccessfulUpdate(), e.getLastMarketTimestamp(), e.getLatencyMs(),
                e.getDataAgeSeconds(), e.getConsecutiveFailures(), e.getHealthScore(),
                e.getUniverseCoveragePct(), e.isFailingOver(), e.getFallbackProvider(), e.getUpdatedAt()
        );
    }

    private DataQualityEventDTO mapDataQualityEvent(DataQualityEventEntity e) {
        return new DataQualityEventDTO(
                e.getId(), e.getProvider(), e.getSymbol(), e.getEventType(), e.getSeverity(),
                e.getDescription(), e.getAffectedRecordsCount(), e.getSourceTimestamp(),
                e.getAvailableTimestamp(), e.getDetectedAt()
        );
    }

    private FeatureDriftEventDTO mapFeatureDriftEvent(FeatureDriftEventEntity e) {
        return new FeatureDriftEventDTO(
                e.getId(), e.getFeatureName(), e.getMetricType(), e.getObservedValue(),
                e.getThreshold(), e.getDriftStatus(), e.getSampleSize(), e.getReferenceWindow(),
                e.getEvaluationWindow(), e.getDetectedAt()
        );
    }

    private SignalAnomalyEventDTO mapSignalAnomalyEvent(SignalAnomalyEventEntity e) {
        return new SignalAnomalyEventDTO(
                e.getId(), e.getSignalType(), e.getAnomalyCategory(), e.getObservedRate(),
                e.getExpectedRate(), e.getAffectedSector(), e.getSeverity(), e.getDescription(),
                e.getDetectedAt()
        );
    }

    private MonitoringRuleDTO mapMonitoringRule(MonitoringRuleEntity e) {
        return new MonitoringRuleDTO(
                e.getId(), e.getRuleVersion(), e.getName(), e.getTargetComponent(), e.getMetricName(),
                e.getConditionOperator(), e.getThresholdValue(), e.getWindowSeconds(), e.getCooldownSeconds(),
                e.getSeverity(), e.isEnabled(), e.getRunbookRef(), e.getUpdatedAt()
        );
    }
}
