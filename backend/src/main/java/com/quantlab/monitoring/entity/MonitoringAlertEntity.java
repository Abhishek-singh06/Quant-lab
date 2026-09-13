package com.quantlab.monitoring.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitoring_alerts")
public class MonitoringAlertEntity {

    @Id
    private UUID id;

    @Column(name = "rule_id", nullable = false, length = 64)
    private String ruleId;

    @Column(name = "rule_name", nullable = false, length = 128)
    private String ruleName;

    @Column(name = "alert_type", nullable = false, length = 64)
    private String alertType;

    @Column(nullable = false, length = 64)
    private String component;

    @Column(nullable = false, length = 32)
    private String severity;

    @Column(nullable = false, length = 32)
    private String status = "OPEN";

    @Column(name = "observed_value", length = 128)
    private String observedValue;

    @Column(name = "threshold_value", length = 128)
    private String thresholdValue;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "runbook_ref", length = 128)
    private String runbookRef;

    @Column(name = "acknowledged_by", length = 64)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }

    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getObservedValue() { return observedValue; }
    public void setObservedValue(String observedValue) { this.observedValue = observedValue; }

    public String getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(String thresholdValue) { this.thresholdValue = thresholdValue; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRunbookRef() { return runbookRef; }
    public void setRunbookRef(String runbookRef) { this.runbookRef = runbookRef; }

    public String getAcknowledgedBy() { return acknowledgedBy; }
    public void setAcknowledgedBy(String acknowledgedBy) { this.acknowledgedBy = acknowledgedBy; }

    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(Instant acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
