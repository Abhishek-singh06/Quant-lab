package com.quantlab.prediction.entity;

import com.quantlab.prediction.model.ModelStatus;
import com.quantlab.prediction.model.ModelType;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "model_registry", schema = "market_data")
public class QuantModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_id", nullable = false, unique = true, length = 64)
    private String modelId;

    @Column(name = "model_name", nullable = false, length = 128)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false, length = 32)
    private ModelType modelType;

    @Column(name = "algorithm", nullable = false, length = 64)
    private String algorithm;

    @Column(name = "model_version", nullable = false, unique = true, length = 32)
    private String modelVersion;

    @Column(name = "feature_version", nullable = false, length = 16)
    private String featureVersion;

    @Column(name = "dataset_version", nullable = false, length = 32)
    private String datasetVersion;

    @Column(name = "target_definition", nullable = false, length = 64)
    private String targetDefinition;

    @Column(name = "target_horizon", nullable = false, length = 16)
    private String targetHorizon;

    @Column(name = "training_start", nullable = false)
    private LocalDate trainingStart;

    @Column(name = "training_end", nullable = false)
    private LocalDate trainingEnd;

    @Column(name = "validation_start", nullable = false)
    private LocalDate validationStart;

    @Column(name = "validation_end", nullable = false)
    private LocalDate validationEnd;

    @Column(name = "test_start")
    private LocalDate testStart;

    @Column(name = "test_end")
    private LocalDate testEnd;

    @Column(name = "hyperparameters", nullable = false, columnDefinition = "JSONB")
    private String hyperparameters;

    @Column(name = "metrics", nullable = false, columnDefinition = "JSONB")
    private String metrics;

    @Column(name = "feature_importance", columnDefinition = "JSONB")
    private String featureImportance;

    @Column(name = "artifact_path", length = 256)
    private String artifactPath;

    @Column(name = "artifact_checksum", length = 64)
    private String artifactChecksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ModelStatus status = ModelStatus.VALIDATED;

    @Column(name = "random_seed", nullable = false)
    private int randomSeed = 42;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public QuantModelEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public ModelType getModelType() { return modelType; }
    public void setModelType(ModelType modelType) { this.modelType = modelType; }
    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }
    public String getDatasetVersion() { return datasetVersion; }
    public void setDatasetVersion(String datasetVersion) { this.datasetVersion = datasetVersion; }
    public String getTargetDefinition() { return targetDefinition; }
    public void setTargetDefinition(String targetDefinition) { this.targetDefinition = targetDefinition; }
    public String getTargetHorizon() { return targetHorizon; }
    public void setTargetHorizon(String targetHorizon) { this.targetHorizon = targetHorizon; }
    public LocalDate getTrainingStart() { return trainingStart; }
    public void setTrainingStart(LocalDate trainingStart) { this.trainingStart = trainingStart; }
    public LocalDate getTrainingEnd() { return trainingEnd; }
    public void setTrainingEnd(LocalDate trainingEnd) { this.trainingEnd = trainingEnd; }
    public LocalDate getValidationStart() { return validationStart; }
    public void setValidationStart(LocalDate validationStart) { this.validationStart = validationStart; }
    public LocalDate getValidationEnd() { return validationEnd; }
    public void setValidationEnd(LocalDate validationEnd) { this.validationEnd = validationEnd; }
    public LocalDate getTestStart() { return testStart; }
    public void setTestStart(LocalDate testStart) { this.testStart = testStart; }
    public LocalDate getTestEnd() { return testEnd; }
    public void setTestEnd(LocalDate testEnd) { this.testEnd = testEnd; }
    public String getHyperparameters() { return hyperparameters; }
    public void setHyperparameters(String hyperparameters) { this.hyperparameters = hyperparameters; }
    public String getMetrics() { return metrics; }
    public void setMetrics(String metrics) { this.metrics = metrics; }
    public String getFeatureImportance() { return featureImportance; }
    public void setFeatureImportance(String featureImportance) { this.featureImportance = featureImportance; }
    public String getArtifactPath() { return artifactPath; }
    public void setArtifactPath(String artifactPath) { this.artifactPath = artifactPath; }
    public String getArtifactChecksum() { return artifactChecksum; }
    public void setArtifactChecksum(String artifactChecksum) { this.artifactChecksum = artifactChecksum; }
    public ModelStatus getStatus() { return status; }
    public void setStatus(ModelStatus status) { this.status = status; }
    public int getRandomSeed() { return randomSeed; }
    public void setRandomSeed(int randomSeed) { this.randomSeed = randomSeed; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
