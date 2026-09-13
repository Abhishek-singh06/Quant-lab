package com.quantlab.prediction.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

public class QuantModelDTO {

    private String modelId;
    private String modelName;
    private ModelType modelType;
    private String algorithm;
    private String modelVersion;
    private String featureVersion;
    private String datasetVersion;
    private String targetDefinition;
    private String targetHorizon;
    private LocalDate trainingStart;
    private LocalDate trainingEnd;
    private LocalDate validationStart;
    private LocalDate validationEnd;
    private LocalDate testStart;
    private LocalDate testEnd;
    private Map<String, Object> hyperparameters;
    private Map<String, Object> metrics;
    private Map<String, Object> featureImportance;
    private String artifactPath;
    private String artifactChecksum;
    private ModelStatus status;
    private int randomSeed;
    private Instant createdAt;
    private Instant updatedAt;

    public QuantModelDTO() {}

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
    public Map<String, Object> getHyperparameters() { return hyperparameters; }
    public void setHyperparameters(Map<String, Object> hyperparameters) { this.hyperparameters = hyperparameters; }
    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }
    public Map<String, Object> getFeatureImportance() { return featureImportance; }
    public void setFeatureImportance(Map<String, Object> featureImportance) { this.featureImportance = featureImportance; }
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
