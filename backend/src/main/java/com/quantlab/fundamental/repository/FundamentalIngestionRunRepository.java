package com.quantlab.fundamental.repository;

import com.quantlab.fundamental.entity.FundamentalIngestionRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundamentalIngestionRunRepository extends JpaRepository<FundamentalIngestionRun, Long> {
    Optional<FundamentalIngestionRun> findByRunUuid(String runUuid);
    java.util.List<FundamentalIngestionRun> findTop20ByOrderByStartTimeDesc();
}
