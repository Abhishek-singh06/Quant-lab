package com.quantlab.risk.repository;

import com.quantlab.risk.entity.RiskProfileEntity;
import com.quantlab.risk.model.RiskProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiskProfileRepository extends JpaRepository<RiskProfileEntity, UUID> {
    Optional<RiskProfileEntity> findByName(String name);
    List<RiskProfileEntity> findByProfileType(RiskProfileType profileType);
    List<RiskProfileEntity> findByActiveTrue();
}
