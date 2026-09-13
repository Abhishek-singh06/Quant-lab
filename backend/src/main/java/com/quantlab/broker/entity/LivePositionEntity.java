package com.quantlab.broker.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "live_positions")
public class LivePositionEntity {

    @Id
    private UUID id;

    @Column(name = "broker_account_id", nullable = false)
    private UUID brokerAccountId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(name = "product_type", nullable = false, length = 20)
    private String productType = "CASH";

    @Column(nullable = false)
    private int quantity;

    @Column(name = "average_price", nullable = false)
    private double averagePrice;

    @Column(name = "current_market_price", nullable = false)
    private double currentMarketPrice;

    @Column(name = "market_value", nullable = false)
    private double marketValue;

    @Column(name = "unrealized_pnl", nullable = false)
    private double unrealizedPnl;

    @Column(name = "realized_pnl", nullable = false)
    private double realizedPnl = 0.0;

    @Column(name = "stop_price")
    private Double stopPrice;

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBrokerAccountId() { return brokerAccountId; }
    public void setBrokerAccountId(UUID brokerAccountId) { this.brokerAccountId = brokerAccountId; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getAveragePrice() { return averagePrice; }
    public void setAveragePrice(double averagePrice) { this.averagePrice = averagePrice; }

    public double getCurrentMarketPrice() { return currentMarketPrice; }
    public void setCurrentMarketPrice(double currentMarketPrice) { this.currentMarketPrice = currentMarketPrice; }

    public double getMarketValue() { return marketValue; }
    public void setMarketValue(double marketValue) { this.marketValue = marketValue; }

    public double getUnrealizedPnl() { return unrealizedPnl; }
    public void setUnrealizedPnl(double unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }

    public double getRealizedPnl() { return realizedPnl; }
    public void setRealizedPnl(double realizedPnl) { this.realizedPnl = realizedPnl; }

    public Double getStopPrice() { return stopPrice; }
    public void setStopPrice(Double stopPrice) { this.stopPrice = stopPrice; }

    public Instant getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(Instant lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}
