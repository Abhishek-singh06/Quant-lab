package com.quantlab.prediction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.prediction.entity.QuantModelEntity;
import com.quantlab.prediction.model.ModelStatus;
import com.quantlab.prediction.model.ModelType;
import com.quantlab.prediction.model.QuantModelDTO;
import com.quantlab.prediction.repository.QuantModelRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class ModelRegistryService {

    private static final Logger log = LoggerFactory.getLogger(ModelRegistryService.class);

    private final QuantModelRepository modelRepository;
    private final ObjectMapper objectMapper;

    public ModelRegistryService(QuantModelRepository modelRepository, ObjectMapper objectMapper) {
        this.modelRepository = modelRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public QuantModelDTO registerModel(QuantModelDTO dto) {
        QuantModelEntity entity = new QuantModelEntity();
        String modelId = (dto.getModelId() != null && !dto.getModelId().isBlank())
                ? dto.getModelId() : "MOD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        entity.setModelId(modelId);
        entity.setModelName(dto.getModelName());
        entity.setModelType(dto.getModelType() != null ? dto.getModelType() : ModelType.REGRESSION);
        entity.setAlgorithm(dto.getAlgorithm() != null ? dto.getAlgorithm() : "GRADIENT_BOOSTING");
        entity.setModelVersion(dto.getModelVersion() != null ? dto.getModelVersion() : "v1.0.0");
        entity.setFeatureVersion(dto.getFeatureVersion() != null ? dto.getFeatureVersion() : "1.0.0");
        entity.setDatasetVersion(dto.getDatasetVersion() != null ? dto.getDatasetVersion() : "DS-v1.0");
        entity.setTargetDefinition(dto.getTargetDefinition() != null ? dto.getTargetDefinition() : "future_return_1d");
        entity.setTargetHorizon(dto.getTargetHorizon() != null ? dto.getTargetHorizon() : "1D");
        entity.setTrainingStart(dto.getTrainingStart() != null ? dto.getTrainingStart() : LocalDate.of(2018, 1, 1));
        entity.setTrainingEnd(dto.getTrainingEnd() != null ? dto.getTrainingEnd() : LocalDate.of(2022, 12, 31));
        entity.setValidationStart(dto.getValidationStart() != null ? dto.getValidationStart() : LocalDate.of(2023, 1, 1));
        entity.setValidationEnd(dto.getValidationEnd() != null ? dto.getValidationEnd() : LocalDate.of(2023, 12, 31));
        entity.setTestStart(dto.getTestStart());
        entity.setTestEnd(dto.getTestEnd());

        try {
            entity.setHyperparameters(objectMapper.writeValueAsString(dto.getHyperparameters() != null ? dto.getHyperparameters() : Map.of()));
            entity.setMetrics(objectMapper.writeValueAsString(dto.getMetrics() != null ? dto.getMetrics() : Map.of()));
            if (dto.getFeatureImportance() != null) {
                entity.setFeatureImportance(objectMapper.writeValueAsString(dto.getFeatureImportance()));
            }
        } catch (Exception e) {
            entity.setHyperparameters("{}");
            entity.setMetrics("{}");
        }

        entity.setArtifactPath(dto.getArtifactPath());
        entity.setArtifactChecksum(dto.getArtifactChecksum());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : ModelStatus.VALIDATED);
        entity.setRandomSeed(dto.getRandomSeed() > 0 ? dto.getRandomSeed() : 42);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        QuantModelEntity saved = modelRepository.save(entity);
        log.info("Registered model: id={}, version={}, type={}, status={}", saved.getModelId(), saved.getModelVersion(), saved.getModelType(), saved.getStatus());
        return toDTO(saved);
    }

    public List<QuantModelDTO> listModels(ModelType type, ModelStatus status) {
        List<QuantModelEntity> list;
        if (type != null && status != null) {
            list = modelRepository.findByModelTypeAndStatus(type, status);
        } else if (type != null) {
            list = modelRepository.findByModelType(type);
        } else if (status != null) {
            list = modelRepository.findByStatus(status);
        } else {
            list = modelRepository.findAll();
        }

        List<QuantModelDTO> dtos = new ArrayList<>();
        for (QuantModelEntity e : list) {
            dtos.add(toDTO(e));
        }
        return dtos;
    }

    public Optional<QuantModelDTO> getModelById(String modelId) {
        return modelRepository.findByModelId(modelId).map(this::toDTO);
    }

    public Optional<QuantModelDTO> getModelByVersion(String modelVersion) {
        return modelRepository.findByModelVersion(modelVersion).map(this::toDTO);
    }

    public QuantModelDTO toDTO(QuantModelEntity entity) {
        QuantModelDTO dto = new QuantModelDTO();
        dto.setModelId(entity.getModelId());
        dto.setModelName(entity.getModelName());
        dto.setModelType(entity.getModelType());
        dto.setAlgorithm(entity.getAlgorithm());
        dto.setModelVersion(entity.getModelVersion());
        dto.setFeatureVersion(entity.getFeatureVersion());
        dto.setDatasetVersion(entity.getDatasetVersion());
        dto.setTargetDefinition(entity.getTargetDefinition());
        dto.setTargetHorizon(entity.getTargetHorizon());
        dto.setTrainingStart(entity.getTrainingStart());
        dto.setTrainingEnd(entity.getTrainingEnd());
        dto.setValidationStart(entity.getValidationStart());
        dto.setValidationEnd(entity.getValidationEnd());
        dto.setTestStart(entity.getTestStart());
        dto.setTestEnd(entity.getTestEnd());
        dto.setArtifactPath(entity.getArtifactPath());
        dto.setArtifactChecksum(entity.getArtifactChecksum());
        dto.setStatus(entity.getStatus());
        dto.setRandomSeed(entity.getRandomSeed());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        try {
            if (entity.getHyperparameters() != null) {
                dto.setHyperparameters(objectMapper.readValue(entity.getHyperparameters(), Map.class));
            }
            if (entity.getMetrics() != null) {
                dto.setMetrics(objectMapper.readValue(entity.getMetrics(), Map.class));
            }
            if (entity.getFeatureImportance() != null) {
                dto.setFeatureImportance(objectMapper.readValue(entity.getFeatureImportance(), Map.class));
            }
        } catch (Exception e) {
            log.warn("Error deserializing model json: {}", e.getMessage());
        }

        return dto;
    }
}
