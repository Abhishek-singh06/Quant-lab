package com.quantlab.news.entity;

import com.quantlab.marketdata.model.ErrorCategory;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "news_processing_errors",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_npe_run_id", columnList = "run_id"),
        @Index(name = "idx_npe_created_at", columnList = "created_at")
    }
)
public class NewsProcessingError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", length = 64)
    private String runId;

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(name = "article_url", length = 1024)
    private String articleUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_category", nullable = false, length = 32)
    private ErrorCategory errorCategory;

    @Column(nullable = false, length = 1024)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public NewsProcessingError() {}

    public NewsProcessingError(String runId, String provider, String articleUrl,
                               ErrorCategory errorCategory, String reason, String payload) {
        this.runId = runId;
        this.provider = provider;
        this.articleUrl = articleUrl;
        this.errorCategory = errorCategory;
        this.reason = reason;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getArticleUrl() { return articleUrl; }
    public void setArticleUrl(String articleUrl) { this.articleUrl = articleUrl; }
    public ErrorCategory getErrorCategory() { return errorCategory; }
    public void setErrorCategory(ErrorCategory errorCategory) { this.errorCategory = errorCategory; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
