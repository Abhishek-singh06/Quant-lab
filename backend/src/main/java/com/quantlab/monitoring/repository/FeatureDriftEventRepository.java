package com.quantlab.monitoring.repository;

import com.quantlab.monitoring.entity.FeatureDriftEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeatureDriftEventRepository extends JpaRepository<FeatureDriftEventEntity, UUID> {
    List<FeatureDriftEventEntity> findAllByOrderByDetectedAtDesc();
    List<FeatureDriftEventEntity> findByFeatureNameOrderByDetectedAtDesc(String featureName);
    List<FeatureDriftEventEntity> findByDriftStatusOrderByDetectedAtDesc(String driftStatus);
}
