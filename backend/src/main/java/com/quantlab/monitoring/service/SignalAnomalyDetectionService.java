package com.quantlab.monitoring.service;

import com.quantlab.monitoring.entity.SignalAnomalyEventEntity;
import com.quantlab.monitoring.repository.SignalAnomalyEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SignalAnomalyDetectionService {

    private static final Logger log = LoggerFactory.getLogger(SignalAnomalyDetectionService.class);

    private final SignalAnomalyEventRepository signalAnomalyEventRepository;
    private final AlertLifecycleService alertLifecycleService;

    public SignalAnomalyDetectionService(
            SignalAnomalyEventRepository signalAnomalyEventRepository,
            AlertLifecycleService alertLifecycleService) {
        this.signalAnomalyEventRepository = signalAnomalyEventRepository;
        this.alertLifecycleService = alertLifecycleService;
    }

    @Transactional
    public SignalAnomalyEventEntity evaluateSignalAnomaly(
            String signalType,
            String anomalyCategory,
            double observedRate,
            double expectedRate,
            double thresholdRatio,
            String affectedSector,
            String description) {

        double ratio = expectedRate > 1e-6 ? (observedRate / expectedRate) : (observedRate > 0 ? 10.0 : 1.0);
        String severity = "INFO";

        if (ratio >= thresholdRatio * 1.5 || ratio <= (1.0 / (thresholdRatio * 1.5))) {
            severity = "CRITICAL";
        } else if (ratio >= thresholdRatio || ratio <= (1.0 / thresholdRatio)) {
            severity = "WARNING";
        }

        SignalAnomalyEventEntity event = new SignalAnomalyEventEntity();
        event.setId(UUID.randomUUID());
        event.setSignalType(signalType != null ? signalType : "ALL_SIGNALS");
        event.setAnomalyCategory(anomalyCategory);
        event.setObservedRate(observedRate);
        event.setExpectedRate(expectedRate);
        event.setAffectedSector(affectedSector);
        event.setSeverity(severity);
        event.setDescription(description != null ? description : String.format("Signal anomaly %s: observed %.4f vs expected %.4f", anomalyCategory, observedRate, expectedRate));
        event.setDetectedAt(Instant.now());

        SignalAnomalyEventEntity saved = signalAnomalyEventRepository.save(event);

        if (!"INFO".equalsIgnoreCase(severity)) {
            alertLifecycleService.evaluateAndRaiseAlert(
                    "SIG_ANOMALY_" + anomalyCategory,
                    "SIGNAL_ENGINE",
                    observedRate,
                    event.getDescription()
            );
        }

        return saved;
    }
}
