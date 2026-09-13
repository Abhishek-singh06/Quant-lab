package com.quantlab.backtest.repository;

import com.quantlab.backtest.entity.BacktestPortfolioSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BacktestPortfolioSnapshotRepository extends JpaRepository<BacktestPortfolioSnapshotEntity, UUID> {
    List<BacktestPortfolioSnapshotEntity> findByRunIdOrderBySnapshotDateAsc(UUID runId);
}
