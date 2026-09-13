package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_equity_curve")
public class BacktestEquityCurveEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false)
    private UUID runId;

    @Column(name = "point_date", nullable = false)
    private LocalDate pointDate;

    @Column(name = "strategy_equity", nullable = false)
    private Double strategyEquity;

    @Column(name = "strategy_return_pct", nullable = false)
    private Double strategyReturnPct;

    @Column(name = "strategy_drawdown_pct", nullable = false)
    private Double strategyDrawdownPct;

    @Column(name = "buy_and_hold_equity", nullable = false)
    private Double buyAndHoldEquity;

    @Column(name = "buy_and_hold_return_pct", nullable = false)
    private Double buyAndHoldReturnPct;

    @Column(name = "benchmark_equity", nullable = false)
    private Double benchmarkEquity;

    @Column(name = "benchmark_return_pct", nullable = false)
    private Double benchmarkReturnPct;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BacktestEquityCurveEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }
    public LocalDate getPointDate() { return pointDate; }
    public void setPointDate(LocalDate pointDate) { this.pointDate = pointDate; }
    public Double getStrategyEquity() { return strategyEquity; }
    public void setStrategyEquity(Double strategyEquity) { this.strategyEquity = strategyEquity; }
    public Double getStrategyReturnPct() { return strategyReturnPct; }
    public void setStrategyReturnPct(Double strategyReturnPct) { this.strategyReturnPct = strategyReturnPct; }
    public Double getStrategyDrawdownPct() { return strategyDrawdownPct; }
    public void setStrategyDrawdownPct(Double strategyDrawdownPct) { this.strategyDrawdownPct = strategyDrawdownPct; }
    public Double getBuyAndHoldEquity() { return buyAndHoldEquity; }
    public void setBuyAndHoldEquity(Double buyAndHoldEquity) { this.buyAndHoldEquity = buyAndHoldEquity; }
    public Double getBuyAndHoldReturnPct() { return buyAndHoldReturnPct; }
    public void setBuyAndHoldReturnPct(Double buyAndHoldReturnPct) { this.buyAndHoldReturnPct = buyAndHoldReturnPct; }
    public Double getBenchmarkEquity() { return benchmarkEquity; }
    public void setBenchmarkEquity(Double benchmarkEquity) { this.benchmarkEquity = benchmarkEquity; }
    public Double getBenchmarkReturnPct() { return benchmarkReturnPct; }
    public void setBenchmarkReturnPct(Double benchmarkReturnPct) { this.benchmarkReturnPct = benchmarkReturnPct; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
