package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_orders")
public class BacktestOrderEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 10)
    private String side;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "requested_price", nullable = false)
    private Double requestedPrice;

    @Column(name = "executed_price")
    private Double executedPrice;

    @Column(name = "signal_timestamp", nullable = false)
    private OffsetDateTime signalTimestamp;

    @Column(name = "order_submitted_timestamp", nullable = false)
    private OffsetDateTime orderSubmittedTimestamp;

    @Column(name = "order_executed_timestamp")
    private OffsetDateTime orderExecutedTimestamp;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "slippage_bps")
    private Double slippageBps;

    @Column(name = "slippage_amount")
    private Double slippageAmount;

    @Column(name = "fees_amount")
    private Double feesAmount;

    @Column(name = "trade_ref_id")
    private UUID tradeRefId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BacktestOrderEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }
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
    public OffsetDateTime getSignalTimestamp() { return signalTimestamp; }
    public void setSignalTimestamp(OffsetDateTime signalTimestamp) { this.signalTimestamp = signalTimestamp; }
    public OffsetDateTime getOrderSubmittedTimestamp() { return orderSubmittedTimestamp; }
    public void setOrderSubmittedTimestamp(OffsetDateTime orderSubmittedTimestamp) { this.orderSubmittedTimestamp = orderSubmittedTimestamp; }
    public OffsetDateTime getOrderExecutedTimestamp() { return orderExecutedTimestamp; }
    public void setOrderExecutedTimestamp(OffsetDateTime orderExecutedTimestamp) { this.orderExecutedTimestamp = orderExecutedTimestamp; }
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
    public UUID getTradeRefId() { return tradeRefId; }
    public void setTradeRefId(UUID tradeRefId) { this.tradeRefId = tradeRefId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
