package com.quantlab.prediction.repository;

import com.quantlab.prediction.entity.QuantModelEntity;
import com.quantlab.prediction.model.ModelStatus;
import com.quantlab.prediction.model.ModelType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuantModelRepository extends JpaRepository<QuantModelEntity, Long> {

    Optional<QuantModelEntity> findByModelId(String modelId);

    Optional<QuantModelEntity> findByModelVersion(String modelVersion);

    List<QuantModelEntity> findByModelType(ModelType modelType);

    List<QuantModelEntity> findByStatus(ModelStatus status);

    List<QuantModelEntity> findByModelTypeAndStatus(ModelType modelType, ModelStatus status);
}
