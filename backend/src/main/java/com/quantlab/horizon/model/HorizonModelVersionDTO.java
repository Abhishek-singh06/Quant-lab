package com.quantlab.horizon.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class HorizonModelVersionDTO {
    private UUID id;
    private UUID configId;
    private String modelId;
    private String modelVersion;
    private TradingHorizon horizon;
    private String targetPeriod;
    private TargetType targetType;
    private String featureSetVersion;
    private String targetSetVersion;
    private ModelAlgorithm algorithm;
    private Map<String, Object> hyperparameters;
    private LocalDate trainingStart;
    private LocalDate trainingEnd;
    private LocalDate validationStart;
    private LocalDate validationEnd;
    private LocalDate testStart;
    private LocalDate testEnd;
    private ModelStatus modelStatus;
    private Instant modelAvailabilityTimestamp;
    private Map<String, Object> metrics;
    private Map<String, Double> featureImportances;
    private Instant createdAt;

    public HorizonModelVersionDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getConfigId() { return configId; }
    public void setConfigId(UUID configId) { this.configId = configId; }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public TradingHorizon getHorizon() { return horizon; }
    public void setHorizon(TradingHorizon horizon) { this.horizon = horizon; }

    public String getTargetPeriod() { return targetPeriod; }
    public void setTargetPeriod(String targetPeriod) { this.targetPeriod = targetPeriod; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public String getFeatureSetVersion() { return featureSetVersion; }
    public void setFeatureSetVersion(String featureSetVersion) { this.featureSetVersion = featureSetVersion; }

    public String getTargetSetVersion() { return targetSetVersion; }
    public void setTargetSetVersion(String targetSetVersion) { this.targetSetVersion = targetSetVersion; }

    public ModelAlgorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(ModelAlgorithm algorithm) { this.algorithm = algorithm; }

    public Map<String, Object> getHyperparameters() { return hyperparameters; }
    public void setHyperparameters(Map<String, Object> hyperparameters) { this.hyperparameters = hyperparameters; }

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

    public ModelStatus getModelStatus() { return modelStatus; }
    public void setModelStatus(ModelStatus modelStatus) { this.modelStatus = modelStatus; }

    public Instant getModelAvailabilityTimestamp() { return modelAvailabilityTimestamp; }
    public void setModelAvailabilityTimestamp(Instant modelAvailabilityTimestamp) { this.modelAvailabilityTimestamp = modelAvailabilityTimestamp; }

    public Map<String, Object> getMetrics() { return metrics; }
    public void setMetrics(Map<String, Object> metrics) { this.metrics = metrics; }

    public Map<String, Double> getFeatureImportances() { return featureImportances; }
    public void setFeatureImportances(Map<String, Double> featureImportances) { this.featureImportances = featureImportances; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
