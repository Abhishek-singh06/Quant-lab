package com.quantlab.marketdata.repository;

import com.quantlab.marketdata.entity.MarketDataIngestionRun;
import com.quantlab.marketdata.model.IngestionRunStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketDataIngestionRunRepository extends JpaRepository<MarketDataIngestionRun, Long> {

    Optional<MarketDataIngestionRun> findByRunId(String runId);

    List<MarketDataIngestionRun> findByStatus(IngestionRunStatus status);

    Page<MarketDataIngestionRun> findByProviderOrderByStartTimeDesc(String provider, Pageable pageable);

    Page<MarketDataIngestionRun> findAllByOrderByStartTimeDesc(Pageable pageable);

    List<MarketDataIngestionRun> findByStartTimeBetweenOrderByStartTimeDesc(Instant from, Instant to);
}
