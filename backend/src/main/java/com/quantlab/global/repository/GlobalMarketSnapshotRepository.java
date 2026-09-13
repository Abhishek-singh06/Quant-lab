package com.quantlab.global.repository;

import com.quantlab.global.entity.GlobalMarketSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalMarketSnapshotRepository extends JpaRepository<GlobalMarketSnapshot, Long> {

    Optional<GlobalMarketSnapshot> findByInstrumentIdAndTimestampAndSource(
        Long instrumentId, Instant timestamp, String source
    );

    Optional<GlobalMarketSnapshot> findFirstByCanonicalSymbolAndSourceTimestampLessThanEqualOrderBySourceTimestampDesc(
        String symbol, Instant asOfTime
    );

    default Optional<GlobalMarketSnapshot> findLatestSnapshotAsOf(String symbol, Instant asOfTime) {
        return findFirstByCanonicalSymbolAndSourceTimestampLessThanEqualOrderBySourceTimestampDesc(symbol, asOfTime);
    }

    @Query("SELECT s FROM GlobalMarketSnapshot s WHERE s.canonicalSymbol = :symbol AND s.tradingDate BETWEEN :fromDate AND :toDate AND s.sourceTimestamp <= :asOfTime ORDER BY s.tradingDate ASC")
    List<GlobalMarketSnapshot> findHistoryBySymbolInRangeAsOf(
        @Param("symbol") String symbol,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("asOfTime") Instant asOfTime
    );

    @Query("SELECT s FROM GlobalMarketSnapshot s WHERE s.sourceTimestamp <= :asOfTime ORDER BY s.sourceTimestamp DESC")
    List<GlobalMarketSnapshot> findAllSnapshotsAsOf(@Param("asOfTime") Instant asOfTime);
}
