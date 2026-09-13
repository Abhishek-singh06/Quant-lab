package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_fills")
public class PaperFillEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 10)
    private String side;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "requested_price", nullable = false)
    private Double requestedPrice;

    @Column(name = "fill_price", nullable = false)
    private Double fillPrice;

    @Column(name = "slippage_bps", nullable = false)
    private Double slippageBps = 0.0;

    @Column(name = "slippage_amount", nullable = false)
    private Double slippageAmount = 0.0;

    @Column(nullable = false)
    private Double brokerage = 0.0;

    @Column(nullable = false)
    private Double stt = 0.0;

    @Column(name = "exchange_charges", nullable = false)
    private Double exchangeCharges = 0.0;

    @Column(nullable = false)
    private Double gst = 0.0;

    @Column(name = "stamp_duty", nullable = false)
    private Double stampDuty = 0.0;

    @Column(name = "total_fees", nullable = false)
    private Double totalFees = 0.0;

    @Column(name = "execution_timestamp", nullable = false)
    private Instant executionTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getRequestedPrice() { return requestedPrice; }
    public void setRequestedPrice(Double requestedPrice) { this.requestedPrice = requestedPrice; }

    public Double getFillPrice() { return fillPrice; }
    public void setFillPrice(Double fillPrice) { this.fillPrice = fillPrice; }

    public Double getSlippageBps() { return slippageBps; }
    public void setSlippageBps(Double slippageBps) { this.slippageBps = slippageBps; }

    public Double getSlippageAmount() { return slippageAmount; }
    public void setSlippageAmount(Double slippageAmount) { this.slippageAmount = slippageAmount; }

    public Double getBrokerage() { return brokerage; }
    public void setBrokerage(Double brokerage) { this.brokerage = brokerage; }

    public Double getStt() { return stt; }
    public void setStt(Double stt) { this.stt = stt; }

    public Double getExchangeCharges() { return exchangeCharges; }
    public void setExchangeCharges(Double exchangeCharges) { this.exchangeCharges = exchangeCharges; }

    public Double getGst() { return gst; }
    public void setGst(Double gst) { this.gst = gst; }

    public Double getStampDuty() { return stampDuty; }
    public void setStampDuty(Double stampDuty) { this.stampDuty = stampDuty; }

    public Double getTotalFees() { return totalFees; }
    public void setTotalFees(Double totalFees) { this.totalFees = totalFees; }

    public Instant getExecutionTimestamp() { return executionTimestamp; }
    public void setExecutionTimestamp(Instant executionTimestamp) { this.executionTimestamp = executionTimestamp; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
