package com.quantlab.regime.repository;

import com.quantlab.regime.entity.RegimeEvaluationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegimeEvaluationRunRepository extends JpaRepository<RegimeEvaluationRunEntity, Long> {

    Optional<RegimeEvaluationRunEntity> findByRunId(String runId);

    List<RegimeEvaluationRunEntity> findTop20ByOrderByCreatedAtDesc();

    Optional<RegimeEvaluationRunEntity> findFirstByOrderByCreatedAtDesc();
}
