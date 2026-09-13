package com.quantlab.prediction.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public class WalkForwardFoldDTO {

    private String foldId;
    private String runId;
    private int foldNumber;
    private LocalDate trainStart;
    private LocalDate trainEnd;
    private LocalDate validationStart;
    private LocalDate validationEnd;
    private LocalDate testStart;
    private LocalDate testEnd;
    private String winningModelId;
    private String winningAlgorithm;
    private String modelVersion;
    private int testObservations;
    private BigDecimal testIc;
    private BigDecimal testRankIc;
    private BigDecimal testMae;
    private BigDecimal testRmse;
    private BigDecimal testRocAuc;
    private BigDecimal testDirectionalAccuracy;
    private Map<String, Object> baselineComparison;
    private Map<String, Object> regimeBreakdown;
    private Map<String, Object> sectorBreakdown;
    private String status;
    private Instant createdAt;

    public WalkForwardFoldDTO() {}

    public String getFoldId() { return foldId; }
    public void setFoldId(String foldId) { this.foldId = foldId; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
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
    public Map<String, Object> getBaselineComparison() { return baselineComparison; }
    public void setBaselineComparison(Map<String, Object> baselineComparison) { this.baselineComparison = baselineComparison; }
    public Map<String, Object> getRegimeBreakdown() { return regimeBreakdown; }
    public void setRegimeBreakdown(Map<String, Object> regimeBreakdown) { this.regimeBreakdown = regimeBreakdown; }
    public Map<String, Object> getSectorBreakdown() { return sectorBreakdown; }
    public void setSectorBreakdown(Map<String, Object> sectorBreakdown) { this.sectorBreakdown = sectorBreakdown; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
