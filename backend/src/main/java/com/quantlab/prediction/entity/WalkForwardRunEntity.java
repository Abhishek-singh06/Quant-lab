package com.quantlab.prediction.entity;

import com.quantlab.prediction.model.ModelType;
import com.quantlab.prediction.model.WalkForwardMode;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "walk_forward_runs", schema = "market_data")
public class WalkForwardRunEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_id", nullable = false, unique = true, length = 64)
    private String runId;

    @Column(name = "run_name", nullable = false, length = 128)
    private String runName;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false, length = 32)
    private WalkForwardMode mode;

    @Column(name = "initial_train_start", nullable = false)
    private LocalDate initialTrainStart;

    @Column(name = "initial_train_end", nullable = false)
    private LocalDate initialTrainEnd;

    @Column(name = "step_size", nullable = false, length = 16)
    private String stepSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false, length = 32)
    private ModelType modelType;

    @Column(name = "target_definition", nullable = false, length = 64)
    private String targetDefinition;

    @Column(name = "purge_window_days", nullable = false)
    private int purgeWindowDays = 5;

    @Column(name = "embargo_window_days", nullable = false)
    private int embargoWindowDays = 2;

    @Column(name = "total_folds", nullable = false)
    private int totalFolds;

    @Column(name = "completed_folds", nullable = false)
    private int completedFolds = 0;

    @Column(name = "status", nullable = false, length = 32)
    private String status = "COMPLETED";

    @Column(name = "aggregate_metrics", nullable = false, columnDefinition = "JSONB")
    private String aggregateMetrics;

    @OneToMany(mappedBy = "walkForwardRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WalkForwardFoldEntity> folds = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public WalkForwardRunEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getAggregateMetrics() { return aggregateMetrics; }
    public void setAggregateMetrics(String aggregateMetrics) { this.aggregateMetrics = aggregateMetrics; }
    public List<WalkForwardFoldEntity> getFolds() { return folds; }
    public void setFolds(List<WalkForwardFoldEntity> folds) { this.folds = folds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
