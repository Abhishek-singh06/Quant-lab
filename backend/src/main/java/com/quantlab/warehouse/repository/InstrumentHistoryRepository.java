package com.quantlab.warehouse.repository;

import com.quantlab.warehouse.entity.InstrumentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InstrumentHistoryRepository extends JpaRepository<InstrumentHistory, Long> {

    List<InstrumentHistory> findByInstrumentIdOrderByValidFromAsc(Long instrumentId);

    @Query("SELECT h FROM InstrumentHistory h WHERE h.symbol = :symbol AND h.validFrom <= :asOfDate AND (h.validTo IS NULL OR h.validTo >= :asOfDate)")
    Optional<InstrumentHistory> findSymbolAtDate(@Param("symbol") String symbol, @Param("asOfDate") LocalDate asOfDate);

    @Query("SELECT h FROM InstrumentHistory h WHERE h.instrumentId = :instrumentId AND h.validFrom <= :asOfDate AND (h.validTo IS NULL OR h.validTo >= :asOfDate)")
    Optional<InstrumentHistory> findInstrumentAtDate(@Param("instrumentId") Long instrumentId, @Param("asOfDate") LocalDate asOfDate);
}
