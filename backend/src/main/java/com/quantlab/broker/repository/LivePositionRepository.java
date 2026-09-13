package com.quantlab.broker.repository;

import com.quantlab.broker.entity.LivePositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LivePositionRepository extends JpaRepository<LivePositionEntity, UUID> {
    List<LivePositionEntity> findByBrokerAccountId(UUID brokerAccountId);
    Optional<LivePositionEntity> findByBrokerAccountIdAndSymbol(UUID brokerAccountId, String symbol);
}
