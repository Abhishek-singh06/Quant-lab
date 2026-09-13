package com.quantlab.backtest.repository;

import com.quantlab.backtest.entity.BacktestMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BacktestMetricsRepository extends JpaRepository<BacktestMetricsEntity, UUID> {
    Optional<BacktestMetricsEntity> findByRunId(UUID runId);
}
