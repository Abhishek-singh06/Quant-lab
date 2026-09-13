package com.quantlab.warehouse.repository;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.entity.HistoricalPriceRaw;
import com.quantlab.warehouse.model.TimeGranularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricalPriceRawRepository extends JpaRepository<HistoricalPriceRaw, Long> {

    Optional<HistoricalPriceRaw> findByInstrumentIdAndExchangeAndTradingDateAndGranularity(
        Long instrumentId, Exchange exchange, LocalDate tradingDate, TimeGranularity granularity
    );

    boolean existsByInstrumentIdAndExchangeAndTradingDateAndGranularity(
        Long instrumentId, Exchange exchange, LocalDate tradingDate, TimeGranularity granularity
    );

    @Query("SELECT p FROM HistoricalPriceRaw p WHERE p.instrumentId = :instrumentId AND p.exchange = :exchange AND p.granularity = :granularity AND p.tradingDate BETWEEN :from AND :to ORDER BY p.tradingDate ASC")
    List<HistoricalPriceRaw> findPricesInRange(
        @Param("instrumentId") Long instrumentId,
        @Param("exchange") Exchange exchange,
        @Param("granularity") TimeGranularity granularity,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("SELECT p FROM HistoricalPriceRaw p WHERE p.symbol = :symbol AND p.exchange = :exchange AND p.granularity = :granularity AND p.tradingDate BETWEEN :from AND :to ORDER BY p.tradingDate ASC")
    List<HistoricalPriceRaw> findPricesBySymbolInRange(
        @Param("symbol") String symbol,
        @Param("exchange") Exchange exchange,
        @Param("granularity") TimeGranularity granularity,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("SELECT p.tradingDate FROM HistoricalPriceRaw p WHERE p.instrumentId = :instrumentId AND p.tradingDate BETWEEN :from AND :to ORDER BY p.tradingDate ASC")
    List<LocalDate> findTradingDatesInRange(
        @Param("instrumentId") Long instrumentId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    long countByInstrumentIdAndTradingDateBetween(Long instrumentId, LocalDate from, LocalDate to);
}
