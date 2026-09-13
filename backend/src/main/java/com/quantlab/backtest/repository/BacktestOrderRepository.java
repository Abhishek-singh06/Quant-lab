package com.quantlab.backtest.repository;

import com.quantlab.backtest.entity.BacktestOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BacktestOrderRepository extends JpaRepository<BacktestOrderEntity, UUID> {
    List<BacktestOrderEntity> findByRunIdOrderByOrderSubmittedTimestampAsc(UUID runId);
}
