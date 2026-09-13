package com.quantlab.risk.repository;

import com.quantlab.risk.entity.RiskAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, UUID> {
    List<RiskAssessmentEntity> findBySymbolOrderByTimestampDesc(String symbol);
    List<RiskAssessmentEntity> findByPortfolioIdOrderByTimestampDesc(UUID portfolioId);
    Optional<RiskAssessmentEntity> findFirstBySymbolOrderByTimestampDesc(String symbol);
    List<RiskAssessmentEntity> findByTimestampBetweenOrderByTimestampDesc(Instant start, Instant end);
}
