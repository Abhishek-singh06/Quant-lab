package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_metrics")
public class BacktestMetricsEntity {

    @Id
    private UUID id;

    @Column(name = "run_id", nullable = false, unique = true)
    private UUID runId;

    @Column(name = "total_return_pct", nullable = false)
    private Double totalReturnPct;

    @Column(nullable = false)
    private Double cagr;

    @Column(name = "annualized_volatility", nullable = false)
    private Double annualizedVolatility;

    @Column(name = "sharpe_ratio", nullable = false)
    private Double sharpeRatio;

    @Column(name = "sortino_ratio", nullable = false)
    private Double sortinoRatio;

    @Column(name = "max_drawdown_pct", nullable = false)
    private Double maxDrawdownPct;

    @Column(name = "max_drawdown_duration_days", nullable = false)
    private Integer maxDrawdownDurationDays;

    @Column(name = "calmar_ratio", nullable = false)
    private Double calmarRatio;

    @Column(name = "win_rate_pct", nullable = false)
    private Double winRatePct;

    @Column(name = "profit_factor", nullable = false)
    private Double profitFactor;

    @Column(name = "average_trade_return_pct", nullable = false)
    private Double averageTradeReturnPct;

    @Column(name = "average_win_return_pct", nullable = false)
    private Double averageWinReturnPct;

    @Column(name = "average_loss_return_pct", nullable = false)
    private Double averageLossReturnPct;

    @Column(name = "win_loss_ratio", nullable = false)
    private Double winLossRatio;

    @Column(name = "total_trades_count", nullable = false)
    private Integer totalTradesCount;

    @Column(name = "winning_trades_count", nullable = false)
    private Integer winningTradesCount;

    @Column(name = "losing_trades_count", nullable = false)
    private Integer losingTradesCount;

    @Column(name = "annualized_turnover", nullable = false)
    private Double annualizedTurnover;

    @Column(name = "beta_to_benchmark")
    private Double betaToBenchmark;

    @Column(name = "alpha_to_benchmark")
    private Double alphaToBenchmark;

    @Column(name = "information_ratio")
    private Double informationRatio;

    @Column(name = "subperiod_metrics", columnDefinition = "JSONB")
    private String subperiodMetrics;

    @Column(name = "regime_breakdown_metrics", columnDefinition = "JSONB")
    private String regimeBreakdownMetrics;

    @Column(name = "sector_breakdown_metrics", columnDefinition = "JSONB")
    private String sectorBreakdownMetrics;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public BacktestMetricsEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRunId() { return runId; }
    public void setRunId(UUID runId) { this.runId = runId; }
    public Double getTotalReturnPct() { return totalReturnPct; }
    public void setTotalReturnPct(Double totalReturnPct) { this.totalReturnPct = totalReturnPct; }
    public Double getCagr() { return cagr; }
    public void setCagr(Double cagr) { this.cagr = cagr; }
    public Double getAnnualizedVolatility() { return annualizedVolatility; }
    public void setAnnualizedVolatility(Double annualizedVolatility) { this.annualizedVolatility = annualizedVolatility; }
    public Double getSharpeRatio() { return sharpeRatio; }
    public void setSharpeRatio(Double sharpeRatio) { this.sharpeRatio = sharpeRatio; }
    public Double getSortinoRatio() { return sortinoRatio; }
    public void setSortinoRatio(Double sortinoRatio) { this.sortinoRatio = sortinoRatio; }
    public Double getMaxDrawdownPct() { return maxDrawdownPct; }
    public void setMaxDrawdownPct(Double maxDrawdownPct) { this.maxDrawdownPct = maxDrawdownPct; }
    public Integer getMaxDrawdownDurationDays() { return maxDrawdownDurationDays; }
    public void setMaxDrawdownDurationDays(Integer maxDrawdownDurationDays) { this.maxDrawdownDurationDays = maxDrawdownDurationDays; }
    public Double getCalmarRatio() { return calmarRatio; }
    public void setCalmarRatio(Double calmarRatio) { this.calmarRatio = calmarRatio; }
    public Double getWinRatePct() { return winRatePct; }
    public void setWinRatePct(Double winRatePct) { this.winRatePct = winRatePct; }
    public Double getProfitFactor() { return profitFactor; }
    public void setProfitFactor(Double profitFactor) { this.profitFactor = profitFactor; }
    public Double getAverageTradeReturnPct() { return averageTradeReturnPct; }
    public void setAverageTradeReturnPct(Double averageTradeReturnPct) { this.averageTradeReturnPct = averageTradeReturnPct; }
    public Double getAverageWinReturnPct() { return averageWinReturnPct; }
    public void setAverageWinReturnPct(Double averageWinReturnPct) { this.averageWinReturnPct = averageWinReturnPct; }
    public Double getAverageLossReturnPct() { return averageLossReturnPct; }
    public void setAverageLossReturnPct(Double averageLossReturnPct) { this.averageLossReturnPct = averageLossReturnPct; }
    public Double getWinLossRatio() { return winLossRatio; }
    public void setWinLossRatio(Double winLossRatio) { this.winLossRatio = winLossRatio; }
    public Integer getTotalTradesCount() { return totalTradesCount; }
    public void setTotalTradesCount(Integer totalTradesCount) { this.totalTradesCount = totalTradesCount; }
    public Integer getWinningTradesCount() { return winningTradesCount; }
    public void setWinningTradesCount(Integer winningTradesCount) { this.winningTradesCount = winningTradesCount; }
    public Integer getLosingTradesCount() { return losingTradesCount; }
    public void setLosingTradesCount(Integer losingTradesCount) { this.losingTradesCount = losingTradesCount; }
    public Double getAnnualizedTurnover() { return annualizedTurnover; }
    public void setAnnualizedTurnover(Double annualizedTurnover) { this.annualizedTurnover = annualizedTurnover; }
    public Double getBetaToBenchmark() { return betaToBenchmark; }
    public void setBetaToBenchmark(Double betaToBenchmark) { this.betaToBenchmark = betaToBenchmark; }
    public Double getAlphaToBenchmark() { return alphaToBenchmark; }
    public void setAlphaToBenchmark(Double alphaToBenchmark) { this.alphaToBenchmark = alphaToBenchmark; }
    public Double getInformationRatio() { return informationRatio; }
    public void setInformationRatio(Double informationRatio) { this.informationRatio = informationRatio; }
    public String getSubperiodMetrics() { return subperiodMetrics; }
    public void setSubperiodMetrics(String subperiodMetrics) { this.subperiodMetrics = subperiodMetrics; }
    public String getRegimeBreakdownMetrics() { return regimeBreakdownMetrics; }
    public void setRegimeBreakdownMetrics(String regimeBreakdownMetrics) { this.regimeBreakdownMetrics = regimeBreakdownMetrics; }
    public String getSectorBreakdownMetrics() { return sectorBreakdownMetrics; }
    public void setSectorBreakdownMetrics(String sectorBreakdownMetrics) { this.sectorBreakdownMetrics = sectorBreakdownMetrics; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
