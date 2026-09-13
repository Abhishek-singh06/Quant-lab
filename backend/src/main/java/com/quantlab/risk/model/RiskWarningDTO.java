package com.quantlab.risk.model;

import java.time.Instant;
import java.util.UUID;

public class RiskWarningDTO {
    private UUID id;
    private UUID assessmentId;
    private String warningCode;
    private String severity; // LOW, MODERATE, HIGH, CRITICAL
    private String message;
    private Instant createdAt;

    public RiskWarningDTO() {}

    public RiskWarningDTO(String warningCode, String severity, String message) {
        this.warningCode = warningCode;
        this.severity = severity;
        this.message = message;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getAssessmentId() { return assessmentId; }
    public void setAssessmentId(UUID assessmentId) { this.assessmentId = assessmentId; }

    public String getWarningCode() { return warningCode; }
    public void setWarningCode(String warningCode) { this.warningCode = warningCode; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
