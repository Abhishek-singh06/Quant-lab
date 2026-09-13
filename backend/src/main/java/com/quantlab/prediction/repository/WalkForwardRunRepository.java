package com.quantlab.prediction.repository;

import com.quantlab.prediction.entity.WalkForwardRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalkForwardRunRepository extends JpaRepository<WalkForwardRunEntity, Long> {

    Optional<WalkForwardRunEntity> findByRunId(String runId);
}
