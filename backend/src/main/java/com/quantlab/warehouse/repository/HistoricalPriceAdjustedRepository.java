package com.quantlab.warehouse.repository;

import com.quantlab.marketdata.model.Exchange;
import com.quantlab.warehouse.entity.HistoricalPriceAdjusted;
import com.quantlab.warehouse.model.AdjustmentMethodology;
import com.quantlab.warehouse.model.TimeGranularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricalPriceAdjustedRepository extends JpaRepository<HistoricalPriceAdjusted, Long> {

    Optional<HistoricalPriceAdjusted> findByInstrumentIdAndExchangeAndTradingDateAndGranularityAndMethodology(
        Long instrumentId, Exchange exchange, LocalDate tradingDate, TimeGranularity granularity, AdjustmentMethodology methodology
    );

    @Query("SELECT p FROM HistoricalPriceAdjusted p WHERE p.instrumentId = :instrumentId AND p.exchange = :exchange AND p.methodology = :methodology AND p.granularity = :granularity AND p.tradingDate BETWEEN :from AND :to ORDER BY p.tradingDate ASC")
    List<HistoricalPriceAdjusted> findAdjustedPricesInRange(
        @Param("instrumentId") Long instrumentId,
        @Param("exchange") Exchange exchange,
        @Param("methodology") AdjustmentMethodology methodology,
        @Param("granularity") TimeGranularity granularity,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("SELECT p FROM HistoricalPriceAdjusted p WHERE p.symbol = :symbol AND p.exchange = :exchange AND p.methodology = :methodology AND p.granularity = :granularity AND p.tradingDate BETWEEN :from AND :to ORDER BY p.tradingDate ASC")
    List<HistoricalPriceAdjusted> findAdjustedPricesBySymbolInRange(
        @Param("symbol") String symbol,
        @Param("exchange") Exchange exchange,
        @Param("methodology") AdjustmentMethodology methodology,
        @Param("granularity") TimeGranularity granularity,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}
