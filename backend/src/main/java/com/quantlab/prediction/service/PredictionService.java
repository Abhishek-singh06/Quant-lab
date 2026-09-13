package com.quantlab.prediction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.prediction.entity.ModelPredictionEntity;
import com.quantlab.prediction.model.ModelPredictionDTO;
import com.quantlab.prediction.model.ModelType;
import com.quantlab.prediction.repository.ModelPredictionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);

    private final ModelPredictionRepository predictionRepository;
    private final ObjectMapper objectMapper;

    public PredictionService(ModelPredictionRepository predictionRepository, ObjectMapper objectMapper) {
        this.predictionRepository = predictionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ModelPredictionDTO savePrediction(ModelPredictionDTO dto) {
        ModelPredictionEntity entity = new ModelPredictionEntity();
        String predId = (dto.getPredictionId() != null && !dto.getPredictionId().isBlank())
                ? dto.getPredictionId() : "PRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        entity.setPredictionId(predId);
        entity.setModelId(dto.getModelId() != null ? dto.getModelId() : "MOD-GB-01");
        entity.setModelVersion(dto.getModelVersion() != null ? dto.getModelVersion() : "v1.0.0");
        entity.setSymbol(dto.getSymbol() != null ? dto.getSymbol() : "NIFTY 50");
        entity.setPredictionTimestamp(dto.getPredictionTimestamp() != null ? dto.getPredictionTimestamp() : Instant.now());
        entity.setTradingDate(dto.getTradingDate() != null ? dto.getTradingDate() : LocalDate.now());
        entity.setTargetHorizon(dto.getTargetHorizon() != null ? dto.getTargetHorizon() : "1D");
        entity.setPredictionType(dto.getPredictionType() != null ? dto.getPredictionType() : ModelType.REGRESSION);
        entity.setPredictedReturn(dto.getPredictedReturn());
        entity.setProbabilityPositive(dto.getProbabilityPositive());
        entity.setProbabilityNegative(dto.getProbabilityNegative());
        entity.setPredictedClass(dto.getPredictedClass());
        entity.setPredictedVolatility(dto.getPredictedVolatility());
        entity.setActualReturn(dto.getActualReturn());
        entity.setActualVolatility(dto.getActualVolatility());
        entity.setRegimeAtPrediction(dto.getRegimeAtPrediction() != null ? dto.getRegimeAtPrediction() : "BULL");
        entity.setInformationAvailableAt(dto.getInformationAvailableAt() != null ? dto.getInformationAvailableAt() : entity.getPredictionTimestamp());
        entity.setCalculatedAt(Instant.now());

        try {
            if (dto.getFeatureContributions() != null) {
                entity.setFeatureContributions(objectMapper.writeValueAsString(dto.getFeatureContributions()));
            }
        } catch (Exception e) {
            entity.setFeatureContributions("{}");
        }

        ModelPredictionEntity saved = predictionRepository.save(entity);
        return toDTO(saved);
    }

    public Optional<ModelPredictionDTO> getLatestPrediction(String symbol, Instant asOf) {
        String effSymbol = (symbol != null && !symbol.isBlank()) ? symbol : "NIFTY 50";
        Instant effAsOf = (asOf != null) ? asOf : Instant.now();

        Optional<ModelPredictionEntity> entityOpt = predictionRepository.findLatestPredictionAsOf(effSymbol, effAsOf);
        if (entityOpt.isPresent()) {
            return Optional.of(toDTO(entityOpt.get()));
        }

        // Return a default point-in-time prediction for demonstration if none stored yet
        ModelPredictionDTO fallback = new ModelPredictionDTO();
        fallback.setPredictionId("PRD-LATEST-DEMO");
        fallback.setModelId("MOD-GB-v1.0");
        fallback.setModelVersion("GB_1D_v1.0");
        fallback.setSymbol(effSymbol);
        fallback.setPredictionTimestamp(effAsOf);
        fallback.setTradingDate(LocalDate.now());
        fallback.setTargetHorizon("1D");
        fallback.setPredictionType(ModelType.REGRESSION);
        fallback.setPredictedReturn(new BigDecimal("0.0042"));
        fallback.setProbabilityPositive(new BigDecimal("0.6850"));
        fallback.setProbabilityNegative(new BigDecimal("0.3150"));
        fallback.setPredictedClass(1);
        fallback.setPredictedVolatility(new BigDecimal("0.1380"));
        fallback.setRegimeAtPrediction("BULL");
        fallback.setInformationAvailableAt(effAsOf);
        fallback.setCalculatedAt(Instant.now());
        fallback.setFeatureContributions(Map.of(
                "RSI_14", 0.0015,
                "MOMENTUM_20D", 0.0018,
                "MARKET_BREADTH", 0.0009,
                "INDIA_VIX", -0.0006,
                "FII_NET_FLOWS", 0.0006
        ));

        return Optional.of(fallback);
    }

    public List<ModelPredictionDTO> getPredictionsBySymbol(String symbol) {
        List<ModelPredictionEntity> list = predictionRepository.findBySymbolOrderByTradingDateDesc(symbol);
        List<ModelPredictionDTO> dtos = new ArrayList<>();
        for (ModelPredictionEntity e : list) {
            dtos.add(toDTO(e));
        }
        return dtos;
    }

    public List<ModelPredictionDTO> getCrossSectionByDate(LocalDate date, String modelVersion) {
        List<ModelPredictionEntity> list = predictionRepository.findCrossSectionByDate(
                date != null ? date : LocalDate.now(),
                modelVersion != null ? modelVersion : "GB_1D_v1.0"
        );
        List<ModelPredictionDTO> dtos = new ArrayList<>();
        for (ModelPredictionEntity e : list) {
            dtos.add(toDTO(e));
        }
        return dtos;
    }

    public ModelPredictionDTO toDTO(ModelPredictionEntity entity) {
        ModelPredictionDTO dto = new ModelPredictionDTO();
        dto.setPredictionId(entity.getPredictionId());
        dto.setModelId(entity.getModelId());
        dto.setModelVersion(entity.getModelVersion());
        dto.setSymbol(entity.getSymbol());
        dto.setPredictionTimestamp(entity.getPredictionTimestamp());
        dto.setTradingDate(entity.getTradingDate());
        dto.setTargetHorizon(entity.getTargetHorizon());
        dto.setPredictionType(entity.getPredictionType());
        dto.setPredictedReturn(entity.getPredictedReturn());
        dto.setProbabilityPositive(entity.getProbabilityPositive());
        dto.setProbabilityNegative(entity.getProbabilityNegative());
        dto.setPredictedClass(entity.getPredictedClass());
        dto.setPredictedVolatility(entity.getPredictedVolatility());
        dto.setActualReturn(entity.getActualReturn());
        dto.setActualVolatility(entity.getActualVolatility());
        dto.setRegimeAtPrediction(entity.getRegimeAtPrediction());
        dto.setInformationAvailableAt(entity.getInformationAvailableAt());
        dto.setCalculatedAt(entity.getCalculatedAt());

        try {
            if (entity.getFeatureContributions() != null) {
                dto.setFeatureContributions(objectMapper.readValue(entity.getFeatureContributions(), Map.class));
            }
        } catch (Exception e) {
            dto.setFeatureContributions(Map.of());
        }

        return dto;
    }
}
