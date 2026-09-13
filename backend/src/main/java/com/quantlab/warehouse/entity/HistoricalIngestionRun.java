package com.quantlab.warehouse.entity;

import com.quantlab.marketdata.model.IngestionRunStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "historical_ingestion_runs",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_hist_run_id", columnList = "run_id", unique = true),
        @Index(name = "idx_hist_run_dates", columnList = "from_date, to_date")
    }
)
public class HistoricalIngestionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true, length = 64)
    private String runId;

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date", nullable = false)
    private LocalDate toDate;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IngestionRunStatus status = IngestionRunStatus.RUNNING;

    @Column(name = "symbols_count", nullable = false)
    private int symbolsCount = 0;

    @Column(name = "records_inserted", nullable = false)
    private int recordsInserted = 0;

    @Column(name = "records_updated", nullable = false)
    private int recordsUpdated = 0;

    @Column(name = "duplicates_count", nullable = false)
    private int duplicatesCount = 0;

    @Column(name = "gaps_count", nullable = false)
    private int gapsCount = 0;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "checkpoint_symbol", length = 32)
    private String checkpointSymbol;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public HistoricalIngestionRun() {}

    public HistoricalIngestionRun(String runId, String provider, LocalDate fromDate, LocalDate toDate, Instant startTime) {
        this.runId = runId;
        this.provider = provider;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.startTime = startTime;
        this.status = IngestionRunStatus.RUNNING;
        this.createdAt = Instant.now();
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
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public IngestionRunStatus getStatus() { return status; }
    public void setStatus(IngestionRunStatus status) { this.status = status; }
    public int getSymbolsCount() { return symbolsCount; }
    public void setSymbolsCount(int symbolsCount) { this.symbolsCount = symbolsCount; }
    public int getRecordsInserted() { return recordsInserted; }
    public void setRecordsInserted(int recordsInserted) { this.recordsInserted = recordsInserted; }
    public int getRecordsUpdated() { return recordsUpdated; }
    public void setRecordsUpdated(int recordsUpdated) { this.recordsUpdated = recordsUpdated; }
    public int getDuplicatesCount() { return duplicatesCount; }
    public void setDuplicatesCount(int duplicatesCount) { this.duplicatesCount = duplicatesCount; }
    public int getGapsCount() { return gapsCount; }
    public void setGapsCount(int gapsCount) { this.gapsCount = gapsCount; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getCheckpointSymbol() { return checkpointSymbol; }
    public void setCheckpointSymbol(String checkpointSymbol) { this.checkpointSymbol = checkpointSymbol; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
