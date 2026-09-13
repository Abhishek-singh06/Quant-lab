package com.quantlab.signal.repository;

import com.quantlab.signal.entity.SignalEvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignalEvidenceRepository extends JpaRepository<SignalEvidenceEntity, UUID> {
    List<SignalEvidenceEntity> findBySignalId(UUID signalId);
}
