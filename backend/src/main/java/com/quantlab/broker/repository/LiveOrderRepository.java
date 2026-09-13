package com.quantlab.broker.repository;

import com.quantlab.broker.entity.LiveOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveOrderRepository extends JpaRepository<LiveOrderEntity, UUID> {
    List<LiveOrderEntity> findByBrokerAccountIdOrderByCreatedAtDesc(UUID brokerAccountId);
    List<LiveOrderEntity> findAllByOrderByCreatedAtDesc();
    Optional<LiveOrderEntity> findByIdempotencyKey(String idempotencyKey);
}
