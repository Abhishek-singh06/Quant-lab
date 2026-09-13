package com.quantlab.monitoring.repository;

import com.quantlab.monitoring.entity.DataQualityEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataQualityEventRepository extends JpaRepository<DataQualityEventEntity, UUID> {
    List<DataQualityEventEntity> findAllByOrderByDetectedAtDesc();
    List<DataQualityEventEntity> findByProviderOrderByDetectedAtDesc(String provider);
    List<DataQualityEventEntity> findByEventTypeOrderByDetectedAtDesc(String eventType);
    List<DataQualityEventEntity> findBySeverityOrderByDetectedAtDesc(String severity);
}
