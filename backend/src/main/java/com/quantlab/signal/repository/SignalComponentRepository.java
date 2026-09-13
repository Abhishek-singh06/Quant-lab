package com.quantlab.signal.repository;

import com.quantlab.signal.entity.SignalComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SignalComponentRepository extends JpaRepository<SignalComponentEntity, UUID> {
    List<SignalComponentEntity> findBySignalId(UUID signalId);
}
