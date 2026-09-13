package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperTradingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaperTradingSessionRepository extends JpaRepository<PaperTradingSessionEntity, UUID> {
    Optional<PaperTradingSessionEntity> findByName(String name);
}
