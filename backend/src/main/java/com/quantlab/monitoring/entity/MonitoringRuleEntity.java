package com.quantlab.monitoring.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "monitoring_rules")
public class MonitoringRuleEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "rule_version", nullable = false, length = 32)
    private String ruleVersion = "1.0.0";

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "target_component", nullable = false, length = 64)
    private String targetComponent;

    @Column(name = "metric_name", nullable = false, length = 64)
    private String metricName;

    @Column(name = "condition_operator", nullable = false, length = 16)
    private String conditionOperator; // GT, GTE, LT, LTE, EQ, NEQ

    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;

    @Column(name = "window_seconds", nullable = false)
    private int windowSeconds = 300;

    @Column(name = "cooldown_seconds", nullable = false)
    private int cooldownSeconds = 600;

    @Column(nullable = false, length = 32)
    private String severity = "WARNING";

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;

    @Column(name = "runbook_ref", length = 128)
    private String runbookRef;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(String ruleVersion) { this.ruleVersion = ruleVersion; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTargetComponent() { return targetComponent; }
    public void setTargetComponent(String targetComponent) { this.targetComponent = targetComponent; }

    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }

    public String getConditionOperator() { return conditionOperator; }
    public void setConditionOperator(String conditionOperator) { this.conditionOperator = conditionOperator; }

    public Double getThresholdValue() { return thresholdValue; }
    public void setThresholdValue(Double thresholdValue) { this.thresholdValue = thresholdValue; }

    public int getWindowSeconds() { return windowSeconds; }
    public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }

    public int getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(int cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public String getRunbookRef() { return runbookRef; }
    public void setRunbookRef(String runbookRef) { this.runbookRef = runbookRef; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
