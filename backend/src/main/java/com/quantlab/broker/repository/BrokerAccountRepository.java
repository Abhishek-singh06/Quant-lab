package com.quantlab.broker.repository;

import com.quantlab.broker.entity.BrokerAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrokerAccountRepository extends JpaRepository<BrokerAccountEntity, UUID> {
    List<BrokerAccountEntity> findByUserId(String userId);
    Optional<BrokerAccountEntity> findFirstByIsLiveTradingEnabledTrue();
}
