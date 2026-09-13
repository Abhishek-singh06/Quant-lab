package com.quantlab.global.repository;

import com.quantlab.global.entity.GlobalMarketIngestionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalMarketIngestionRunRepository extends JpaRepository<GlobalMarketIngestionRun, Long> {
    Optional<GlobalMarketIngestionRun> findByRunUuid(String runUuid);
}
