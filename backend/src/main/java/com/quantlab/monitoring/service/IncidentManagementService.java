package com.quantlab.monitoring.service;

import com.quantlab.monitoring.entity.MonitoringAlertEntity;
import com.quantlab.monitoring.entity.MonitoringIncidentEntity;
import com.quantlab.monitoring.repository.MonitoringIncidentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class IncidentManagementService {

    private static final Logger log = LoggerFactory.getLogger(IncidentManagementService.class);

    private final MonitoringIncidentRepository incidentRepository;

    public IncidentManagementService(MonitoringIncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public MonitoringIncidentEntity escalateAlertToIncident(MonitoringAlertEntity alert) {
        if (!"CRITICAL".equalsIgnoreCase(alert.getSeverity())) {
            return null;
        }

        // Check if there is an active incident for this component
        List<MonitoringIncidentEntity> activeIncidents = incidentRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(inc -> !"RESOLVED".equalsIgnoreCase(inc.getStatus()) && !"POSTMORTEM".equalsIgnoreCase(inc.getStatus()))
                .filter(inc -> inc.getAffectedComponents() != null && inc.getAffectedComponents().contains(alert.getComponent()))
                .toList();

        if (!activeIncidents.isEmpty()) {
            MonitoringIncidentEntity existing = activeIncidents.get(0);
            log.info("Attaching critical alert [{}] to existing active incident [{}]", alert.getRuleName(), existing.getIncidentNumber());
            existing.setRootCause(existing.getRootCause() + "\n- Related Alert: " + alert.getMessage());
            return incidentRepository.save(existing);
        }

        // Generate new incident
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        int randomId = ThreadLocalRandom.current().nextInt(1000, 9999);
        String incidentNumber = "INC-" + dateStr + "-" + randomId;

        MonitoringIncidentEntity incident = new MonitoringIncidentEntity();
        incident.setId(UUID.randomUUID());
        incident.setIncidentNumber(incidentNumber);
        incident.setTitle("CRITICAL ALERT: " + alert.getRuleName() + " on " + alert.getComponent());
        incident.setSeverity("CRITICAL");
        incident.setStatus("DETECTED");
        incident.setAffectedComponents(alert.getComponent());
        incident.setRootCause("Triggered by Alert: " + alert.getMessage() + " (Observed: " + alert.getObservedValue() + ", Threshold: " + alert.getThresholdValue() + ")");
        incident.setCreatedAt(Instant.now());

        log.warn("Escalated critical alert to new Incident [{}]: {}", incidentNumber, incident.getTitle());
        return incidentRepository.save(incident);
    }

    @Transactional
    public Optional<MonitoringIncidentEntity> updateIncidentStatus(UUID incidentId, String status, String rootCause) {
        return incidentRepository.findById(incidentId).map(inc -> {
            inc.setStatus(status);
            if (rootCause != null && !rootCause.isBlank()) {
                inc.setRootCause(rootCause);
            }
            if ("RESOLVED".equalsIgnoreCase(status) || "POSTMORTEM".equalsIgnoreCase(status)) {
                inc.setResolvedAt(Instant.now());
            }
            return incidentRepository.save(inc);
        });
    }
}
