package com.quantlab.global.repository;

import com.quantlab.global.entity.GlobalMarketRegime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GlobalMarketRegimeRepository extends JpaRepository<GlobalMarketRegime, Long> {

    Optional<GlobalMarketRegime> findByTimestamp(Instant timestamp);

    Optional<GlobalMarketRegime> findFirstByTimestampLessThanEqualOrderByTimestampDesc(Instant asOfTime);

    default Optional<GlobalMarketRegime> findLatestRegimeAsOf(Instant asOfTime) {
        return findFirstByTimestampLessThanEqualOrderByTimestampDesc(asOfTime);
    }

    @Query("SELECT r FROM GlobalMarketRegime r WHERE r.timestamp BETWEEN :fromTime AND :toTime ORDER BY r.timestamp ASC")
    List<GlobalMarketRegime> findRegimesInRange(
        @Param("fromTime") Instant fromTime,
        @Param("toTime") Instant toTime
    );
}
