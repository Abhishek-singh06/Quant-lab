package com.quantlab.paper.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "paper_equity_curve")
public class PaperEquityCurveEntity {

    @Id
    private UUID id;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "snapshot_timestamp", nullable = false)
    private Instant snapshotTimestamp;

    @Column(name = "portfolio_value", nullable = false)
    private Double portfolioValue;

    @Column(name = "cash_balance", nullable = false)
    private Double cashBalance;

    @Column(name = "invested_value", nullable = false)
    private Double investedValue;

    @Column(name = "daily_return_pct", nullable = false)
    private Double dailyReturnPct = 0.0;

    @Column(name = "cumulative_return_pct", nullable = false)
    private Double cumulativeReturnPct = 0.0;

    @Column(name = "drawdown_pct", nullable = false)
    private Double drawdownPct = 0.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPortfolioId() { return portfolioId; }
    public void setPortfolioId(UUID portfolioId) { this.portfolioId = portfolioId; }

    public Instant getSnapshotTimestamp() { return snapshotTimestamp; }
    public void setSnapshotTimestamp(Instant snapshotTimestamp) { this.snapshotTimestamp = snapshotTimestamp; }

    public Double getPortfolioValue() { return portfolioValue; }
    public void setPortfolioValue(Double portfolioValue) { this.portfolioValue = portfolioValue; }

    public Double getCashBalance() { return cashBalance; }
    public void setCashBalance(Double cashBalance) { this.cashBalance = cashBalance; }

    public Double getInvestedValue() { return investedValue; }
    public void setInvestedValue(Double investedValue) { this.investedValue = investedValue; }

    public Double getDailyReturnPct() { return dailyReturnPct; }
    public void setDailyReturnPct(Double dailyReturnPct) { this.dailyReturnPct = dailyReturnPct; }

    public Double getCumulativeReturnPct() { return cumulativeReturnPct; }
    public void setCumulativeReturnPct(Double cumulativeReturnPct) { this.cumulativeReturnPct = cumulativeReturnPct; }

    public Double getDrawdownPct() { return drawdownPct; }
    public void setDrawdownPct(Double drawdownPct) { this.drawdownPct = drawdownPct; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
