package com.quantlab.signal.repository;

import com.quantlab.signal.entity.SignalConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SignalConfigurationRepository extends JpaRepository<SignalConfigurationEntity, UUID> {
    Optional<SignalConfigurationEntity> findFirstByIsActiveTrueOrderByCreatedAtDesc();
    Optional<SignalConfigurationEntity> findByVersion(String version);
}
