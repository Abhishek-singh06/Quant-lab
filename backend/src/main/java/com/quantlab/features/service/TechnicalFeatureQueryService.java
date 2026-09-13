package com.quantlab.features.service;

import com.quantlab.features.entity.FeatureCalculationRun;
import com.quantlab.features.entity.TechnicalFeature;
import com.quantlab.features.model.FeatureCalculationRequest;
import com.quantlab.features.model.FeatureDefinitionDTO;
import com.quantlab.features.model.TechnicalFeatureDTO;
import com.quantlab.features.repository.FeatureCalculationRunRepository;
import com.quantlab.features.repository.TechnicalFeatureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TechnicalFeatureQueryService {

    private final TechnicalFeatureRepository featureRepository;
    private final FeatureCalculationRunRepository runRepository;
    private final TechnicalFeatureRegistry registry;
    private final TechnicalFeatureBatchService batchService;

    public TechnicalFeatureQueryService(
            TechnicalFeatureRepository featureRepository,
            FeatureCalculationRunRepository runRepository,
            TechnicalFeatureRegistry registry,
            TechnicalFeatureBatchService batchService) {
        this.featureRepository = featureRepository;
        this.runRepository = runRepository;
        this.registry = registry;
        this.batchService = batchService;
    }

    public List<TechnicalFeatureDTO> getLatestFeatures(String symbol, String timeframe) {
        String tf = (timeframe != null) ? timeframe : "1D";
        List<TechnicalFeature> list = featureRepository.findLatestFeaturesBySymbol(symbol.toUpperCase(), tf);

        if (list.isEmpty()) {
            // Auto-calculate on demand if no features exist
            FeatureCalculationRequest req = new FeatureCalculationRequest();
            req.setSymbols(List.of(symbol.toUpperCase()));
            req.setTimeframe(tf);
            batchService.calculateForRequest(req);

            list = featureRepository.findLatestFeaturesBySymbol(symbol.toUpperCase(), tf);
        }

        return mapToDTOList(list);
    }

    public List<TechnicalFeatureDTO> getFeaturesAvailableAt(String symbol, String timeframe, Instant asOf) {
        String tf = (timeframe != null) ? timeframe : "1D";
        Instant effectiveAsOf = (asOf != null) ? asOf : Instant.now();
        List<TechnicalFeature> list = featureRepository.findFeaturesBySymbolAvailableAt(symbol.toUpperCase(), tf, effectiveAsOf);
        return mapToDTOList(list);
    }

    public List<TechnicalFeatureDTO> getFeatureTimeSeries(
            Long instrumentId, String featureName, String timeframe, LocalDate from, LocalDate to) {
        String tf = (timeframe != null) ? timeframe : "1D";
        LocalDate fromDate = (from != null) ? from : LocalDate.now().minusYears(1);
        LocalDate toDate = (to != null) ? to : LocalDate.now();

        List<TechnicalFeature> list = featureRepository.findFeatureTimeSeries(
                instrumentId, featureName.toUpperCase(), tf, fromDate, toDate
        );
        return mapToDTOList(list);
    }

    public List<FeatureDefinitionDTO> getDefinitions() {
        return registry.getRegisteredFeatureDefinitions();
    }

    public List<FeatureCalculationRun> getRecentRuns() {
        return runRepository.findTop20ByOrderByStartTimeDesc();
    }

    private List<TechnicalFeatureDTO> mapToDTOList(List<TechnicalFeature> list) {
        List<TechnicalFeatureDTO> dtos = new ArrayList<>();
        for (TechnicalFeature tf : list) {
            TechnicalFeatureDTO dto = new TechnicalFeatureDTO();
            dto.setId(tf.getId());
            dto.setInstrumentId(tf.getInstrumentId());
            dto.setSymbol(tf.getSymbol());
            dto.setFeatureName(tf.getFeatureName());
            dto.setFeatureValue(tf.getFeatureValue());
            dto.setFeatureTimestamp(tf.getFeatureTimestamp());
            dto.setTradingDate(tf.getTradingDate());
            dto.setTimeframe(tf.getTimeframe());
            dto.setFrequency(tf.getFrequency());
            dto.setFeatureVersion(tf.getFeatureVersion());
            dto.setCalculationVersion(tf.getCalculationVersion());
            dto.setSourceDataTimestamp(tf.getSourceDataTimestamp());
            dto.setInformationAvailableAt(tf.getInformationAvailableAt());
            dto.setCalculatedAt(tf.getCalculatedAt());
            dto.setSource(tf.getSource());
            dto.setIngestionRunId(tf.getIngestionRunId());
            dtos.add(dto);
        }
        return dtos;
    }
}
