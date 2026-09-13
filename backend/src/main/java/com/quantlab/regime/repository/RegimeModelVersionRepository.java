package com.quantlab.regime.repository;

import com.quantlab.regime.entity.RegimeModelVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegimeModelVersionRepository extends JpaRepository<RegimeModelVersionEntity, Long> {

    Optional<RegimeModelVersionEntity> findByModelVersion(String modelVersion);

    Optional<RegimeModelVersionEntity> findFirstByIsActiveTrueOrderByCreatedAtDesc();
}
