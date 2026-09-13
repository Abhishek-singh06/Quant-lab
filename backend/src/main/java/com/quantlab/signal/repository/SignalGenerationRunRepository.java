package com.quantlab.signal.repository;

import com.quantlab.signal.entity.SignalGenerationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignalGenerationRunRepository extends JpaRepository<SignalGenerationRunEntity, UUID> {
    List<SignalGenerationRunEntity> findAllByOrderByRunTimestampDesc();
}
