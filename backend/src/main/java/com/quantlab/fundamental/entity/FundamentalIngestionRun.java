package com.quantlab.fundamental.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "fundamental_ingestion_runs",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_fir_provider", columnList = "provider"),
        @Index(name = "idx_fir_start_time", columnList = "start_time"),
        @Index(name = "idx_fir_status", columnList = "status")
    }
)
public class FundamentalIngestionRun {

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

    @Column(name = "companies_requested")
    private int companiesRequested;

    @Column(name = "filings_received")
    private int filingsReceived;

    @Column(name = "filings_inserted")
    private int filingsInserted;

    @Column(name = "duplicates_count")
    private int duplicatesCount;

    @Column(name = "rejected_count")
    private int rejectedCount;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public FundamentalIngestionRun() {}

    public FundamentalIngestionRun(String runUuid, String provider, Instant startTime, String status) {
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
    public int getCompaniesRequested() { return companiesRequested; }
    public void setCompaniesRequested(int companiesRequested) { this.companiesRequested = companiesRequested; }
    public int getFilingsReceived() { return filingsReceived; }
    public void setFilingsReceived(int filingsReceived) { this.filingsReceived = filingsReceived; }
    public int getFilingsInserted() { return filingsInserted; }
    public void setFilingsInserted(int filingsInserted) { this.filingsInserted = filingsInserted; }
    public int getDuplicatesCount() { return duplicatesCount; }
    public void setDuplicatesCount(int duplicatesCount) { this.duplicatesCount = duplicatesCount; }
    public int getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(int rejectedCount) { this.rejectedCount = rejectedCount; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
