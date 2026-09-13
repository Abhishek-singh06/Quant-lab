package com.quantlab.broker.repository;

import com.quantlab.broker.entity.TradingSafetyLockEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradingSafetyLockRepository extends JpaRepository<TradingSafetyLockEntity, String> {
}
