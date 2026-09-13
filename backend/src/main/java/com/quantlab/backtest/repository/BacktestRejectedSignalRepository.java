package com.quantlab.backtest.repository;

import com.quantlab.backtest.entity.BacktestRejectedSignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BacktestRejectedSignalRepository extends JpaRepository<BacktestRejectedSignalEntity, UUID> {
    List<BacktestRejectedSignalEntity> findByRunIdOrderBySignalTimestampAsc(UUID runId);
}
