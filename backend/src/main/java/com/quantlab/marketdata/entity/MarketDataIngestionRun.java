package com.quantlab.marketdata.entity;

import com.quantlab.marketdata.model.IngestionRunStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "market_data_ingestion_runs",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_ingestion_run_id", columnList = "run_id", unique = true),
        @Index(name = "idx_ingestion_provider", columnList = "provider"),
        @Index(name = "idx_ingestion_status", columnList = "status"),
        @Index(name = "idx_ingestion_start_time", columnList = "start_time")
    }
)
public class MarketDataIngestionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true, length = 64)
    private String runId;

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IngestionRunStatus status = IngestionRunStatus.RUNNING;

    @Column(name = "records_received", nullable = false)
    private int recordsReceived = 0;

    @Column(name = "records_accepted", nullable = false)
    private int recordsAccepted = 0;

    @Column(name = "records_rejected", nullable = false)
    private int recordsRejected = 0;

    @Column(name = "duplicates_count", nullable = false)
    private int duplicatesCount = 0;

    @Column(name = "missing_count", nullable = false)
    private int missingCount = 0;

    @Column(name = "stale_count", nullable = false)
    private int staleCount = 0;

    @Column(name = "error_count", nullable = false)
    private int errorCount = 0;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public MarketDataIngestionRun() {}

    public MarketDataIngestionRun(String runId, String provider, Instant startTime) {
        this.runId = runId;
        this.provider = provider;
        this.startTime = startTime;
        this.status = IngestionRunStatus.RUNNING;
    }

    public void complete(IngestionRunStatus finalStatus) {
        this.endTime = Instant.now();
        this.status = finalStatus;
        if (this.startTime != null) {
            this.durationMs = this.endTime.toEpochMilli() - this.startTime.toEpochMilli();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public IngestionRunStatus getStatus() { return status; }
    public void setStatus(IngestionRunStatus status) { this.status = status; }
    public int getRecordsReceived() { return recordsReceived; }
    public void setRecordsReceived(int recordsReceived) { this.recordsReceived = recordsReceived; }
    public int getRecordsAccepted() { return recordsAccepted; }
    public void setRecordsAccepted(int recordsAccepted) { this.recordsAccepted = recordsAccepted; }
    public int getRecordsRejected() { return recordsRejected; }
    public void setRecordsRejected(int recordsRejected) { this.recordsRejected = recordsRejected; }
    public int getDuplicatesCount() { return duplicatesCount; }
    public void setDuplicatesCount(int duplicatesCount) { this.duplicatesCount = duplicatesCount; }
    public int getMissingCount() { return missingCount; }
    public void setMissingCount(int missingCount) { this.missingCount = missingCount; }
    public int getStaleCount() { return staleCount; }
    public void setStaleCount(int staleCount) { this.staleCount = staleCount; }
    public int getErrorCount() { return errorCount; }
    public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
