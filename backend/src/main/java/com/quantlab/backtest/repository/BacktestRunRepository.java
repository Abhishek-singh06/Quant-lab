package com.quantlab.backtest.repository;

import com.quantlab.backtest.entity.BacktestRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BacktestRunRepository extends JpaRepository<BacktestRunEntity, UUID> {
    List<BacktestRunEntity> findByConfigIdOrderByCreatedAtDesc(UUID configId);
    List<BacktestRunEntity> findAllByOrderByCreatedAtDesc();
}
