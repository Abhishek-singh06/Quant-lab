package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_ledger")
public class PaperLedgerEntity {

    @Id
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "transaction_timestamp", nullable = false)
    private Instant transactionTimestamp;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(length = 32)
    private String symbol;

    @Column(nullable = false)
    private Double amount;

    @Column(name = "cash_balance_before", nullable = false)
    private Double cashBalanceBefore;

    @Column(name = "cash_balance_after", nullable = false)
    private Double cashBalanceAfter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public Instant getTransactionTimestamp() { return transactionTimestamp; }
    public void setTransactionTimestamp(Instant transactionTimestamp) { this.transactionTimestamp = transactionTimestamp; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public Double getCashBalanceBefore() { return cashBalanceBefore; }
    public void setCashBalanceBefore(Double cashBalanceBefore) { this.cashBalanceBefore = cashBalanceBefore; }

    public Double getCashBalanceAfter() { return cashBalanceAfter; }
    public void setCashBalanceAfter(Double cashBalanceAfter) { this.cashBalanceAfter = cashBalanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
