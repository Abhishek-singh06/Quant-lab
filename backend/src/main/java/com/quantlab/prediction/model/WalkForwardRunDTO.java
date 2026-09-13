package com.quantlab.prediction.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class WalkForwardRunDTO {

    private String runId;
    private String runName;
    private WalkForwardMode mode;
    private LocalDate initialTrainStart;
    private LocalDate initialTrainEnd;
    private String stepSize;
    private ModelType modelType;
    private String targetDefinition;
    private int purgeWindowDays;
    private int embargoWindowDays;
    private int totalFolds;
    private int completedFolds;
    private String status;
    private Map<String, Object> aggregateMetrics;
    private List<WalkForwardFoldDTO> folds;
    private Instant createdAt;
    private Instant completedAt;

    public WalkForwardRunDTO() {}

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getRunName() { return runName; }
    public void setRunName(String runName) { this.runName = runName; }
    public WalkForwardMode getMode() { return mode; }
    public void setMode(WalkForwardMode mode) { this.mode = mode; }
    public LocalDate getInitialTrainStart() { return initialTrainStart; }
    public void setInitialTrainStart(LocalDate initialTrainStart) { this.initialTrainStart = initialTrainStart; }
    public LocalDate getInitialTrainEnd() { return initialTrainEnd; }
    public void setInitialTrainEnd(LocalDate initialTrainEnd) { this.initialTrainEnd = initialTrainEnd; }
    public String getStepSize() { return stepSize; }
    public void setStepSize(String stepSize) { this.stepSize = stepSize; }
    public ModelType getModelType() { return modelType; }
    public void setModelType(ModelType modelType) { this.modelType = modelType; }
    public String getTargetDefinition() { return targetDefinition; }
    public void setTargetDefinition(String targetDefinition) { this.targetDefinition = targetDefinition; }
    public int getPurgeWindowDays() { return purgeWindowDays; }
    public void setPurgeWindowDays(int purgeWindowDays) { this.purgeWindowDays = purgeWindowDays; }
    public int getEmbargoWindowDays() { return embargoWindowDays; }
    public void setEmbargoWindowDays(int embargoWindowDays) { this.embargoWindowDays = embargoWindowDays; }
    public int getTotalFolds() { return totalFolds; }
    public void setTotalFolds(int totalFolds) { this.totalFolds = totalFolds; }
    public int getCompletedFolds() { return completedFolds; }
    public void setCompletedFolds(int completedFolds) { this.completedFolds = completedFolds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Map<String, Object> getAggregateMetrics() { return aggregateMetrics; }
    public void setAggregateMetrics(Map<String, Object> aggregateMetrics) { this.aggregateMetrics = aggregateMetrics; }
    public List<WalkForwardFoldDTO> getFolds() { return folds; }
    public void setFolds(List<WalkForwardFoldDTO> folds) { this.folds = folds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
