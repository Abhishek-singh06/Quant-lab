package com.quantlab.features.repository;

import com.quantlab.features.entity.TechnicalFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TechnicalFeatureRepository extends JpaRepository<TechnicalFeature, Long> {

    Optional<TechnicalFeature> findByInstrumentIdAndFeatureNameAndFeatureTimestampAndTimeframeAndFeatureVersion(
            Long instrumentId, String featureName, Instant featureTimestamp, String timeframe, String featureVersion
    );

    @Query("SELECT tf FROM TechnicalFeature tf WHERE tf.instrumentId = :instrumentId AND tf.timeframe = :timeframe AND tf.informationAvailableAt <= :asOf ORDER BY tf.featureTimestamp DESC")
    List<TechnicalFeature> findFeaturesAvailableAt(
            @Param("instrumentId") Long instrumentId,
            @Param("timeframe") String timeframe,
            @Param("asOf") Instant asOf
    );

    @Query("SELECT tf FROM TechnicalFeature tf WHERE tf.symbol = :symbol AND tf.timeframe = :timeframe AND tf.informationAvailableAt <= :asOf ORDER BY tf.featureTimestamp DESC")
    List<TechnicalFeature> findFeaturesBySymbolAvailableAt(
            @Param("symbol") String symbol,
            @Param("timeframe") String timeframe,
            @Param("asOf") Instant asOf
    );

    @Query("SELECT tf FROM TechnicalFeature tf WHERE tf.instrumentId = :instrumentId AND tf.featureName = :featureName AND tf.timeframe = :timeframe AND tf.tradingDate BETWEEN :fromDate AND :toDate ORDER BY tf.tradingDate ASC")
    List<TechnicalFeature> findFeatureTimeSeries(
            @Param("instrumentId") Long instrumentId,
            @Param("featureName") String featureName,
            @Param("timeframe") String timeframe,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    @Query("SELECT tf FROM TechnicalFeature tf WHERE tf.symbol = :symbol AND tf.timeframe = :timeframe AND tf.tradingDate = (SELECT MAX(tf2.tradingDate) FROM TechnicalFeature tf2 WHERE tf2.symbol = :symbol AND tf2.timeframe = :timeframe)")
    List<TechnicalFeature> findLatestFeaturesBySymbol(
            @Param("symbol") String symbol,
            @Param("timeframe") String timeframe
    );

    @Query("SELECT DISTINCT tf.tradingDate FROM TechnicalFeature tf WHERE tf.instrumentId = :instrumentId AND tf.timeframe = :timeframe AND tf.tradingDate BETWEEN :fromDate AND :toDate")
    List<LocalDate> findDistinctCalculatedDates(
            @Param("instrumentId") Long instrumentId,
            @Param("timeframe") String timeframe,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
