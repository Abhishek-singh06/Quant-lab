package com.quantlab.backtest.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "backtest_runs")
public class BacktestRunEntity {

    @Id
    private UUID id;

    @Column(name = "config_id")
    private UUID configId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "engine_version", nullable = false, length = 32)
    private String engineVersion;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_bars_processed", nullable = false)
    private Integer totalBarsProcessed;

    @Column(name = "total_trades_count", nullable = false)
    private Integer totalTradesCount;

    @Column(name = "initial_capital", nullable = false)
    private Double initialCapital;

    @Column(name = "final_equity")
    private Double finalEquity;

    @Column(name = "total_net_pnl")
    private Double totalNetPnl;

    @Column(name = "total_fees_paid")
    private Double totalFeesPaid;

    @Column(name = "total_slippage_paid")
    private Double totalSlippagePaid;

    @Column(name = "total_dividends_received")
    private Double totalDividendsReceived;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    @Column(name = "data_quality_trust_level", nullable = false, length = 32)
    private String dataQualityTrustLevel;

    @Column(name = "data_quality_report", columnDefinition = "JSONB")
    private String dataQualityReport;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public BacktestRunEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getConfigId() { return configId; }
    public void setConfigId(UUID configId) { this.configId = configId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEngineVersion() { return engineVersion; }
    public void setEngineVersion(String engineVersion) { this.engineVersion = engineVersion; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getTotalBarsProcessed() { return totalBarsProcessed; }
    public void setTotalBarsProcessed(Integer totalBarsProcessed) { this.totalBarsProcessed = totalBarsProcessed; }
    public Integer getTotalTradesCount() { return totalTradesCount; }
    public void setTotalTradesCount(Integer totalTradesCount) { this.totalTradesCount = totalTradesCount; }
    public Double getInitialCapital() { return initialCapital; }
    public void setInitialCapital(Double initialCapital) { this.initialCapital = initialCapital; }
    public Double getFinalEquity() { return finalEquity; }
    public void setFinalEquity(Double finalEquity) { this.finalEquity = finalEquity; }
    public Double getTotalNetPnl() { return totalNetPnl; }
    public void setTotalNetPnl(Double totalNetPnl) { this.totalNetPnl = totalNetPnl; }
    public Double getTotalFeesPaid() { return totalFeesPaid; }
    public void setTotalFeesPaid(Double totalFeesPaid) { this.totalFeesPaid = totalFeesPaid; }
    public Double getTotalSlippagePaid() { return totalSlippagePaid; }
    public void setTotalSlippagePaid(Double totalSlippagePaid) { this.totalSlippagePaid = totalSlippagePaid; }
    public Double getTotalDividendsReceived() { return totalDividendsReceived; }
    public void setTotalDividendsReceived(Double totalDividendsReceived) { this.totalDividendsReceived = totalDividendsReceived; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getExecutionDurationMs() { return executionDurationMs; }
    public void setExecutionDurationMs(Long executionDurationMs) { this.executionDurationMs = executionDurationMs; }
    public String getDataQualityTrustLevel() { return dataQualityTrustLevel; }
    public void setDataQualityTrustLevel(String dataQualityTrustLevel) { this.dataQualityTrustLevel = dataQualityTrustLevel; }
    public String getDataQualityReport() { return dataQualityReport; }
    public void setDataQualityReport(String dataQualityReport) { this.dataQualityReport = dataQualityReport; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
