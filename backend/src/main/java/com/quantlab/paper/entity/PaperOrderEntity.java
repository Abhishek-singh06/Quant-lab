package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_orders")
public class PaperOrderEntity {

    @Id
    private UUID id;

    @Column(name = "decision_id")
    private UUID decisionId;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 10)
    private String side;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType = "MARKET";

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "requested_price", nullable = false)
    private Double requestedPrice;

    @Column(name = "executed_price")
    private Double executedPrice;

    @Column(name = "signal_timestamp", nullable = false)
    private Instant signalTimestamp;

    @Column(name = "order_submitted_timestamp", nullable = false)
    private Instant orderSubmittedTimestamp;

    @Column(name = "order_executed_timestamp")
    private Instant orderExecutedTimestamp;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "slippage_bps")
    private Double slippageBps = 0.0;

    @Column(name = "slippage_amount")
    private Double slippageAmount = 0.0;

    @Column(name = "fees_amount")
    private Double feesAmount = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getDecisionId() { return decisionId; }
    public void setDecisionId(UUID decisionId) { this.decisionId = decisionId; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getRequestedPrice() { return requestedPrice; }
    public void setRequestedPrice(Double requestedPrice) { this.requestedPrice = requestedPrice; }

    public Double getExecutedPrice() { return executedPrice; }
    public void setExecutedPrice(Double executedPrice) { this.executedPrice = executedPrice; }

    public Instant getSignalTimestamp() { return signalTimestamp; }
    public void setSignalTimestamp(Instant signalTimestamp) { this.signalTimestamp = signalTimestamp; }

    public Instant getOrderSubmittedTimestamp() { return orderSubmittedTimestamp; }
    public void setOrderSubmittedTimestamp(Instant orderSubmittedTimestamp) { this.orderSubmittedTimestamp = orderSubmittedTimestamp; }

    public Instant getOrderExecutedTimestamp() { return orderExecutedTimestamp; }
    public void setOrderExecutedTimestamp(Instant orderExecutedTimestamp) { this.orderExecutedTimestamp = orderExecutedTimestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Double getSlippageBps() { return slippageBps; }
    public void setSlippageBps(Double slippageBps) { this.slippageBps = slippageBps; }

    public Double getSlippageAmount() { return slippageAmount; }
    public void setSlippageAmount(Double slippageAmount) { this.slippageAmount = slippageAmount; }

    public Double getFeesAmount() { return feesAmount; }
    public void setFeesAmount(Double feesAmount) { this.feesAmount = feesAmount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
