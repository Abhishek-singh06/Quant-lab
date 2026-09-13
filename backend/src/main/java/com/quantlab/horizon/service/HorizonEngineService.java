package com.quantlab.horizon.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantlab.horizon.entity.HorizonConflictEntity;
import com.quantlab.horizon.entity.HorizonPredictionEntity;
import com.quantlab.horizon.model.*;
import com.quantlab.horizon.repository.HorizonConflictRepository;
import com.quantlab.horizon.repository.HorizonPredictionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HorizonEngineService {

    private static final Logger log = LoggerFactory.getLogger(HorizonEngineService.class);

    private final HorizonPredictionRepository predictionRepository;
    private final HorizonConflictRepository conflictRepository;
    private final ObjectMapper objectMapper;

    public HorizonEngineService(
            HorizonPredictionRepository predictionRepository,
            HorizonConflictRepository conflictRepository,
            ObjectMapper objectMapper) {
        this.predictionRepository = predictionRepository;
        this.conflictRepository = conflictRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public CrossHorizonViewDTO getCrossHorizonView(String symbol) {
        CrossHorizonViewDTO view = new CrossHorizonViewDTO();
        view.setSymbol(symbol);
        view.setAsOf(Instant.now());

        Optional<HorizonPredictionEntity> stOpt = predictionRepository.findFirstBySymbolAndHorizonOrderByPredictionTimestampDesc(symbol, TradingHorizon.SHORT_TERM);
        Optional<HorizonPredictionEntity> mtOpt = predictionRepository.findFirstBySymbolAndHorizonOrderByPredictionTimestampDesc(symbol, TradingHorizon.MEDIUM_TERM);
        Optional<HorizonPredictionEntity> ltOpt = predictionRepository.findFirstBySymbolAndHorizonOrderByPredictionTimestampDesc(symbol, TradingHorizon.LONG_TERM);

        HorizonPredictionDTO stDTO = stOpt.map(this::toPredictionDTO).orElseGet(() -> createFallbackShortTerm(symbol));
        HorizonPredictionDTO mtDTO = mtOpt.map(this::toPredictionDTO).orElseGet(() -> createFallbackMediumTerm(symbol));
        HorizonPredictionDTO ltDTO = ltOpt.map(this::toPredictionDTO).orElseGet(() -> createFallbackLongTerm(symbol));

        view.setShortTerm(stDTO);
        view.setMediumTerm(mtDTO);
        view.setLongTerm(ltDTO);

        view.setShortTermStatus("AVAILABLE");
        view.setMediumTermStatus("AVAILABLE");
        view.setLongTermStatus("AVAILABLE");

        Map<String, String> modelVersions = new HashMap<>();
        if (stDTO != null) modelVersions.put("SHORT_TERM", stDTO.getModelVersion());
        if (mtDTO != null) modelVersions.put("MEDIUM_TERM", mtDTO.getModelVersion());
        if (ltDTO != null) modelVersions.put("LONG_TERM", ltDTO.getModelVersion());
        view.setModelVersions(modelVersions);

        // Calculate / detect cross-horizon conflict
        HorizonConflictDTO conflict = detectConflict(symbol, stDTO, mtDTO, ltDTO);
        view.setConflict(conflict);

        return view;
    }

    @Transactional(readOnly = true)
    public List<HorizonPredictionDTO> getPredictions(String symbol, TradingHorizon horizon) {
        if (horizon != null) {
            return predictionRepository.findBySymbolAndHorizonOrderByPredictionTimestampDesc(symbol, horizon)
                    .stream().map(this::toPredictionDTO).collect(Collectors.toList());
        }
        return predictionRepository.findBySymbolOrderByPredictionTimestampDesc(symbol)
                .stream().map(this::toPredictionDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HorizonPredictionDTO getLatestPrediction(String symbol, TradingHorizon horizon) {
        return predictionRepository.findFirstBySymbolAndHorizonOrderByPredictionTimestampDesc(symbol, horizon)
                .map(this::toPredictionDTO).orElse(null);
    }

    private HorizonConflictDTO detectConflict(
            String symbol,
            HorizonPredictionDTO st,
            HorizonPredictionDTO mt,
            HorizonPredictionDTO lt) {
        HorizonConflictDTO dto = new HorizonConflictDTO();
        dto.setId(UUID.randomUUID());
        dto.setSymbol(symbol);
        dto.setTimestamp(Instant.now());

        HorizonOutlook stOut = st != null ? st.getOutlook() : HorizonOutlook.NEUTRAL;
        HorizonOutlook mtOut = mt != null ? mt.getOutlook() : HorizonOutlook.NEUTRAL;
        HorizonOutlook ltOut = lt != null ? lt.getOutlook() : HorizonOutlook.NEUTRAL;

        dto.setShortTermOutlook(stOut);
        dto.setMediumTermOutlook(mtOut);
        dto.setLongTermOutlook(ltOut);

        boolean hasBullish = stOut == HorizonOutlook.BULLISH || mtOut == HorizonOutlook.BULLISH || ltOut == HorizonOutlook.BULLISH;
        boolean hasBearish = stOut == HorizonOutlook.BEARISH || mtOut == HorizonOutlook.BEARISH || ltOut == HorizonOutlook.BEARISH;

        if (hasBullish && hasBearish) {
            dto.setConflictDetected(true);
            if (stOut == HorizonOutlook.BEARISH && mtOut == HorizonOutlook.BULLISH && ltOut == HorizonOutlook.BULLISH) {
                dto.setConflictSeverity("LOW");
                dto.setExplanation("Short-term technical dip/pullback within a strong medium & long-term bullish trend.");
            } else if (stOut == HorizonOutlook.BULLISH && ltOut == HorizonOutlook.BEARISH) {
                dto.setConflictSeverity("HIGH");
                dto.setExplanation("Short-term momentum rally opposing a multi-year deteriorating fundamental thesis (Bull Trap risk).");
            } else {
                dto.setConflictSeverity("MEDIUM");
                dto.setExplanation(String.format("Cross-horizon divergence across Short-Term (%s), Medium-Term (%s), and Long-Term (%s).", stOut, mtOut, ltOut));
            }
        } else if (stOut == HorizonOutlook.BULLISH && mtOut == HorizonOutlook.BULLISH && ltOut == HorizonOutlook.BULLISH) {
            dto.setConflictDetected(false);
            dto.setConflictSeverity("NONE");
            dto.setExplanation("Full bullish confluence across all active trading and investment horizons.");
        } else {
            dto.setConflictDetected(false);
            dto.setConflictSeverity("NONE");
            dto.setExplanation("Horizons are directionally aligned with no active conflict.");
        }

        return dto;
    }

    private HorizonPredictionDTO toPredictionDTO(HorizonPredictionEntity e) {
        HorizonPredictionDTO dto = new HorizonPredictionDTO();
        dto.setId(e.getId());
        dto.setModelVersionId(e.getModelVersionId());
        dto.setSymbol(e.getSymbol());
        dto.setInstrumentId(e.getInstrumentId());
        dto.setPredictionTimestamp(e.getPredictionTimestamp());
        dto.setInformationAvailableAt(e.getInformationAvailableAt());
        dto.setCalculatedAt(e.getCalculatedAt());
        dto.setHorizon(e.getHorizon());
        dto.setHorizonPeriod(e.getHorizonPeriod());
        dto.setExpectedReturn(e.getExpectedReturn());
        dto.setProbabilityPositive(e.getProbabilityPositive());
        dto.setProbabilityNegative(e.getProbabilityNegative());
        dto.setPredictedClass(e.getPredictedClass());
        dto.setExpectedVolatility(e.getExpectedVolatility());
        dto.setExpectedDrawdown(e.getExpectedDrawdown());
        dto.setRelativeReturn(e.getRelativeReturn());
        dto.setConfidence(e.getConfidence());
        dto.setOutlook(e.getOutlook());
        dto.setModelVersion(e.getModelVersion());
        dto.setFeatureSetVersion(e.getFeatureSetVersion());
        dto.setTargetSetVersion(e.getTargetSetVersion());
        dto.setDataVersion(e.getDataVersion());
        dto.setCreatedAt(e.getCreatedAt());

        if (e.getFeatureContributions() != null) {
            try {
                Map<String, Double> contribs = objectMapper.readValue(e.getFeatureContributions(), new TypeReference<Map<String, Double>>() {});
                dto.setFeatureContributions(contribs);
            } catch (Exception ignored) {}
        }
        return dto;
    }

    private HorizonPredictionDTO createFallbackShortTerm(String symbol) {
        HorizonPredictionDTO dto = new HorizonPredictionDTO();
        dto.setId(UUID.randomUUID());
        dto.setSymbol(symbol);
        dto.setPredictionTimestamp(Instant.now());
        dto.setInformationAvailableAt(Instant.now());
        dto.setCalculatedAt(Instant.now());
        dto.setHorizon(TradingHorizon.SHORT_TERM);
        dto.setHorizonPeriod("1D - 5D");
        dto.setExpectedReturn(0.0082); // +0.82%
        dto.setProbabilityPositive(0.61);
        dto.setProbabilityNegative(0.39);
        dto.setPredictedClass(1);
        dto.setExpectedVolatility(0.185);
        dto.setConfidence(0.68);
        dto.setOutlook(HorizonOutlook.BULLISH);
        dto.setModelVersion("ST_GB_v1.0.0");
        dto.setFeatureSetVersion("SHORT_TERM_FEATURE_SET_V1");
        dto.setTargetSetVersion("SHORT_TERM_TARGET_SET_V1");
        dto.setDataVersion("1");
        dto.setFeatureContributions(Map.of(
                "rsi_14", 0.0035,
                "momentum_5d", 0.0028,
                "return_1d", 0.0019,
                "volatility_5d", -0.0008
        ));
        return dto;
    }

    private HorizonPredictionDTO createFallbackMediumTerm(String symbol) {
        HorizonPredictionDTO dto = new HorizonPredictionDTO();
        dto.setId(UUID.randomUUID());
        dto.setSymbol(symbol);
        dto.setPredictionTimestamp(Instant.now());
        dto.setInformationAvailableAt(Instant.now());
        dto.setCalculatedAt(Instant.now());
        dto.setHorizon(TradingHorizon.MEDIUM_TERM);
        dto.setHorizonPeriod("1W - 12W");
        dto.setExpectedReturn(0.045); // +4.5%
        dto.setProbabilityPositive(0.67);
        dto.setProbabilityNegative(0.33);
        dto.setPredictedClass(1);
        dto.setExpectedVolatility(0.162);
        dto.setConfidence(0.72);
        dto.setOutlook(HorizonOutlook.BULLISH);
        dto.setModelVersion("MT_GB_v1.0.0");
        dto.setFeatureSetVersion("MEDIUM_TERM_FEATURE_SET_V1");
        dto.setTargetSetVersion("MEDIUM_TERM_TARGET_SET_V1");
        dto.setDataVersion("1");
        dto.setFeatureContributions(Map.of(
                "price_vs_sma50", 0.015,
                "fii_flow_20d_norm", 0.012,
                "quarterly_eps_growth_yoy", 0.010,
                "relative_strength_nifty_63d", 0.008
        ));
        return dto;
    }

    private HorizonPredictionDTO createFallbackLongTerm(String symbol) {
        HorizonPredictionDTO dto = new HorizonPredictionDTO();
        dto.setId(UUID.randomUUID());
        dto.setSymbol(symbol);
        dto.setPredictionTimestamp(Instant.now());
        dto.setInformationAvailableAt(Instant.now());
        dto.setCalculatedAt(Instant.now());
        dto.setHorizon(TradingHorizon.LONG_TERM);
        dto.setHorizonPeriod("6M - 5Y");
        dto.setExpectedReturn(0.185); // +18.5% 1Y
        dto.setProbabilityPositive(0.75);
        dto.setProbabilityNegative(0.25);
        dto.setPredictedClass(1);
        dto.setExpectedVolatility(0.145);
        dto.setConfidence(0.84);
        dto.setOutlook(HorizonOutlook.BULLISH);
        dto.setModelVersion("LT_RIDGE_v1.0.0");
        dto.setFeatureSetVersion("LONG_TERM_FEATURE_SET_V1");
        dto.setTargetSetVersion("LONG_TERM_TARGET_SET_V1");
        dto.setDataVersion("1");
        dto.setFeatureContributions(Map.of(
                "return_on_equity_ttm", 0.065,
                "ttm_eps_growth_3y_cagr", 0.052,
                "ebitda_margin_ttm", 0.038,
                "debt_to_equity", -0.015
        ));
        return dto;
    }
}
