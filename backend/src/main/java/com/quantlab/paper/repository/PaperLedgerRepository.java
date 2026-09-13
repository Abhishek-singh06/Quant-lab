package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperLedgerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaperLedgerRepository extends JpaRepository<PaperLedgerEntity, UUID> {
    List<PaperLedgerEntity> findByPortfolioIdOrderByTransactionTimestampDesc(UUID portfolioId);
}
