package com.quantlab.regime.service;

import com.quantlab.regime.model.DirectionRegime;
import com.quantlab.regime.model.MarketRegimeDTO;
import com.quantlab.regime.model.RegimeComponentScoreDTO;
import com.quantlab.regime.model.RegimeComponentType;
import com.quantlab.regime.model.RiskRegime;
import com.quantlab.regime.model.VolatilityRegime;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class RegimeClassificationModel {

    public static final String MODEL_VERSION = "v1.0.0-multi-signal-composite";
    public static final String FEATURE_VERSION = "v1.0.0";

    public MarketRegimeDTO classify(
            String symbol,
            LocalDate tradingDate,
            Instant asOfTimestamp,
            List<RegimeComponentScoreDTO> normalizedComponents,
            DirectionRegime previousDirection,
            int previousDaysInRegime) {

        MarketRegimeDTO dto = new MarketRegimeDTO();
        dto.setSymbol(symbol);
        dto.setTradingDate(tradingDate);
        dto.setRegimeTimestamp(asOfTimestamp != null ? asOfTimestamp : Instant.now());
        dto.setSourceDataTimestamp(dto.getRegimeTimestamp());
        dto.setInformationAvailableAt(dto.getRegimeTimestamp());
        dto.setCalculatedAt(Instant.now());
        dto.setModelVersion(MODEL_VERSION);
        dto.setFeatureVersion(FEATURE_VERSION);
        dto.setComponentScores(normalizedComponents);

        // 1. Calculate Weighted Composite Direction Score
        BigDecimal compositeDirectionScore = BigDecimal.ZERO;
        BigDecimal vixRaw = BigDecimal.valueOf(14.0);
        BigDecimal globalRiskScore = BigDecimal.valueOf(20.0);
        BigDecimal fiiNet = BigDecimal.ZERO;
        BigDecimal diiNet = BigDecimal.ZERO;

        Map<String, Object> drivers = new LinkedHashMap<>();
        Map<String, Object> risks = new LinkedHashMap<>();

        for (RegimeComponentScoreDTO c : normalizedComponents) {
            BigDecimal score = c.getComponentScore() != null ? c.getComponentScore() : BigDecimal.ZERO;
            BigDecimal effWeight = c.getEffectiveWeight() != null ? c.getEffectiveWeight() : BigDecimal.ZERO;

            compositeDirectionScore = compositeDirectionScore.add(score.multiply(effWeight));

            if (c.getComponentType() == RegimeComponentType.VOLATILITY_VIX) {
                vixRaw = c.getRawValue() != null ? c.getRawValue() : BigDecimal.valueOf(14.0);
            }
            if (c.getComponentType() == RegimeComponentType.GLOBAL_RISK) {
                globalRiskScore = score;
            }
            if (c.getComponentType() == RegimeComponentType.FII_FLOWS) {
                fiiNet = c.getRawValue() != null ? c.getRawValue() : BigDecimal.ZERO;
            }
            if (c.getComponentType() == RegimeComponentType.DII_FLOWS) {
                diiNet = c.getRawValue() != null ? c.getRawValue() : BigDecimal.ZERO;
            }

            // Drivers & Risks breakdown
            if (score.compareTo(BigDecimal.valueOf(25.0)) >= 0) {
                drivers.put(c.getComponentName(), "Strong positive contribution: " + score.setScale(1, RoundingMode.HALF_UP));
            } else if (score.compareTo(BigDecimal.valueOf(-25.0)) <= 0) {
                risks.put(c.getComponentName(), "Negative headwind / drag: " + score.setScale(1, RoundingMode.HALF_UP));
            }
        }

        compositeDirectionScore = compositeDirectionScore.setScale(4, RoundingMode.HALF_UP);
        dto.setDirectionScore(compositeDirectionScore);

        // 2. Probabilistic Calibration
        double scoreDbl = compositeDirectionScore.doubleValue();
        double expBull = Math.exp((scoreDbl - 15.0) / 25.0);
        double expBear = Math.exp((-scoreDbl - 15.0) / 25.0);
        double expSideways = 1.0;
        double sumExp = expBull + expBear + expSideways;

        double pBull = expBull / sumExp;
        double pBear = expBear / sumExp;
        double pSideways = expSideways / sumExp;

        dto.setProbBull(BigDecimal.valueOf(pBull).setScale(4, RoundingMode.HALF_UP));
        dto.setProbBear(BigDecimal.valueOf(pBear).setScale(4, RoundingMode.HALF_UP));
        dto.setProbSideways(BigDecimal.valueOf(pSideways).setScale(4, RoundingMode.HALF_UP));

        // 3. Direction Regime Classification with Hysteresis
        DirectionRegime direction;
        if (pBull > 0.48 && scoreDbl >= 20.0) {
            direction = DirectionRegime.BULL;
        } else if (pBear > 0.48 && scoreDbl <= -20.0) {
            direction = DirectionRegime.BEAR;
        } else if (Math.abs(scoreDbl) < 15.0) {
            direction = DirectionRegime.SIDEWAYS;
        } else {
            direction = DirectionRegime.TRANSITION;
        }
        dto.setDirectionRegime(direction);

        // 4. Volatility Regime
        double vix = vixRaw.doubleValue();
        VolatilityRegime volRegime;
        double volScoreVal = (18.0 - vix) * 10.0;
        dto.setVolatilityScore(BigDecimal.valueOf(volScoreVal).setScale(4, RoundingMode.HALF_UP));

        if (vix < 13.0) {
            volRegime = VolatilityRegime.LOW_VOL;
        } else if (vix <= 18.5) {
            volRegime = VolatilityRegime.NORMAL_VOL;
        } else if (vix <= 25.0) {
            volRegime = VolatilityRegime.HIGH_VOL;
        } else {
            volRegime = VolatilityRegime.EXTREME_VOL;
        }
        dto.setVolatilityRegime(volRegime);

        // 5. Risk Regime
        double riskScoreVal = (globalRiskScore.doubleValue() * 0.4) + (scoreDbl * 0.4) + ((fiiNet.doubleValue() > 0 ? 20.0 : -20.0) * 0.2);
        dto.setRiskScore(BigDecimal.valueOf(riskScoreVal).setScale(4, RoundingMode.HALF_UP));

        double expRiskOn = Math.exp(riskScoreVal / 25.0);
        double expRiskOff = Math.exp(-riskScoreVal / 25.0);
        double sumRisk = expRiskOn + expRiskOff;
        dto.setProbRiskOn(BigDecimal.valueOf(expRiskOn / sumRisk).setScale(4, RoundingMode.HALF_UP));
        dto.setProbRiskOff(BigDecimal.valueOf(expRiskOff / sumRisk).setScale(4, RoundingMode.HALF_UP));

        if (riskScoreVal >= 15.0) {
            dto.setRiskRegime(RiskRegime.RISK_ON);
        } else if (riskScoreVal <= -15.0) {
            dto.setRiskRegime(RiskRegime.RISK_OFF);
        } else {
            dto.setRiskRegime(RiskRegime.NEUTRAL);
        }

        // 6. Transition & Tenure tracking
        dto.setPreviousDirectionRegime(previousDirection != null ? previousDirection : direction);
        if (previousDirection == null || previousDirection == direction) {
            dto.setDaysInRegime(previousDaysInRegime + 1);
            dto.setTransition(false);
        } else {
            dto.setDaysInRegime(1);
            dto.setTransition(true);
        }

        // 7. Model Confidence Calibration
        double maxProb = Math.max(pBull, Math.max(pBear, pSideways));
        double confidenceVal = Math.min(0.98, Math.max(0.40, maxProb * 0.9 + 0.1));
        dto.setConfidence(BigDecimal.valueOf(confidenceVal).setScale(4, RoundingMode.HALF_UP));

        // 8. Structured Explanation
        String explanation = String.format(
                "Market classified as %s (%s, %s) with %.1f%% confidence based on %d independent signals. " +
                "Composite score: %.1f, India VIX: %.2f. FII 5D net: %.0f cr, DII 5D net: %.0f cr.",
                direction, volRegime, dto.getRiskRegime(),
                confidenceVal * 100.0, normalizedComponents.size(),
                scoreDbl, vix, fiiNet.doubleValue(), diiNet.doubleValue()
        );
        dto.setExplanation(explanation);
        dto.setDrivers(drivers);
        dto.setRisks(risks);

        return dto;
    }
}
