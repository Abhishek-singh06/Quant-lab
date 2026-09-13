package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaperDecisionRepository extends JpaRepository<PaperDecisionEntity, UUID> {
    List<PaperDecisionEntity> findByPortfolioIdOrderByTimestampDesc(UUID portfolioId);
    List<PaperDecisionEntity> findBySymbolOrderByTimestampDesc(String symbol);
    List<PaperDecisionEntity> findAllByOrderByTimestampDesc();
}
