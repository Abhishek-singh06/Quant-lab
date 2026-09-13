package com.quantlab.backtest.repository;

import com.quantlab.backtest.entity.BacktestTradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BacktestTradeRepository extends JpaRepository<BacktestTradeEntity, UUID> {
    List<BacktestTradeEntity> findByRunIdOrderByExitTimestampDesc(UUID runId);
}
