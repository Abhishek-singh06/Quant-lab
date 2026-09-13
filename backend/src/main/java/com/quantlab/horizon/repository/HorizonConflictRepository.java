package com.quantlab.horizon.repository;

import com.quantlab.horizon.entity.HorizonConflictEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HorizonConflictRepository extends JpaRepository<HorizonConflictEntity, UUID> {
    List<HorizonConflictEntity> findBySymbolOrderByTimestampDesc(String symbol);
    Optional<HorizonConflictEntity> findFirstBySymbolOrderByTimestampDesc(String symbol);
}
