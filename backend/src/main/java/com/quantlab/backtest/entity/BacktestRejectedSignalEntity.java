package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_rejected_signals")
public class BacktestRejectedSignalEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "signal_timestamp", nullable = false)
    private OffsetDateTime signalTimestamp;

    @Column(name = "signal_type", nullable = false, length = 20)
    private String signalType;

    @Column(name = "signal_strength", nullable = false)
    private Double signalStrength;

    @Column(name = "rejection_reason", nullable = false, length = 64)
    private String rejectionReason;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BacktestRejectedSignalEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public OffsetDateTime getSignalTimestamp() { return signalTimestamp; }
    public void setSignalTimestamp(OffsetDateTime signalTimestamp) { this.signalTimestamp = signalTimestamp; }
    public String getSignalType() { return signalType; }
    public void setSignalType(String signalType) { this.signalType = signalType; }
    public Double getSignalStrength() { return signalStrength; }
    public void setSignalStrength(Double signalStrength) { this.signalStrength = signalStrength; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
