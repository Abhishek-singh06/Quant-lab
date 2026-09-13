package com.quantlab.horizon.repository;

import com.quantlab.horizon.entity.HorizonPredictionEntity;
import com.quantlab.horizon.model.TradingHorizon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HorizonPredictionRepository extends JpaRepository<HorizonPredictionEntity, UUID> {
    List<HorizonPredictionEntity> findBySymbolOrderByPredictionTimestampDesc(String symbol);
    List<HorizonPredictionEntity> findBySymbolAndHorizonOrderByPredictionTimestampDesc(String symbol, TradingHorizon horizon);
    Optional<HorizonPredictionEntity> findFirstBySymbolAndHorizonOrderByPredictionTimestampDesc(String symbol, TradingHorizon horizon);
    List<HorizonPredictionEntity> findByHorizonOrderByPredictionTimestampDesc(TradingHorizon horizon);
}
