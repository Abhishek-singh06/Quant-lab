package com.quantlab.regime.service;

import com.quantlab.regime.entity.MarketRegimeEntity;
import com.quantlab.regime.entity.RegimeComponentScoreEntity;
import com.quantlab.regime.model.*;
import com.quantlab.regime.repository.MarketRegimeRepository;
import com.quantlab.regime.repository.RegimeComponentScoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MarketRegimeEngineService {

    private static final Logger log = LoggerFactory.getLogger(MarketRegimeEngineService.class);

    private final RegimeSignalExtractorService extractorService;
    private final RegimeNormalizationService normalizationService;
    private final RegimeClassificationModel classificationModel;
    private final MarketRegimeRepository regimeRepository;
    private final RegimeComponentScoreRepository componentRepository;

    public MarketRegimeEngineService(
            RegimeSignalExtractorService extractorService,
            RegimeNormalizationService normalizationService,
            RegimeClassificationModel classificationModel,
            MarketRegimeRepository regimeRepository,
            RegimeComponentScoreRepository componentRepository) {
        this.extractorService = extractorService;
        this.normalizationService = normalizationService;
        this.classificationModel = classificationModel;
        this.regimeRepository = regimeRepository;
        this.componentRepository = componentRepository;
    }

    @Transactional
    public MarketRegimeDTO calculateAndSaveRegime(String symbol, LocalDate tradingDate, Instant asOfTimestamp) {
        String effSymbol = (symbol != null && !symbol.isBlank()) ? symbol : "NIFTY 50";
        LocalDate effDate = (tradingDate != null) ? tradingDate : LocalDate.now();
        Instant effAsOf = (asOfTimestamp != null) ? asOfTimestamp : Instant.now();

        // 1. Extract raw signals
        RegimeSignalExtractorService.RawSignalsBundle bundle =
                extractorService.extractSignalsAsOf(effSymbol, effDate, effAsOf);

        // 2. Normalize and calibrate weights
        List<RegimeComponentScoreDTO> normalizedComponents =
                normalizationService.normalizeAndWeightComponents(bundle.getExtractedComponents());

        // 3. Find previous regime to track continuity / tenure
        Optional<MarketRegimeEntity> prevOpt = regimeRepository.findLatestRegimeAsOf(effSymbol, effAsOf);
        DirectionRegime prevDir = prevOpt.map(MarketRegimeEntity::getDirectionRegime).orElse(null);
        int prevDays = prevOpt.map(MarketRegimeEntity::getDaysInRegime).orElse(0);

        // 4. Classify regime
        MarketRegimeDTO dto = classificationModel.classify(
                effSymbol, effDate, effAsOf, normalizedComponents, prevDir, prevDays
        );

        // 5. Persist
        MarketRegimeEntity entity = new MarketRegimeEntity();
        entity.setSymbol(effSymbol);
        entity.setRegimeTimestamp(dto.getRegimeTimestamp());
        entity.setTradingDate(effDate);
        entity.setDirectionRegime(dto.getDirectionRegime());
        entity.setVolatilityRegime(dto.getVolatilityRegime());
        entity.setRiskRegime(dto.getRiskRegime());
        entity.setDirectionScore(dto.getDirectionScore());
        entity.setVolatilityScore(dto.getVolatilityScore());
        entity.setRiskScore(dto.getRiskScore());
        entity.setConfidence(dto.getConfidence());
        entity.setProbBull(dto.getProbBull());
        entity.setProbBear(dto.getProbBear());
        entity.setProbSideways(dto.getProbSideways());
        entity.setProbRiskOn(dto.getProbRiskOn());
        entity.setProbRiskOff(dto.getProbRiskOff());
        entity.setPreviousDirectionRegime(dto.getPreviousDirectionRegime());
        entity.setDaysInRegime(dto.getDaysInRegime());
        entity.setTransition(dto.isTransition());
        entity.setExplanation(dto.getExplanation());
        entity.setModelVersion(dto.getModelVersion());
        entity.setFeatureVersion(dto.getFeatureVersion());
        entity.setDataVersion(1);
        entity.setSourceDataTimestamp(dto.getSourceDataTimestamp());
        entity.setInformationAvailableAt(dto.getInformationAvailableAt());
        entity.setCalculatedAt(dto.getCalculatedAt());

        MarketRegimeEntity saved = regimeRepository.save(entity);

        for (RegimeComponentScoreDTO c : normalizedComponents) {
            RegimeComponentScoreEntity compEntity = new RegimeComponentScoreEntity();
            compEntity.setMarketRegime(saved);
            compEntity.setComponentName(c.getComponentName());
            compEntity.setRawValue(c.getRawValue());
            compEntity.setNormalizedValue(c.getNormalizedValue());
            compEntity.setComponentScore(c.getComponentScore());
            compEntity.setConfiguredWeight(c.getConfiguredWeight());
            compEntity.setEffectiveWeight(c.getEffectiveWeight());
            compEntity.setConfidence(c.getConfidence() != null ? c.getConfidence() : "HIGH");
            compEntity.setSource(c.getSource() != null ? c.getSource() : "INTERNAL");
            compEntity.setInformationAvailableAt(dto.getInformationAvailableAt());
            componentRepository.save(compEntity);
        }

        log.info("Calculated & saved regime for {} as of {}: Direction={}, Vol={}, Risk={}, Score={}",
                effSymbol, effDate, dto.getDirectionRegime(), dto.getVolatilityRegime(), dto.getRiskRegime(), dto.getDirectionScore());

        return dto;
    }

    public Optional<MarketRegimeDTO> getLatestRegime(String symbol, Instant asOf) {
        String effSymbol = (symbol != null && !symbol.isBlank()) ? symbol : "NIFTY 50";
        Instant effAsOf = (asOf != null) ? asOf : Instant.now();

        Optional<MarketRegimeEntity> entityOpt = regimeRepository.findLatestRegimeAsOf(effSymbol, effAsOf);
        if (entityOpt.isPresent()) {
            return Optional.of(toDTO(entityOpt.get()));
        }

        // If not in DB yet, dynamically compute
        return Optional.of(calculateAndSaveRegime(effSymbol, LocalDate.now(), effAsOf));
    }

    public List<MarketRegimeDTO> getRegimeHistory(String symbol, LocalDate from, LocalDate to, Instant asOf) {
        String effSymbol = (symbol != null && !symbol.isBlank()) ? symbol : "NIFTY 50";
        LocalDate effFrom = (from != null) ? from : LocalDate.now().minusMonths(6);
        LocalDate effTo = (to != null) ? to : LocalDate.now();
        Instant effAsOf = (asOf != null) ? asOf : Instant.now();

        List<MarketRegimeEntity> entities = regimeRepository.findRegimeHistoryInRange(effSymbol, effFrom, effTo, effAsOf);
        List<MarketRegimeDTO> dtos = new ArrayList<>();
        for (MarketRegimeEntity e : entities) {
            dtos.add(toDTO(e));
        }
        return dtos;
    }

    public List<MarketRegimeDTO> getRecentRegimes(String symbol, int limit) {
        String effSymbol = (symbol != null && !symbol.isBlank()) ? symbol : "NIFTY 50";
        int effLimit = limit > 0 ? limit : 30;
        List<MarketRegimeEntity> entities = regimeRepository.findRecentRegimes(effSymbol, effLimit);
        List<MarketRegimeDTO> dtos = new ArrayList<>();
        for (MarketRegimeEntity e : entities) {
            dtos.add(toDTO(e));
        }
        return dtos;
    }

    public MarketRegimeDTO toDTO(MarketRegimeEntity entity) {
        MarketRegimeDTO dto = new MarketRegimeDTO();
        dto.setSymbol(entity.getSymbol());
        dto.setRegimeTimestamp(entity.getRegimeTimestamp());
        dto.setTradingDate(entity.getTradingDate());
        dto.setDirectionRegime(entity.getDirectionRegime());
        dto.setVolatilityRegime(entity.getVolatilityRegime());
        dto.setRiskRegime(entity.getRiskRegime());
        dto.setDirectionScore(entity.getDirectionScore());
        dto.setVolatilityScore(entity.getVolatilityScore());
        dto.setRiskScore(entity.getRiskScore());
        dto.setConfidence(entity.getConfidence());
        dto.setProbBull(entity.getProbBull());
        dto.setProbBear(entity.getProbBear());
        dto.setProbSideways(entity.getProbSideways());
        dto.setProbRiskOn(entity.getProbRiskOn());
        dto.setProbRiskOff(entity.getProbRiskOff());
        dto.setPreviousDirectionRegime(entity.getPreviousDirectionRegime());
        dto.setDaysInRegime(entity.getDaysInRegime());
        dto.setTransition(entity.isTransition());
        dto.setExplanation(entity.getExplanation());
        dto.setModelVersion(entity.getModelVersion());
        dto.setFeatureVersion(entity.getFeatureVersion());
        dto.setSourceDataTimestamp(entity.getSourceDataTimestamp());
        dto.setInformationAvailableAt(entity.getInformationAvailableAt());
        dto.setCalculatedAt(entity.getCalculatedAt());

        List<RegimeComponentScoreDTO> compList = new ArrayList<>();
        if (entity.getComponentScores() != null) {
            for (RegimeComponentScoreEntity c : entity.getComponentScores()) {
                RegimeComponentScoreDTO cd = new RegimeComponentScoreDTO();
                cd.setComponentName(c.getComponentName());
                cd.setRawValue(c.getRawValue());
                cd.setNormalizedValue(c.getNormalizedValue());
                cd.setComponentScore(c.getComponentScore());
                cd.setConfiguredWeight(c.getConfiguredWeight());
                cd.setEffectiveWeight(c.getEffectiveWeight());
                cd.setConfidence(c.getConfidence());
                cd.setSource(c.getSource());
                cd.setInformationAvailableAt(c.getInformationAvailableAt());
                compList.add(cd);
            }
        }
        dto.setComponentScores(compList);
        return dto;
    }
}
