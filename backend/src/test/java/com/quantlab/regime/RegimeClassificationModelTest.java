package com.quantlab.regime;

import com.quantlab.regime.model.DirectionRegime;
import com.quantlab.regime.model.MarketRegimeDTO;
import com.quantlab.regime.model.RegimeComponentScoreDTO;
import com.quantlab.regime.model.RegimeComponentType;
import com.quantlab.regime.model.RiskRegime;
import com.quantlab.regime.model.VolatilityRegime;
import com.quantlab.regime.service.RegimeClassificationModel;
import com.quantlab.regime.service.RegimeNormalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegimeClassificationModelTest {

    private RegimeNormalizationService normalizationService;
    private RegimeClassificationModel classificationModel;

    @BeforeEach
    void setUp() {
        normalizationService = new RegimeNormalizationService();
        classificationModel = new RegimeClassificationModel();
    }

    @Test
    void testBullishSignalsProduceBullRegime() {
        List<RegimeComponentScoreDTO> rawList = new ArrayList<>();

        rawList.add(createDTO(RegimeComponentType.NIFTY_TREND, "NIFTY_TREND", new BigDecimal("75.0"), new BigDecimal("0.25")));
        rawList.add(createDTO(RegimeComponentType.MARKET_BREADTH, "MARKET_BREADTH", new BigDecimal("60.0"), new BigDecimal("0.15")));
        rawList.add(createDTO(RegimeComponentType.VOLATILITY_VIX, "VOLATILITY_VIX", new BigDecimal("12.5"), new BigDecimal("0.15")));
        rawList.add(createDTO(RegimeComponentType.MOMENTUM, "MOMENTUM", new BigDecimal("65.0"), new BigDecimal("0.15")));
        rawList.add(createDTO(RegimeComponentType.GLOBAL_RISK, "GLOBAL_RISK", new BigDecimal("50.0"), new BigDecimal("0.10")));
        rawList.add(createDTO(RegimeComponentType.FII_FLOWS, "FII_FLOWS", new BigDecimal("2800.0"), new BigDecimal("0.08")));
        rawList.add(createDTO(RegimeComponentType.DII_FLOWS, "DII_FLOWS", new BigDecimal("1500.0"), new BigDecimal("0.04")));

        List<RegimeComponentScoreDTO> normalized = normalizationService.normalizeAndWeightComponents(rawList);
        MarketRegimeDTO regime = classificationModel.classify(
                "NIFTY 50", LocalDate.now(), Instant.now(), normalized, null, 0
        );

        assertNotNull(regime);
        assertEquals(DirectionRegime.BULL, regime.getDirectionRegime());
        assertEquals(VolatilityRegime.LOW_VOL, regime.getVolatilityRegime());
        assertEquals(RiskRegime.RISK_ON, regime.getRiskRegime());
        assertTrue(regime.getProbBull().compareTo(new BigDecimal("0.50")) > 0);
        assertTrue(regime.getConfidence().compareTo(new BigDecimal("0.60")) > 0);
        assertNotNull(regime.getExplanation());
        assertFalse(regime.getDrivers().isEmpty());
    }

    @Test
    void testBearishHighVolSignalsProduceBearRegime() {
        List<RegimeComponentScoreDTO> rawList = new ArrayList<>();

        rawList.add(createDTO(RegimeComponentType.NIFTY_TREND, "NIFTY_TREND", new BigDecimal("-80.0"), new BigDecimal("0.25")));
        rawList.add(createDTO(RegimeComponentType.MARKET_BREADTH, "MARKET_BREADTH", new BigDecimal("-70.0"), new BigDecimal("0.15")));
        rawList.add(createDTO(RegimeComponentType.VOLATILITY_VIX, "VOLATILITY_VIX", new BigDecimal("26.5"), new BigDecimal("0.15")));
        rawList.add(createDTO(RegimeComponentType.MOMENTUM, "MOMENTUM", new BigDecimal("-65.0"), new BigDecimal("0.15")));
        rawList.add(createDTO(RegimeComponentType.GLOBAL_RISK, "GLOBAL_RISK", new BigDecimal("-50.0"), new BigDecimal("0.10")));
        rawList.add(createDTO(RegimeComponentType.FII_FLOWS, "FII_FLOWS", new BigDecimal("-3500.0"), new BigDecimal("0.08")));

        List<RegimeComponentScoreDTO> normalized = normalizationService.normalizeAndWeightComponents(rawList);
        MarketRegimeDTO regime = classificationModel.classify(
                "NIFTY 50", LocalDate.now(), Instant.now(), normalized, DirectionRegime.BULL, 5
        );

        assertNotNull(regime);
        assertEquals(DirectionRegime.BEAR, regime.getDirectionRegime());
        assertEquals(VolatilityRegime.EXTREME_VOL, regime.getVolatilityRegime());
        assertEquals(RiskRegime.RISK_OFF, regime.getRiskRegime());
        assertTrue(regime.getProbBear().compareTo(new BigDecimal("0.50")) > 0);
        assertTrue(regime.isTransition());
        assertEquals(1, regime.getDaysInRegime());
    }

    @Test
    void testDynamicRenormalizationWhenComponentsMissing() {
        List<RegimeComponentScoreDTO> partialList = new ArrayList<>();
        // Only 2 components present
        partialList.add(createDTO(RegimeComponentType.NIFTY_TREND, "NIFTY_TREND", new BigDecimal("40.0"), new BigDecimal("0.25")));
        partialList.add(createDTO(RegimeComponentType.VOLATILITY_VIX, "VOLATILITY_VIX", new BigDecimal("14.0"), new BigDecimal("0.15")));

        List<RegimeComponentScoreDTO> normalized = normalizationService.normalizeAndWeightComponents(partialList);
        assertEquals(2, normalized.size());

        BigDecimal sumWeights = BigDecimal.ZERO;
        for (RegimeComponentScoreDTO c : normalized) {
            sumWeights = sumWeights.add(c.getEffectiveWeight());
        }
        // Sum of effective weights must equal 1.0000
        assertEquals(new BigDecimal("1.0000"), sumWeights.setScale(4, BigDecimal.ROUND_HALF_UP));
    }

    private RegimeComponentScoreDTO createDTO(RegimeComponentType type, String name, BigDecimal raw, BigDecimal weight) {
        RegimeComponentScoreDTO dto = new RegimeComponentScoreDTO();
        dto.setComponentType(type);
        dto.setComponentName(name);
        dto.setRawValue(raw);
        dto.setNormalizedValue(raw);
        dto.setComponentScore(raw);
        dto.setConfiguredWeight(weight);
        dto.setEffectiveWeight(weight);
        dto.setConfidence("HIGH");
        dto.setSource("TEST");
        dto.setInformationAvailableAt(Instant.now());
        return dto;
    }
}
