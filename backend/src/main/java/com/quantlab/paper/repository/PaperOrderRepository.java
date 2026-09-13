package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaperOrderRepository extends JpaRepository<PaperOrderEntity, UUID> {
    List<PaperOrderEntity> findByPortfolioIdOrderByOrderSubmittedTimestampDesc(UUID portfolioId);
    List<PaperOrderEntity> findBySessionIdOrderByOrderSubmittedTimestampDesc(UUID sessionId);
    List<PaperOrderEntity> findByStatus(String status);
}
