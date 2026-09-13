package com.quantlab.broker.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_orders")
public class LiveOrderEntity {

    @Id
    private UUID id;

    @Column(name = "broker_account_id", nullable = false)
    private UUID brokerAccountId;

    @Column(name = "broker_order_id", length = 64)
    private String brokerOrderId;

    @Column(name = "order_intent_id", nullable = false, unique = true)
    private UUID orderIntentId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 16)
    private String exchange = "NSE";

    @Column(nullable = false, length = 10)
    private String side;

    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType = "LIMIT";

    @Column(name = "product_type", nullable = false, length = 20)
    private String productType = "CASH";

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double price;

    @Column(name = "trigger_price")
    private Double triggerPrice;

    @Column(name = "stop_loss_price")
    private Double stopLossPrice;

    @Column(name = "target_price")
    private Double targetPrice;

    @Column(nullable = false, length = 32)
    private String status = "AWAITING_CONFIRMATION";

    @Column(name = "filled_quantity", nullable = false)
    private int filledQuantity = 0;

    @Column(name = "average_fill_price")
    private Double averageFillPrice;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "signal_id")
    private UUID signalId;

    @Column(name = "signal_score")
    private Double signalScore;

    @Column(name = "signal_confidence")
    private Double signalConfidence;

    @Column(name = "expected_return")
    private Double expectedReturn;

    @Column(name = "risk_assessment_id")
    private UUID riskAssessmentId;

    @Column(name = "risk_level", length = 32)
    private String riskLevel;

    @Column(name = "suggested_allocation")
    private Double suggestedAllocation;

    @Column(name = "is_manually_confirmed", nullable = false)
    private boolean isManuallyConfirmed = false;

    @Column(name = "confirmed_by_user", length = 64)
    private String confirmedByUser;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "order_submitted_at")
    private Instant orderSubmittedAt;

    @Column(name = "order_executed_at")
    private Instant orderExecutedAt;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBrokerAccountId() { return brokerAccountId; }
    public void setBrokerAccountId(UUID brokerAccountId) { this.brokerAccountId = brokerAccountId; }

    public String getBrokerOrderId() { return brokerOrderId; }
    public void setBrokerOrderId(String brokerOrderId) { this.brokerOrderId = brokerOrderId; }

    public UUID getOrderIntentId() { return orderIntentId; }
    public void setOrderIntentId(UUID orderIntentId) { this.orderIntentId = orderIntentId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Double getTriggerPrice() { return triggerPrice; }
    public void setTriggerPrice(Double triggerPrice) { this.triggerPrice = triggerPrice; }

    public Double getStopLossPrice() { return stopLossPrice; }
    public void setStopLossPrice(Double stopLossPrice) { this.stopLossPrice = stopLossPrice; }

    public Double getTargetPrice() { return targetPrice; }
    public void setTargetPrice(Double targetPrice) { this.targetPrice = targetPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(int filledQuantity) { this.filledQuantity = filledQuantity; }

    public Double getAverageFillPrice() { return averageFillPrice; }
    public void setAverageFillPrice(Double averageFillPrice) { this.averageFillPrice = averageFillPrice; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public UUID getSignalId() { return signalId; }
    public void setSignalId(UUID signalId) { this.signalId = signalId; }

    public Double getSignalScore() { return signalScore; }
    public void setSignalScore(Double signalScore) { this.signalScore = signalScore; }

    public Double getSignalConfidence() { return signalConfidence; }
    public void setSignalConfidence(Double signalConfidence) { this.signalConfidence = signalConfidence; }

    public Double getExpectedReturn() { return expectedReturn; }
    public void setExpectedReturn(Double expectedReturn) { this.expectedReturn = expectedReturn; }

    public UUID getRiskAssessmentId() { return riskAssessmentId; }
    public void setRiskAssessmentId(UUID riskAssessmentId) { this.riskAssessmentId = riskAssessmentId; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Double getSuggestedAllocation() { return suggestedAllocation; }
    public void setSuggestedAllocation(Double suggestedAllocation) { this.suggestedAllocation = suggestedAllocation; }

    public boolean isManuallyConfirmed() { return isManuallyConfirmed; }
    public void setManuallyConfirmed(boolean manuallyConfirmed) { isManuallyConfirmed = manuallyConfirmed; }

    public String getConfirmedByUser() { return confirmedByUser; }
    public void setConfirmedByUser(String confirmedByUser) { this.confirmedByUser = confirmedByUser; }

    public Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }

    public Instant getOrderSubmittedAt() { return orderSubmittedAt; }
    public void setOrderSubmittedAt(Instant orderSubmittedAt) { this.orderSubmittedAt = orderSubmittedAt; }

    public Instant getOrderExecutedAt() { return orderExecutedAt; }
    public void setOrderExecutedAt(Instant orderExecutedAt) { this.orderExecutedAt = orderExecutedAt; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
