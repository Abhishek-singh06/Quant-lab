package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaperPositionRepository extends JpaRepository<PaperPositionEntity, UUID> {
    List<PaperPositionEntity> findByPortfolioId(UUID portfolioId);
    List<PaperPositionEntity> findByPortfolioIdAndIsActiveTrue(UUID portfolioId);
    Optional<PaperPositionEntity> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol);
}
