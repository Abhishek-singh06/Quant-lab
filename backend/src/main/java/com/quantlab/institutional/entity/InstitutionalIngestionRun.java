package com.quantlab.institutional.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "institutional_ingestion_runs",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_iir_provider", columnList = "provider"),
        @Index(name = "idx_iir_start_time", columnList = "start_time"),
        @Index(name = "idx_iir_status", columnList = "status")
    }
)
public class InstitutionalIngestionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_uuid", nullable = false, unique = true, length = 64)
    private String runUuid;

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(nullable = false, length = 32)
    private String status; // SUCCESS, PARTIAL_SUCCESS, FAILED, RUNNING

    @Column(name = "records_received")
    private int recordsReceived;

    @Column(name = "records_inserted")
    private int recordsInserted;

    @Column(name = "duplicates_count")
    private int duplicatesCount;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public InstitutionalIngestionRun() {}

    public InstitutionalIngestionRun(String runUuid, String provider, Instant startTime, String status) {
        this.runUuid = runUuid;
        this.provider = provider;
        this.startTime = startTime;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRunUuid() { return runUuid; }
    public void setRunUuid(String runUuid) { this.runUuid = runUuid; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getRecordsReceived() { return recordsReceived; }
    public void setRecordsReceived(int recordsReceived) { this.recordsReceived = recordsReceived; }
    public int getRecordsInserted() { return recordsInserted; }
    public void setRecordsInserted(int recordsInserted) { this.recordsInserted = recordsInserted; }
    public int getDuplicatesCount() { return duplicatesCount; }
    public void setDuplicatesCount(int duplicatesCount) { this.duplicatesCount = duplicatesCount; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
