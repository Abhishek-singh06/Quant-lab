package com.quantlab.news.entity;

import com.quantlab.marketdata.model.IngestionRunStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "news_ingestion_runs",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_news_run_id", columnList = "run_id", unique = true),
        @Index(name = "idx_news_run_provider", columnList = "provider")
    }
)
public class NewsIngestionRun {

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

    @Column(name = "articles_received", nullable = false)
    private int articlesReceived = 0;

    @Column(name = "articles_inserted", nullable = false)
    private int articlesInserted = 0;

    @Column(name = "duplicates_count", nullable = false)
    private int duplicatesCount = 0;

    @Column(name = "events_extracted", nullable = false)
    private int eventsExtracted = 0;

    @Column(name = "entity_matches", nullable = false)
    private int entityMatches = 0;

    @Column(name = "failed_count", nullable = false)
    private int failedCount = 0;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public NewsIngestionRun() {}

    public NewsIngestionRun(String runId, String provider, Instant startTime) {
        this.runId = runId;
        this.provider = provider;
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
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public IngestionRunStatus getStatus() { return status; }
    public void setStatus(IngestionRunStatus status) { this.status = status; }
    public int getArticlesReceived() { return articlesReceived; }
    public void setArticlesReceived(int articlesReceived) { this.articlesReceived = articlesReceived; }
    public int getArticlesInserted() { return articlesInserted; }
    public void setArticlesInserted(int articlesInserted) { this.articlesInserted = articlesInserted; }
    public int getDuplicatesCount() { return duplicatesCount; }
    public void setDuplicatesCount(int duplicatesCount) { this.duplicatesCount = duplicatesCount; }
    public int getEventsExtracted() { return eventsExtracted; }
    public void setEventsExtracted(int eventsExtracted) { this.eventsExtracted = eventsExtracted; }
    public int getEntityMatches() { return entityMatches; }
    public void setEntityMatches(int entityMatches) { this.entityMatches = entityMatches; }
    public int getFailedCount() { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
