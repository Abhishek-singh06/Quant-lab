package com.quantlab.features.service;

import com.quantlab.features.calculator.TechnicalFeatureCalculator;
import com.quantlab.features.entity.FeatureDataQuality;
import com.quantlab.features.entity.TechnicalFeature;
import com.quantlab.features.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class TechnicalFeaturePipeline {

    private static final Logger log = LoggerFactory.getLogger(TechnicalFeaturePipeline.class);

    private final TechnicalFeatureRegistry registry;
    private final TechnicalFeatureValidatorService validatorService;

    public TechnicalFeaturePipeline(TechnicalFeatureRegistry registry, TechnicalFeatureValidatorService validatorService) {
        this.registry = registry;
        this.validatorService = validatorService;
    }

    public static class PipelineExecutionOutput {
        private final List<TechnicalFeature> validFeatures;
        private final List<FeatureDataQuality> dataQualityIssues;

        public PipelineExecutionOutput(List<TechnicalFeature> validFeatures, List<FeatureDataQuality> dataQualityIssues) {
            this.validFeatures = validFeatures;
            this.dataQualityIssues = dataQualityIssues;
        }

        public List<TechnicalFeature> getValidFeatures() { return validFeatures; }
        public List<FeatureDataQuality> getDataQualityIssues() { return dataQualityIssues; }
    }

    /**
     * Executes the technical feature pipeline for an instrument as of target date.
     * All price bars after target date are strictly filtered out (guaranteeing zero look-ahead bias).
     */
    public PipelineExecutionOutput executePipeline(
            Long instrumentId,
            String symbol,
            LocalDate targetDate,
            Instant targetTimestamp,
            List<PriceBar> allHistoricalBars,
            List<PriceBar> benchmarkBars,
            Collection<String> requestedFeatureNames,
            String timeframe,
            String runId) {

        // 1. Point-in-Time Slicing: strictly bars <= targetDate
        List<PriceBar> availableBars = new ArrayList<>();
        for (PriceBar bar : allHistoricalBars) {
            if (!bar.getTradingDate().isAfter(targetDate)) {
                availableBars.add(bar);
            }
        }

        List<PriceBar> availableBenchmarkBars = new ArrayList<>();
        if (benchmarkBars != null) {
            for (PriceBar bar : benchmarkBars) {
                if (!bar.getTradingDate().isAfter(targetDate)) {
                    availableBenchmarkBars.add(bar);
                }
            }
        }

        if (availableBars.isEmpty()) {
            return new PipelineExecutionOutput(Collections.emptyList(), Collections.emptyList());
        }

        PriceBar latestBar = availableBars.get(availableBars.size() - 1);
        Instant sourceDataTs = latestBar.getTimestamp();
        Instant infoAvailAt = sourceDataTs; // Market data available as of close timestamp

        TechnicalFeatureContext context = new TechnicalFeatureContext(
                instrumentId, symbol, targetDate, targetTimestamp,
                Timeframe.fromCode(timeframe), availableBars, availableBenchmarkBars, Collections.emptyMap()
        );

        Collection<TechnicalFeatureCalculator> calculatorsToRun;
        if (requestedFeatureNames == null || requestedFeatureNames.isEmpty()) {
            calculatorsToRun = registry.getAllCalculators();
        } else {
            calculatorsToRun = new ArrayList<>();
            for (String name : requestedFeatureNames) {
                registry.getCalculator(name).ifPresent(calculatorsToRun::add);
            }
        }

        List<TechnicalFeature> validFeatures = new ArrayList<>();
        List<FeatureDataQuality> qualityIssues = new ArrayList<>();
        Instant calcTime = Instant.now();

        for (TechnicalFeatureCalculator calc : calculatorsToRun) {
            try {
                TechnicalFeatureResult result = calc.calculate(context);
                ValidationStatus valStatus = validatorService.validateResult(result);

                if (valStatus == ValidationStatus.VALID && result.getValue() != null) {
                    TechnicalFeature tf = new TechnicalFeature();
                    tf.setInstrumentId(instrumentId);
                    tf.setSymbol(symbol);
                    tf.setFeatureName(calc.getFeatureName());
                    tf.setFeatureValue(result.getValue());
                    tf.setFeatureTimestamp(targetTimestamp);
                    tf.setTradingDate(targetDate);
                    tf.setTimeframe(timeframe);
                    tf.setFrequency("DAILY");
                    tf.setFeatureVersion(calc.getFeatureVersion());
                    tf.setCalculationVersion(calc.getFormulaVersion());
                    tf.setDataVersion(1);
                    tf.setSourceDataTimestamp(sourceDataTs);
                    tf.setInformationAvailableAt(infoAvailAt);
                    tf.setCalculatedAt(calcTime);
                    tf.setSource("TECHNICAL_FEATURE_ENGINE");
                    tf.setIngestionRunId(runId);

                    validFeatures.add(tf);
                } else if (valStatus != ValidationStatus.INSUFFICIENT_HISTORY) {
                    // Log anomaly for out-of-bounds or unexpected errors
                    FeatureDataQuality dq = new FeatureDataQuality();
                    dq.setRunId(runId);
                    dq.setInstrumentId(instrumentId);
                    dq.setSymbol(symbol);
                    dq.setFeatureName(calc.getFeatureName());
                    dq.setFeatureTimestamp(targetTimestamp);
                    dq.setFeatureValue(result != null ? result.getValue() : null);
                    dq.setValidationStatus(valStatus);
                    dq.setAnomalyReason(result != null ? result.getMessage() : "Validation failed");
                    dq.setCheckedAt(calcTime);

                    qualityIssues.add(dq);
                }
            } catch (Exception ex) {
                log.error("Error executing calculator {} for {}: {}", calc.getFeatureName(), symbol, ex.getMessage());
                FeatureDataQuality dq = new FeatureDataQuality();
                dq.setRunId(runId);
                dq.setInstrumentId(instrumentId);
                dq.setSymbol(symbol);
                dq.setFeatureName(calc.getFeatureName());
                dq.setFeatureTimestamp(targetTimestamp);
                dq.setValidationStatus(ValidationStatus.CALCULATION_ERROR);
                dq.setAnomalyReason(ex.getMessage());
                dq.setCheckedAt(calcTime);

                qualityIssues.add(dq);
            }
        }

        return new PipelineExecutionOutput(validFeatures, qualityIssues);
    }
}
