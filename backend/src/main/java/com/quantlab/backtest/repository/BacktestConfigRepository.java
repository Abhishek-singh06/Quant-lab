package com.quantlab.backtest.repository;

import com.quantlab.backtest.entity.BacktestConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BacktestConfigRepository extends JpaRepository<BacktestConfigEntity, UUID> {
    Optional<BacktestConfigEntity> findByName(String name);
}
