package com.quantlab.warehouse.repository;

import com.quantlab.warehouse.entity.HistoricalIngestionRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistoricalIngestionRunRepository extends JpaRepository<HistoricalIngestionRun, Long> {

    Optional<HistoricalIngestionRun> findByRunId(String runId);

    Page<HistoricalIngestionRun> findAllByOrderByStartTimeDesc(Pageable pageable);
}
