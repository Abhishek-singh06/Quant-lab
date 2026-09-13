package com.quantlab.marketdata.entity;

import com.quantlab.marketdata.model.ErrorCategory;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "market_data_errors",
    schema = "market_data",
    indexes = {
        @Index(name = "idx_errors_run_id", columnList = "run_id"),
        @Index(name = "idx_errors_symbol", columnList = "symbol"),
        @Index(name = "idx_errors_category", columnList = "error_category"),
        @Index(name = "idx_errors_created_at", columnList = "created_at")
    }
)
public class MarketDataError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", length = 64)
    private String runId;

    @Column(nullable = false, length = 64)
    private String provider;

    @Column(length = 32)
    private String symbol;

    @Column(name = "source_timestamp")
    private Instant sourceTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_category", nullable = false, length = 32)
    private ErrorCategory errorCategory;

    @Column(nullable = false, length = 1024)
    private String reason;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public MarketDataError() {}

    public MarketDataError(String runId, String provider, String symbol, Instant sourceTimestamp,
                           ErrorCategory errorCategory, String reason, String rawPayload) {
        this.runId = runId;
        this.provider = provider;
        this.symbol = symbol;
        this.sourceTimestamp = sourceTimestamp;
        this.errorCategory = errorCategory;
        this.reason = reason;
        this.rawPayload = rawPayload;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Instant getSourceTimestamp() { return sourceTimestamp; }
    public void setSourceTimestamp(Instant sourceTimestamp) { this.sourceTimestamp = sourceTimestamp; }
    public ErrorCategory getErrorCategory() { return errorCategory; }
    public void setErrorCategory(ErrorCategory errorCategory) { this.errorCategory = errorCategory; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRawPayload() { return rawPayload; }
    public void setRawPayload(String rawPayload) { this.rawPayload = rawPayload; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
