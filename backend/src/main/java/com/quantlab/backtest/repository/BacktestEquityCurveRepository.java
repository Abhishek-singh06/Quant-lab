package com.quantlab.backtest.repository;

import com.quantlab.backtest.entity.BacktestEquityCurveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BacktestEquityCurveRepository extends JpaRepository<BacktestEquityCurveEntity, UUID> {
    List<BacktestEquityCurveEntity> findByRunIdOrderByPointDateAsc(UUID runId);
}
