package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperPortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaperPortfolioRepository extends JpaRepository<PaperPortfolioEntity, UUID> {
    Optional<PaperPortfolioEntity> findByName(String name);
    List<PaperPortfolioEntity> findBySessionId(UUID sessionId);
    List<PaperPortfolioEntity> findByHorizon(String horizon);
}
