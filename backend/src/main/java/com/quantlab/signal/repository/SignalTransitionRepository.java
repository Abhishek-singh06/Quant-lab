package com.quantlab.signal.repository;

import com.quantlab.signal.entity.SignalTransitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignalTransitionRepository extends JpaRepository<SignalTransitionEntity, UUID> {
    List<SignalTransitionEntity> findBySymbolOrderByTransitionTimestampDesc(String symbol);
}
