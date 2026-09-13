package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperSignalOutcomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaperSignalOutcomeRepository extends JpaRepository<PaperSignalOutcomeEntity, UUID> {
    List<PaperSignalOutcomeEntity> findBySymbolOrderBySignalTimestampDesc(String symbol);
    List<PaperSignalOutcomeEntity> findAllByOrderBySignalTimestampDesc();
}
