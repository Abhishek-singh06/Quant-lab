package com.quantlab.monitoring.repository;

import com.quantlab.monitoring.entity.ProviderHealthEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderHealthRepository extends JpaRepository<ProviderHealthEntity, UUID> {
    Optional<ProviderHealthEntity> findByProvider(String provider);
    List<ProviderHealthEntity> findAllByOrderByUpdatedAtDesc();
    List<ProviderHealthEntity> findByConnectionStatus(String connectionStatus);
}
