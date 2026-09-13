package com.quantlab.prediction.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "walk_forward_folds", schema = "market_data")
public class WalkForwardFoldEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fold_id", nullable = false, unique = true, length = 64)
    private String foldId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", referencedColumnName = "run_id", nullable = false)
    private WalkForwardRunEntity walkForwardRun;

    @Column(name = "fold_number", nullable = false)
    private int foldNumber;

    @Column(name = "train_start", nullable = false)
    private LocalDate trainStart;

    @Column(name = "train_end", nullable = false)
    private LocalDate trainEnd;

    @Column(name = "validation_start", nullable = false)
    private LocalDate validationStart;

    @Column(name = "validation_end", nullable = false)
    private LocalDate validationEnd;

    @Column(name = "test_start", nullable = false)
    private LocalDate testStart;

    @Column(name = "test_end", nullable = false)
    private LocalDate testEnd;

    @Column(name = "winning_model_id", nullable = false, length = 64)
    private String winningModelId;

    @Column(name = "winning_algorithm", nullable = false, length = 64)
    private String winningAlgorithm;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @Column(name = "test_observations", nullable = false)
    private int testObservations;

    @Column(name = "test_ic", precision = 8, scale = 4)
    private BigDecimal testIc;

    @Column(name = "test_rank_ic", precision = 8, scale = 4)
    private BigDecimal testRankIc;

    @Column(name = "test_mae", precision = 10, scale = 6)
    private BigDecimal testMae;

    @Column(name = "test_rmse", precision = 10, scale = 6)
    private BigDecimal testRmse;

    @Column(name = "test_roc_auc", precision = 6, scale = 4)
    private BigDecimal testRocAuc;

    @Column(name = "test_directional_accuracy", precision = 6, scale = 4)
    private BigDecimal testDirectionalAccuracy;

    @Column(name = "baseline_comparison", nullable = false, columnDefinition = "JSONB")
    private String baselineComparison;

    @Column(name = "regime_breakdown", columnDefinition = "JSONB")
    private String regimeBreakdown;

    @Column(name = "sector_breakdown", columnDefinition = "JSONB")
    private String sectorBreakdown;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "COMPLETED";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public WalkForwardFoldEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFoldId() { return foldId; }
    public void setFoldId(String foldId) { this.foldId = foldId; }
    public WalkForwardRunEntity getWalkForwardRun() { return walkForwardRun; }
    public void setWalkForwardRun(WalkForwardRunEntity walkForwardRun) { this.walkForwardRun = walkForwardRun; }
    public int getFoldNumber() { return foldNumber; }
    public void setFoldNumber(int foldNumber) { this.foldNumber = foldNumber; }
    public LocalDate getTrainStart() { return trainStart; }
    public void setTrainStart(LocalDate trainStart) { this.trainStart = trainStart; }
    public LocalDate getTrainEnd() { return trainEnd; }
    public void setTrainEnd(LocalDate trainEnd) { this.trainEnd = trainEnd; }
    public LocalDate getValidationStart() { return validationStart; }
    public void setValidationStart(LocalDate validationStart) { this.validationStart = validationStart; }
    public LocalDate getValidationEnd() { return validationEnd; }
    public void setValidationEnd(LocalDate validationEnd) { this.validationEnd = validationEnd; }
    public LocalDate getTestStart() { return testStart; }
    public void setTestStart(LocalDate testStart) { this.testStart = testStart; }
    public LocalDate getTestEnd() { return testEnd; }
    public void setTestEnd(LocalDate testEnd) { this.testEnd = testEnd; }
    public String getWinningModelId() { return winningModelId; }
    public void setWinningModelId(String winningModelId) { this.winningModelId = winningModelId; }
    public String getWinningAlgorithm() { return winningAlgorithm; }
    public void setWinningAlgorithm(String winningAlgorithm) { this.winningAlgorithm = winningAlgorithm; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public int getTestObservations() { return testObservations; }
    public void setTestObservations(int testObservations) { this.testObservations = testObservations; }
    public BigDecimal getTestIc() { return testIc; }
    public void setTestIc(BigDecimal testIc) { this.testIc = testIc; }
    public BigDecimal getTestRankIc() { return testRankIc; }
    public void setTestRankIc(BigDecimal testRankIc) { this.testRankIc = testRankIc; }
    public BigDecimal getTestMae() { return testMae; }
    public void setTestMae(BigDecimal testMae) { this.testMae = testMae; }
    public BigDecimal getTestRmse() { return testRmse; }
    public void setTestRmse(BigDecimal testRmse) { this.testRmse = testRmse; }
    public BigDecimal getTestRocAuc() { return testRocAuc; }
    public void setTestRocAuc(BigDecimal testRocAuc) { this.testRocAuc = testRocAuc; }
    public BigDecimal getTestDirectionalAccuracy() { return testDirectionalAccuracy; }
    public void setTestDirectionalAccuracy(BigDecimal testDirectionalAccuracy) { this.testDirectionalAccuracy = testDirectionalAccuracy; }
    public String getBaselineComparison() { return baselineComparison; }
    public void setBaselineComparison(String baselineComparison) { this.baselineComparison = baselineComparison; }
    public String getRegimeBreakdown() { return regimeBreakdown; }
    public void setRegimeBreakdown(String regimeBreakdown) { this.regimeBreakdown = regimeBreakdown; }
    public String getSectorBreakdown() { return sectorBreakdown; }
    public void setSectorBreakdown(String sectorBreakdown) { this.sectorBreakdown = sectorBreakdown; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
