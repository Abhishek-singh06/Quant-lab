package com.quantlab.risk.repository;

import com.quantlab.risk.entity.PortfolioPositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioPositionRepository extends JpaRepository<PortfolioPositionEntity, UUID> {
    List<PortfolioPositionEntity> findByPortfolioIdAndActiveTrue(UUID portfolioId);
    Optional<PortfolioPositionEntity> findByPortfolioIdAndSymbolAndActiveTrue(UUID portfolioId, String symbol);
}
