package com.quantlab.monitoring.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "monitoring_health_checks")
public class MonitoringHealthCheckEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 64)
    private String component;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "latency_ms")
    private Double latencyMs;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Double latencyMs) { this.latencyMs = latencyMs; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}
