package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperFillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaperFillRepository extends JpaRepository<PaperFillEntity, UUID> {
    List<PaperFillEntity> findByPortfolioIdOrderByExecutionTimestampDesc(UUID portfolioId);
    List<PaperFillEntity> findByOrderId(UUID orderId);
}
