package com.quantlab.signal.repository;

import com.quantlab.signal.entity.SignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SignalRepository extends JpaRepository<SignalEntity, UUID> {

    Optional<SignalEntity> findFirstBySymbolAndIsLatestTrueOrderBySignalTimestampDesc(String symbol);

    List<SignalEntity> findByIsLatestTrueOrderBySignalTimestampDesc();

    List<SignalEntity> findBySymbolOrderBySignalTimestampDesc(String symbol);

    Optional<SignalEntity> findFirstBySymbolAndInformationAvailableAtLessThanEqualOrderBySignalTimestampDesc(
        String symbol, Instant asOfTimestamp
    );

    default Optional<SignalEntity> findPointInTimeSignal(String symbol, Instant asOfTimestamp) {
        return findFirstBySymbolAndInformationAvailableAtLessThanEqualOrderBySignalTimestampDesc(symbol, asOfTimestamp);
    }
}
