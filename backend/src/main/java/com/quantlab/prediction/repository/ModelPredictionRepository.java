package com.quantlab.prediction.repository;

import com.quantlab.prediction.entity.ModelPredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ModelPredictionRepository extends JpaRepository<ModelPredictionEntity, Long> {

    Optional<ModelPredictionEntity> findByPredictionId(String predictionId);

    List<ModelPredictionEntity> findBySymbolOrderByTradingDateDesc(String symbol);

    Optional<ModelPredictionEntity> findFirstBySymbolAndInformationAvailableAtLessThanEqualOrderByTradingDateDesc(
            String symbol, Instant asOf
    );

    default Optional<ModelPredictionEntity> findLatestPredictionAsOf(String symbol, Instant asOf) {
        return findFirstBySymbolAndInformationAvailableAtLessThanEqualOrderByTradingDateDesc(symbol, asOf);
    }

    @Query("SELECT p FROM ModelPredictionEntity p WHERE p.tradingDate = :date AND p.modelVersion = :version")
    List<ModelPredictionEntity> findCrossSectionByDate(
            @Param("date") LocalDate date,
            @Param("version") String version
    );
}
