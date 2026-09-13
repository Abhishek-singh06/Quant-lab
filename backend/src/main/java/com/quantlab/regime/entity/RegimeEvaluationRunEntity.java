package com.quantlab.regime.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "regime_evaluation_runs", schema = "market_data")
public class RegimeEvaluationRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true, length = 64)
    private String runId;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @Column(name = "evaluation_type", nullable = false, length = 32)
    private String evaluationType = "WALK_FORWARD";

    @Column(name = "train_start", nullable = false)
    private LocalDate trainStart;

    @Column(name = "train_end", nullable = false)
    private LocalDate trainEnd;

    @Column(name = "test_start", nullable = false)
    private LocalDate testStart;

    @Column(name = "test_end", nullable = false)
    private LocalDate testEnd;

    @Column(name = "bull_forward_return_20d", precision = 10, scale = 4)
    private BigDecimal bullForwardReturn20d;

    @Column(name = "bear_forward_return_20d", precision = 10, scale = 4)
    private BigDecimal bearForwardReturn20d;

    @Column(name = "sideways_forward_return_20d", precision = 10, scale = 4)
    private BigDecimal sidewaysForwardReturn20d;

    @Column(name = "bull_sharpe", precision = 8, scale = 4)
    private BigDecimal bullSharpe;

    @Column(name = "bear_sharpe", precision = 8, scale = 4)
    private BigDecimal bearSharpe;

    @Column(name = "regime_persistence", precision = 6, scale = 4)
    private BigDecimal regimePersistence;

    @Column(name = "baseline1_sma50_sharpe", precision = 8, scale = 4)
    private BigDecimal baseline1Sma50Sharpe;

    @Column(name = "baseline2_sma200_sharpe", precision = 8, scale = 4)
    private BigDecimal baseline2Sma200Sharpe;

    @Column(name = "baseline3_momentum_sharpe", precision = 8, scale = 4)
    private BigDecimal baseline3MomentumSharpe;

    @Column(name = "baseline4_vix_sharpe", precision = 8, scale = 4)
    private BigDecimal baseline4VixSharpe;

    @Column(name = "summary_report", nullable = false, columnDefinition = "JSONB")
    private String summaryReport;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public RegimeEvaluationRunEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getEvaluationType() { return evaluationType; }
    public void setEvaluationType(String evaluationType) { this.evaluationType = evaluationType; }
    public LocalDate getTrainStart() { return trainStart; }
    public void setTrainStart(LocalDate trainStart) { this.trainStart = trainStart; }
    public LocalDate getTrainEnd() { return trainEnd; }
    public void setTrainEnd(LocalDate trainEnd) { this.trainEnd = trainEnd; }
    public LocalDate getTestStart() { return testStart; }
    public void setTestStart(LocalDate testStart) { this.testStart = testStart; }
    public LocalDate getTestEnd() { return testEnd; }
    public void setTestEnd(LocalDate testEnd) { this.testEnd = testEnd; }
    public BigDecimal getBullForwardReturn20d() { return bullForwardReturn20d; }
    public void setBullForwardReturn20d(BigDecimal bullForwardReturn20d) { this.bullForwardReturn20d = bullForwardReturn20d; }
    public BigDecimal getBearForwardReturn20d() { return bearForwardReturn20d; }
    public void setBearForwardReturn20d(BigDecimal bearForwardReturn20d) { this.bearForwardReturn20d = bearForwardReturn20d; }
    public BigDecimal getSidewaysForwardReturn20d() { return sidewaysForwardReturn20d; }
    public void setSidewaysForwardReturn20d(BigDecimal sidewaysForwardReturn20d) { this.sidewaysForwardReturn20d = sidewaysForwardReturn20d; }
    public BigDecimal getBullSharpe() { return bullSharpe; }
    public void setBullSharpe(BigDecimal bullSharpe) { this.bullSharpe = bullSharpe; }
    public BigDecimal getBearSharpe() { return bearSharpe; }
    public void setBearSharpe(BigDecimal bearSharpe) { this.bearSharpe = bearSharpe; }
    public BigDecimal getRegimePersistence() { return regimePersistence; }
    public void setRegimePersistence(BigDecimal regimePersistence) { this.regimePersistence = regimePersistence; }
    public BigDecimal getBaseline1Sma50Sharpe() { return baseline1Sma50Sharpe; }
    public void setBaseline1Sma50Sharpe(BigDecimal baseline1Sma50Sharpe) { this.baseline1Sma50Sharpe = baseline1Sma50Sharpe; }
    public BigDecimal getBaseline2Sma200Sharpe() { return baseline2Sma200Sharpe; }
    public void setBaseline2Sma200Sharpe(BigDecimal baseline2Sma200Sharpe) { this.baseline2Sma200Sharpe = baseline2Sma200Sharpe; }
    public BigDecimal getBaseline3MomentumSharpe() { return baseline3MomentumSharpe; }
    public void setBaseline3MomentumSharpe(BigDecimal baseline3MomentumSharpe) { this.baseline3MomentumSharpe = baseline3MomentumSharpe; }
    public BigDecimal getBaseline4VixSharpe() { return baseline4VixSharpe; }
    public void setBaseline4VixSharpe(BigDecimal baseline4VixSharpe) { this.baseline4VixSharpe = baseline4VixSharpe; }
    public String getSummaryReport() { return summaryReport; }
    public void setSummaryReport(String summaryReport) { this.summaryReport = summaryReport; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
