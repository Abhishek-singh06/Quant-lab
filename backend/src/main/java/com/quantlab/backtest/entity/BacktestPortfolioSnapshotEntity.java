package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_portfolio_snapshots")
public class BacktestPortfolioSnapshotEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "cash_balance", nullable = false)
    private Double cashBalance;

    @Column(name = "positions_market_value", nullable = false)
    private Double positionsMarketValue;

    @Column(name = "total_equity", nullable = false)
    private Double totalEquity;

    @Column(name = "gross_exposure", nullable = false)
    private Double grossExposure;

    @Column(name = "net_exposure", nullable = false)
    private Double netExposure;

    @Column(nullable = false)
    private Double leverage;

    @Column(name = "daily_pnl", nullable = false)
    private Double dailyPnl;

    @Column(name = "daily_return", nullable = false)
    private Double dailyReturn;

    @Column(name = "cumulative_return", nullable = false)
    private Double cumulativeReturn;

    @Column(name = "drawdown_pct", nullable = false)
    private Double drawdownPct;

    @Column(name = "open_positions_count", nullable = false)
    private Integer openPositionsCount;

    @Column(name = "trades_executed_today", nullable = false)
    private Integer tradesExecutedToday;

    @Column(name = "dividends_credited_today", nullable = false)
    private Double dividendsCreditedToday;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BacktestPortfolioSnapshotEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }
    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
    public Double getCashBalance() { return cashBalance; }
    public void setCashBalance(Double cashBalance) { this.cashBalance = cashBalance; }
    public Double getPositionsMarketValue() { return positionsMarketValue; }
    public void setPositionsMarketValue(Double positionsMarketValue) { this.positionsMarketValue = positionsMarketValue; }
    public Double getTotalEquity() { return totalEquity; }
    public void setTotalEquity(Double totalEquity) { this.totalEquity = totalEquity; }
    public Double getGrossExposure() { return grossExposure; }
    public void setGrossExposure(Double grossExposure) { this.grossExposure = grossExposure; }
    public Double getNetExposure() { return netExposure; }
    public void setNetExposure(Double netExposure) { this.netExposure = netExposure; }
    public Double getLeverage() { return leverage; }
    public void setLeverage(Double leverage) { this.leverage = leverage; }
    public Double getDailyPnl() { return dailyPnl; }
    public void setDailyPnl(Double dailyPnl) { this.dailyPnl = dailyPnl; }
    public Double getDailyReturn() { return dailyReturn; }
    public void setDailyReturn(Double dailyReturn) { this.dailyReturn = dailyReturn; }
    public Double getCumulativeReturn() { return cumulativeReturn; }
    public void setCumulativeReturn(Double cumulativeReturn) { this.cumulativeReturn = cumulativeReturn; }
    public Double getDrawdownPct() { return drawdownPct; }
    public void setDrawdownPct(Double drawdownPct) { this.drawdownPct = drawdownPct; }
    public Integer getOpenPositionsCount() { return openPositionsCount; }
    public void setOpenPositionsCount(Integer openPositionsCount) { this.openPositionsCount = openPositionsCount; }
    public Integer getTradesExecutedToday() { return tradesExecutedToday; }
    public void setTradesExecutedToday(Integer tradesExecutedToday) { this.tradesExecutedToday = tradesExecutedToday; }
    public Double getDividendsCreditedToday() { return dividendsCreditedToday; }
    public void setDividendsCreditedToday(Double dividendsCreditedToday) { this.dividendsCreditedToday = dividendsCreditedToday; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
