package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_positions")
public class BacktestPositionEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(nullable = false, length = 32)
    private String symbol;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "average_entry_price", nullable = false)
    private Double averageEntryPrice;

    @Column(name = "current_market_price", nullable = false)
    private Double currentMarketPrice;

    @Column(name = "cost_basis", nullable = false)
    private Double costBasis;

    @Column(name = "market_value", nullable = false)
    private Double marketValue;

    @Column(name = "unrealized_pnl", nullable = false)
    private Double unrealizedPnl;

    @Column(name = "unrealized_return_pct", nullable = false)
    private Double unrealizedReturnPct;

    @Column(name = "weight_in_portfolio", nullable = false)
    private Double weightInPortfolio;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BacktestPositionEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getAverageEntryPrice() { return averageEntryPrice; }
    public void setAverageEntryPrice(Double averageEntryPrice) { this.averageEntryPrice = averageEntryPrice; }
    public Double getCurrentMarketPrice() { return currentMarketPrice; }
    public void setCurrentMarketPrice(Double currentMarketPrice) { this.currentMarketPrice = currentMarketPrice; }
    public Double getCostBasis() { return costBasis; }
    public void setCostBasis(Double costBasis) { this.costBasis = costBasis; }
    public Double getMarketValue() { return marketValue; }
    public void setMarketValue(Double marketValue) { this.marketValue = marketValue; }
    public Double getUnrealizedPnl() { return unrealizedPnl; }
    public void setUnrealizedPnl(Double unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; }
    public Double getUnrealizedReturnPct() { return unrealizedReturnPct; }
    public void setUnrealizedReturnPct(Double unrealizedReturnPct) { this.unrealizedReturnPct = unrealizedReturnPct; }
    public Double getWeightInPortfolio() { return weightInPortfolio; }
    public void setWeightInPortfolio(Double weightInPortfolio) { this.weightInPortfolio = weightInPortfolio; }
    public LocalDate getAsOfDate() { return asOfDate; }
    public void setAsOfDate(LocalDate asOfDate) { this.asOfDate = asOfDate; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
