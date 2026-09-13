package com.quantlab.prediction.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "dataset_snapshots", schema = "market_data")
public class DatasetSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dataset_id", nullable = false, unique = true, length = 64)
    private String datasetId;

    @Column(name = "dataset_version", nullable = false, length = 32)
    private String datasetVersion;

    @Column(name = "feature_version", nullable = false, length = 16)
    private String featureVersion;

    @Column(name = "target_version", nullable = false, length = 16)
    private String targetVersion;

    @Column(name = "universe_version", nullable = false, length = 32)
    private String universeVersion;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "row_count", nullable = false)
    private int rowCount;

    @Column(name = "instrument_count", nullable = false)
    private int instrumentCount;

    @Column(name = "feature_count", nullable = false)
    private int featureCount;

    @Column(name = "feature_columns", nullable = false, columnDefinition = "JSONB")
    private String featureColumns;

    @Column(name = "missingness_stats", columnDefinition = "JSONB")
    private String missingnessStats;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public DatasetSnapshotEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getFeatureColumns() { return featureColumns; }
    public void setFeatureColumns(String featureColumns) { this.featureColumns = featureColumns; }
    public String getMissingnessStats() { return missingnessStats; }
    public void setMissingnessStats(String missingnessStats) { this.missingnessStats = missingnessStats; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
