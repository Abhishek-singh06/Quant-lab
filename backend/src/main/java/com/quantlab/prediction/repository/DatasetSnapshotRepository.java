package com.quantlab.prediction.repository;

import com.quantlab.prediction.entity.DatasetSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatasetSnapshotRepository extends JpaRepository<DatasetSnapshotEntity, Long> {

    Optional<DatasetSnapshotEntity> findByDatasetId(String datasetId);

    Optional<DatasetSnapshotEntity> findByDatasetVersion(String datasetVersion);
}
