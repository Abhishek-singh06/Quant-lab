package com.quantlab.features.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "feature_calculation_runs", schema = "market_data")
public class FeatureCalculationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true, length = 64)
    private String runId;

    @Column(name = "instruments_requested", nullable = false)
    private int instrumentsRequested;

    @Column(name = "instruments_processed", nullable = false)
    private int instrumentsProcessed;

    @Column(name = "features_calculated", nullable = false)
    private long featuresCalculated;

    @Column(name = "features_failed", nullable = false)
    private int featuresFailed;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "RUNNING";

    @Column(name = "feature_set", nullable = false, length = 64)
    private String featureSet = "ALL_TECHNICAL";

    @Column(name = "timeframe", nullable = false, length = 16)
    private String timeframe = "1D";

    @Column(name = "from_date")
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public FeatureCalculationRun() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public int getInstrumentsRequested() { return instrumentsRequested; }
    public void setInstrumentsRequested(int instrumentsRequested) { this.instrumentsRequested = instrumentsRequested; }
    public int getInstrumentsProcessed() { return instrumentsProcessed; }
    public void setInstrumentsProcessed(int instrumentsProcessed) { this.instrumentsProcessed = instrumentsProcessed; }
    public long getFeaturesCalculated() { return featuresCalculated; }
    public void setFeaturesCalculated(long featuresCalculated) { this.featuresCalculated = featuresCalculated; }
    public int getFeaturesFailed() { return featuresFailed; }
    public void setFeaturesFailed(int featuresFailed) { this.featuresFailed = featuresFailed; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getFeatureSet() { return featureSet; }
    public void setFeatureSet(String featureSet) { this.featureSet = featureSet; }
    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
