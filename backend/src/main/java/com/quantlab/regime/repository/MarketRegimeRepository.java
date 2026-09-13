package com.quantlab.regime.repository;

import com.quantlab.regime.entity.MarketRegimeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketRegimeRepository extends JpaRepository<MarketRegimeEntity, Long> {

    Optional<MarketRegimeEntity> findBySymbolAndRegimeTimestampAndModelVersion(
            String symbol, Instant regimeTimestamp, String modelVersion
    );

    Optional<MarketRegimeEntity> findFirstBySymbolAndInformationAvailableAtLessThanEqualOrderByRegimeTimestampDesc(
            String symbol, Instant asOf
    );

    default Optional<MarketRegimeEntity> findLatestRegimeAsOf(String symbol, Instant asOf) {
        return findFirstBySymbolAndInformationAvailableAtLessThanEqualOrderByRegimeTimestampDesc(symbol, asOf);
    }

    @Query("SELECT r FROM MarketRegimeEntity r WHERE r.symbol = :symbol AND r.tradingDate BETWEEN :from AND :to AND r.informationAvailableAt <= :asOf ORDER BY r.tradingDate ASC")
    List<MarketRegimeEntity> findRegimeHistoryInRange(
            @Param("symbol") String symbol,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("asOf") Instant asOf
    );

    List<MarketRegimeEntity> findBySymbolOrderByTradingDateDesc(String symbol, org.springframework.data.domain.Pageable pageable);

    default List<MarketRegimeEntity> findRecentRegimes(String symbol, int limit) {
        return findBySymbolOrderByTradingDateDesc(symbol, org.springframework.data.domain.PageRequest.of(0, limit));
    }
}
