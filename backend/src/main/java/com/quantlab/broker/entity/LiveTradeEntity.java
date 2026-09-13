package com.quantlab.broker.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_trades")
public class LiveTradeEntity {

    @Id
    private UUID id;

    @Column(name = "live_order_id", nullable = false)
    private UUID liveOrderId;

    @Column(name = "broker_trade_id", nullable = false, length = 64)
    private String brokerTradeId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false, length = 10)
    private String side;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "execution_price", nullable = false)
    private double executionPrice;

    @Column(nullable = false)
    private double brokerage = 0.0;

    @Column(nullable = false)
    private double stt = 0.0;

    @Column(name = "exchange_charges", nullable = false)
    private double exchangeCharges = 0.0;

    @Column(nullable = false)
    private double gst = 0.0;

    @Column(name = "stamp_duty", nullable = false)
    private double stampDuty = 0.0;

    @Column(name = "total_fees", nullable = false)
    private double totalFees = 0.0;

    @Column(name = "execution_timestamp", nullable = false)
    private Instant executionTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getLiveOrderId() { return liveOrderId; }
    public void setLiveOrderId(UUID liveOrderId) { this.liveOrderId = liveOrderId; }

    public String getBrokerTradeId() { return brokerTradeId; }
    public void setBrokerTradeId(String brokerTradeId) { this.brokerTradeId = brokerTradeId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getExecutionPrice() { return executionPrice; }
    public void setExecutionPrice(double executionPrice) { this.executionPrice = executionPrice; }

    public double getBrokerage() { return brokerage; }
    public void setBrokerage(double brokerage) { this.brokerage = brokerage; }

    public double getStt() { return stt; }
    public void setStt(double stt) { this.stt = stt; }

    public double getExchangeCharges() { return exchangeCharges; }
    public void setExchangeCharges(double exchangeCharges) { this.exchangeCharges = exchangeCharges; }

    public double getGst() { return gst; }
    public void setGst(double gst) { this.gst = gst; }

    public double getStampDuty() { return stampDuty; }
    public void setStampDuty(double stampDuty) { this.stampDuty = stampDuty; }

    public double getTotalFees() { return totalFees; }
    public void setTotalFees(double totalFees) { this.totalFees = totalFees; }

    public Instant getExecutionTimestamp() { return executionTimestamp; }
    public void setExecutionTimestamp(Instant executionTimestamp) { this.executionTimestamp = executionTimestamp; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
