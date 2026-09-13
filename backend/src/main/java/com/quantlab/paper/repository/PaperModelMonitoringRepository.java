package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperModelMonitoringEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaperModelMonitoringRepository extends JpaRepository<PaperModelMonitoringEntity, UUID> {
    List<PaperModelMonitoringEntity> findByModelVersionOrderByEvaluatedAtDesc(String modelVersion);
    List<PaperModelMonitoringEntity> findAllByOrderByEvaluatedAtDesc();
    Optional<PaperModelMonitoringEntity> findFirstByModelVersionOrderByEvaluatedAtDesc(String modelVersion);
}
