package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperRiskMonitoringEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaperRiskMonitoringRepository extends JpaRepository<PaperRiskMonitoringEntity, UUID> {
    List<PaperRiskMonitoringEntity> findByPortfolioIdOrderByEvaluationTimestampDesc(UUID portfolioId);
}
