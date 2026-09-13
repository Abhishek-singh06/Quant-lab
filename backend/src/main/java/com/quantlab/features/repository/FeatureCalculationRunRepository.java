package com.quantlab.features.repository;

import com.quantlab.features.entity.FeatureCalculationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureCalculationRunRepository extends JpaRepository<FeatureCalculationRun, Long> {

    Optional<FeatureCalculationRun> findByRunId(String runId);

    List<FeatureCalculationRun> findTop20ByOrderByStartTimeDesc();
}
