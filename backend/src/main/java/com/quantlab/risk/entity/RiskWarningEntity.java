package com.quantlab.risk.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_warnings")
public class RiskWarningEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    private RiskAssessmentEntity assessment;

    @Column(name = "warning_code", nullable = false, length = 50)
    private String warningCode;

    @Column(name = "severity", nullable = false, length = 20)
    private String severity; // LOW, MODERATE, HIGH, CRITICAL

    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public RiskWarningEntity() {}

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public RiskAssessmentEntity getAssessment() { return assessment; }
    public void setAssessment(RiskAssessmentEntity assessment) { this.assessment = assessment; }

    public String getWarningCode() { return warningCode; }
    public void setWarningCode(String warningCode) { this.warningCode = warningCode; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
