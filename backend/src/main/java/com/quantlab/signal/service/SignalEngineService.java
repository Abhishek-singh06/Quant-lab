package com.quantlab.signal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.signal.entity.*;
import com.quantlab.signal.model.*;
import com.quantlab.signal.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SignalEngineService {

    private static final Logger log = LoggerFactory.getLogger(SignalEngineService.class);

    private final SignalRepository signalRepository;
    private final SignalComponentRepository componentRepository;
    private final SignalEvidenceRepository evidenceRepository;
    private final SignalTransitionRepository transitionRepository;
    private final SignalConfigurationRepository configRepository;
    private final SignalGenerationRunRepository runRepository;
    private final ObjectMapper objectMapper;

    public SignalEngineService(
        SignalRepository signalRepository,
        SignalComponentRepository componentRepository,
        SignalEvidenceRepository evidenceRepository,
        SignalTransitionRepository transitionRepository,
        SignalConfigurationRepository configRepository,
        SignalGenerationRunRepository runRepository,
        ObjectMapper objectMapper
    ) {
        this.signalRepository = signalRepository;
        this.componentRepository = componentRepository;
        this.evidenceRepository = evidenceRepository;
        this.transitionRepository = transitionRepository;
        this.configRepository = configRepository;
        this.runRepository = runRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<SignalDTO> getLatestSignal(String symbol) {
        return signalRepository.findFirstBySymbolAndIsLatestTrueOrderBySignalTimestampDesc(symbol)
            .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Optional<SignalDTO> getPointInTimeSignal(String symbol, Instant asOfTimestamp) {
        return signalRepository.findPointInTimeSignal(symbol, asOfTimestamp)
            .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<SignalDTO> getAllCurrentSignals() {
        return signalRepository.findByIsLatestTrueOrderBySignalTimestampDesc().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SignalDTO> getSignalHistory(String symbol) {
        return signalRepository.findBySymbolOrderBySignalTimestampDesc(symbol).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SignalEvidenceDTO> getSignalEvidence(UUID signalId) {
        return evidenceRepository.findBySignalId(signalId).stream()
            .map(this::toEvidenceDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SignalComponentDTO> getSignalComponents(UUID signalId) {
        return componentRepository.findBySignalId(signalId).stream()
            .map(this::toComponentDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SignalTransitionDTO> getSignalTransitions(String symbol) {
        return transitionRepository.findBySymbolOrderByTransitionTimestampDesc(symbol).stream()
            .map(this::toTransitionDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<SignalConfigurationDTO> getActiveConfiguration() {
        return configRepository.findFirstByIsActiveTrueOrderByCreatedAtDesc()
            .map(this::toConfigDTO);
    }

    private SignalDTO toDTO(SignalEntity entity) {
        List<Map<String, Object>> structuredReasoning = parseJsonList(entity.getStructuredReasoning());
        List<Map<String, Object>> supportingEvidence = parseJsonList(entity.getSupportingCategories());
        List<Map<String, Object>> opposingEvidence = parseJsonList(entity.getOpposingCategories());

        Map<String, SignalComponentDTO> compMap = componentRepository.findBySignalId(entity.getId()).stream()
            .collect(Collectors.toMap(c -> c.getCategory().name(), this::toComponentDTO));

        return new SignalDTO(
            entity.getId(),
            entity.getRunId(),
            entity.getInstrumentId(),
            entity.getSymbol(),
            entity.getSignalTimestamp(),
            entity.getInformationAvailableAt(),
            entity.getCalculatedAt(),
            entity.getSignal(),
            entity.getSignalScore(),
            entity.getConfidence(),
            entity.getExpectedReturn(),
            entity.getExpectedVolatility(),
            entity.getReturnToVolatilityRatio(),
            entity.getDirection(),
            entity.getConflictSeverity(),
            entity.getConflictScore(),
            entity.getDataQualityStatus(),
            entity.getFreshnessScore(),
            entity.getReasoning(),
            structuredReasoning,
            supportingEvidence,
            opposingEvidence,
            compMap,
            entity.getSignalVersion(),
            entity.getConfigurationVersion(),
            entity.getFeatureVersion(),
            entity.getModelVersion(),
            entity.getRegimeVersion(),
            entity.getDataVersion(),
            entity.getIsLatest()
        );
    }

    private SignalComponentDTO toComponentDTO(SignalComponentEntity entity) {
        return new SignalComponentDTO(
            entity.getId(),
            entity.getSignalId(),
            entity.getCategory(),
            entity.getCategoryScore(),
            entity.getWeight(),
            entity.getWeightedContribution(),
            entity.getDirection(),
            entity.getStrength(),
            entity.getQuality(),
            entity.getFreshness(),
            entity.getIsPresent(),
            entity.getMissingReason()
        );
    }

    private SignalEvidenceDTO toEvidenceDTO(SignalEvidenceEntity entity) {
        return new SignalEvidenceDTO(
            entity.getId(),
            entity.getSignalId(),
            entity.getCategory(),
            entity.getFeatureName(),
            entity.getRawValue(),
            entity.getRawValueStr(),
            entity.getNormalizedScore(),
            entity.getDirection(),
            entity.getStrength(),
            entity.getQuality(),
            entity.getFreshness(),
            entity.getConfidence(),
            entity.getWeight(),
            entity.getContribution(),
            entity.getSource(),
            entity.getSourceTimestamp(),
            entity.getAvailableAt(),
            entity.getVersion(),
            entity.getReason()
        );
    }

    private SignalTransitionDTO toTransitionDTO(SignalTransitionEntity entity) {
        return new SignalTransitionDTO(
            entity.getId(),
            entity.getInstrumentId(),
            entity.getSymbol(),
            entity.getPreviousSignal(),
            entity.getNewSignal(),
            entity.getPreviousScore(),
            entity.getNewScore(),
            entity.getTransitionTimestamp(),
            entity.getTransitionReason(),
            entity.getSignalId()
        );
    }

    private SignalConfigurationDTO toConfigDTO(SignalConfigurationEntity entity) {
        Map<String, Double> weights = parseJsonMap(entity.getWeights());
        Map<String, Double> caps = parseJsonMap(entity.getCategoryCaps());
        Map<String, List<String>> groups = parseJsonListMap(entity.getCorrelationGroups());

        return new SignalConfigurationDTO(
            entity.getId(),
            entity.getVersion(),
            entity.getMinSupportingCategories(),
            entity.getBuyThreshold(),
            entity.getSellThreshold(),
            entity.getMinConfidence(),
            weights,
            caps,
            groups,
            entity.getIsActive(),
            entity.getCreatedAt()
        );
    }

    private List<Map<String, Object>> parseJsonList(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON list: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private Map<String, Double> parseJsonMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, List<String>> parseJsonListMap(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON list map: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}
