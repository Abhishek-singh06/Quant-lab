package com.quantlab.features.repository;

import com.quantlab.features.entity.FeatureDefinition;
import com.quantlab.features.model.FeatureCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureDefinitionRepository extends JpaRepository<FeatureDefinition, Long> {

    Optional<FeatureDefinition> findByFeatureName(String featureName);

    List<FeatureDefinition> findByIsEnabledTrue();

    List<FeatureDefinition> findByCategoryAndIsEnabledTrue(FeatureCategory category);
}
