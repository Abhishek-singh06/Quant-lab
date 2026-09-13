package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_signal_outcomes")
public class PaperSignalOutcomeEntity {

    @Id
    private UUID id;

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 32)
    private String horizon;

    @Column(name = "signal_timestamp", nullable = false)
    private Instant signalTimestamp;

    @Column(name = "evaluation_timestamp", nullable = false)
    private Instant evaluationTimestamp;

    @Column(name = "expected_direction", nullable = false, length = 20)
    private String expectedDirection;

    @Column(name = "realized_direction", nullable = false, length = 20)
    private String realizedDirection;

    @Column(name = "expected_return", nullable = false)
    private Double expectedReturn;

    @Column(name = "realized_return", nullable = false)
    private Double realizedReturn;

    @Column(name = "outcome_status", nullable = false, length = 32)
    private String outcomeStatus;

    @Column(columnDefinition = "TEXT")
    private String attribution;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDecisionId() { return decisionId; }
    public void setDecisionId(UUID decisionId) { this.decisionId = decisionId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }

    public Instant getSignalTimestamp() { return signalTimestamp; }
    public void setSignalTimestamp(Instant signalTimestamp) { this.signalTimestamp = signalTimestamp; }

    public Instant getEvaluationTimestamp() { return evaluationTimestamp; }
    public void setEvaluationTimestamp(Instant evaluationTimestamp) { this.evaluationTimestamp = evaluationTimestamp; }

    public String getExpectedDirection() { return expectedDirection; }
    public void setExpectedDirection(String expectedDirection) { this.expectedDirection = expectedDirection; }

    public String getRealizedDirection() { return realizedDirection; }
    public void setRealizedDirection(String realizedDirection) { this.realizedDirection = realizedDirection; }

    public Double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(Double expectedReturn) { this.expectedReturn = expectedReturn; }

    public Double getRealizedReturn() { return realizedReturn; }
    public void setRealizedReturn(Double realizedReturn) { this.realizedReturn = realizedReturn; }

    public String getOutcomeStatus() { return outcomeStatus; }
    public void setOutcomeStatus(String outcomeStatus) { this.outcomeStatus = outcomeStatus; }

    public String getAttribution() { return attribution; }
    public void setAttribution(String attribution) { this.attribution = attribution; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
