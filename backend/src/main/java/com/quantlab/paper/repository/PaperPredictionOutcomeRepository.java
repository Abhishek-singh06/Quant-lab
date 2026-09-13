package com.quantlab.paper.repository;

import com.quantlab.paper.entity.PaperPredictionOutcomeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaperPredictionOutcomeRepository extends JpaRepository<PaperPredictionOutcomeEntity, UUID> {
    List<PaperPredictionOutcomeEntity> findByModelVersionOrderByPredictionTimestampDesc(String modelVersion);
    List<PaperPredictionOutcomeEntity> findBySymbolOrderByPredictionTimestampDesc(String symbol);
    List<PaperPredictionOutcomeEntity> findAllByOrderByPredictionTimestampDesc();
}
