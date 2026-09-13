package com.quantlab.regime.repository;

import com.quantlab.regime.entity.RegimeComponentScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegimeComponentScoreRepository extends JpaRepository<RegimeComponentScoreEntity, Long> {

    List<RegimeComponentScoreEntity> findByMarketRegimeId(Long regimeId);
}
