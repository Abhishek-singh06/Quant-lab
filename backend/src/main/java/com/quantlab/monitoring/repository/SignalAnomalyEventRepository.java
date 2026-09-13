package com.quantlab.monitoring.repository;

import com.quantlab.monitoring.entity.SignalAnomalyEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignalAnomalyEventRepository extends JpaRepository<SignalAnomalyEventEntity, UUID> {
    List<SignalAnomalyEventEntity> findAllByOrderByDetectedAtDesc();
    List<SignalAnomalyEventEntity> findBySignalTypeOrderByDetectedAtDesc(String signalType);
    List<SignalAnomalyEventEntity> findByAnomalyCategoryOrderByDetectedAtDesc(String anomalyCategory);
}
