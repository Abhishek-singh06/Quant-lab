package com.quantlab.features.repository;

import com.quantlab.features.entity.FeatureDataQuality;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureDataQualityRepository extends JpaRepository<FeatureDataQuality, Long> {

    List<FeatureDataQuality> findByRunId(String runId);

    Page<FeatureDataQuality> findAllByOrderByCheckedAtDesc(Pageable pageable);
}
