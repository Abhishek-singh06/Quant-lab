package com.quantlab.paper.entity;

import com.quantlab.paper.model.ModelMonitoringStatus;
import com.quantlab.paper.model.PaperModelStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "paper_model_monitoring")
public class PaperModelMonitoringEntity {

    @Id
    private UUID id;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @Column(nullable = false, length = 32)
    private String horizon;

    @Column(name = "evaluation_window_start", nullable = false)
    private LocalDate evaluationWindowStart;

    @Column(name = "evaluation_window_end", nullable = false)
    private LocalDate evaluationWindowEnd;

    @Column(name = "sample_size", nullable = false)
    private Integer sampleSize;

    @Column(name = "directional_accuracy", nullable = false)
    private Double directionalAccuracy;

    @Column(nullable = false)
    private Double mae;

    @Column(nullable = false)
    private Double rmse;

    private Double ic;

    @Column(name = "rank_ic")
    private Double rankIc;

    @Enumerated(EnumType.STRING)
    @Column(name = "drift_status", nullable = false, length = 32)
    private ModelMonitoringStatus driftStatus = ModelMonitoringStatus.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_status", nullable = false, length = 32)
    private PaperModelStatus modelStatus = PaperModelStatus.ACTIVE;

    @Column(name = "evaluated_at", nullable = false)
    private Instant evaluatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getHorizon() { return horizon; }
    public void setHorizon(String horizon) { this.horizon = horizon; }

    public LocalDate getEvaluationWindowStart() { return evaluationWindowStart; }
    public void setEvaluationWindowStart(LocalDate evaluationWindowStart) { this.evaluationWindowStart = evaluationWindowStart; }

    public LocalDate getEvaluationWindowEnd() { return evaluationWindowEnd; }
    public void setEvaluationWindowEnd(LocalDate evaluationWindowEnd) { this.evaluationWindowEnd = evaluationWindowEnd; }

    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }

    public Double getDirectionalAccuracy() { return directionalAccuracy; }
    public void setDirectionalAccuracy(Double directionalAccuracy) { this.directionalAccuracy = directionalAccuracy; }

    public Double getMae() { return mae; }
    public void setMae(Double mae) { this.mae = mae; }

    public Double getRmse() { return rmse; }
    public void setRmse(Double rmse) { this.rmse = rmse; }

    public Double getIc() { return ic; }
    public void setIc(Double ic) { this.ic = ic; }

    public Double getRankIc() { return rankIc; }
    public void setRankIc(Double rankIc) { this.rankIc = rankIc; }

    public ModelMonitoringStatus getDriftStatus() { return driftStatus; }
    public void setDriftStatus(ModelMonitoringStatus driftStatus) { this.driftStatus = driftStatus; }

    public PaperModelStatus getModelStatus() { return modelStatus; }
    public void setModelStatus(PaperModelStatus modelStatus) { this.modelStatus = modelStatus; }

    public Instant getEvaluatedAt() { return evaluatedAt; }
    public void setEvaluatedAt(Instant evaluatedAt) { this.evaluatedAt = evaluatedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
