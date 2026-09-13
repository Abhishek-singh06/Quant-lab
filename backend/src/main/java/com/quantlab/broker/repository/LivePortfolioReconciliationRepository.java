package com.quantlab.broker.repository;

import com.quantlab.broker.entity.LivePortfolioReconciliationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LivePortfolioReconciliationRepository extends JpaRepository<LivePortfolioReconciliationEntity, UUID> {
    List<LivePortfolioReconciliationEntity> findByBrokerAccountIdOrderByReconciliationTimestampDesc(UUID brokerAccountId);
    Optional<LivePortfolioReconciliationEntity> findFirstByBrokerAccountIdOrderByReconciliationTimestampDesc(UUID brokerAccountId);
}
