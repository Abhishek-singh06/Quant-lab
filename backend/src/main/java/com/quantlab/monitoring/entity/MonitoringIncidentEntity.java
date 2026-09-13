package com.quantlab.monitoring.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitoring_incidents")
public class MonitoringIncidentEntity {

    @Id
    private UUID id;

    @Column(name = "incident_number", nullable = false, unique = true, length = 32)
    private String incidentNumber;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false, length = 32)
    private String severity;

    @Column(nullable = false, length = 32)
    private String status = "DETECTED";

    @Column(name = "affected_components", nullable = false, columnDefinition = "TEXT")
    private String affectedComponents;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIncidentNumber() { return incidentNumber; }
    public void setIncidentNumber(String incidentNumber) { this.incidentNumber = incidentNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAffectedComponents() { return affectedComponents; }
    public void setAffectedComponents(String affectedComponents) { this.affectedComponents = affectedComponents; }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
