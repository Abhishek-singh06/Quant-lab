package com.quantlab.signal.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signal_generation_runs", schema = "market_data")
public class SignalGenerationRunEntity {

    @Id
    private UUID id;

    @Column(name = "run_timestamp", nullable = false)
    private Instant runTimestamp;

    @Column(name = "as_of_timestamp", nullable = false)
    private Instant asOfTimestamp;

    @Column(name = "instrument_count", nullable = false)
    private Integer instrumentCount = 0;

    @Column(name = "signal_count", nullable = false)
    private Integer signalCount = 0;

    @Column(name = "configuration_version", nullable = false, length = 50)
    private String configurationVersion;

    @Column(name = "signal_version", nullable = false, length = 50)
    private String signalVersion;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "regime_version", length = 50)
    private String regimeVersion;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "duration_ms")
    private Long durationMs = 0L;

    @Column(name = "error_summary", columnDefinition = "TEXT")
    private String errorSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SignalGenerationRunEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Instant getRunTimestamp() { return runTimestamp; }
    public void setRunTimestamp(Instant runTimestamp) { this.runTimestamp = runTimestamp; }

    public Instant getAsOfTimestamp() { return asOfTimestamp; }
    public void setAsOfTimestamp(Instant asOfTimestamp) { this.asOfTimestamp = asOfTimestamp; }

    public Integer getInstrumentCount() { return instrumentCount; }
    public void setInstrumentCount(Integer instrumentCount) { this.instrumentCount = instrumentCount; }

    public Integer getSignalCount() { return signalCount; }
    public void setSignalCount(Integer signalCount) { this.signalCount = signalCount; }

    public String getConfigurationVersion() { return configurationVersion; }
    public void setConfigurationVersion(String configurationVersion) { this.configurationVersion = configurationVersion; }

    public String getSignalVersion() { return signalVersion; }
    public void setSignalVersion(String signalVersion) { this.signalVersion = signalVersion; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getRegimeVersion() { return regimeVersion; }
    public void setRegimeVersion(String regimeVersion) { this.regimeVersion = regimeVersion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getErrorSummary() { return errorSummary; }
    public void setErrorSummary(String errorSummary) { this.errorSummary = errorSummary; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
