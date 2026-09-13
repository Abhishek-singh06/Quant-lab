package com.quantlab.marketdata.repository;

import com.quantlab.marketdata.entity.MarketDataRecord;
import com.quantlab.marketdata.model.Exchange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketDataRecordRepository extends JpaRepository<MarketDataRecord, Long> {

    Optional<MarketDataRecord> findBySymbolAndExchangeAndTimestampAndDataType(
        String symbol, Exchange exchange, Instant timestamp, String dataType
    );

    boolean existsBySymbolAndExchangeAndTimestampAndDataType(
        String symbol, Exchange exchange, Instant timestamp, String dataType
    );

    Optional<MarketDataRecord> findFirstBySymbolAndExchangeOrderByTimestampDesc(
        String symbol, Exchange exchange
    );

    default Optional<MarketDataRecord> findLatestBySymbolAndExchange(String symbol, Exchange exchange) {
        return findFirstBySymbolAndExchangeOrderByTimestampDesc(symbol, exchange);
    }

    @Query("SELECT r FROM MarketDataRecord r WHERE r.symbol IN :symbols AND r.exchange = :exchange ORDER BY r.timestamp DESC")
    List<MarketDataRecord> findLatestBySymbolsAndExchange(
        @Param("symbols") List<String> symbols,
        @Param("exchange") Exchange exchange
    );

    @Query("SELECT r FROM MarketDataRecord r WHERE r.symbol = :symbol AND r.exchange = :exchange AND r.timestamp BETWEEN :from AND :to ORDER BY r.timestamp ASC")
    List<MarketDataRecord> findRange(
        @Param("symbol") String symbol,
        @Param("exchange") Exchange exchange,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    Page<MarketDataRecord> findByRunId(String runId, Pageable pageable);
}
