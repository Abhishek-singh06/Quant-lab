package com.quantlab.prediction.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DatasetSnapshotDTO {

    private String datasetId;
    private String datasetVersion;
    private String featureVersion;
    private String targetVersion;
    private String universeVersion;
    private LocalDate startDate;
    private LocalDate endDate;
    private int rowCount;
    private int instrumentCount;
    private int featureCount;
    private List<String> featureColumns;
    private Map<String, Object> missingnessStats;
    private Instant createdAt;

    public DatasetSnapshotDTO() {}

    public String getDatasetId() { return datasetId; }
    public void setDatasetId(String datasetId) { this.datasetId = datasetId; }
    public String getDatasetVersion() { return datasetVersion; }
    public void setDatasetVersion(String datasetVersion) { this.datasetVersion = datasetVersion; }
    public String getFeatureVersion() { return featureVersion; }
    public void setFeatureVersion(String featureVersion) { this.featureVersion = featureVersion; }
    public String getTargetVersion() { return targetVersion; }
    public void setTargetVersion(String targetVersion) { this.targetVersion = targetVersion; }
    public String getUniverseVersion() { return universeVersion; }
    public void setUniverseVersion(String universeVersion) { this.universeVersion = universeVersion; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public int getRowCount() { return rowCount; }
    public void setRowCount(int rowCount) { this.rowCount = rowCount; }
    public int getInstrumentCount() { return instrumentCount; }
    public void setInstrumentCount(int instrumentCount) { this.instrumentCount = instrumentCount; }
    public int getFeatureCount() { return featureCount; }
    public void setFeatureCount(int featureCount) { this.featureCount = featureCount; }
    public List<String> getFeatureColumns() { return featureColumns; }
    public void setFeatureColumns(List<String> featureColumns) { this.featureColumns = featureColumns; }
    public Map<String, Object> getMissingnessStats() { return missingnessStats; }
    public void setMissingnessStats(Map<String, Object> missingnessStats) { this.missingnessStats = missingnessStats; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
